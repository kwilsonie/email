package com.example.demo;

/**
 * Strategy that performs the actual (or simulated) email delivery for a given
 * persisted email ID.  Implementing beans are injected into EmailAsyncProcessor.
 *
 * <p>Throwing any exception signals delivery failure; the processor will mark
 * the email FAILED and record the exception message as failureReason.</p>
 *
 * <p>In tests, replace this bean with a @MockBean to control success/failure
 * without touching production code.</p>
 */
@FunctionalInterface
public interface EmailSendStrategy {
    void execute(Long emailId) throws Exception;
}
