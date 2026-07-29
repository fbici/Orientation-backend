package com.orientation.orientationapp.dataplat_formats.core.exception;

public class VersionException extends DataPlatformException {

    public VersionException(String message) {
        super(message, "VERSION_ERROR");
    }
}
