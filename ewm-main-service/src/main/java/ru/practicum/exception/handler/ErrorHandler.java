package ru.practicum.exception.handler;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
@Generated
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.info("400 {}", e.getMessage(), e);
        return new ApiError("BAD_REQUEST", "Incorrectly made request", e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final DataNotFoundException e) {
        log.info("404 {}", e.getMessage(), e);
        return new ApiError("NOT_FOUND", "Please check your request", e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final DataConflictException e) {
        log.info("409 {}", e.getMessage(), e);
        return new ApiError("CONFLICT", "There is some conflict", e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityException(final DataIntegrityViolationException e) {
        log.info("409 {}", e.getMessage(), e);
        return new ApiError("CONFLICT", "Data integrity violation", e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.info("400 {}", e.getMessage(), e);
        return new ApiError("BAD_REQUEST", "Incorrect request", e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestParameterException(final MissingServletRequestParameterException e) {
        log.info("400 {}", e.getMessage(), e);
        return new ApiError("BAD_REQUEST", "Missing Parameter", e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowableExceptions(final Exception e) {
        log.info("500 {}", e.getMessage(), e);
        return new ApiError("INTERNAL_SERVER_ERROR", "Ooops, something went wrong", e.getMessage(),
                LocalDateTime.now());
    }

    @Getter
    static class ApiError {
        private final String status;
        private final String reason;
        private final String message;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private final LocalDateTime timestamp;

        public ApiError(String status, String reason, String message, LocalDateTime timestamp) {
            this.status = status;
            this.reason = reason;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}
