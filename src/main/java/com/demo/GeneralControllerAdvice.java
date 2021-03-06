package com.demo;

import com.demo.account.exception.RequestException;
import com.demo.account.model.ErrorResponse;
import com.demo.keycloak.exception.AuthenticationException;
import com.demo.keycloak.exception.KeycloakException;
import com.demo.mail.exception.MailException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GeneralControllerAdvice {
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({KeycloakException.class, MailException.class})
    public HttpEntity<ErrorResponse> internalErrorExceptionHandler(Exception e) {
        return new HttpEntity<>(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({AuthenticationException.class, MissingRequestHeaderException.class})
    public HttpEntity<ErrorResponse> unauthorizedExceptionHandler(Exception e) {
        return new HttpEntity<>(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MissingPathVariableException.class, MissingServletRequestParameterException.class,
            ConstraintViolationException.class, RequestException.class})
    public HttpEntity<ErrorResponse> badRequestExceptionHandler(Exception e) {
        return new HttpEntity<>(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HttpEntity<ErrorResponse> badRequestExceptionHandler(MethodArgumentNotValidException e) {
        return new HttpEntity<>(new ErrorResponse(e.getClass().getSimpleName(), getValidationErrors(e)));
    }

    public String getValidationErrors(MethodArgumentNotValidException e) {
        return e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
