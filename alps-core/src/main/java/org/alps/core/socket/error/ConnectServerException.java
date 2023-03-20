package org.alps.core.socket.error;

public class ConnectServerException extends RuntimeException {

    public ConnectServerException() {
    }

    public ConnectServerException(String message) {
        super(message);
    }

    public ConnectServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectServerException(Throwable cause) {
        super(cause);
    }
}
