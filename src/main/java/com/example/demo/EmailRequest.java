package com.example.demo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailRequest {

    @NotBlank(message = "fromAddress is required")
    @Email(message = "fromAddress must be a valid email address")
    private String fromAddress;

    @NotBlank(message = "toAddress is required")
    @Email(message = "toAddress must be a valid email address")
    private String toAddress;

    /** Optional. When present, must be a comma-separated list of valid emails. */
    @ValidEmailList
    private String cc;

    /** Optional. When present, must be a comma-separated list of valid emails. */
    @ValidEmailList
    private String bcc;

    @NotBlank(message = "subject is required")
    private String subject;

    @NotBlank(message = "body is required")
    private String body;

    private String attachments;
    private String signature;

    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

    public String getCc() { return cc; }
    public void setCc(String cc) { this.cc = cc; }

    public String getBcc() { return bcc; }
    public void setBcc(String bcc) { this.bcc = bcc; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}
