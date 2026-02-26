package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @EnableAsync activates Spring's async task executor.  Spring Boot's
 * TaskExecutionAutoConfiguration provides a ThreadPoolTaskExecutor with
 * sensible defaults (tunable via spring.task.execution.* properties).
 */
@SpringBootApplication
@EnableAsync
public class EmailApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailApplication.class, args);
	}
}
