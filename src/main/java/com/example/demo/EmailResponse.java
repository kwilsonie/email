package com.example.demo;

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

    public static EmailResponse from(Email email) {
        EmailResponse r = new EmailResponse();
        r.id           = email.getId();
        r.fromAddress  = email.getFromAddress();
        r.toAddress    = email.getToAddress();
        r.cc           = email.getCc();
        r.subject      = email.getSubject();
        r.body         = email.getBody();
        r.attachments  = email.getAttachments();
        r.signature    = email.getSignature();
        return r;
    }

    public Long getId()           { return id; }
    public String getFromAddress(){ return fromAddress; }
    public String getToAddress()  { return toAddress; }
    public String getCc()         { return cc; }
    public String getSubject()    { return subject; }
    public String getBody()       { return body; }
    public String getAttachments(){ return attachments; }
    public String getSignature()  { return signature; }
}
