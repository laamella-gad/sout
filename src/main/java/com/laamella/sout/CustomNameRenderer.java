package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

@FunctionalInterface
public interface CustomNameRenderer {
    /**
     * Custom name rendering. Ignored for loop syntax.
     *
     * @param model        the data model where the name can be evaluated.
     * @param name         the name to evaluate.
     * @param outputWriter the output writer.
     * @param userData     a map for storing anything you like.
     * @return true when this handler has handled the name, and no further processing needs to be done.
     * false when normal evaluation should continue (meaning the normal evaluation of name on the model.)
     */
    boolean render(Object model, String name, Writer outputWriter, Map<String, Object> userData) throws IOException, IllegalAccessException;
}
