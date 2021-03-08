package com.laamella.sout;

import java.io.Writer;

public interface Renderable {
    void render(Object model, Scope scope, Writer outputWriter);
}

