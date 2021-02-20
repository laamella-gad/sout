package com.laamella.sout;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NameRendererTest {
    private final CustomNameRenderer noCustomNameRenderer = (model, name, outputWriter) -> false;
    private final CustomTypeRenderer noCustomTypeRenderer = (model, outputWriter) -> false;

    @Test
    public void renderStringToText() throws IOException, IllegalAccessException {
        NameRenderer nameNode = new NameRenderer("", new Position(0, 0), noCustomNameRenderer, new NameResolver(), noCustomTypeRenderer);

        var output = new StringWriter();
        nameNode.render("abc", output);
        assertThat(output.toString()).isEqualTo("abc");
    }

    @Test
    public void renderIntToText() throws IOException, IllegalAccessException {
        NameRenderer nameNode = new NameRenderer("", new Position(0, 0), noCustomNameRenderer, new NameResolver(), noCustomTypeRenderer);
        var output = new StringWriter();
        nameNode.render(123, output);
        assertThat(output.toString()).isEqualTo("123");
    }

    @Test
    public void nullsAreNotAllowedInTheModel() {
        NameRenderer nameNode = new NameRenderer("", new Position(0, 0), noCustomNameRenderer, new NameResolver(), noCustomTypeRenderer);
        assertThatThrownBy(() -> nameNode.render(null, new StringWriter()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Null value.");
    }

    @Test
    public void specialNullRendererAllowsNullsInTheModel() throws IOException, IllegalAccessException {
        CustomTypeRenderer allowNullCustomTypeRenderer = (model, outputWriter) -> model == null;
        NameRenderer nameNode = new NameRenderer("", new Position(0, 0), noCustomNameRenderer, new NameResolver(), allowNullCustomTypeRenderer);
        var output = new StringWriter();
        nameNode.render(null, output);
        assertThat(output.toString()).isEqualTo("");
    }

    @Test
    public void specialRenderer() throws IOException, IllegalAccessException {
        CustomTypeRenderer specialCustomTypeRenderer = (value, output) -> {
            if (value instanceof Integer) {
                output.append("INT");
                return true;
            }
            return false;
        };
        NameRenderer nameNode = new NameRenderer("", new Position(0, 0), noCustomNameRenderer, new NameResolver(), specialCustomTypeRenderer);

        var output = new StringWriter();
        nameNode.render(123, output);
        assertThat(output.toString()).isEqualTo("INT");
    }
}