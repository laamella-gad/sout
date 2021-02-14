package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface NameHandler {
    /**
     * Custom name handling. Does not work with loop syntax.
     *
     * @param model  the data model where the name can be evaluated.
     * @param name   the name to evaluate.
     * @param output the output writer.
     * @return true when this handler has handled the name, and no further processing needs to be done.
     * false when normal evaluationg should continue (meaning the normal evaluation of name on the model.)
     */
    boolean render(Object model, String name, Writer output) throws IOException, IllegalAccessException;
}
