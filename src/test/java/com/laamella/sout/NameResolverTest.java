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
        Object value = nameResolver.resolveComplexNameOnModel(ImmutableMap.of("x", "y"), "x");
        assertThat(value).isEqualTo("y");
    }

    @Test
    public void findValueOfFunctionApplication() {
        Object value = nameResolver.resolveComplexNameOnModel((Function<String, String>) o -> o + "woo", "name");
        assertThat(value).isEqualTo("namewoo");
    }

    @Test
    public void findValueOfField() {
        TestModel testModel = new TestModel();
        Object value = nameResolver.resolveComplexNameOnModel(testModel, "field");
        assertThat(value).isEqualTo("*field*");
    }

    @Test
    public void findValueOfGetter() {
        TestModel testModel = new TestModel();
        Object value = nameResolver.resolveComplexNameOnModel(testModel, "getter");
        assertThat(value).isEqualTo("*getter*");
    }

    @Test
    public void findValueOfIsser() {
        TestModel testModel = new TestModel();
        Object value = nameResolver.resolveComplexNameOnModel(testModel, "isser");
        assertThat(value).isEqualTo(TRUE);
    }

}
