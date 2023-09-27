package dev.wscp.monadics.result;

import dev.wscp.monadics.option.None;
import dev.wscp.monadics.option.Some;
import dev.wscp.monadics.util.UnwrapException;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    @Test
    void unwrap() {
        Result<String, Integer> res1 = Result.okOf("e");
        assertDoesNotThrow(res1::unwrap);
        assertEquals("e", res1.unwrap());

        Result<String, Integer> res2 = Result.errOf(3);
        assertThrows(UnwrapException.class, res2::unwrap);

        var res3 = res2.mapError((err) -> new RuntimeException(err.toString())).andThen((it) -> new Err<>(new RuntimeException(it)));

        assertThrows(UnwrapException.class, res3::unwrap);
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
        var bytes2 = Result.errOf(19).map((it) -> 4);
        assertEquals(19, bytes2.unwrapError());
    }

    @Test
    void mapError() {
        Result<String, Integer> res1 = Result.errOf(0xE);
        Result<String, String> bytes = res1.mapError(Object::toString);
        assertEquals("14", bytes.unwrapError());
        assertNull(bytes.unwrapOrNull());
        assertNull(Result.okOfNullable(null).mapError((e) -> 3).unwrapOrNull());
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

        var newRes2 = newRes.orElse((it) -> new Err<>(4));

        assertEquals("5", newRes2.unwrap());
    }

    @Test
    void okOf() {
        assertInstanceOf(Ok.class, Result.okOf(1));
        assertThrows(NullPointerException.class, () -> Result.okOf(null));
    }

    @Test
    void okOfNullable() {
        assertInstanceOf(Ok.class, Result.okOfNullable(1));
        assertNull(Result.<Integer, Object>okOfNullable(null).value());
    }

    @Test
    void errOf() {
        assertInstanceOf(Err.class, Result.errOf(3));
        assertThrows(NullPointerException.class, () -> Result.errOf(null));
    }

    @Test
    void errOfNullable() {
        assertInstanceOf(Err.class, Result.errOfNullable(1));
        assertNull(Result.<Integer, Object>errOfNullable(null).error());
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
        assertNull(Result.errOfNullable(10).unwrapOrNull());
    }

    @Test
    void unwrapError() {
        assertEquals(10, Result.errOf(10).unwrapError());
    }

    @Test
    void unwrapErrorOrNull() {
        assertNull(Result.okOf(10).unwrapErrorOrNull());
    }

    @Test
    void unwrapErrorOrDefault() {
        assertEquals(4, Result.okOf(10).unwrapErrorOrDefault(4));
        assertEquals(10, Result.errOf(10).unwrapErrorOrDefault(3));
    }


    @Test
    void andThenRunCatching() {
        var res1 = Result.okOf(3).andThenRunCatching((it) -> 34);
        assertEquals(34, res1.unwrap());
        var res2 = res1.andThenRunCatching((it) -> it / (it - it));
        assertInstanceOf(ArithmeticException.class, res2.unwrapError());
        var res3 = res2.andThenRunCatching(Object::toString);
        assertInstanceOf(ArithmeticException.class, res3.unwrapError());
        var res4 = res3.mapError(Throwable::getMessage).andThenRunCatching(Objects::hashCode);
        assertEquals("Wrapping java.lang.String error value in rethrow exception.", res4.unwrapError().getMessage());

    }

    @Test
    void orElseRunCatching() {
        var res1 = Result.okOf(3).andThenRunCatching((it) -> 34 / 0);
        var res2 = res1.orElseRunCatching((it) -> it.getMessage().length());

        assertEquals(9, res2.unwrap());
        assertEquals(2, Result.okOf(2).orElseRunCatching((it) -> it.toString().length()).unwrap());
        assertEquals(2, Result.errOf(2).orElseRunCatching((it) -> it.toString().length() / 0).unwrapOrDefault(2));
    }

    @Test
    void swap() {
        Result<Integer, ?> res1 = Result.okOf(3);

        var res2 = res1.swap();
        assertEquals(3, res2.unwrapError());

        res1 = res2.swap();
        assertEquals(3, res1.unwrap());
    }

    @Test
    void ok() {
        assertInstanceOf(Some.class,  Result.okOf(3).ok());
        assertInstanceOf(None.class,  Result.errOf(3).ok());
    }

    @Test
    void err() {
        assertInstanceOf(None.class,  Result.okOf(3).err());
        assertInstanceOf(Some.class,  Result.errOf(3).err());
    }

    @Test
    void binding() {
        Result<String, Integer> res = Result.errOf(3);
        Result<String, Integer> res1 = Result.binding(res::bind);
        Result<String, Integer> res2 = Result.binding(() -> res1.orElse((it) -> Result.okOf("4")).bind());
        assertEquals(3, res1.unwrapError());
        assertEquals("4", res2.unwrap());
    }
}