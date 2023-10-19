package com.backbase.modelbank.exceptions;

/**
 * Exception that throw when failed to obtain drive-wealth access token
 */
public class DriveWealthObtainTokenException extends RuntimeException {

    public DriveWealthObtainTokenException(String message) {
        super(message);
    }
}
