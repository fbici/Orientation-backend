package com.orientation.orientationapp.dataplat_formats.core.exception;

public class SecurityException extends DataPlatformException {

    public SecurityException(String message) {
        super(message, "SECURITY_ERROR");
    }
}
