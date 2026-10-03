package com.mailserver.model;

import java.time.LocalDateTime;

public class Email {

    private int id;

    private String sender;
    private String receiver;
    private String subject;
    private String content;

    private boolean isRead;
    private boolean isDeleted;

    private LocalDateTime sentAt;

    // ==========================================
    // THREAD
    // ==========================================

    private int threadId;

    private Integer inReplyTo;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public Email() {

    }

    public Email(
            String sender,
            String receiver,
            String subject,
            String content) {

        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
    }

    // ==========================================
    // BASIC GETTERS / SETTERS
    // ==========================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    // ==========================================
    // THREAD GETTERS / SETTERS
    // ==========================================

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public Integer getInReplyTo() {
        return inReplyTo;
    }

    public void setInReplyTo(Integer inReplyTo) {
        this.inReplyTo = inReplyTo;
    }
}