package com.orientation.orientationapp.dataplat_formats.core.exception;

import lombok.Getter;

@Getter
public class DataPlatformException extends RuntimeException {

    private final String code;

    public DataPlatformException(String message) {
        super(message);
        this.code = "DATA_PLATFORM_ERROR";
    }

    public DataPlatformException(String message, String code) {
        super(message);
        this.code = code;
    }

    public DataPlatformException(String message, Throwable cause) {
        super(message, cause);
        this.code = "DATA_PLATFORM_ERROR";
    }

    public DataPlatformException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
