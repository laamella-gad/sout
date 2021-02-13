package com.laamella.sout;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.laamella.sout.DataTraveller.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class DataTravellerTest {
    @Test
    void listsGetConvertedToLists() {
        List<Integer> objects = newArrayList(valueIterator(asList(1, 2, 3)));
        assertThat(objects).containsExactly(1, 2, 3);
    }

    @Test
    void arraysGetConvertedToLists() {
        List<Integer> objects = newArrayList(valueIterator(new int[]{1, 2, 3}));
        assertThat(objects).containsExactly(1, 2, 3);
    }

    @Test
    void streamsGetConvertedToLists() {
        List<Integer> objects = newArrayList(valueIterator(Stream.of(1, 2, 3)));
        assertThat(objects).containsExactly(1, 2, 3);
    }

    @Test
    void convertSomethingToText() {
        assertThat(convertValueToText("abc")).isEqualTo("abc");
        assertThat(convertValueToText(123)).isEqualTo("123");
        assertThat(convertValueToText(null)).isEqualTo("");
    }

    @Test
    void findValueOfMapEntry() throws IllegalAccessException {
        String value = findValueOf(ImmutableMap.of("x", "y"), "x");
        assertThat(value).isEqualTo("y");
    }

    @Test
    void findValueOfFunctionApplication() throws IllegalAccessException {
        String value = findValueOf((Function<String, String>) o -> o + "woo", "name");
        assertThat(value).isEqualTo("namewoo");
    }

    @Test
    void findValueOfField() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        String value = findValueOf(testModel, "field");
        assertThat(value).isEqualTo("*field*");
    }

    @Test
    void findValueOfGetter() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        String value = findValueOf(testModel, "getter");
        assertThat(value).isEqualTo("*getter*");
    }

    @Test
    void findValueOfIsser() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        boolean value = findValueOf(testModel, "isser");
        assertThat(value).isTrue();
    }
}