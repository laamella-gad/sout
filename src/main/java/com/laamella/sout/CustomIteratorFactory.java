package com.laamella.sout;

import java.util.Iterator;

/**
 * A nesting tries to turn the model value into an iterator for looping.
 * With a {@link CustomIteratorFactory} you can define additional ways in which model values can be iterated.
 */
public interface CustomIteratorFactory {
    /**
     * @return an {@link Iterator} for model, or null if this factory doesn't handle this kind of model.
     */
    Iterator<?> toIterator(Object model, Scope scope, Position position);
}
