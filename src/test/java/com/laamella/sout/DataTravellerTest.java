package com.laamella.sout;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class DataTravellerTest {

    private final SoutConfiguration defaultConfiguration = new SoutConfiguration('{', '|', '}', '\\', emptyList(), emptyList());
    private final DataTraveller dataTraveller = new DataTraveller(defaultConfiguration);

    @Test
    public void findValueOfMapEntry() throws IllegalAccessException {
        Object value = dataTraveller.evaluateNameOnTarget(ImmutableMap.of("x", "y"), "x");
        assertThat(value).isEqualTo("y");
    }

    @Test
    public void findValueOfFunctionApplication() throws IllegalAccessException {
        Object value = dataTraveller.evaluateNameOnTarget((Function<String, String>) o -> o + "woo", "name");
        assertThat(value).isEqualTo("namewoo");
    }

    @Test
    public void findValueOfField() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = dataTraveller.evaluateNameOnTarget(testModel, "field");
        assertThat(value).isEqualTo("*field*");
    }

    @Test
    public void findValueOfGetter() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = dataTraveller.evaluateNameOnTarget(testModel, "getter");
        assertThat(value).isEqualTo("*getter*");
    }

    @Test
    public void findValueOfIsser() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = dataTraveller.evaluateNameOnTarget(testModel, "isser");
        assertThat(value).isEqualTo(TRUE);
    }
}