package dev.wscp.monadics.result;


import org.jetbrains.annotations.Nullable;

/**
 * An exception that is returned as the alternative when a non-throwable error value is passed to Result.andThenRunCatching or Result.orElseRunCatching.
 * It wraps the original non-throwable error value, so we can transform the final value from Result<T, E> into a Result<V, Throwable>
 * Unfortunately, Java disallows generics in throwable classes, so we cannot explicitly specify the type of the wrapped original error.
 */
public class ResultRethrowException extends RuntimeException {
    public final @Nullable Object originalErr;

    public ResultRethrowException(String message, Throwable cause, @Nullable Object originalErr) {
        super(message, cause);
        this.originalErr = originalErr;
    }

    public ResultRethrowException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public ResultRethrowException(String message) {
        this(message, null, null);
    }

    public ResultRethrowException(Throwable cause) {
        this(null, cause, null);
    }

    public ResultRethrowException() {
        this(null, null, null);
    }
}
