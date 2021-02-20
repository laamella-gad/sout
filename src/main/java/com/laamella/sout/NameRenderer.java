package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface NameRenderer {
    /**
     * Custom name rendering. Ignored for loop syntax.
     *
     * @param model        the data model where the name can be evaluated.
     * @param name         the name to evaluate.
     * @param outputWriter the output writer.
     * @return true when this handler has handled the name, and no further processing needs to be done.
     * false when normal evaluation should continue (meaning the normal evaluation of name on the model.)
     */
    boolean render(Object model, String name, Writer outputWriter) throws IOException, IllegalAccessException;
}
