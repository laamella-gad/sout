package com.laamella.sout;

import java.io.Writer;

/**
 * The standard type handler for booleans. Takes one or two parts.
 * Renders the first when the boolean is true, renders the second if it exists and the boolean is false.
 */
class BooleanRenderer implements CustomTypeRenderer {
    @Override
    public boolean render(String name, Renderable[] parts, Object model, Scope scope, Object parentModel, Scope parentScope, Position position, Writer outputWriter) {
        if (!(model instanceof Boolean)) {
            return false;
        }
        final Renderable truePart, falsePart;
        if (parts.length == 1) {
            truePart = parts[0];
            falsePart = null;
        } else if (parts.length == 2) {
            truePart = parts[0];
            falsePart = parts[1];
        } else {
            throw new SoutException(position, "Wrong amount of parts (%d) for rendering boolean \"%s\".", parts.length, name);
        }
        if ((boolean) model) {
            truePart.render(parentModel, scope, outputWriter);
        } else if (falsePart != null) {
            falsePart.render(parentModel, scope, outputWriter);
        }
        return true;
    }
}
