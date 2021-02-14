package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface TypeHandler {
    boolean render(Object value, Writer output) throws IOException, IllegalAccessException;
}
