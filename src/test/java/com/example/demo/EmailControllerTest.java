package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailController.class)
class EmailControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  EmailService emailService;   // controller delegates here; no repository needed

    private EmailResponse mockResponse;

    @BeforeEach
    void setUp() {
        // Build a realistic EmailResponse via the factory method so status fields are included.
        Email email = new Email();
        email.setId(1L);
        email.setFromAddress("from@example.com");
        email.setToAddress("to@example.com");
        email.setSubject("Hello");
        email.setBody("World");
        email.setBcc("secret@example.com"); // must never appear in responses
        email.setStatus(EmailStatus.QUEUED);
        email.setStatusUpdatedAt(Instant.now());
        mockResponse = EmailResponse.from(email);
    }

    // -------------------------------------------------------------------------
    // POST /email — success
    // -------------------------------------------------------------------------

    @Test
    void postEmail_validRequest_returns201WithQueuedStatus() throws Exception {
        when(emailService.createEmail(any(EmailRequest.class))).thenReturn(mockResponse);

        EmailRequest request = new EmailRequest();
        request.setFromAddress("from@example.com");
        request.setToAddress("to@example.com");
        request.setSubject("Hello");
        request.setBody("World");
        request.setBcc("secret@example.com");

        mockMvc.perform(post("/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fromAddress").value("from@example.com"))
                .andExpect(jsonPath("$.toAddress").value("to@example.com"))
                .andExpect(jsonPath("$.status").value("QUEUED"))
                // BCC must never appear in any response
                .andExpect(jsonPath("$.bcc").doesNotExist());
    }

    // -------------------------------------------------------------------------
    // POST /email — validation failure
    // -------------------------------------------------------------------------

    @Test
    void postEmail_invalidRequest_returns400WithErrorShape() throws Exception {
        // fromAddress is not a valid email; toAddress, subject, body are missing
        String invalidJson = "{\"fromAddress\": \"not-an-email\"}";

        mockMvc.perform(post("/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/email"));
    }

    // -------------------------------------------------------------------------
    // GET /email/{id} — not found
    // -------------------------------------------------------------------------

    @Test
    void getEmail_unknownId_returns404WithErrorShape() throws Exception {
        when(emailService.getEmailById(99L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Email not found with id: 99"));

        mockMvc.perform(get("/email/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Email not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/email/99"));
    }

    // -------------------------------------------------------------------------
    // GET /emails — pagination
    // -------------------------------------------------------------------------

    @Test
    void getEmails_withPageAndSize_returnsPagedResponse() throws Exception {
        when(emailService.getAllEmails(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockResponse), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/emails")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].fromAddress").value("from@example.com"))
                .andExpect(jsonPath("$.content[0].status").value("QUEUED"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0))
                // BCC must not appear in paged results either
                .andExpect(jsonPath("$.content[0].bcc").doesNotExist());
    }
}
