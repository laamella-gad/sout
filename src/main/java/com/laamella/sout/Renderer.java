package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

abstract class Renderer {
    final Position position;

    Renderer(Position position) {
        this.position = position;
    }

    abstract void render(Object model, Writer outputWriter) throws IOException, IllegalAccessException;
}

class NameRenderer extends Renderer {
    private final String name;
    private final CustomNameRenderer customNameRenderer;
    private final NameResolver nameResolver;
    private final CustomTypeRenderer customTypeRenderer;

    NameRenderer(String name, Position position, CustomNameRenderer customNameRenderer, NameResolver nameResolver, CustomTypeRenderer customTypeRenderer) {
        super(position);
        this.name = name;
        this.customNameRenderer = customNameRenderer;
        this.nameResolver = nameResolver;
        this.customTypeRenderer = customTypeRenderer;
    }

    @Override
    void render(Object model, Writer outputWriter) throws IOException, IllegalAccessException {
        if (customNameRenderer.render(model, name, outputWriter)) {
            return;
        }
        Object subModel = nameResolver.evaluateNameOnModel(model, name);
        if (customTypeRenderer.write(subModel, outputWriter)) {
            return;
        }
        if (subModel == null) {
            throw new IllegalArgumentException("Null value.");
        }
        outputWriter.append(subModel.toString());
    }

    @Override
    public String toString() {
        return "❰" + name + "❱";
    }
}

abstract class ContainerRenderer extends Renderer {
    final List<Renderer> children = new ArrayList<>();

    ContainerRenderer(Position position) {
        super(position);
    }
}

class LoopRenderer extends ContainerRenderer {
    private final String name;
    private final NameResolver nameResolver;
    private final IteratorFactory iteratorFactory;
    private final CustomIteratorFactory customIteratorFactory;
    LoopPartRenderer mainPart = null;
    LoopPartRenderer separatorPart = null;
    LoopPartRenderer leadIn = null;
    LoopPartRenderer leadOut = null;

    LoopRenderer(String name, Position position, NameResolver nameResolver, IteratorFactory iteratorFactory, CustomIteratorFactory customIteratorFactory) {
        super(position);
        this.name = name;
        this.nameResolver = nameResolver;
        this.iteratorFactory = iteratorFactory;
        this.customIteratorFactory = customIteratorFactory;
    }

    @Override
    public void render(Object model, Writer outputWriter) throws IOException, IllegalAccessException {
        var iterator = customIteratorFactory.toIterator(model);
        if (iterator == null) {
            iterator = iteratorFactory.toIterator(nameResolver.evaluateNameOnModel(model, name));
        }
        var hasItems = iterator.hasNext();

        if (hasItems && leadIn != null) {
            leadIn.render(model, outputWriter);
        }

        var printSeparator = false;
        while (iterator.hasNext()) {
            var listElement = iterator.next();
            if (printSeparator && separatorPart != null) {
                separatorPart.render(listElement, outputWriter);
            }
            printSeparator = true;
            mainPart.render(listElement, outputWriter);
        }
        if (hasItems && leadOut != null) {
            leadOut.render(model, outputWriter);
        }
    }

    @Override
    public String toString() {
        return '❰' + name + "❚" + children.stream().map(Object::toString).collect(joining("❚")) + '❱';
    }
}

class LoopPartRenderer extends ContainerRenderer {
    LoopPartRenderer(Position position) {
        super(position);
    }

    @Override
    void render(Object model, Writer outputWriter) throws IOException, IllegalAccessException {
        for (var child : children) {
            child.render(model, outputWriter);
        }
    }

    @Override
    public String toString() {
        return children.stream().map(Object::toString).collect(joining());
    }
}

class TextRenderer extends Renderer {
    final String text;

    TextRenderer(String text, Position position) {
        super(position);
        this.text = text;
    }

    @Override
    public void render(Object model, Writer outputWriter) throws IOException {
        outputWriter.append(text);
    }

    @Override
    public String toString() {
        return text;
    }
}

class RootRenderer extends ContainerRenderer {
    RootRenderer() {
        super(new Position(0, 0));
    }

    @Override
    public void render(Object model, Writer outputWriter) throws IOException, IllegalAccessException {
        for (var child : children) {
            child.render(model, outputWriter);
        }
    }

    @Override
    public String toString() {
        return children.stream().map(Object::toString).collect(joining());
    }
}