package dev.wscp.monadics.result;

import dev.wscp.monadics.option.Option;
import dev.wscp.monadics.util.ResultBindingException;
import dev.wscp.monadics.util.ResultRethrowException;
import dev.wscp.monadics.util.UnwrapException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public sealed interface Result<T, E> permits Ok, Err {

    /**
     * Factory constructor for Ok values. If you want to use nullable values, use {@link #okOfNullable(T)}
     *
     * @param value A non-null value to wrap within a result.
     * @param <E>   The error type. Can be anything, not just {@link Throwable}.
     */
    static <T, E> Ok<@NotNull T, E> okOf(@NotNull T value) {
        return new Ok<>(Objects.requireNonNull(value));
    }

    /**
     * Factory constructor for Ok values with nullable contents.
     *
     * @param value A nullable value to wrap within a result.
     * @param <E>
     */
    static <T, E> Ok<@Nullable T, E> okOfNullable(@Nullable T value) {
        return new Ok<>(value);
    }

    /**
     * Factory constructor for Err values. If you want to use nullable values instead, use {@link #errOfNullable(E)}
     *
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
     * @param <T>    The value type for this result. The error type is always Throwable.
     * @return {@link Ok} with the result if action succeeds. If action fails, return {@link Err} with any thrown exception inside.
     */
    static <T> Result<T, Throwable> runCatching(Supplier<T> action) {
        try {
            return okOf(action.get());
        } catch (Throwable error) {
            return errOf(error);
        }
    }

    default boolean isOk() {
        return this instanceof Ok<T, E>;
    }

    default boolean isErr() {
        return this instanceof Err<T, E>;
    }

    default Stream<T> toStream() {
        return switch (this) {
            case Ok(T value) -> Stream.of(value);
            default -> Stream.empty();
        };
    }

    /**
     * Extracts the value of a result.
     *
     * @return the contained value.
     * @throws UnwrapException if this result is an Err and not an Ok.
     */
    default T unwrap() {
        return switch (this) {
            case Ok(T value) -> value;
            case Err(E err) -> {
                if (err instanceof Throwable e) {
                    var exc = new UnwrapException("Attempted to unwrap an exception.");
                    exc.addSuppressed(e);
                    throw exc;
                } else {
                    throw new UnwrapException("Attempted to unwrap an error value " + err.toString());
                }
            }
        };
    }

    /**
     * Extracts the value of a result and returns a default value if the result is an {@link Err}.
     * If you want to set the default value to null, consider using {@link #unwrapOrNull()} instead.
     *
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
     *
     * @return The wrapped value if {@link Ok}, else null.
     */
    default @Nullable T unwrapOrNull() {
        return unwrapOrDefault(null);
    }

    /**
     * Extracts the error of a result. throws an {@link UnwrapException} if there is no error value to return.
     *
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
     *
     * @return The wrappe error if {@link Err}, else null.
     */
    default @Nullable E unwrapErrorOrNull() {
        return unwrapErrorOrDefault(null);
    }

    /**
     * Extracts the error of a result and returns a default value if the result is an {@link Ok}.
     * If you want to set the default value to null, consider using {@link #unwrapErrorOrNull()} instead.
     *
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
     *
     * @param action The action to perform on T to get V.
     * @param <V>    The new value type of the result.
     * @return An Ok of type V
     */
    @SuppressWarnings("unchecked")
    default <V> Result<V, E> map(@NotNull Function<T, V> action) {
        return switch (this) {
            case Ok(T value) -> okOf(action.apply(value));
            case Err<T, E> err -> (Err<V, E>) err;
        };
    }

    /**
     * Converts an Err of type E into an Err of type F by applying an action to it.
     *
     * @param action The error transformation action to apply.
     * @param <F>    the new error type.
     * @return an Err of type F.
     */
    @SuppressWarnings("unchecked")
    default <F> Result<T, F> mapError(@NotNull Function<E, F> action) {
        return switch (this) {
            case Err(E err) -> errOf(action.apply(err));
            case Ok<T, E> value -> (Ok<T, F>) value;
        };
    }

    /**
     * If this is {@link Ok}, return the result of action. Else, return the existing {@link Err}.
     *
     * @param action The action to perform if this is {@link Ok}.
     * @param <V>    The new value type.
     * @return Ok<V> if this is {@link Ok}. {@link Err} if this is {@link Err}.
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
     * @param errorHandler   The transformation to apply when this is {@link Err} and not a throwable value.
     * @param <V>
     * @return
     */
    default <V> Result<V, ? extends Throwable> andThenRunCatching(@NotNull Function<T, V> fallibleAction, @NotNull Function<E, Throwable> errorHandler) {
        return switch (this) {
            case Ok(T value) -> {
                try {
                    yield okOf(fallibleAction.apply(value));
                } catch (Throwable t) {
                    yield errOf(t);
                }
            }
            case Err(E err) -> errOf(errorHandler.apply(err));
        };
    }

    /**
     * This method allows for chaining multiple operations that could potentially throw exceptions.
     * If the initial error type is not {@link Throwable}, this method will wrap the error value in an {@link ResultRethrowException}
     *
     * @param fallibleAction An action to perform that could throw an exception.
     * @return If this is Ok, returns Ok if fallibleAction succeeds, and Err if fallibleAction fails.
     * If this is Err, will return the error, transforming it into ResultRethrowException if required.
     */
    default <V> Result<V, ? extends Throwable> andThenRunCatching(@NotNull Function<T, V> fallibleAction) {
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
     * Allows for fallible recovery from an error. If action returns Ok, this method will return an Ok of type {@link T}.
     * Else, it will return an Err of type {@link F}.
     * If you want to instead convert between error types alone, consider using {@link #mapError(Function)}.
     *
     * @param action The action to perform if this is an Err.
     * @param <F>    The new error type.
     * @return an Ok of type T if the action is successful, or an Err of type F if the action fails.
     */
    @SuppressWarnings("unchecked")
    default <F> Result<T, F> orElse(@NotNull Function<E, Result<T, F>> action) {
        return switch (this) {
            case Err(E err) -> action.apply(err);
            case Ok<T, E> ok -> (Ok<T, F>) ok;
        };
    }

    /**
     * Allows for recovery from errors by performing a fallible operation that may throw.
     *
     * @param fallibleAction an action that may throw an exception. Takes an error value as a parameter.
     * @param <F> The new error type
     * @return If this is Err and fallibleAction succeeds, returns Ok. If fallibleAction throws an exception, returns Err.
     * If this is Ok, returns this.
     */
    @SuppressWarnings("unchecked")
    default <F extends Throwable> Result<T, F> orElseRunCatching(@NotNull Class<F> errType, @NotNull Function<E, T> fallibleAction) {
        return switch (this) {
            case Err(E value) -> {
                try {
                    yield okOf(fallibleAction.apply(value));
                } catch (Throwable t) {
                    yield errOf(errType.cast(t));
                }
            }
            case Ok<T, E> ok -> (Ok<T, F>) ok;
        };
    }

    default Result<E, T> swap() {
        return switch (this) {
            case Ok(T value) -> errOf(value);
            case Err(E err) -> okOf(err);
        };
    }

    default Option<T> ok() {
        return switch (this) {
            case Ok(T value) -> Option.someOfNullable(value);
            case Err<T, E> e -> Option.none();
        };
    }

    default Option<E> err() {
        return switch (this) {
            case Ok<T, E> ok -> Option.none();
            case Err(E err) -> Option.someOfNullable(err);
        };
    }

    /**
     * A simple way to implement railway oriented programming in Java.
     * This method should only be called within the context of the action
     * of the {@link #binding(Class, Supplier)} method.
     *
     * @return The success value of the relevant result if
     */
    default T bind() {
        return switch (this) {
            case Ok(T value) -> value;
            case Err(E error) -> throw new ResultBindingException("Bad result binding.", null, error);
        };
    }

    /**
     * This method allows for easy implementation of "Happy-railroad" programming,
     * where any error values will cause an early exit without resorting to a lot
     * of consecutive if-else logic with
     * @param action
     * @return
     * @throws ClassCastException If any of the .bind calls within the action are not of the expected type.
     * @param <T>
     * @param <E>
     */
    static <T, E> Result<T, E> binding(Class<E> errType, Supplier<T> action) {
        try {
            var result = action.get();
            return okOfNullable(result);
        } catch (ResultBindingException r) {
            return errOf(errType.cast(r.originalErr));
        }
    }
}
