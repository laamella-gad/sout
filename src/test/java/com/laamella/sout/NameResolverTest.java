package com.laamella.sout;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

public class NameResolverTest {

    private final NameResolver nameResolver = new NameResolver();

    @Test
    public void findValueOfMapEntry() {
        var value = nameResolver.resolveComplexNameOnModel(ImmutableMap.of("x", "y"), "x");
        assertThat(value.failed).isFalse();
        assertThat(value.value).isEqualTo("y");
    }

    @Test
    public void findValueOfFunctionApplication() {
        var value = nameResolver.resolveComplexNameOnModel((Function<String, String>) o -> o + "woo", "name");
        assertThat(value.failed).isFalse();
        assertThat(value.value).isEqualTo("namewoo");
    }

    @Test
    public void findValueOfField() {
        var testModel = new TestModel();
        var value = nameResolver.resolveComplexNameOnModel(testModel, "field");
        assertThat(value.failed).isFalse();
        assertThat(value.value).isEqualTo("*field*");
    }

    @Test
    public void findValueOfGetter() {
        var testModel = new TestModel();
        var value = nameResolver.resolveComplexNameOnModel(testModel, "getter");
        assertThat(value.failed).isFalse();
        assertThat(value.value).isEqualTo("*getter*");
    }

    @Test
    public void findValueOfIsser() {
        var testModel = new TestModel();
        var value = nameResolver.resolveComplexNameOnModel(testModel, "isser");
        assertThat(value.failed).isFalse();
        assertThat(value.value).isEqualTo(TRUE);
    }

    @Test
    public void findValueOfPlainMethod() {
        var testModel = new TestModel();
        var value = nameResolver.resolveComplexNameOnModel(testModel, "plainMethod");
        assertThat(value.failed).isFalse();
        assertThat(value.value).isEqualTo(15);
    }

}
