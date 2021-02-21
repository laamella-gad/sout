package com.laamella.sout;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SoutTemplateTest {
    @Test
    public void emptyMainPart() throws IOException, IllegalAccessException {
        var template = parse("Hello {name}, {friends|{} and }uh!");
        var data = ImmutableMap.of(
                "name", "Piet",
                "friends", ImmutableList.of("Hans", "Henk"));

        assertRendered("Hello Piet, Hans and Henk and uh!", template, data);
    }

    @Test
    public void oneElementLoop() throws IOException, IllegalAccessException {
        var template = parse("Hello {name}, {friends|{name} and }uh!");
        var data = ImmutableMap.of(
                "name", "Piet",
                "friends", ImmutableList.of(
                        ImmutableMap.of("name", "Hans"),
                        ImmutableMap.of("name", "Henk")));

        assertRendered("Hello Piet, Hans and Henk and uh!", template, data);
    }

    @Test
    public void twoElementLoop() throws IOException, IllegalAccessException {
        var template = parse("Hello {name}, {friends|{name}| and }!");
        var data = ImmutableMap.of(
                "name", "Piet",
                "friends", ImmutableList.of(
                        ImmutableMap.of("name", "Hans"),
                        ImmutableMap.of("name", "Henk")));

        assertRendered("Hello Piet, Hans and Henk!", template, data);
    }

    @Test
    public void fourElementLoop() throws IOException, IllegalAccessException {
        var template = parse("Hello {name}{friends| and your {friendState} friends |{name}| and |! {exclamation}}");
        var data = ImmutableMap.of(
                "name", "Piet",
                "friendState", "happy",
                "exclamation", "hurray!",
                "friends", ImmutableList.of(
                        ImmutableMap.of("name", "Hans"),
                        ImmutableMap.of("name", "Henk")));

        assertRendered("Hello Piet and your happy friends Hans and Henk! hurray!", template, data);
    }

    @Test
    public void nestedName() throws IOException, IllegalAccessException {
        var template = parse("{recurser|{value}} {recurser|{recurser|{value}}} {recurser|{recurser|{recurser|{value}}}}");
        var data = new TestModel();

        assertRendered("1 2 3", template, data);
    }

    @Test
    public void nestedNameWithDots() throws IOException, IllegalAccessException {
        var template = parse("{recurser.value} {recurser.recurser.value} {recurser.recurser.recurser.value}");
        var data = new TestModel();

        assertRendered("1 2 3", template, data);
    }

    @Test
    public void escapeOpeningBrace() throws IOException, IllegalAccessException {
        assertRendered("...{...", parse("...\\{..."), null);
    }

    @Test
    public void escapeClosingBrace() throws IOException, IllegalAccessException {
        assertRendered("...}...", parse("...\\}..."), null);
    }

    @Test
    public void escapeSeparator() throws IOException, IllegalAccessException {
        assertRendered("...|...", parse("...\\|..."), null);
    }

    @Test
    public void escapeEscape() throws IOException, IllegalAccessException {
        assertRendered("...\\...", parse("...\\\\..."), null);
    }

    @Test
    public void escapeNonEscapable() throws IOException, IllegalAccessException {
        assertRendered("...\\s...", parse("...\\s..."), null);
    }

    @Test
    public void renderStringToText() throws IOException, IllegalAccessException {
        var selfTemplate = parse("{}");
        assertRendered("abc", selfTemplate, "abc");
    }

    @Test
    public void renderIntToText() throws IOException, IllegalAccessException {
        var selfTemplate = parse("{}");
        assertRendered("123", selfTemplate, 123);
    }

    @Test
    public void nullsAreNotAllowedInTheModel() throws IOException {
        var selfTemplate = parse("{}");
        assertThatThrownBy(() -> selfTemplate.render(null, new StringWriter()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Null value.");
    }

    @Test
    public void specialNullRendererAllowsNullsInTheModel() throws IOException, IllegalAccessException {
        CustomTypeRenderer allowNullCustomTypeRenderer = (model, outputWriter) -> model == null;
        var configuration = new SoutConfiguration('{', '|', '}', '\\', null, allowNullCustomTypeRenderer, null);
        var selfTemplate = parse("{}", configuration);
        assertRendered("", selfTemplate, null);
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
        var configuration = new SoutConfiguration('{', '|', '}', '\\', null, specialCustomTypeRenderer, null);
        var selfTemplate = parse("{}", configuration);
        assertRendered("INT", selfTemplate, 123);
    }

    @Test
    public void tooManyClosingChars() {
        assertThatThrownBy(() -> parse("123{abc}456}789"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unexpected closing } at top level.");
    }

    @Test
    public void tooFewClosingChars() {
        assertThatThrownBy(() -> parse("123{abc"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Name abc was not closed before end of file.");
    }

    @Test
    public void unexpectedOpenCharInName() {
        assertThatThrownBy(() -> parse("123{abc{def}}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unexpected open { in name.");
    }

    @Test
    public void unexpectedEndOfFileWhileReadingLoop() {
        assertThatThrownBy(() -> parse("123{abc|def|ghi"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("End of template while reading a loop.");
    }

    @Test
    public void wrongAmountOfPartsForLoop() {
        assertThatThrownBy(() -> parse("{abc|def|ghi|jkl}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wrong amount of parts (3) for loop abc.");
    }

    @Test
    public void loopingOverNull() throws IOException {
        var selfLoopTemplate = parse("{|}");
        assertThatThrownBy(() -> selfLoopTemplate.render(null, new StringWriter()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trying to loop over null.");
    }

    @Test
    public void cantFindNameOnNullModel() throws IOException {
        var template = parse("{abc}");
        assertThatThrownBy(() -> template.render(null, new StringWriter()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("abc not found on null object.");
    }

    @Test
    public void cantFindNameInMap() throws IOException {
        var template = parse("{abc}");

        assertThatThrownBy(() -> template.render(new HashMap<>(), new StringWriter()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("abc not found in map {}.");
    }

    @Test
    public void resolvedValueIsNull() throws IOException {
        var template = parse("{abc}");

        var model = new HashMap<String, Object>();
        model.put("abc", null);

        assertThatThrownBy(() -> template.render(model, new StringWriter()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Null value.");
    }


    private SoutTemplate parse(String template) throws IOException {
        return parse(template, new SoutConfiguration('{', '|', '}', '\\', null, null, null));
    }

    private SoutTemplate parse(String template, SoutConfiguration configuration) throws IOException {
        return new SoutTemplate(new StringReader(template), configuration);
    }

    private void assertRendered(String expected, SoutTemplate template, Object data) throws IOException, IllegalAccessException {
        var output = new StringWriter();
        template.render(data, output);
        assertEquals(expected, output.toString());
    }
}