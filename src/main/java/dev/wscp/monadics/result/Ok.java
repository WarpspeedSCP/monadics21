package dev.wscp.monadics.result;

public record Ok<T, E>(T value) implements Result<T, E> {}
