package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * Handles asynchronous email delivery.  Called by EmailServiceImpl after a
 * new email has been committed to the database with status=QUEUED.
 *
 * <p>Design notes:
 * <ul>
 *   <li>This bean is separate from EmailServiceImpl so that Spring's AOP proxy
 *       can intercept the {@code @Async} annotation (self-invocation bypass
 *       is avoided).</li>
 *   <li>{@code @Async} is intentionally NOT combined with {@code @Transactional}
 *       on the same method to avoid Spring proxy-ordering ambiguity. Each
 *       repository call carries its own transaction via Spring Data JPA's
 *       built-in {@code @Transactional} on {@code SimpleJpaRepository}.</li>
 *   <li>Idempotency: if the email is already SENT when the task runs, it is
 *       silently skipped — safe to call multiple times.</li>
 * </ul>
 * </p>
 */
@Component
public class EmailAsyncProcessor {

    private static final Logger log = LoggerFactory.getLogger(EmailAsyncProcessor.class);

    private final EmailRepository emailRepository;
    private final EmailSendStrategy sendStrategy;

    public EmailAsyncProcessor(EmailRepository emailRepository, EmailSendStrategy sendStrategy) {
        this.emailRepository = emailRepository;
        this.sendStrategy = sendStrategy;
    }

    /**
     * Runs in the shared task executor thread pool.  Looks up the email by ID,
     * attempts delivery via the configured strategy, then updates status.
     */
    @Async
    public void processAsync(Long emailId) {
        log.info("processAsync: starting delivery for emailId={}", emailId);

        Optional<Email> optional = emailRepository.findById(emailId);
        if (optional.isEmpty()) {
            log.warn("processAsync: emailId={} not found, skipping", emailId);
            return;
        }
        Email email = optional.get();

        // Idempotency guard — do not re-send an already-delivered email.
        if (email.getStatus() == EmailStatus.SENT) {
            log.info("processAsync: emailId={} already SENT, skipping (idempotent)", emailId);
            return;
        }

        try {
            sendStrategy.execute(emailId);

            email.setStatus(EmailStatus.SENT);
            email.setStatusUpdatedAt(Instant.now());
            email.setFailureReason(null);
            emailRepository.save(email);
            log.info("processAsync: emailId={} marked SENT", emailId);

        } catch (Exception ex) {
            log.error("processAsync: emailId={} FAILED — {}", emailId, ex.getMessage());
            email.setStatus(EmailStatus.FAILED);
            email.setStatusUpdatedAt(Instant.now());
            email.setFailureReason(ex.getMessage());
            emailRepository.save(email);
        }
    }
}
