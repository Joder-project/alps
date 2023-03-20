package org.alps.core.socket.error;

public class StartServerException extends RuntimeException {

    public StartServerException() {
    }

    public StartServerException(String message) {
        super(message);
    }

    public StartServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public StartServerException(Throwable cause) {
        super(cause);
    }
}
