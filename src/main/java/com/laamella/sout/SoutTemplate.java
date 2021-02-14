package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class SoutTemplate implements Renderable {
    private final RootNode rootNode;
    private final SoutConfiguration configuration;

    private SoutTemplate(RootNode rootNode, SoutConfiguration configuration) {
        this.rootNode = rootNode;
        this.configuration = configuration;
    }

    public static SoutTemplate read(Reader template, SoutConfiguration configuration) throws IOException {
        var parser = new SoutTemplateParser(configuration);
        var rootNode = parser.parse(template);
        return new SoutTemplate(rootNode, configuration);
    }

    @Override
    public void render(Object data, Writer output) throws IOException, IllegalAccessException {
        rootNode.render(data, output, configuration);
    }
}

