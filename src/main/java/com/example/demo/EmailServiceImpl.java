package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final EmailRepository emailRepository;
    private final EmailAsyncProcessor asyncProcessor;

    public EmailServiceImpl(EmailRepository emailRepository, EmailAsyncProcessor asyncProcessor) {
        this.emailRepository = emailRepository;
        this.asyncProcessor = asyncProcessor;
    }

    /**
     * Saves the email with status=QUEUED, then fires the async processor.
     * The save is committed before processAsync is submitted, ensuring the
     * async thread will always find the entity in the database.
     */
    @Override
    public EmailResponse createEmail(EmailRequest request) {
        Email email = toEntity(request);
        email.setStatus(EmailStatus.QUEUED);
        email.setStatusUpdatedAt(Instant.now());

        Email saved = emailRepository.save(email); // committed in its own @Transactional
        log.info("createEmail: persisted id={} as QUEUED", saved.getId());

        asyncProcessor.processAsync(saved.getId()); // fire-and-forget
        return EmailResponse.from(saved);
    }

    @Override
    public Page<EmailResponse> getAllEmails(Pageable pageable) {
        return emailRepository.findAll(pageable).map(EmailResponse::from);
    }

    @Override
    public EmailResponse getEmailById(Long id) {
        return emailRepository.findById(id)
                .map(EmailResponse::from)
                .orElseThrow(() -> {
                    log.warn("getEmailById: not found id={}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Email not found with id: " + id);
                });
    }

    @Override
    public void deleteEmail(Long id) {
        emailRepository.deleteById(id);
    }

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
