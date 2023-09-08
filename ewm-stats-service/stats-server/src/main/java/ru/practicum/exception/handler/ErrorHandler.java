package ru.practicum.exception.handler;

import lombok.Generated;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.ValidationException;

@RestControllerAdvice
@Slf4j
@Generated
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        log.info("400 {}", e.getMessage(), e);
        return new ErrorResponse("Not valid", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final DataNotFoundException e) {
        log.info("404 {}", e.getMessage(), e);
        return new ErrorResponse("Data not found", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(final DataConflictException e) {
        log.info("409 {}", e.getMessage(), e);
        return new ErrorResponse("Data conflict", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityException(final DataIntegrityViolationException e) {
        log.info("409 {}", e.getMessage(), e);
        return new ErrorResponse("Data integrity violation", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.info("400 {}", e.getMessage(), e);
        return new ErrorResponse("Method Argument Not Valid", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestParameterException(final MissingServletRequestParameterException e) {
        log.info("400 {}", e.getMessage(), e);
        return new ErrorResponse("Missing Parameter", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowableExceptions(final Exception e) {
        log.info("500 {}", e.getMessage(), e);
        return new ErrorResponse("error", e.getMessage());
    }

    @Getter
    static class ErrorResponse {
        String error;
        String description;

        public ErrorResponse(String error, String description) {
            this.error = error;
            this.description = description;
        }
    }
}
