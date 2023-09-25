package dev.wscp.monadics.result;

public record Err<T, E>(E err) implements Result<T, E> {}
