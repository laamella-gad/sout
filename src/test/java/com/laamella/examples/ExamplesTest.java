package com.laamella.examples;

import com.laamella.sout.SoutTemplate;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExamplesTest {
    @Test
    void specifyTheTemplateDirectlyInAString() throws IOException, IllegalAccessException {
        var template = SoutTemplate.parse(new StringReader("Hello {}"), '{', '|', '}', '\\');
        var output = new StringWriter();
        template.render("Piet", output);
        assertEquals("Hello Piet", output.toString());
    }

    @Test
    void loadTheTemplateFromTheClassPath() throws IOException, IllegalAccessException {
        try (var templateInputStream = getClass().getResource("/templates/hello.sout").openStream();
             var reader = new InputStreamReader(templateInputStream, UTF_8)) {
            var template = SoutTemplate.parse(reader, '<', '|', '>', '\\');
            var output = new StringWriter();
            template.render(new Letter("Piet", "Hopscotch inc.", new Item("ball", 14.55), new Item("Triangle", 3.99)), output);
            assertEquals("""
                    Hello dear Piet,

                    It would be great if you paid for the items you ordered.
                    ball €14.55
                    Triangle €3.99

                    Thanks a lot,
                    Hopscotch inc.
                    """, output.toString());
        }
    }
}

class Letter {
    final String name;
    final String us;
    final Item[] items;

    Letter(String name, String us, Item... items) {
        this.name = name;
        this.us = us;
        this.items = items;
    }
}

class Item {
    final String name;
    final double price;

    Item(String name, double price) {
        this.name = name;
        this.price = price;
    }
}