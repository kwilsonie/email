package com.example.demo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmailService {

    /** Persist with status=QUEUED and trigger async delivery. Returns immediately. */
    EmailResponse createEmail(EmailRequest request);

    Page<EmailResponse> getAllEmails(Pageable pageable);

    /** @throws org.springframework.web.server.ResponseStatusException 404 if not found */
    EmailResponse getEmailById(Long id);

    void deleteEmail(Long id);
}
