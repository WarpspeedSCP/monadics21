package dev.wscp.monadics.option;

import dev.wscp.monadics.result.Err;
import dev.wscp.monadics.result.Result;
import dev.wscp.monadics.util.UnwrapException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public sealed interface Option<T> permits Some, None {

    static <T> Option<T> none() {
        return new None<>();
    }

    static <T> Option<@NotNull T> someOf(@NotNull T value) {
        return new Some<> (Objects.requireNonNull(value));
    }

    static <T> Option<@NotNull T> someOfNullable(@Nullable T value) {
        if (value == null) return none();
        else return new Some<>(value);
    }

    @SuppressWarnings("unchecked")
    static <V> Option<@NotNull V> fromOptional(@NotNull Optional<V> optional) {
        if (optional.isPresent()) {
            return new Some<V>(optional.get());
        } else {
            return none();
        }
    }

    default boolean isSome() {
        return this instanceof Some<T>;
    }

    default boolean isNone() {
        return this instanceof None<T>;
    }

    default Optional<@NotNull T> toOptional() {
        return switch (this) {
            case Some(T value) -> Optional.of(value);
            case None<T> ignored -> Optional.empty();
        };
    }

    default Stream<@NotNull T> toStream() {
        return switch (this) {
            case Some(T value) -> Stream.of(value);
            default -> Stream.empty();
        };
    }

    default @NotNull T unwrap() {
        return switch (this) {
            case Some(T value) -> value;
            case None<T> n -> throw new UnwrapException("Unwrapped a none value!");
        };
    }

    default @Nullable T unwrapOrDefault(@Nullable T defaultValue) {
        return switch (this) {
            case Some(T value) -> value;
            case None<T> n -> null;
        };
    }

    default @Nullable T unwrapOrNull() {
        return unwrapOrDefault(null);
    }

    @SuppressWarnings("unchecked")
    default <V> Option<V> map(@NotNull Function<T, V> action) {
        return switch (this) {
            case Some(T value) -> new Some<V>(action.apply(value));
            case None<T> n -> (None<V>) n;
        };
    }

    @SuppressWarnings("unchecked")
    default <V> Option<V> mapDefault(@NotNull Function<T, V> action, V defaultValue) {
        return switch (this) {
            case Some(T value) -> new Some<V>(action.apply(value));
            case None<T> n -> new Some<>(defaultValue);
        };
    }

    @SuppressWarnings("unchecked")
    default <V> Option<V> andThen(@NotNull Function<T, Option<V>> action) {
        return switch (this) {
            case Some(T value) -> action.apply(value);
            case None<T> n -> (None<V>)n;
        };
    }

    default Option<T> orElse(Supplier<Option<T>> action) {
        return switch (this) {
            case Some<T> s -> s;
            case None<T> n -> action.get();
        };
    }

    default Option<T> or(Option<T> other) {
        return switch (this) {
            case Some<T> some -> some;
            case None<T> n -> other;
        };
    }

    @SuppressWarnings("unchecked")
    default <V> Option<V> and(Option<V> other) {
        return switch (this) {
            case Some<T> s -> other;
            case None<T> n -> (None<V>)n;
        };
    }

    @SuppressWarnings("unchecked")
    default Option<T> xor(Option<T> other) {
        record Pair<L, R>(L left, R right) {}
        return switch (new Pair<>(this, other)) {
            case Pair(Some<T> s, None<T> ignored) -> s;
            case Pair(None<T> ignored, Some<T> o) -> o;
            default -> none();
        };
    }

    default <E> Result<T, E> okOr(Err<T, E> err) {
        return switch (this) {
            case Some(T value) -> Result.okOf(value);
            case None<T> n -> err;
        };
    }

    default <E> Result<T, E> okOrElse(Supplier<E> errAction) {
        return switch (this) {
            case Some(T value) -> Result.okOf(value);
            case None<T> n -> Result.errOf(errAction.get());
        };
    }
}
