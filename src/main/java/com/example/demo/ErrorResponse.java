package com.example.demo;

/**
 * Uniform error body returned by GlobalExceptionHandler for all error responses.
 *
 * <pre>
 * {
 *   "timestamp": "2024-06-01T12:00:00Z",
 *   "status":    400,
 *   "error":     "Bad Request",
 *   "message":   "fromAddress: must be a valid email address",
 *   "path":      "/email"
 * }
 * </pre>
 */
public record ErrorResponse(String timestamp, int status, String error, String message, String path) {}
