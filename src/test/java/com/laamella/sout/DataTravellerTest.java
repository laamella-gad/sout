package com.laamella.sout;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.laamella.sout.DataTraveller.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
    void renderStringToText() throws IOException {
        var configuration = new SoutConfiguration('{', '|', '}', '\\', emptyList(), emptyList());
        var output = new StringWriter();
        renderValueAsText("abc", output, configuration);
        assertThat(output.toString()).isEqualTo("abc");
    }

    @Test
    void renderIntToText() throws IOException {
        var configuration = new SoutConfiguration('{', '|', '}', '\\', emptyList(), emptyList());
        var output = new StringWriter();
        renderValueAsText(123, output, configuration);
        assertThat(output.toString()).isEqualTo("123");
    }

    @Test
    void renderNullToText() throws IOException {
        var configuration = new SoutConfiguration('{', '|', '}', '\\', emptyList(), emptyList());
        var output = new StringWriter();
        renderValueAsText(null, output, configuration);
        assertThat(output.toString()).isEqualTo("");
    }

    @Test
    void specialRenderer() throws IOException {
        TypeHandler specialTypeHandler = (value, output) -> {
            if (value instanceof Integer) {
                output.append("INT");
            }
            return false;
        };
        var configuration = new SoutConfiguration('{', '|', '}', '\\', emptyList(), singletonList(specialTypeHandler));
        var output = new StringWriter();
        renderValueAsText(123, output, configuration);
        assertThat(output.toString()).isEqualTo("INT");
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