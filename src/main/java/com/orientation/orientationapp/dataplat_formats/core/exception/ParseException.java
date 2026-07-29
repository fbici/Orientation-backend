package com.orientation.orientationapp.dataplat_formats.core.exception;

public class ParseException extends DataPlatformException {

    public ParseException(String message) {
        super(message, "PARSE_ERROR");
    }

    public ParseException(String message, Throwable cause) {
        super(message, "PARSE_ERROR", cause);
    }
}
