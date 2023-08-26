package org.alps.core.common;

public class AlpsSocketException extends RuntimeException {

    public AlpsSocketException() {
    }

    public AlpsSocketException(String message) {
        super(message);
    }

    public AlpsSocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlpsSocketException(Throwable cause) {
        super(cause);
    }
}
