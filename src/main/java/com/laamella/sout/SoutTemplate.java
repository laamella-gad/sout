package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A template. The main class of the sout library.
 */
public class SoutTemplate {
    private final RootNode rootNode;
    private final SoutConfiguration configuration;

    /**
     * Create a new template. It is parsed immediately, so be prepared to handle exceptions about invalid templates here.
     */
    public SoutTemplate(Reader templateReader, SoutConfiguration configuration) throws IOException {
        this.configuration = configuration;
        var parser = new SoutTemplateParser(configuration);
        rootNode = parser.parse(templateReader);
    }

    public void render(Object data, Writer outputWriter) throws IOException, IllegalAccessException {
        rootNode.render(data, outputWriter, configuration);
    }

    @Override
    public String toString() {
        return rootNode.toString();
    }
}

