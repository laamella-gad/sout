package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;

class DataConverter {
    private final SoutConfiguration configuration;

    DataConverter(SoutConfiguration configuration) {
        this.configuration = configuration;
    }

    Iterator<?> toIterator(Object value) {
        if (value instanceof List) {
            return ((List<?>) value).iterator();
        } else if (value instanceof Object[]) {
            return stream((Object[]) value).iterator();
        } else if (value instanceof int[]) {
            return stream((int[]) value).boxed().iterator();
        } else if (value instanceof Stream) {
            return ((Stream<?>) value).iterator();
        } else if (value instanceof Iterator) {
            return (Iterator<?>) value;
        } else if (value instanceof Iterable) {
            return ((Iterable<?>) value).iterator();
        }
        // TODO and so on, and so on...
        return singletonList(value).iterator();
    }

    void renderAsText(Object value, Writer output) throws IOException, IllegalAccessException {
        if (value == null) {
            return;
        }
        for (TypeHandler typeHandler : configuration.typeHandlers) {
            if (typeHandler.render(value, output)) {
                return;
            }
        }
        output.append(value.toString());
    }

}
