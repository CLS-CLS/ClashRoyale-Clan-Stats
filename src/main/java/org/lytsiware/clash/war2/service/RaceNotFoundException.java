package org.lytsiware.clash.war2.service;

public class RaceNotFoundException extends RuntimeException {
    public RaceNotFoundException() {
        super();
    }

    public RaceNotFoundException(String message) {
        super(message);
    }

    public RaceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RaceNotFoundException(Throwable cause) {
        super(cause);
    }

    protected RaceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
