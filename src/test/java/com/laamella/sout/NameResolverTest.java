package com.laamella.sout;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

public class NameResolverTest {

    private final NameResolver nameResolver = new NameResolver();

    @Test
    public void findValueOfMapEntry() throws IllegalAccessException {
        Object value = nameResolver.resolveNameOnModel(ImmutableMap.of("x", "y"), "x");
        assertThat(value).isEqualTo("y");
    }

    @Test
    public void findValueOfFunctionApplication() throws IllegalAccessException {
        Object value = nameResolver.resolveNameOnModel((Function<String, String>) o -> o + "woo", "name");
        assertThat(value).isEqualTo("namewoo");
    }

    @Test
    public void findValueOfField() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = nameResolver.resolveNameOnModel(testModel, "field");
        assertThat(value).isEqualTo("*field*");
    }

    @Test
    public void findValueOfGetter() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = nameResolver.resolveNameOnModel(testModel, "getter");
        assertThat(value).isEqualTo("*getter*");
    }

    @Test
    public void findValueOfIsser() throws IllegalAccessException {
        TestModel testModel = new TestModel();
        Object value = nameResolver.resolveNameOnModel(testModel, "isser");
        assertThat(value).isEqualTo(TRUE);
    }

}
