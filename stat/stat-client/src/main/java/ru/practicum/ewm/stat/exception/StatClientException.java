package ru.practicum.ewm.stat.exception;

public class StatClientException extends RuntimeException {
    public StatClientException(int statusCode, String message) {
        super("StatusCode is " + statusCode + ".\n" + message);
    }
}
