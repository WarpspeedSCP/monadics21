package dev.wscp.monadics.option;

import java.util.Optional;
import java.util.function.Function;

public sealed interface Option<T> permits Some, None {
    static <V> Option<V> fromOptional(Optional<V> optional) {
        if (optional.isPresent()) {
            return new Some<V>(optional.get());
        } else {
            return new None<V>();
        }
    }

    default Optional<T> toOptional() {
        return switch (this) {
            case Some(T value) -> Optional.of(value);
            case None<T> ignored -> Optional.empty();
        };
    }

    @SuppressWarnings("unchecked")
    default <V> Option<V> map(Function<T, V> transformer) {
        return switch (this) {
            case Some(T value) -> new Some<V>(transformer.apply(value));
            case None<T> none -> (None<V>) none;
        };
    }

    @SuppressWarnings("unchecked")
    default <V> Option<V> map(Function<T, V> transformer, V defaultValue) {
        return switch (this) {
            case Some(T value) -> new Some<V>(transformer.apply(value));
            case None<T> none -> (None<V>) none;
        };
    }

}
