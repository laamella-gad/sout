package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface CustomTypeRenderer {
    /**
     * Custom rendering for types.
     *
     * @param name         the name being rendered. It was resolved on "parentModel" and the result is "model".
     * @param parts        the parts supplied to the name. null when no parts are there.
     * @param model        the value to write to the output.
     * @param scope        a place to store variables that can be seen inside the nesting.
     * @param parentModel  when nesting, this is the value that contains "model", otherwise null.
     * @param parentScope  when nesting, this is the scope just above the nesting, otherwise null.
     * @param position     the position in the template, for passing to a {@link SoutException}.
     * @param outputWriter where to write your output.
     * @return true if this {@link CustomTypeRenderer} has written the type and no further handling is wanted.
     */
    boolean render(String name, Renderable[] parts, Object model, Scope scope, Object parentModel, Scope parentScope, Position position, Writer outputWriter) throws IOException;
}