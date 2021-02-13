package com.laamella.sout;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MicroTLTest {
    @Test
    void oneElementLoop() throws IOException {
        var template = new Template(new StringReader("Hello {name}, {friends|{name} and }uh!"));
        var data = ImmutableMap.of(
                "name", "Piet",
                "friends", ImmutableList.of(
                        ImmutableMap.of("name", "Hans"),
                        ImmutableMap.of("name", "Henk")));

        var output = new StringWriter();
        template.apply(data, output);
        assertEquals("Hello Piet, Hans and Henk and uh!", output.toString());
    }
    @Test
    void twoElementLoop() throws IOException {
        var template = new Template(new StringReader("Hello {name}, {friends|{name}| and }!"));
        var data = ImmutableMap.of(
                "name", "Piet",
                "friends", ImmutableList.of(
                        ImmutableMap.of("name", "Hans"),
                        ImmutableMap.of("name", "Henk")));

        var output = new StringWriter();
        template.apply(data, output);
        assertEquals("Hello Piet, Hans and Henk!", output.toString());
    }
    @Test
    void fourElementLoop() throws IOException {
        var template = new Template(new StringReader("Hello {name}{friends| and your {friendState} friends |{name}| and |! {exclamation}}"));
        var data = ImmutableMap.of(
                "name", "Piet",
                "friendState", "happy",
                "exclamation", "hurray!",
                "friends", ImmutableList.of(
                        ImmutableMap.of("name", "Hans"),
                        ImmutableMap.of("name", "Henk")));

        var output = new StringWriter();
        template.apply(data, output);
        assertEquals("Hello Piet and your happy friends Hans and Henk! hurray!", output.toString());
    }
}
