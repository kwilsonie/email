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
import org.springframework.web.server.ResponseStatusException;

@RestController
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    private final EmailRepository emailRepository;

    public EmailController(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @RequestMapping(value = "/emails", method = RequestMethod.GET)
    public Page<EmailResponse> getAllEmails(@PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /emails - page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<EmailResponse> page = emailRepository.findAll(pageable).map(EmailResponse::from);
        log.info("GET /emails - returning {} of {} total email(s)",
                page.getNumberOfElements(), page.getTotalElements());
        return page;
    }

    @RequestMapping(value = "/email", method = RequestMethod.POST)
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
        log.info("POST /email - saving email from={} to={}", request.getFromAddress(), request.getToAddress());
        Email saved = emailRepository.save(toEntity(request));
        log.info("POST /email - saved with id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(EmailResponse.from(saved));
    }

    @RequestMapping(value = "/email/{id}", method = RequestMethod.GET)
    public ResponseEntity<EmailResponse> getEmail(@PathVariable Long id) {
        log.info("GET /email/{} - fetching email", id);
        Email email = emailRepository.findById(id).orElseThrow(() -> {
            log.warn("GET /email/{} - not found", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found with id: " + id);
        });
        log.info("GET /email/{} - found", id);
        return ResponseEntity.ok(EmailResponse.from(email));
    }

    @RequestMapping(value = "/email/{id}", method = RequestMethod.DELETE)
    public String deleteEmail(@PathVariable Long id) {
        log.info("DELETE /email/{} - deleting email", id);
        emailRepository.deleteById(id);
        log.info("DELETE /email/{} - deleted", id);
        return "Deleted";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Email toEntity(EmailRequest req) {
        Email email = new Email();
        email.setFromAddress(req.getFromAddress());
        email.setToAddress(req.getToAddress());
        email.setCc(req.getCc());
        email.setBcc(req.getBcc()); // stored in DB; never returned via EmailResponse
        email.setSubject(req.getSubject());
        email.setBody(req.getBody());
        email.setAttachments(req.getAttachments());
        email.setSignature(req.getSignature());
        return email;
    }
}
