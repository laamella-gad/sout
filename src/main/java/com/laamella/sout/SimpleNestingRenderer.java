package com.laamella.sout;

import java.io.Writer;

/**
 * When all other renderers fail, this one will simply render the single supplied part.
 */
public class SimpleNestingRenderer implements CustomTypeRenderer {
    @Override
    public boolean render(String name, Renderable[] parts, Object model, Scope scope, Object parentModel, Scope parentScope, Position position, Writer outputWriter) {
        if (parts.length == 1) {
            parts[0].render(model, scope, outputWriter);
            return true;
        }
        return false;
    }
}
