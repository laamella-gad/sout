package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.singletonList;

class DataConverter {
    private final TypeRenderer typeRenderer;
    private final boolean allowNullValues;
    private final boolean allowNullLoops;

    DataConverter(TypeRenderer typeRenderer, boolean allowNullValues, boolean allowNullLoops) {
        this.typeRenderer = typeRenderer;
        this.allowNullValues = allowNullValues;
        this.allowNullLoops = allowNullLoops;
    }

    Iterator<?> toIterator(Object model) {
        if (model == null) {
            if (allowNullLoops) {
                return emptyIterator();
            }
            throw new IllegalArgumentException("Null loop.");
        } else if (model instanceof List) {
            return ((List<?>) model).iterator();
        } else if (model instanceof Object[]) {
            return stream((Object[]) model).iterator();
        } else if (model instanceof int[]) {
            return stream((int[]) model).boxed().iterator();
        } else if (model instanceof Stream) {
            return ((Stream<?>) model).iterator();
        } else if (model instanceof Iterator) {
            return (Iterator<?>) model;
        } else if (model instanceof Iterable) {
            return ((Iterable<?>) model).iterator();
        }
        // TODO and so on, and so on...
        return singletonList(model).iterator();
    }

    void renderAsText(Object model, Writer writer) throws IOException, IllegalAccessException {
        if (model == null) {
            if (allowNullValues) {
                return;
            }
            throw new IllegalArgumentException("Null value.");
        }
        if (typeRenderer.write(model, writer)) {
            return;
        }
        writer.append(model.toString());
    }
}
