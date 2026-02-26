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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailController.class)
class EmailControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  EmailRepository emailRepository;

    private Email savedEmail;

    @BeforeEach
    void setUp() {
        savedEmail = new Email();
        savedEmail.setId(1L);
        savedEmail.setFromAddress("from@example.com");
        savedEmail.setToAddress("to@example.com");
        savedEmail.setSubject("Hello");
        savedEmail.setBody("World");
        savedEmail.setBcc("secret@example.com"); // BCC stored but must never appear in responses
    }

    // -------------------------------------------------------------------------
    // POST /email — success
    // -------------------------------------------------------------------------

    @Test
    void postEmail_validRequest_returns201WithEmailResponse() throws Exception {
        when(emailRepository.save(any(Email.class))).thenReturn(savedEmail);

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
                .andExpect(jsonPath("$.subject").value("Hello"))
                .andExpect(jsonPath("$.body").value("World"))
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
        when(emailRepository.findById(99L)).thenReturn(Optional.empty());

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
        when(emailRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(savedEmail), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/emails")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].fromAddress").value("from@example.com"))
                .andExpect(jsonPath("$.content[0].toAddress").value("to@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0))
                // BCC must not appear in paged results either
                .andExpect(jsonPath("$.content[0].bcc").doesNotExist());
    }
}
