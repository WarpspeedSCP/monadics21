package dev.wscp.monadics.result;

import dev.wscp.monadics.option.None;
import dev.wscp.monadics.option.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Result<T, E> permits Ok, Err {

    /**
     * Factory constructor for Ok values. If you want to use nullable values, use {@link #okOfNullable(T)}
     * @param value A non-null value to wrap within a result.
     * @param <E> The error type. Can be anything, not just {@link Throwable}.
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

    /**
     * Factory constructor for Err values. If you want to use nullable values instead, use {@link #errOfNullable(E)}
     * @param value the non-null value to wrap in an Err.
     * @return An Err of type E.
     */
    static <T, E> Err<T, @NotNull E> errOf(@NotNull E value) {
        return new Err<>(Objects.requireNonNull(value));
    }

    static <T, E> Err<T, @Nullable E> errOfNullable(@Nullable E value) {
        return new Err<>(value);
    }

    /**
     * @param action An action that may potentially throw.
     * @return {@link Ok} with the result if action succeeds. If action fails, return {@link Err} with any thrown exception inside.
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
     * Extracts the value of a result and returns a default value if the result is an {@link Err}.
     * If you want to set the default value to null, consider using {@link #unwrapOrNull()} instead.
     * @param defaultValue The default value to return if this result is not Ok.
     * @return The contained value, or the default if Err.
     */
    default T unwrapOrDefault(@Nullable T defaultValue) {
        return switch (this) {
            case Ok(T value) -> value;
            default -> defaultValue;
        };
    }

    /**
     * Extracts the value of a result and returns null if the result is an {@link Err}.
     * @return The wrapped value if {@link Ok}, else null.
     */
    default @Nullable T unwrapOrNull() {
        return unwrapOrDefault(null);
    }

    /**
     * Extracts the error of a result. throws an {@link UnwrapException} if there is no error value to return.
     * @return The unwrapped error if this is Err.
     * @throws UnwrapException if this is Ok.
     */
    default E unwrapError() {
        return switch (this) {
            case Err(E err) -> err;
            default -> throw new UnwrapException("Attempted to unwrapErr an ok value ");
        };
    }

    /**
     * Extracts the error of a result and returns null if the result is an {@link Ok}.
     * @return The wrappe error if {@link Err}, else null.
     */
    default @Nullable E unwrapErrorOrNull() {
        return unwrapErrorOrDefault(null);
    }

    /**
     * Extracts the error of a result and returns a default value if the result is an {@link Ok}.
     * If you want to set the default value to null, consider using {@link #unwrapErrorOrNull()} instead.
     * @param defaultValue The default value to return if this result is not Err.
     * @return The contained value, or the default if Ok.
     */
    default E unwrapErrorOrDefault(@Nullable E defaultValue) {
        return switch (this) {
            case Err(E value) -> value;
            default -> defaultValue;
        };
    }

    /**
     * Converts an Ok of type T to an Ok of type V.
     * @param action The action to perform on T to get V.
     * @return An Ok of type V
     * @param <V> The new value type of the result.
     */
    @SuppressWarnings("unchecked")
    default <V> Result<V, E> map(@NotNull Function<T, V> action) {
        return switch (this) {
            case Ok(T value) -> new Ok<>(action.apply(value));
            case Err<T, E> err -> (Err<V, E>) err;
        };
    }

    /**
     * Converts an Err of type E into an Err of type F by applying an action to it.
     * @param action The error transformation action to apply.
     * @return an Err of type F.
     * @param <F> the new error type.
     */
    @SuppressWarnings("unchecked")
    default <F> Result<T, F> mapError(@NotNull Function<E, F> action) {
        return switch (this) {
            case Err(E err) -> new Err<>(action.apply(err));
            case Ok<T, E> value -> (Ok<T, F>) value;
        };
    }

    /**
     * If this is {@link Ok}, return the result of action. Else, return the existing {@link Err}.
     * @param action The action to perform if this is {@link Ok}.
     * @return Ok<V> if this is {@link Ok}. {@link Err} if this is {@link Err}.
     * @param <V> The new value type.
     */
    @SuppressWarnings("unchecked")
    default <V> Result<V, E> andThen(@NotNull Function<T, Result<V, E>> action) {
        return switch (this) {
            case Ok(T value) -> action.apply(value);
            case Err<T, E> err -> (Err<V, E>) err;
        };
    }

    /**
     * This method allows for chaining multiple operations that could potentially throw exceptions.
     * It also allows for customising how initial error values are converted into throwables.
     *
     * <p>Use {@link #andThenRunCatching(Function)} if you want to use the default behaviour.</p>
     *
     * @param fallibleAction The fallible operation you want to perform.
     * @param errorHandler The transformation to apply when this is {@link Err} and not a throwable value.
     * @return
     * @param <V>
     */
    default <V> Result<V, Throwable> andThenRunCatching(@NotNull Function<T, V> fallibleAction, @NotNull Function<E, Throwable> errorHandler) {
        return switch (this) {
            case Ok(T value) -> {
                try {
                    yield new Ok<>(fallibleAction.apply(value));
                } catch (Throwable t) {
                    yield new Err<>(t);
                }
            }
            case Err(E err) -> new Err<>(errorHandler.apply(err));
        };
    }

    /**
     * This method allows for chaining multiple operations that could potentially throw exceptions.
     * If the initial error type is not {@link Throwable}, this method will wrap the error value in an {@link ResultRethrowException}
     *
     * @param fallibleAction An action to perform that could throw an exception.
     * @return If this is Ok, returns Ok if fallibleAction succeeds, and Err if fallibleAction fails.
     *         If this is Err, will return the error, transforming it into ResultRethrowException if required.
     */
    default <V> Result<V, Throwable> andThenRunCatching(@NotNull Function<T, V> fallibleAction) {
        return andThenRunCatching(
                fallibleAction,
                (E err) -> {
                    if (err instanceof Throwable e) {
                        return e;
                    } else {
                        return new ResultRethrowException("Wrapping " + err.getClass().getName() + " error value in rethrow exception.", null, err);
                    }
                });
    }

    /**
     * Allows for recovery from an error.
     * @param action The action to perform if this is an Err.
     * @return the result returned by action.
     * @param <F> The new error type.
     */
    @SuppressWarnings("unchecked")
    default <F> Result<? extends T, ? extends F> orElse(@NotNull Function<E, Result<T, F>> action) {
        return switch (this) {
            case Err(E err) -> action.apply(err);
            case Ok<T, E> ok-> (Ok<T, F>) ok;
        };
    }

    /**
     * Allows for recovery from errors by performing a fallible operation that may throw.
     *
     * @param fallibleAction an action that may throw an exception. Takes an error value as a parameter.
     * @return If this is Err and fallibleAction succeeds, returns Ok. If fallibleAction throws an exception, returns Err.
     *         If this is Ok, returns this.
     */
    @SuppressWarnings("unchecked")
    default Result<? extends T, ? extends Throwable> orElseRunCatching(@NotNull Function<E, T> fallibleAction) {
        return switch (this) {
            case Err(E value) -> {
                try {
                    yield new Ok<>(fallibleAction.apply(value));
                } catch (Throwable t) {
                    yield new Err<>(t);
                }
            }
            case Ok<T, E> ok -> (Ok<? extends T, ? extends Throwable>)ok;
        };
    }

    default Result<? extends E, ? extends T> swap() {
        return switch (this) {
            case Ok(T value) -> new Err<>(value);
            case Err(E err) -> new Ok<>(err);
        };
    }

    default Option<? extends T> ok() {
        return switch (this) {
            case Ok(T value) -> Option.some(value);
            case Err<T, E> e -> (None<? extends T>) Option.none;
        };
    }

    default Option<? extends E> err() {
        return switch (this) {
            case Ok<T, E> ok -> (None<E>)Option.none;
            case Err(E err) -> Option.some(err);
        };
    }
}
