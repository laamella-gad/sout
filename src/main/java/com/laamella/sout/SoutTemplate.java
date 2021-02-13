package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static java.util.stream.Collectors.joining;

public class SoutTemplate extends ContainerNode {
    SoutTemplate() {
        super(new Position(0, 0));
    }

    @Override
    public void render(Object data, Writer output) throws IOException {
        for (var child : children) {
            child.render(data, output);
        }
    }

    public static SoutTemplate parse(Reader template, char openChar, char separatorChar, char closeChar, char escapeChar) throws IOException {
        return new SoutTemplateParser(openChar, separatorChar, closeChar, escapeChar).parse(template);
    }

    @Override
    public String toString() {
        return children.stream().map(Object::toString).collect(joining());
    }
}

