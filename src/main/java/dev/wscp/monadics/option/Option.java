package dev.wscp.monadics.option;

import dev.wscp.monadics.result.Result;
import dev.wscp.monadics.util.UnwrapException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public sealed interface Option<T> permits Some, None {
    None<?> none = new None<>();

    static <T> Option<@Nullable T> some(@Nullable T value) {
        return new Some<>(value);
    }

    @SuppressWarnings("unchecked")
    static <V> Option<V> fromOptional(@NotNull Optional<V> optional) {
        if (optional.isPresent()) {
            return new Some<V>(optional.get());
        } else {
            return (None<V>) none;
        }
    }

    default Optional<T> toOptional() {
        return switch (this) {
            case Some(T value) -> Optional.of(value);
            case None<T> ignored -> Optional.empty();
        };
    }

    default Stream<T> toStream() {
        return switch (this) {
            case Some(T value) -> Stream.ofNullable(value);
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
    default <V> Option<? extends V> map(@NotNull Function<? super T, ? extends V> action) {
        return switch (this) {
            case Some(T value) -> new Some<V>(action.apply(value));
            case None<T> n -> (None<V>) n;
        };
    }

    @SuppressWarnings("unchecked")
    default <V> Option<? extends V> mapDefault(@NotNull Function<? super T, ? extends V> action, V defaultValue) {
        return switch (this) {
            case Some(T value) -> new Some<V>(action.apply(value));
            case None<T> n -> (None<V>) n;
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

    default Option<T> or(T other) {
        return switch (this) {
            case Some<T> some -> some;
            case None<T> n -> new Some<>(other);
        };
    }

    @SuppressWarnings("unchecked")
    default <V> Option<V> and(V other) {
        return switch (this) {
            case Some<T> s -> new Some<>(other);
            case None<T> n -> (None<V>)n;
        };
    }

    @SuppressWarnings("unchecked")
    default Option<T> xor(Option<T> other) {
        record Pair<L, R>(L left, R right) {}
        return switch (new Pair<>(this, other)) {
            case Pair(Some<T> s, None<T> ignored) -> s;
            case Pair(None<T> ignored, Some<T> o) -> o;
            default -> (None<T>) none;
        };
    }

    default <E> Result<T, E> okOr(E err) {
        return switch (this) {
            case Some(T value) -> Result.okOf(value);
            case None<T> n -> Result.errOf(err);
        };
    }

    default <E> Result<T, E> okOrElse(Supplier<E> errAction) {
        return switch (this) {
            case Some(T value) -> Result.okOf(value);
            case None<T> n -> Result.errOf(errAction.get());
        };
    }
}
