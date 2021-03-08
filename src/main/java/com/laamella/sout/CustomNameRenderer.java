package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface CustomNameRenderer {
    /**
     * Custom rendering based on name.
     *
     * @param name         the name to evaluate.
     * @param parts
     * @param model        the data model where the name can be evaluated.
     * @param scope
     * @param position
     * @param outputWriter the output writer.
     * @return true when this handler has handled the name, and no further processing needs to be done.
     * false when normal evaluation should continue (meaning the normal evaluation of name on the model.)
     */
    boolean render(String name, Renderable[] parts, Object model, Scope scope, Position position, Writer outputWriter) throws IOException;
}
