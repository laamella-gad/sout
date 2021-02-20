package com.laamella.sout;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NameNodeTest {
    private final NameRenderer noNameRenderer = (model, name, outputWriter) -> false;
    private final TypeRenderer noTypeRenderer = (model, outputWriter) -> false;

    @Test
    public void renderStringToText() throws IOException, IllegalAccessException {
        NameNode nameNode = new NameNode("", new Position(0, 0), noNameRenderer,
                new NameResolver(),
                noTypeRenderer
        );

        var output = new StringWriter();
        nameNode.render("abc", output);
        assertThat(output.toString()).isEqualTo("abc");
    }

    @Test
    public void renderIntToText() throws IOException, IllegalAccessException {
        NameNode nameNode = new NameNode("", new Position(0, 0), noNameRenderer,
                new NameResolver(),
                noTypeRenderer
        );
        var output = new StringWriter();
        nameNode.render(123, output);
        assertThat(output.toString()).isEqualTo("123");
    }

    @Test
    public void nullsAreNotAllowedInTheModel() {
        NameNode nameNode = new NameNode("", new Position(0, 0), noNameRenderer,
                new NameResolver(),
                noTypeRenderer);
        assertThatThrownBy(() -> nameNode.render(null, new StringWriter()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Null value.");
    }

    @Test
    public void specialNullRendererAllowsNullsInTheModel() throws IOException, IllegalAccessException {
        TypeRenderer allowNullTypeRenderer = (model, outputWriter) -> model == null;
        NameNode nameNode = new NameNode("", new Position(0, 0), noNameRenderer,
                new NameResolver(),
                allowNullTypeRenderer);
        var output = new StringWriter();
        nameNode.render(null, output);
        assertThat(output.toString()).isEqualTo("");
    }

    @Test
    public void specialRenderer() throws IOException, IllegalAccessException {
        TypeRenderer specialTypeRenderer = (value, output) -> {
            if (value instanceof Integer) {
                output.append("INT");
                return true;
            }
            return false;
        };
        NameNode nameNode = new NameNode("", new Position(0, 0), noNameRenderer,
                new NameResolver(),
                specialTypeRenderer);

        var output = new StringWriter();
        nameNode.render(123, output);
        assertThat(output.toString()).isEqualTo("INT");
    }
}