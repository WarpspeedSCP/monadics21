package dev.wscp.monadics.util;

import org.jetbrains.annotations.Nullable;

public class ResultBindingException extends ResultRethrowException {
    public ResultBindingException(String message, Throwable cause, @Nullable Object originalErr) {
        super(message, cause, originalErr);
    }

    public ResultBindingException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public ResultBindingException(String message) {
        this(message, null, null);
    }

    public ResultBindingException(Throwable cause) {
        this(null, cause, null);
    }

    public ResultBindingException() {
        this(null, null, null);
    }
}
