package dev.wscp.monadics.option;

public record Some<T>(T value) implements Option<T> { }
