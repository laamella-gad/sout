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
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class DataTravellerTest {

    private final SoutConfiguration defaultConfiguration = new SoutConfiguration('{', '|', '}', '\\', emptyList(), emptyList());

    @Test
    void listsGetConvertedToLists() {
        var objects = (List<Integer>) newArrayList(valueIterator(asList(1, 2, 3)));
        assertThat(objects).containsExactly(1, 2, 3);
    }

    @Test
    void arraysGetConvertedToLists() {
        var objects = (List<Integer>) newArrayList(valueIterator(new int[]{1, 2, 3}));
        assertThat(objects).containsExactly(1, 2, 3);
    }

    @Test
    void streamsGetConvertedToLists() {
        var objects = (List<Integer>) newArrayList(valueIterator(Stream.of(1, 2, 3)));
        assertThat(objects).containsExactly(1, 2, 3);
    }

    @Test
    void renderStringToText() throws IOException {
        var output = new StringWriter();
        renderValueAsText("abc", output, defaultConfiguration);
        assertThat(output.toString()).isEqualTo("abc");
    }

    @Test
    void renderIntToText() throws IOException {
        var output = new StringWriter();
        renderValueAsText(123, output, defaultConfiguration);
        assertThat(output.toString()).isEqualTo("123");
    }

    @Test
    void renderNullToText() throws IOException {
        var output = new StringWriter();
        renderValueAsText(null, output, defaultConfiguration);
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
        Object value = findValueOf(ImmutableMap.of("x", "y"), "x", defaultConfiguration);
        assertThat(value).isEqualTo("y");
    }

    @Test
    void findValueOfFunctionApplication() throws IllegalAccessException {
        Object value = findValueOf((Function<String, String>) o -> o + "woo", "name", defaultConfiguration);
        assertThat(value).isEqualTo("namewoo");
    }

    @Test
    void findValueOfField() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = findValueOf(testModel, "field", defaultConfiguration);
        assertThat(value).isEqualTo("*field*");
    }

    @Test
    void findValueOfGetter() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = findValueOf(testModel, "getter", defaultConfiguration);
        assertThat(value).isEqualTo("*getter*");
    }

    @Test
    void findValueOfIsser() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = findValueOf(testModel, "isser", defaultConfiguration);
        assertThat(value).isEqualTo(TRUE);
    }

    @Test
    void findCustomValue() throws IllegalAccessException {
        NameResolver hrankResolver = (target, name) -> {
            if (name.equals("hrank")) {
                return "vavoom";
            }
            return null;
        };
        var configuration = new SoutConfiguration('{', '|', '}', '\\', singletonList(hrankResolver), emptyList());
        TestModel testModel = new TestModel();
        Object value = findValueOf(testModel, "hrank", configuration);
        assertThat(value).isEqualTo("vavoom");
    }
}