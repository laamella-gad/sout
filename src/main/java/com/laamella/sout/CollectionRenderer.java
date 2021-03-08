package com.laamella.sout;

import java.io.Writer;

/**
 * The standard collection renderer. Takes a variety of parts. See README.md.
 */
class CollectionRenderer implements CustomTypeRenderer {
    private final IteratorFactory iteratorFactory;

    CollectionRenderer(IteratorFactory iteratorFactory) {
        this.iteratorFactory = iteratorFactory;
    }

    @Override
    public boolean render(String name, Renderable[] parts, Object model, Scope scope, Object parentModel, Scope parentScope, Position position, Writer outputWriter) {

        var iterator = iteratorFactory.toIterator(model, parentScope, position);
        if (iterator == null) {
            return false;
        }
        if (!iterator.hasNext()) {
            // Empty collection, nothing to do.
            return true;
        }
        Renderable mainPart, leadIn = null, separatorPart = null, leadOut = null;
        switch (parts.length) {
            case 1 -> {
                mainPart = parts[0];
            }
            case 2 -> {
                mainPart = parts[0];
                separatorPart = parts[1];
            }
            case 4 -> {
                leadIn = parts[0];
                mainPart = parts[1];
                separatorPart = parts[2];
                leadOut = parts[3];
            }
            default -> throw new SoutException(position, "Wrong amount of parts (%d) for rendering loop \"%s\".", parts.length, name);
        }

        if (leadIn != null) {
            leadIn.render(parentModel, scope, outputWriter);
        }

        var printSeparator = false;
        while (iterator.hasNext()) {
            var listElement = iterator.next();
            if (printSeparator && separatorPart != null) {
                separatorPart.render(listElement, scope, outputWriter);
            }
            printSeparator = true;
            mainPart.render(listElement, scope, outputWriter);
        }
        if (leadOut != null) {
            leadOut.render(parentModel, scope, outputWriter);
        }
        return true;
    }
}
