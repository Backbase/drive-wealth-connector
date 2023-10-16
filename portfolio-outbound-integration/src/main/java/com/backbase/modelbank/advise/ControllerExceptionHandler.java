package com.backbase.modelbank.advise;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ValidationException;
import java.time.OffsetDateTime;

@ControllerAdvice
@Order(1)
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {InternalServerErrorException.class})
    protected ResponseEntity<Object> handelInternalServerError(InternalServerErrorException ex, WebRequest request) {
        var error = new ErrorResponse(OffsetDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getLocalizedMessage(),request.getContextPath());
        return handleExceptionInternal(ex, error, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {ValidationException.class})
    protected ResponseEntity<Object> handelValidationError(ValidationException ex, WebRequest request) {
        var error = new ErrorResponse(OffsetDateTime.now(), HttpStatus.BAD_REQUEST.value(), ex.getLocalizedMessage(),request.getContextPath());
        return handleExceptionInternal(ex, error, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    public record ErrorResponse(OffsetDateTime date, int status, String error, String path) { }
}
