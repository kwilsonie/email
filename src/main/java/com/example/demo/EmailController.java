package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    private final EmailRepository emailRepository;

    public EmailController(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @RequestMapping(value = "/emails", method = RequestMethod.GET)
    public List<Email> getAllEmails() {
        log.info("GET /emails - fetching all emails");
        List<Email> emails = emailRepository.findAll();
        log.info("GET /emails - returning {} email(s)", emails.size());
        return emails;
    }

    @RequestMapping(value = "/email", method = RequestMethod.POST)
    public Email sendEmail(@RequestBody Email email) {
        log.info("POST /email - saving email from={} to={}", email.getFromAddress(), email.getToAddress());
        Email saved = emailRepository.save(email);
        log.info("POST /email - saved with id={}", saved.getId());
        return saved;
    }

    @RequestMapping(value = "/email/{id}", method = RequestMethod.GET)
    public ResponseEntity<Email> getEmail(@PathVariable Long id) {
        log.info("GET /email/{} - fetching email", id);
        return emailRepository.findById(id)
                .map(email -> {
                    log.info("GET /email/{} - found", id);
                    return ResponseEntity.ok(email);
                })
                .orElseGet(() -> {
                    log.warn("GET /email/{} - not found", id);
                    return ResponseEntity.notFound().<Email>build();
                });
    }

    @RequestMapping(value = "/email/{id}", method = RequestMethod.DELETE)
    public String deleteEmail(@PathVariable Long id) {
        log.info("DELETE /email/{} - deleting email", id);
        emailRepository.deleteById(id);
        log.info("DELETE /email/{} - deleted", id);
        return "Deleted";
    }
}