package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;

/**
 * If a name resolved to an object implementing this interface,
 * it will take over rendering.
 * {@link SoutTemplate} implements it,
 * so that templates can be used inside templates.
 */
@FunctionalInterface
public interface Renderable {
    void render(Object data, Writer output) throws IOException, IllegalAccessException;
}
