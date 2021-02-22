package com.laamella.sout;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SoutTemplateTest {
    @Test
    public void emptyMainPart() {
        var template = parse("Hello {name}, {friends|{} and }uh!");
        var data = ImmutableMap.of(
                "name", "Piet",
                "friends", ImmutableList.of("Hans", "Henk"));

        assertRendered("Hello Piet, Hans and Henk and uh!", template, data);
    }

    @Test
    public void oneElementLoop() {
        var template = parse("Hello {name}, {friends|{name} and }uh!");
        var data = ImmutableMap.of(
                "name", "Piet",
                "friends", ImmutableList.of(
                        ImmutableMap.of("name", "Hans"),
                        ImmutableMap.of("name", "Henk")));

        assertRendered("Hello Piet, Hans and Henk and uh!", template, data);
    }

    @Test
    public void twoElementLoop() {
        var template = parse("Hello {name}, {friends|{name}| and }!");
        var data = ImmutableMap.of(
                "name", "Piet",
                "friends", ImmutableList.of(
                        ImmutableMap.of("name", "Hans"),
                        ImmutableMap.of("name", "Henk")));

        assertRendered("Hello Piet, Hans and Henk!", template, data);
    }

    @Test
    public void fourElementLoop() {
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
    public void nestedName() {
        var template = parse("{recurser|{value}} {recurser|{recurser|{value}}} {recurser|{recurser|{recurser|{value}}}}");
        var data = new TestModel();

        assertRendered("1 2 3", template, data);
    }

    @Test
    public void nestedNameWithDots() {
        var template = parse("{recurser.value} {recurser.recurser.value} {recurser.recurser.recurser.value}");
        var data = new TestModel();

        assertRendered("1 2 3", template, data);
    }

    @Test
    public void escapeOpeningBrace() {
        assertRendered("...{...", parse("...\\{..."), null);
    }

    @Test
    public void escapeClosingBrace() {
        assertRendered("...}...", parse("...\\}..."), null);
    }

    @Test
    public void escapeSeparator() {
        assertRendered("...|...", parse("...\\|..."), null);
    }

    @Test
    public void escapeEscape() {
        assertRendered("...\\...", parse("...\\\\..."), null);
    }

    @Test
    public void escapeNonEscapable() {
        assertRendered("...\\s...", parse("...\\s..."), null);
    }

    @Test
    public void renderStringToText() {
        var selfTemplate = parse("{}");
        assertRendered("abc", selfTemplate, "abc");
    }

    @Test
    public void renderIntToText() {
        var selfTemplate = parse("{}");
        assertRendered("123", selfTemplate, 123);
    }

    @Test
    public void nullsAreNotAllowedInTheModel() {
        var selfTemplate = parse("{}");
        assertThatThrownBy(() -> selfTemplate.render(null, new StringWriter()))
                .isInstanceOf(SoutException.class)
                .hasMessage("1:1 Null value.");
    }

    @Test
    public void specialNullRendererAllowsNullsInTheModel() {
        CustomTypeRenderer allowNullCustomTypeRenderer = (model, scope, outputWriter) -> model == null;
        var configuration = new SoutConfiguration('{', '|', '}', '\\', null, allowNullCustomTypeRenderer, null);
        var selfTemplate = parse("{}", configuration);
        assertRendered("", selfTemplate, null);
    }

    @Test
    public void specialRenderer() {
        CustomTypeRenderer specialCustomTypeRenderer = (model, scope, output) -> {
            if (model instanceof Integer) {
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
                .isInstanceOf(SoutException.class)
                .hasMessage("1:12 Unexpected closing } at top level.");
    }

    @Test
    public void tooFewClosingChars() {
        assertThatThrownBy(() -> parse("123{abc"))
                .isInstanceOf(SoutException.class)
                .hasMessage("1:8 Name abc was not closed before end of file.");
    }

    @Test
    public void unexpectedOpenCharInName() {
        assertThatThrownBy(() -> parse("123{abc{def}}"))
                .isInstanceOf(SoutException.class)
                .hasMessage("1:8 Unexpected open { in name.");
    }

    @Test
    public void unexpectedEndOfFileWhileReadingLoop() {
        assertThatThrownBy(() -> parse("123{abc|def|ghi"))
                .isInstanceOf(SoutException.class)
                .hasMessage("1:16 End of template while reading a loop.");
    }

    @Test
    public void wrongAmountOfPartsForLoop() {
        assertThatThrownBy(() -> parse("{abc|def|ghi|jkl}"))
                .isInstanceOf(SoutException.class)
                .hasMessage("1:17 Wrong amount of parts (3) for loop abc.");
    }

    @Test
    public void loopingOverNull() {
        var selfLoopTemplate = parse("{|}");
        assertThatThrownBy(() -> selfLoopTemplate.render(null, new StringWriter()))
                .isInstanceOf(SoutException.class)
                .hasMessage("1:3 Trying to loop over null.");
    }

    @Test
    public void cantFindNameOnNullModel() {
        var template = parse("{abc}");
        assertThatThrownBy(() -> template.render(null, new StringWriter()))
                .isInstanceOf(SoutException.class)
                .hasMessage("1:1 abc not found on null object.");
    }

    @Test
    public void cantFindNameInMap() {
        var template = parse("{abc}");

        assertThatThrownBy(() -> template.render(new HashMap<>(), new StringWriter()))
                .isInstanceOf(SoutException.class)
                .hasMessage("1:1 abc not found in map {}.");
    }

    @Test
    public void resolvedValueIsNull() {
        var template = parse("{abc}");

        var model = new HashMap<String, Object>();
        model.put("abc", null);

        assertThatThrownBy(() -> template.render(model, new StringWriter()))
                .isInstanceOf(SoutException.class)
                .hasMessage("1:1 Null value.");
    }


    private SoutTemplate parse(String template) {
        return parse(template, new SoutConfiguration('{', '|', '}', '\\', null, null, null));
    }

    private SoutTemplate parse(String template, SoutConfiguration configuration) {
        return new SoutTemplate(new StringReader(template), configuration);
    }

    private void assertRendered(String expected, SoutTemplate template, Object data) {
        var output = new StringWriter();
        template.render(data, output);
        assertEquals(expected, output.toString());
    }
}