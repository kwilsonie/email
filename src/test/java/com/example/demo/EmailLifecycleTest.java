package com.example.demo;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

/**
 * Full-stack integration tests for the async email delivery lifecycle.
 *
 * <p>{@code @MockBean EmailSendStrategy} replaces the SimulatedEmailSendStrategy,
 * giving each test deterministic control over success vs. failure without any
 * changes to production code.  Spring Boot automatically resets @MockBean
 * state between test methods.</p>
 *
 * <p>JsonPath is used for response assertions to avoid needing Jackson
 * deserialization setters on the output-only EmailResponse DTO.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmailLifecycleTest {

    @Autowired TestRestTemplate restTemplate;

    /**
     * Replaces SimulatedEmailSendStrategy in the Spring context.
     * Configure per-test with doNothing() / doThrow().
     */
    @MockBean EmailSendStrategy sendStrategy;

    private HttpHeaders jsonHeaders;

    @BeforeEach
    void setUp() throws Exception {
        // Explicit reset to guarantee no bleed between tests, even though
        // Spring Boot resets @MockBean after each method by default.
        reset(sendStrategy);

        jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    // -------------------------------------------------------------------------
    // POST /email → immediately returns QUEUED
    // -------------------------------------------------------------------------

    @Test
    void post_persistsWithQueuedStatus() throws Exception {
        // Strategy configured to succeed (not needed for this assertion, but explicit).
        doNothing().when(sendStrategy).execute(anyLong());

        ResponseEntity<String> resp = post(makeRequest("queued@example.com"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(JsonPath.<String>read(resp.getBody(), "$.status")).isEqualTo("QUEUED");
        assertThat(JsonPath.<Object>read(resp.getBody(), "$.id")).isNotNull();
        // BCC must never appear
        assertThat(resp.getBody()).doesNotContain("\"bcc\"");
    }

    // -------------------------------------------------------------------------
    // Async success path → eventually transitions to SENT
    // -------------------------------------------------------------------------

    @Test
    void post_eventuallyTransitionsToSent() throws Exception {
        doNothing().when(sendStrategy).execute(anyLong());

        ResponseEntity<String> postResp = post(makeRequest("sent@example.com"));
        assertThat(postResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        long emailId = ((Number) JsonPath.read(postResp.getBody(), "$.id")).longValue();

        await().atMost(Duration.ofSeconds(5))
               .pollInterval(Duration.ofMillis(200))
               .untilAsserted(() -> {
                   String body = restTemplate.getForObject("/email/" + emailId, String.class);
                   assertThat(JsonPath.<String>read(body, "$.status")).isEqualTo("SENT");
                   // statusUpdatedAt must be populated
                   assertThat(JsonPath.<Object>read(body, "$.statusUpdatedAt")).isNotNull();
                   // BCC still absent on GET
                   assertThat(body).doesNotContain("\"bcc\"");
               });
    }

    // -------------------------------------------------------------------------
    // Async failure path → eventually transitions to FAILED with reason
    // -------------------------------------------------------------------------

    @Test
    void post_whenSendStrategyThrows_transitionsToFailed() throws Exception {
        doThrow(new RuntimeException("SMTP connection refused"))
                .when(sendStrategy).execute(anyLong());

        ResponseEntity<String> postResp = post(makeRequest("fail@example.com"));
        assertThat(postResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // Immediate response still shows QUEUED
        assertThat(JsonPath.<String>read(postResp.getBody(), "$.status")).isEqualTo("QUEUED");

        long emailId = ((Number) JsonPath.read(postResp.getBody(), "$.id")).longValue();

        await().atMost(Duration.ofSeconds(5))
               .pollInterval(Duration.ofMillis(200))
               .untilAsserted(() -> {
                   String body = restTemplate.getForObject("/email/" + emailId, String.class);
                   assertThat(JsonPath.<String>read(body, "$.status")).isEqualTo("FAILED");
                   assertThat(JsonPath.<String>read(body, "$.failureReason"))
                           .contains("SMTP connection refused");
                   assertThat(JsonPath.<Object>read(body, "$.statusUpdatedAt")).isNotNull();
               });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<String> post(EmailRequest request) {
        HttpEntity<EmailRequest> entity = new HttpEntity<>(request, jsonHeaders);
        return restTemplate.postForEntity("/email", entity, String.class);
    }

    private EmailRequest makeRequest(String toAddress) {
        EmailRequest r = new EmailRequest();
        r.setFromAddress("sender@example.com");
        r.setToAddress(toAddress);
        r.setSubject("Lifecycle Test");
        r.setBody("Integration test body");
        return r;
    }
}
