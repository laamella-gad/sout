package com.laamella.examples;

import com.google.common.collect.ImmutableMap;
import com.laamella.sout.NameResolver;
import com.laamella.sout.SoutConfiguration;
import com.laamella.sout.SoutTemplate;
import com.laamella.sout.TypeHandler;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExamplesTest {
    @Test
    void specifyTheTemplateDirectlyInAString() throws IOException, IllegalAccessException {
        var configuration = new SoutConfiguration('{', '|', '}', '\\', emptyList(), emptyList());
        var template = SoutTemplate.parse(new StringReader("Hello {}"), configuration);
        var output = new StringWriter();
        template.render("Piet", output);
        assertEquals("Hello Piet", output.toString());
    }

    @Test
    void loadTheTemplateFromTheClassPath() throws IOException, IllegalAccessException {
        try (var templateInputStream = getClass().getResource("/templates/hello.sout").openStream();
             var reader = new InputStreamReader(templateInputStream, UTF_8)) {
            var configuration = new SoutConfiguration('<', '|', '>', '\\', emptyList(), emptyList());
            var template = SoutTemplate.parse(reader, configuration);
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

    @Test
    void useACustomDateFormatter() throws IOException, IllegalAccessException {
        var customDateHandler = new TypeHandler() {
            @Override
            public boolean render(Object value, Writer output) throws IOException {
                if (value instanceof Date) {
                    String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format((Date) value);
                    output.write(formattedDate);
                    return true;
                }
                return false;
            }
        };

        var configuration = new SoutConfiguration('{', '|', '}', '\\', emptyList(), singletonList(customDateHandler));
        var template = SoutTemplate.parse(new StringReader("Date zero is {}"), configuration);
        var output = new StringWriter();
        template.render(new Date(0), output);
        assertEquals("Date zero is 01-01-1970", output.toString());
    }

    @Test
    void useNameResolverToForwardToAnotherTemplate() throws IOException, IllegalAccessException {
        // The TemplateResolver stores a map of name->template.
        var templateResolver = new TemplateResolver();
        var configuration = new SoutConfiguration('{', '|', '}', '\\', singletonList(templateResolver), emptyList());

        // Put one template in the resolver, named "oei".
        var templateToResolve = SoutTemplate.parse(new StringReader("oei {name} oeiii"), configuration);
        templateResolver.put("oei", templateToResolve);

        // The name oei should be resolved to the template.
        var template = SoutTemplate.parse(new StringReader("Hello {oei}"), configuration);
        var output = new StringWriter();
        template.render(ImmutableMap.of("name", "Piet"), output);
        assertEquals("Hello oei Piet oeiii", output.toString());
    }
}

/**
 * A way to use templates inside templates.
 * <p>
 * Keeps a map of name->template,
 * and whenever one of these names is encountered in the template,
 * this will return the corresponding "sub"template,
 * and that will be rendered.
 */
class TemplateResolver implements NameResolver {
    private final Map<String, SoutTemplate> templates = new HashMap<>();

    @Override
    public Object resolve(Object target, String name) {
        return templates.get(name);
    }

    public void put(String name, SoutTemplate template) {
        templates.put(name, template);
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