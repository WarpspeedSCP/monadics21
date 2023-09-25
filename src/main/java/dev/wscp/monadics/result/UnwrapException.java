package dev.wscp.monadics.result;

import java.util.NoSuchElementException;

public class UnwrapException extends NoSuchElementException {
    public UnwrapException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnwrapException(String message) {
        super(message);
    }

    public UnwrapException(Throwable cause) {
        super(cause);
    }

    public UnwrapException() {
        super();
    }
}
