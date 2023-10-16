package com.backbase.modelbank.advise;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ValidationException;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    @Mock
    WebRequest webRequest;

    @InjectMocks
    ControllerExceptionHandler controllerExceptionHandler;

    @Test
    void testHandelInternalServerError() {

        // Given
        ResponseEntity<ControllerExceptionHandler.ErrorResponse> internalErrorResponse = ResponseEntity.internalServerError().body(
                new ControllerExceptionHandler.ErrorResponse(OffsetDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null));

        // When
        ResponseEntity<Object> result = controllerExceptionHandler.handelInternalServerError(new InternalServerErrorException(), webRequest);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(ControllerExceptionHandler.ErrorResponse.class, result.getBody());
        Assertions.assertEquals(internalErrorResponse.getStatusCode(), result.getStatusCode());
    }

    @Test
    void testHandelValidationError() {
        // Given
        ResponseEntity<ControllerExceptionHandler.ErrorResponse> badRequestException = ResponseEntity.badRequest().body(
                new ControllerExceptionHandler.ErrorResponse(OffsetDateTime.now(), HttpStatus.BAD_REQUEST.value(), null, null));
        // When
        ResponseEntity<Object> result = controllerExceptionHandler.handelValidationError(new ValidationException(), webRequest);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(ControllerExceptionHandler.ErrorResponse.class, result.getBody());
        Assertions.assertEquals(badRequestException.getStatusCode(), result.getStatusCode());
    }
}