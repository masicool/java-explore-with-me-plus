package ru.practicum.ewm.main.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.main.exception.type.BadRequestException;
import ru.practicum.ewm.main.exception.type.ForbiddenException;
import ru.practicum.ewm.main.exception.type.NotFoundException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {
        return buildResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return buildResponseEntity(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException ex) {
        return buildResponseEntity(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ru.practicum.ewm.main.exception.type.BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleArgumentNotValidException(MethodArgumentNotValidException ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ApiError> buildResponseEntity(Exception ex, HttpStatus status) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(
                ApiError.builder()
                        .status(status.toString())
                        .reason(ex.getClass().toString())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build(),
                status
        );
    }
}
