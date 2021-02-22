package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static java.util.Objects.requireNonNull;

/**
 * A template. The main class of the sout library.
 */
public class SoutTemplate {
    private final ContainerRenderer rootRenderer;

    /**
     * Create a new template. It is parsed immediately, so be prepared to handle exceptions about invalid templates here.
     *
     * @throws SoutException when something goes wrong with parsing the template.
     */
    public SoutTemplate(Reader templateReader, SoutConfiguration configuration) {
        requireNonNull(configuration);
        requireNonNull(templateReader);
        var parser = new SoutTemplateParser(
                configuration.openChar, configuration.separatorChar, configuration.closeChar, configuration.escapeChar,
                new IteratorFactory(configuration.customIteratorFactory),
                configuration.customNameRenderer,
                configuration.customTypeRenderer);
        try {
            rootRenderer = parser.parseTemplate(templateReader);
        } catch (IOException e) {
            throw new SoutException(e);
        }
    }

    /**
     * Render a template.
     *
     * @param data         the model containing the data that should be filled in the template.
     * @param outputWriter where the result will be written.
     * @throws SoutException when something goes wrong with rendering the template.
     */
    public void render(Object data, Writer outputWriter) {
        requireNonNull(outputWriter);
        try {
            rootRenderer.render(data, new Scope(null), outputWriter);
        } catch (IOException e) {
            throw new SoutException(e);
        }
    }

    @Override
    public String toString() {
        return rootRenderer.toString();
    }
}

