package ru.practicum.ewm.stat.server.exception;

import lombok.Builder;

@Builder
public class ApiError {
    private final String status;
    private final String prefixMessage;
    private final String message;
    private final String stackTrace;
}
