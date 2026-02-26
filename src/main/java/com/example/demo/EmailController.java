package com.example.demo;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @RequestMapping(value = "/emails", method = RequestMethod.GET)
    public Page<EmailResponse> getAllEmails(@PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /emails - page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return emailService.getAllEmails(pageable);
    }

    @RequestMapping(value = "/email", method = RequestMethod.POST)
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
        log.info("POST /email - from={} to={}", request.getFromAddress(), request.getToAddress());
        EmailResponse response = emailService.createEmail(request);
        log.info("POST /email - created id={} status={}", response.getId(), response.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequestMapping(value = "/email/{id}", method = RequestMethod.GET)
    public ResponseEntity<EmailResponse> getEmail(@PathVariable Long id) {
        log.info("GET /email/{}", id);
        // 404 is handled by EmailServiceImpl throwing ResponseStatusException,
        // which GlobalExceptionHandler converts to the standard error JSON.
        return ResponseEntity.ok(emailService.getEmailById(id));
    }

    @RequestMapping(value = "/email/{id}", method = RequestMethod.DELETE)
    public String deleteEmail(@PathVariable Long id) {
        log.info("DELETE /email/{}", id);
        emailService.deleteEmail(id);
        return "Deleted";
    }
}
