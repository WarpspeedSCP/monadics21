package dev.wscp.monadics.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    @Test
    void unwrap() {
        Result<String, Integer> res1 = Result.okOf("e");
        assertDoesNotThrow(res1::unwrap);
        assertEquals("e", res1.unwrap());

        Result<String, Integer> res2 = Result.errOf(3);
        assertThrows(UnwrapException.class, res2::unwrap);
    }

    @Test
    void unwrapErr() {
        Result<String, Integer> res1 = Result.okOf("e");
        assertThrows(UnwrapException.class, res1::unwrapError);

        Result<String, Integer> res2 = Result.errOf(3);
        assertDoesNotThrow(res2::unwrapError);
        assertEquals(3, res2.unwrapError());
    }

    @Test
    void map() {
        Result<String, Integer> res1 = Result.okOf("E");
        Result<byte[], Integer> bytes = res1.map(String::getBytes);

        assertInstanceOf(byte[].class, bytes.unwrap());
    }

    @Test
    void mapError() {
        Result<String, Integer> res1 = Result.errOf(0xE);
        Result<String, String> bytes = res1.mapError(Object::toString);

        assertEquals("14", bytes.unwrapError());
        assertNull(bytes.unwrapOrNull());
    }

    @Test
    void andThen() {
        Result<String, Integer> res1 = Result.okOf("e");

        var newRes = res1.andThen((it) -> new Ok<>(it.length()));

        assertEquals(1, newRes.unwrap());

        assertThrows(UnwrapException.class, newRes::unwrapError);
    }

    @Test
    void orElse() {
        Result<String, Integer> res1 = Result.errOf(3);

        var newRes = res1.orElse((it) -> new Ok<>(String.valueOf(it + 2)));

        assertEquals("5", newRes.unwrap());

        assertThrows(UnwrapException.class, newRes::unwrapError);
    }

    @Test
    void okOf() {
        assertInstanceOf(Ok.class, Result.okOf(1));
        assertThrows(IllegalArgumentException.class, () -> Result.okOf(null));
    }

    @Test
    void okOfNullable() {
        assertInstanceOf(Ok.class, Result.okOfNullable(1));
        assertNull(Result.<Integer, Object>okOfNullable(null).value());
    }

    @Test
    void errOf() {
        assertInstanceOf(Err.class, Result.errOf(3));
        assertThrows(IllegalArgumentException.class, () -> Result.errOf(null));
    }

    @Test
    void errOfNullable() {
        assertInstanceOf(Err.class, Result.errOfNullable(1));
        assertNull(Result.<Integer, Object>errOfNullable(null).err());
    }

    @Test
    void runCatching() {
        assertEquals(new Ok<>(""), Result.runCatching(() -> ""));
        assertEquals(ArithmeticException.class, Result.runCatching(() -> 1 / (10 - 10)).unwrapError().getClass());
    }

    @Test
    void unwrapOrDefault() {
        assertEquals(10, Result.okOf(10).unwrapOrDefault(3));
        assertEquals(3, Result.errOf(10).unwrapOrDefault(3));
    }

    @Test
    void unwrapOrNull() {
    }

    @Test
    void unwrapError() {
    }

    @Test
    void unwrapErrorOrNull() {
    }

    @Test
    void unwrapErrorOrDefault() {
    }


    @Test
    void andThenRunCatching() {
        var res1 = Result.okOf(3).andThenRunCatching((it) -> 34);
        assertEquals(34, res1.unwrap());
        var res2 = res1.andThenRunCatching((it) -> it / (it - it));
        assertInstanceOf(ArithmeticException.class, res2.unwrapError());
    }

    @Test
    void orElseRunCatching() {
        var res1 = Result.okOf(3).andThenRunCatching((it) -> 34 / 0);
        var res2 = res1.orElseRunCatching((it) -> it.getMessage().length());
        assertEquals(9, res2.unwrap());
    }
}