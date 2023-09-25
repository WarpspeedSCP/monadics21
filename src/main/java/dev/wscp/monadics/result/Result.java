package dev.wscp.monadics.result;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Result<T, E> permits Ok, Err {

    /**
     * Factory constructor for Ok values.
     * @param value A non-null value to wrap within a result.
     * @param <E> The error type. Can be anything, not just [Throwable].
     */
    static <T, E> Ok<@NotNull T, E> okOf(@NotNull T value) {
        return new Ok<>(Objects.requireNonNull(value));
    }

    /**
     * Factory constructor for Ok values with nullable contents.
     * @param value A nullable value to wrap within a result.
     * @param <E>
     */
    static <T, E> Ok<@Nullable T, E> okOfNullable(@Nullable T value) {
        return new Ok<>(value);
    }

    static <T, E> Err<T, @NotNull E> errOf(@NotNull E value) {
        return new Err<>(value);
    }

    static <T, E> Err<T, @Nullable E> errOfNullable(@Nullable E value) {
        return new Err<>(value);
    }

    /**
     * @param action An action that may potentially throw.
     * @return Ok with the result if [action] succeeds. If [action] fails, return Err with any thrown exception inside.
     * @param <T> The value type for this result. The error type is always Throwable.
     */
    static <T> Result<T, Throwable> runCatching(Supplier<T> action) {
        try {
            return new Ok<>(action.get());
        } catch (Throwable error) {
            return new Err<>(error);
        }
    }

    /**
     * Extracts the value of a result.
     * @throws UnwrapException if this result is an Err and not an Ok.
     * @return the contained value.
     */
    default T unwrap() {
        return switch (this) {
            case Ok(T value) -> value;
            case Err(E err) -> {
                if (err instanceof Throwable e) {
                    throw new UnwrapException("Attempted to unwrap an exception.", e);
                } else {
                    throw new UnwrapException("Attempted to unwrap an error value " + err.toString());
                }
            }
        };
    }

    /**
     * @param defaultValue The default value to return if this result is not Ok.
     * @return The contained value, or the default if Err.
     */
    default T unwrapOrDefault(T defaultValue) {
        return switch (this) {
            case Ok(T value) -> value;
            default -> defaultValue;
        };
    }

    /**
     * @return The returned value if Ok, else null.
     */
    default @Nullable T unwrapOrNull() {
        return  switch (this) {
            case Ok(T value) -> value;
            default -> null;
        };
    }

    default E unwrapError() {
        return switch (this) {
            case Err(E err) -> err;
            default -> throw new UnwrapException("Attempted to unwrapErr an ok value ");
        };
    }

    @SuppressWarnings("unchecked")
    default <V> Result<V, E> map(Function<T, V> transform) {
        return switch (this) {
            case Ok(T value) -> new Ok<>(transform.apply(value));
            case Err<T, E> err -> (Err<V, E>) err;
        };
    }

    @SuppressWarnings("unchecked")
    default <F> Result<T, F> mapError(Function<E, F> transform) {
        return switch (this) {
            case Err(E err) -> new Err<>(transform.apply(err));
            case Ok<T, E> value -> (Ok<T, F>) value;
        };
    }

    @SuppressWarnings("unchecked")
    default <V> Result<V, E> andThen(Function<T, Result<V, E>> transform) {
        return switch (this) {
            case Ok(T value) -> transform.apply(value);
            case Err<T, E> err -> (Err<V, E>) err;
        };
    }

    /**
     *
     * @param transform The fallible operation you want to perform.
     * @param errorTransform The transformation to apply when this is an error and you need to convert the wrapped error into a throwable to keep types compatible.
     * @return
     * @param <V>
     */
    default <V> Result<V, Throwable> andThenRunCatching(Function<T, V> transform, Function<E, Throwable> errorTransform) {
        return switch (this) {
            case Ok(T value) -> {
                try {
                    yield new Ok<>(transform.apply(value));
                } catch (Throwable t) {
                    yield new Err<>(t);
                }
            }
            case Err(E err) -> new Err<>(errorTransform.apply(err));
        };
    }

    /**
     *
     *
     * @param transform
     * @return
     * @param <V>
     */
    default <V> Result<V, Throwable> andThenRunCatching(Function<T, V> transform) {
        return andThenRunCatching(
                transform,
                (E err) -> {
                    if (err instanceof Throwable e) {
                        return new ResultRethrowException("Rethrown exception", e);
                    } else {
                        return new ResultRethrowException("Wrapping " + err.getClass().getName() + " value in rethrow exception.", null, err);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    default <F> Result<T, F> orElse(Function<E, Result<T, F>> transform) {
        return switch (this) {
            case Err(E err) -> transform.apply(err);
            case Ok<T, E> ok-> (Ok<T, F>) ok;
        };
    }

    @SuppressWarnings("unchecked")
    default <V> Result<T, Throwable> orElseRunCatching(Function<E, T> transform) {
        return switch (this) {
            case Err(E value) -> {
                try {
                    yield new Ok<>(transform.apply(value));
                } catch (Throwable t) {
                    yield new Err<>(t);
                }
            }
            case Ok<T, E> ok -> (Ok<T, Throwable>)ok;
        };
    }
}
