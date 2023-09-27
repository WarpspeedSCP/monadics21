package dev.wscp.monadics.result;

public record Err<T, E>(E error) implements Result<T, E> {}
