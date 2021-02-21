package com.laamella.examples;

import com.google.common.collect.ImmutableMap;
import com.laamella.sout.CustomNameRenderer;
import com.laamella.sout.CustomTypeRenderer;
import com.laamella.sout.SoutConfiguration;
import com.laamella.sout.SoutTemplate;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExamplesTest {
    @Test
    public void specifyTheTemplateDirectlyInAString() throws IOException, IllegalAccessException {
        var configuration = new SoutConfiguration('{', '|', '}', '\\', null, null, null);
        var template = new SoutTemplate(new StringReader("Hello {}"), configuration);
        var output = new StringWriter();
        template.render("Piet", output);
        assertEquals("Hello Piet", output.toString());
    }

    @Test
    public void loadTheTemplateFromTheClassPath() throws IOException, IllegalAccessException, URISyntaxException {
        Path templatePath = Paths.get(getClass().getResource("/templates/hello.sout").toURI());
        try (BufferedReader reader = Files.newBufferedReader(templatePath)) {
            var configuration = new SoutConfiguration('<', '|', '>', '\\', null, null, null);
            var template = new SoutTemplate(reader, configuration);
            var output = new StringWriter();
            template.render(new Letter("Piet", "Hopscotch inc.", new Item("Ball", 14.55), new Item("Triangle", 3.99)), output);
            assertEquals("""
                    Hello dear Piet,

                    It would be great if you paid for the items you ordered.
                    Ball €14.55
                    Triangle €3.99

                    Thanks a lot,
                    Hopscotch inc.
                    """, output.toString());
        }
    }

    @Test
    public void useACustomDateFormatter() throws IOException, IllegalAccessException {
        var customDateRenderer = new CustomTypeRenderer() {
            @Override
            public boolean write(Object model, Writer outputWriter, Map<String, Object> userData) throws IOException {
                if (model instanceof Date) {
                    var formattedDate = new SimpleDateFormat("dd-MM-yyyy").format((Date) model);
                    outputWriter.write(formattedDate);
                    return true;
                }
                return false;
            }
        };

        var configuration = new SoutConfiguration('{', '|', '}', '\\', null, customDateRenderer, null);
        var template = new SoutTemplate(new StringReader("Date zero is {}"), configuration);
        var output = new StringWriter();
        template.render(new Date(0), output);
        assertEquals("Date zero is 01-01-1970", output.toString());
    }

    @Test
    public void useCustomNameRendererToForwardToAnotherTemplate() throws IOException, IllegalAccessException {
        // The TemplateResolver stores a map of name->template.
        var nestedTemplateRenderer = new NestedTemplateRenderer();
        var configuration = new SoutConfiguration('{', '|', '}', '\\', nestedTemplateRenderer, null, null);

        // Put one template in the resolver, named "oei".
        var templateToResolve = new SoutTemplate(new StringReader("oei {name} oeiii"), configuration);
        nestedTemplateRenderer.put("oei", templateToResolve);

        // The name oei should trigger the TemplateWriter to render the template to the output.
        var template = new SoutTemplate(new StringReader("Hello {oei}"), configuration);
        var output = new StringWriter();
        template.render(ImmutableMap.of("name", "Piet"), output);
        assertEquals("Hello oei Piet oeiii", output.toString());
    }

    /**
     * A way to use templates inside templates.
     * <p>
     * Keeps a map of name->template,
     * and whenever one of these names is encountered in the template,
     * this will return the corresponding "sub"template,
     * and that will be rendered.
     */
    static class NestedTemplateRenderer implements CustomNameRenderer {
        private final Map<String, SoutTemplate> templates = new HashMap<>();

        public void put(String name, SoutTemplate template) {
            templates.put(name, template);
        }

        @Override
        public boolean render(Object model, String name, Writer outputWriter, Map<String, Object> userData) throws IOException, IllegalAccessException {
            SoutTemplate template = templates.get(name);
            if (template == null) {
                return false;
            }
            template.render(model, outputWriter, userData);
            return true;
        }
    }


    @Test
    public void chainCustomTypeRenderers() throws IOException, IllegalAccessException {
        CustomTypeRenderer dummyTypeRenderer = (model, outputWriter, userData) -> false;

        var customTypeRendererList = new CustomTypeRendererList(dummyTypeRenderer, dummyTypeRenderer, dummyTypeRenderer);

        var configuration = new SoutConfiguration('{', '|', '}', '\\', null, customTypeRendererList, null);
        // The renderers in the list are dummies, so nothing useful happens here:
        var template = new SoutTemplate(new StringReader(""), configuration);
        var output = new StringWriter();
        template.render(null, output);
        assertEquals("", output.toString());
    }

    /**
     * A {@link CustomTypeRenderer} that holds a list of {@link CustomTypeRenderer}s that are tried in order.
     * A class like this can easily be built for {@link CustomNameRenderer} or {@link com.laamella.sout.CustomIteratorFactory}.
     */
    static class CustomTypeRendererList implements CustomTypeRenderer {
        private final CustomTypeRenderer[] renderers;

        CustomTypeRendererList(CustomTypeRenderer... renderers) {
            this.renderers = renderers;
        }

        @Override
        public boolean write(Object model, Writer outputWriter, Map<String, Object> userData) throws IOException, IllegalAccessException {
            for (CustomTypeRenderer renderer : renderers) {
                if (renderer.write(model, outputWriter, userData)) {
                    return true;
                }
            }
            return false;
        }
    }


    @Test
    public void useNameResolverToAddACounter() throws IOException, IllegalAccessException {
        var counterRenderer = new CounterRenderer();
        var configuration = new SoutConfiguration('{', '|', '}', '\\', counterRenderer, null, null);

        var template = new SoutTemplate(new StringReader("Items: {items|{counter}. {name}|, }"), configuration);
        var output = new StringWriter();
        template.render(new Letter("", "", new Item("ball", 14.55), new Item("Triangle", 3.99), new Item("Flarb", 15.50)), output);
        assertEquals("Items: 1. ball, 2. Triangle, 3. Flarb", output.toString());
    }

    static class CounterRenderer implements CustomNameRenderer {
        @Override
        public boolean render(Object model, String name, Writer outputWriter, Map<String, Object> userData) throws IOException {
            // "startsWith" so that you can define different counters like "{counter1}{counter-tiles}{counterABC}"
            if (name.startsWith("counter")) {
                // Use the userData map instead of a field so that the counter is reset every render.
                Integer count = (Integer) userData.getOrDefault(name, 1);
                userData.put(name, count + 1);
                outputWriter.write("" + count);
                return true;
            }
            return false;
        }
    }
}


// A test model:
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