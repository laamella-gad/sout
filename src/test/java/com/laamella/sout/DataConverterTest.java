package com.laamella.sout;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

@SuppressWarnings("unchecked")
public class DataConverterTest {
    private final DataConverter dataConverter = new DataConverter((model, outputWriter) -> false, true, true);

    @Test
    public void listsGetConvertedToLists() {
        var objects = (List<Integer>) newArrayList(dataConverter.toIterator(asList(1, 2, 3)));
        assertThat(objects).containsExactly(1, 2, 3);
    }

    @Test
    public void arraysGetConvertedToLists() {
        var objects = (List<Integer>) newArrayList(dataConverter.toIterator(new int[]{1, 2, 3}));
        assertThat(objects).containsExactly(1, 2, 3);
    }

    @Test
    public void streamsGetConvertedToLists() {
        var objects = (List<Integer>) newArrayList(dataConverter.toIterator(Stream.of(1, 2, 3)));
        assertThat(objects).containsExactly(1, 2, 3);
    }

    @Test
    public void renderStringToText() throws IOException, IllegalAccessException {
        var output = new StringWriter();
        dataConverter.renderAsText("abc", output);
        assertThat(output.toString()).isEqualTo("abc");
    }

    @Test
    public void renderIntToText() throws IOException, IllegalAccessException {
        var output = new StringWriter();
        dataConverter.renderAsText(123, output);
        assertThat(output.toString()).isEqualTo("123");
    }

    @Test
    public void renderNullToText() throws IOException, IllegalAccessException {
        var output = new StringWriter();
        dataConverter.renderAsText(null, output);
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
        var output = new StringWriter();
        var dataConverter = new DataConverter(specialTypeRenderer, true, true);
        dataConverter.renderAsText(123, output);
        assertThat(output.toString()).isEqualTo("INT");
    }
}