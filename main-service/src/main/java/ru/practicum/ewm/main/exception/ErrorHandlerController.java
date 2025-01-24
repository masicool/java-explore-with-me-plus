package ru.practicum.ewm.main.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.main.exception.type.BadRequestException;
import ru.practicum.ewm.main.exception.type.ForbiddenException;
import ru.practicum.ewm.main.exception.type.NotFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException ex) {
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND.toString())
                .reason("The required object was not found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return ApiError.builder()
                .status(HttpStatus.CONFLICT.toString())
                .reason("Integrity constraint has been violated")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleForbiddenException(ForbiddenException ex) {
        return ApiError.builder()
                .status(HttpStatus.CONFLICT.toString())
                .reason("For the requested operation the conditions are not met")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ru.practicum.ewm.main.exception.type.BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(BadRequestException ex) {
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Incorrectly made request")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleArgumentNotValidException(MethodArgumentNotValidException ex) {
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Incorrectly made request")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
