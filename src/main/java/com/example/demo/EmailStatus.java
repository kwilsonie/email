package com.example.demo;

public enum EmailStatus {
    /** Persisted and queued for async delivery. */
    QUEUED,
    /** Async processor completed delivery successfully. */
    SENT,
    /** Async processor encountered an error; see failureReason. */
    FAILED
}
