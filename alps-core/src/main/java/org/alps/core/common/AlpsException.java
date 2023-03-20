package org.alps.core.common;

public class AlpsException extends RuntimeException {

    public AlpsException() {
    }

    public AlpsException(String message) {
        super(message);
    }

    public AlpsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlpsException(Throwable cause) {
        super(cause);
    }
}
