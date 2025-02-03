package ru.practicum.ewm.stat.server.exception;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiError {
    final String status;
    final String prefixMessage;
    final String message;
    final String stackTrace;
}
