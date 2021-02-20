package com.laamella.sout;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;

class DataConverter {
    Iterator<?> toIterator(Object model) {
        if (model instanceof List) {
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

}
