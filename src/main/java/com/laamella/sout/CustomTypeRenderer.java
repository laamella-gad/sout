package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface CustomTypeRenderer {
    /**
     * Custom rendering for types.
     *
     * @param model        the value to write to the output.
     * @param outputWriter the output.
     * @return true if this {@link CustomTypeRenderer} has written the type and no further handling is wanted.
     */
    boolean write(Object model, Writer outputWriter) throws IOException, IllegalAccessException;
}
