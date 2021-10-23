package com.laamella.sout;

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
        rootRenderer = parser.parseTemplate(templateReader);
    }

    /**
     * Render a template.
     *
     * @param model        the model containing the data that should be filled in the template.
     * @param outputWriter where the result will be written.
     * @throws SoutException when something goes wrong with rendering the template.
     */
    public void render(Object model, Writer outputWriter) {
        requireNonNull(outputWriter);
        rootRenderer.render(model, new Scope(null), outputWriter);
    }

    @Override
    public String toString() {
        return rootRenderer.toString();
    }
}

