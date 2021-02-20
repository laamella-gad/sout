package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A template. The main class of the sout library.
 */
public class SoutTemplate {
    private final RootNode rootNode;

    /**
     * Create a new template. It is parsed immediately, so be prepared to handle exceptions about invalid templates here.
     */
    public SoutTemplate(Reader templateReader, SoutConfiguration configuration) throws IOException {
        var parser = new SoutTemplateParser(
                configuration.openChar, configuration.separatorChar, configuration.closeChar, configuration.escapeChar,
                new ModelTraveller(),
                new DataConverter(configuration.typeRenderer, configuration.allowNullValues, configuration.allowNullLoops),
                configuration.nameRenderer);
        rootNode = parser.parse(templateReader);
    }

    /**
     * Render a template.
     *
     * @param data         the model containing the data that should be filled in the template.
     * @param outputWriter where the result will be written.
     */
    public void render(Object data, Writer outputWriter) throws IOException, IllegalAccessException {
        rootNode.render(data, outputWriter);
    }

    @Override
    public String toString() {
        return rootNode.toString();
    }
}

