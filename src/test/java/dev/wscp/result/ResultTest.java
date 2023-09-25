package dev.wscp.result;

import dev.wscp.monadics.result.Ok;
import dev.wscp.monadics.result.Result;
import dev.wscp.monadics.result.UnwrapException;
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
        assertThrows(NullPointerException.class, () -> Result.<Integer, Object>okOf(null));
    }

    @Test
    void okOfNullable() {
        assertInstanceOf(Ok.class, Result.okOfNullable(1));
        assertNull(Result.<Integer, Object>okOfNullable(null).value());
    }

    @Test
    void errOf() {
    }

    @Test
    void errOfNullable() {
    }

    @Test
    void runCatching() {
    }

    @Test
    void unwrapOrDefault() {
    }

    @Test
    void unwrapOrNull() {
    }

    @Test
    void unwrapError() {
    }

    @Test
    void testMap() {
    }

    @Test
    void testMapError() {
    }

    @Test
    void testAndThen() {
    }

    @Test
    void andThenRunCatching() {
    }

    @Test
    void testAndThenRunCatching() {
    }

    @Test
    void testOrElse() {
    }

    @Test
    void orElseRunCatching() {
    }
}