package com.example.demo;



import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.util.Date;


@Entity
@Table(name = "email")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromAddress;
    private String toAddress;
    private String cc;
    private String bcc;

    private String subject;

    @Lob
    private String body;

    private String attachments;
    private String signature;

    public Email() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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