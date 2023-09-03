package org.alps.core.common;

public class AlpsAuthException extends RuntimeException {

    public AlpsAuthException() {
    }

    public AlpsAuthException(String message) {
        super(message);
    }

    public AlpsAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlpsAuthException(Throwable cause) {
        super(cause);
    }
}
