package com.example.demo;

import java.time.Instant;

public class EmailResponse {

    private Long id;
    private String fromAddress;
    private String toAddress;
    private String cc;
    // bcc is intentionally omitted — never expose BCC to API consumers
    private String subject;
    private String body;
    private String attachments;
    private String signature;

    // Delivery lifecycle — always present after POST /email
    private EmailStatus status;
    private Instant statusUpdatedAt;
    private String failureReason; // null unless status == FAILED

    public static EmailResponse from(Email email) {
        EmailResponse r = new EmailResponse();
        r.id              = email.getId();
        r.fromAddress     = email.getFromAddress();
        r.toAddress       = email.getToAddress();
        r.cc              = email.getCc();
        r.subject         = email.getSubject();
        r.body            = email.getBody();
        r.attachments     = email.getAttachments();
        r.signature       = email.getSignature();
        r.status          = email.getStatus();
        r.statusUpdatedAt = email.getStatusUpdatedAt();
        r.failureReason   = email.getFailureReason();
        return r;
    }

    public Long getId()              { return id; }
    public String getFromAddress()   { return fromAddress; }
    public String getToAddress()     { return toAddress; }
    public String getCc()            { return cc; }
    public String getSubject()       { return subject; }
    public String getBody()          { return body; }
    public String getAttachments()   { return attachments; }
    public String getSignature()     { return signature; }
    public EmailStatus getStatus()   { return status; }
    public Instant getStatusUpdatedAt() { return statusUpdatedAt; }
    public String getFailureReason() { return failureReason; }
}
