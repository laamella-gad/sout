package com.laamella.sout;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class DataTravellerTest {

    private final SoutConfiguration defaultConfiguration = new SoutConfiguration('{', '|', '}', '\\', emptyList(), emptyList());
    private final DataTraveller dataTraveller = new DataTraveller(defaultConfiguration);

    @Test
    void findValueOfMapEntry() throws IllegalAccessException {
        Object value = dataTraveller.findValueOf(ImmutableMap.of("x", "y"), "x");
        assertThat(value).isEqualTo("y");
    }

    @Test
    void findValueOfFunctionApplication() throws IllegalAccessException {
        Object value = dataTraveller.findValueOf((Function<String, String>) o -> o + "woo", "name");
        assertThat(value).isEqualTo("namewoo");
    }

    @Test
    void findValueOfField() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = dataTraveller.findValueOf(testModel, "field");
        assertThat(value).isEqualTo("*field*");
    }

    @Test
    void findValueOfGetter() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = dataTraveller.findValueOf(testModel, "getter");
        assertThat(value).isEqualTo("*getter*");
    }

    @Test
    void findValueOfIsser() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = dataTraveller.findValueOf(testModel, "isser");
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
        Object value = new DataTraveller(configuration).findValueOf(testModel, "hrank");
        assertThat(value).isEqualTo("vavoom");
    }
}