package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * A template. The main class of the sout library.
 */
public class SoutTemplate {
    private final ContainerRenderer rootRenderer;

    /**
     * Create a new template. It is parsed immediately, so be prepared to handle exceptions about invalid templates here.
     */
    public SoutTemplate(Reader templateReader, SoutConfiguration configuration) throws IOException {
        var parser = new SoutTemplateParser(
                configuration.openChar, configuration.separatorChar, configuration.closeChar, configuration.escapeChar,
                new IteratorFactory(configuration.customIteratorFactory),
                configuration.customNameRenderer,
                configuration.customTypeRenderer);
        rootRenderer = parser.parseTemplate(templateReader);
    }

    /**
     * Render a template.
     *
     * @param data         the model containing the data that should be filled in the template.
     * @param outputWriter where the result will be written.
     */
    public void render(Object data, Writer outputWriter) throws IOException, IllegalAccessException {
        render(data, outputWriter, new HashMap<>());
    }

    /**
     * Render a template.
     *
     * @param data            the model containing the data that should be filled in the template.
     * @param outputWriter    where the result will be written.
     * @param globalVariables a key->value store where custom renderers and factories can store their state.
     *                        Pass it if you want to initialize some variables before rendering.
     */
    public void render(Object data, Writer outputWriter, Map<String, Object> globalVariables) throws IOException, IllegalAccessException {
        rootRenderer.render(data, new Scope(null, globalVariables), outputWriter);
    }

    @Override
    public String toString() {
        return rootRenderer.toString();
    }
}

