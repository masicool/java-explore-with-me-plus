package ru.practicum.ewm.stat.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException e) {
        return buildApiError(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleInternalServerError(final Exception e, HttpStatus status) {
        return buildApiError(e, status);
    }

    private ResponseEntity<ApiError> buildApiError(Exception e, HttpStatus status) {
        log.info("{} {}", status.value(), e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ResponseEntity<>(ApiError.builder()
                .status(status.toString())
                .prefixMessage("Error ....")
                .message(e.getMessage())
                .stackTrace(stackTrace)
                .build(),
                status);
    }
}
