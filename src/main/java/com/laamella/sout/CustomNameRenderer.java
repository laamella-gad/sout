package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface CustomNameRenderer {
    /**
     * Custom rendering based on name, before name is resolved in the model.
     * With this you can take over rendering before sout does anything with the name,
     * and you can stop it from doing anything at all with the name by returning false.
     * <p>
     * If you want name to be resolved for you, you should use a {@link CustomTypeRenderer}.
     *
     * @param name         the name being rendered. It has not been resolved yet.
     * @param parts        the parts supplied to the name. null when no parts are there.
     * @param model        the data model where the name can be resolved.
     * @param scope        a place to store your variables.
     * @param position     the position in the template, for passing to a {@link SoutException}.
     * @param outputWriter where to write your output.
     * @return true when this handler has handled the name, and no further processing needs to be done.
     * false when normal evaluation should continue (meaning the normal evaluation of name on the model.)
     */
    boolean render(String name, Renderable[] parts, Object model, Scope scope, Position position, Writer outputWriter) throws IOException;
}
