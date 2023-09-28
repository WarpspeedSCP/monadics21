package dev.wscp.monadics.option;

import dev.wscp.monadics.result.Err;
import dev.wscp.monadics.result.Result;
import dev.wscp.monadics.util.UnwrapException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OptionTest {

    @Test
    void testSome() {
        Option<String> option = Option.someOfNullable("Hello");
        assertInstanceOf(Some.class, option);
        assertEquals("Hello", option.unwrap());
    }

    @Test
    void testNone() {
        Option<String> option = Option.none();
        assertInstanceOf(None.class, option);
        assertThrows(UnwrapException.class, option::unwrap);
    }

    @Test
    void testFromOptional() {
        Optional<String> optional = Optional.of("World");
        Option<String> option = Option.fromOptional(optional);
        assertInstanceOf(Some.class, option);
        assertEquals("World", option.unwrap());
    }

    @Test
    void testFromOptionalEmpty() {
        Optional<String> optional = Optional.empty();
        Option<String> option = Option.fromOptional(optional);
        assertInstanceOf(None.class, option);
    }

    @Test
    void testToOptionalSome() {
        Option<String> option = Option.someOfNullable("JUnit");
        Optional<String> optional = option.toOptional();
        assertTrue(optional.isPresent());
        assertEquals("JUnit", optional.get());
    }

    @Test
    void testToOptionalNone() {
        Option<String> option = Option.none();
        Optional<String> optional = option.toOptional();
        assertFalse(optional.isPresent());
    }

    @Test
    void testMap() {
        Option<Integer> option = Option.someOf(5);
        Option<Integer> mappedOption = option.map(x -> x * 2);
        assertInstanceOf(Some.class, mappedOption);
        assertEquals(10, mappedOption.unwrap());
    }

    @Test
    void testMapNone() {
        Option<Integer> option = Option.none();
        Option<Integer> mappedOption = option.map(x -> x * 2);
        assertInstanceOf(None.class, mappedOption);
    }

    @Test
    void testAndThen() {
        Option<Integer> option = Option.someOf(5);
        Option<Integer> result = option.andThen(x -> Option.someOf(x * 2));
        assertInstanceOf(Some.class, result);
        assertEquals(10, result.unwrap());
    }

    @Test
    void testAndThenNone() {
        Option<Integer> option = Option.none();
        Option<Integer> result = option.andThen(x -> Option.someOf(x * 2));
        assertInstanceOf(None.class, result);
    }

    @Test
    void testOrElse() {
        Option<String> option = Option.none();
        Option<String> result = option.orElse(() -> Option.someOf("Fallback"));
        Option<String> some = Option.someOf("3");
        assertEquals("Fallback", result.unwrap());
        assertEquals("3", some.orElse(Option::none).unwrap());
    }

    @Test
    void testOr() {
        Option<String> option = Option.none();
        Option<String> result = option.or(Option.someOf("Fallback"));
        assertInstanceOf(Some.class, result);
        assertEquals("Fallback", result.unwrap());
        assertEquals("Fallback", result.or(Option.none()).unwrap());
    }

    @Test
    void testAnd() {
        Option<Integer> option = Option.none();
        Option<Integer> result = option.and(Option.someOf(42));
        assertInstanceOf(None.class, result);
        assertEquals(30, Option.someOf(4).and(Option.someOf(30)).unwrap());
    }

    @Test
    void testXorSome() {
        Option<Integer> option1 = Option.someOf(5);
        Option<Integer> option2 = Option.none();
        Option<Integer> result1 = option1.xor(option2);
        Option<Integer> result2 = option2.xor(option1);
        assertTrue(result1.isSome());
        assertTrue(result2.isSome());
        assertEquals(5, result1.unwrap());
        assertEquals(5, result2.unwrap());
    }

    @Test
    void testXorNone() {
        Option<Integer> option1 = Option.none();
        Option<Integer> option2 = Option.none();
        Option<Integer> result = option1.xor(option2);
        assertTrue(result.isNone());
    }

    @Test
    void testOkOr() {
        Option<Integer> option = Option.someOf(42);
        Result<Integer, String> result = option.okOr(new Err<>("Error"));
        assertTrue(result.isOk());
        assertEquals(42, result.unwrap());
        assertEquals(4, Option.none().okOr(Result.errOf(4)).unwrapError());
    }

    @Test
    void testOkOrElse() {
        Option<Integer> option = Option.none();
        Result<Integer, String> result = option.okOrElse(() -> "Error");
        assertTrue(result.isErr());
        assertEquals("Error", result.unwrapError());
        Option<Integer> opt1 = Option.someOf(2);
        Result<Integer, String> result2 = opt1.okOrElse(() -> "err");
        assertEquals(2, opt1.unwrap());
    }

    @Test
    void testToStream() {
        Option<String> noneOpt = Option.none();
        Option<String> someOpt = Option.someOf("3");

        assertEquals(0, noneOpt.toStream().count());
        assertEquals(1, someOpt.toStream().count());
    }

    @Test
    void testUnwrap() {
        Option<String> option = Option.someOf("Hello");
        assertEquals("Hello", option.unwrap());
    }

    @Test
    void testUnwrapOrDefault() {
        Option<String> option = Option.someOf("Hello");
        assertEquals("Hello", option.unwrapOrDefault("Default"));

        Option<String> noneOption = Option.none();
        assertNull(noneOption.unwrapOrDefault("Default"));
    }

    @Test
    void testUnwrapOrNull() {
        Option<String> option = Option.someOf("Hello");
        assertEquals("Hello", option.unwrapOrNull());

        Option<String> noneOption = Option.none();
        assertNull(noneOption.unwrapOrNull());
    }

    @Test
    void testMapDefault() {
        Option<Integer> option = Option.someOf(5);
        Option<Integer> mappedOption = option.mapDefault(x -> x * 2, 10);
        assertEquals(10, mappedOption.unwrap());

        Option<Integer> noneOption = Option.none();
        Option<Integer> mappedNoneOption = noneOption.mapDefault(x -> x * 2, 10);
        assertEquals(10, mappedNoneOption.unwrap());
    }
}
