package com.github.api.client.exception;

import com.github.api.client.PropertiesValues;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RequiredArgsConstructor
@RestControllerAdvice
public class ResponseExceptionHandler {
    private final PropertiesValues propertyValues;

    @ExceptionHandler(GithubUserNotFoundException.class)
    public ResponseEntity<ExceptionMessage> githubUserNotFoundException(GithubUserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionMessage(HttpStatus.NOT_FOUND.value(), exception.getMessage()));
    }

    @ExceptionHandler(WrongParamValueException.class)
    public ResponseEntity<ExceptionMessage> wrongParamValueException(WrongParamValueException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionMessage(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ExceptionMessage> handleHttpMediaTypeNotAcceptableException(
            HttpMediaTypeNotAcceptableException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionMessage(HttpStatus.NOT_ACCEPTABLE.value(), this.propertyValues.notAcceptableMessage));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ExceptionMessage> missingRequestHeaderException(MissingRequestHeaderException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExceptionMessage(HttpStatus.BAD_REQUEST.value(), this.propertyValues.missingHeader));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ExceptionMessage> httpClientErrorException(HttpClientErrorException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionMessage(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            this.propertyValues.unexpectedErrorMessage + exception.getMessage()
                        )
                );
    }
}
