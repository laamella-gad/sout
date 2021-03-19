package com.laamella.sout;

import java.io.Writer;

/**
 * Something that can render an object to a Writer.
 */
public interface Renderable {
    void render(Object model, Scope scope, Writer outputWriter);
}

