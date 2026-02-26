package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default EmailSendStrategy used in production. Simulates an SMTP round-trip
 * with a short sleep. Replace with a real SMTP/API implementation when ready.
 */
@Component
public class SimulatedEmailSendStrategy implements EmailSendStrategy {

    private static final Logger log = LoggerFactory.getLogger(SimulatedEmailSendStrategy.class);

    @Override
    public void execute(Long emailId) throws Exception {
        log.debug("Simulating send for emailId={}", emailId);
        Thread.sleep(100); // simulate network I/O
        log.debug("Simulated send complete for emailId={}", emailId);
    }
}
