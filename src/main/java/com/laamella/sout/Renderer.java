package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

abstract class Renderer {
    final Position position;

    Renderer(Position position) {
        this.position = position;
    }

    abstract void render(Object model, Writer outputWriter, Map<String, Object> userData) throws IOException, IllegalAccessException;
}

class NameRenderer extends Renderer {
    private final String name;
    private final NameResolver nameResolver;
    private final CustomNameRenderer customNameRenderer;
    private final CustomTypeRenderer customTypeRenderer;

    NameRenderer(String name, Position position, NameResolver nameResolver, CustomNameRenderer customNameRenderer, CustomTypeRenderer customTypeRenderer) {
        super(position);
        this.name = name;
        this.customNameRenderer = customNameRenderer;
        this.nameResolver = nameResolver;
        this.customTypeRenderer = customTypeRenderer;
    }

    @Override
    void render(Object model, Writer outputWriter, Map<String, Object> userData) throws IOException, IllegalAccessException {
        if (customNameRenderer.render(model, name, outputWriter, userData)) {
            return;
        }
        Object subModel = nameResolver.resolveComplexNameOnModel(model, name);
        if (customTypeRenderer.write(subModel, outputWriter, userData)) {
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

class ContainerRenderer extends Renderer {
    private final List<Renderer> children;

    ContainerRenderer(Position position, List<Renderer> children) {
        super(position);
        this.children = children;
    }

    @Override
    void render(Object model, Writer outputWriter, Map<String, Object> userData) throws IOException, IllegalAccessException {
        for (var child : children) {
            child.render(model, outputWriter, userData);
        }
    }

    @Override
    public String toString() {
        return children.stream().map(Object::toString).collect(joining());
    }
}

class LoopRenderer extends Renderer {
    private final String name;
    private final NameResolver nameResolver;
    private final IteratorFactory iteratorFactory;
    private final ContainerRenderer mainPart;
    private final ContainerRenderer separatorPart;
    private final ContainerRenderer leadIn;
    private final ContainerRenderer leadOut;

    LoopRenderer(String name, Position position, NameResolver nameResolver, IteratorFactory iteratorFactory,
                 ContainerRenderer mainPart, ContainerRenderer separatorPart, ContainerRenderer leadIn, ContainerRenderer leadOut) {
        super(position);
        this.name = name;
        this.nameResolver = nameResolver;
        this.iteratorFactory = iteratorFactory;
        this.mainPart = mainPart;
        this.separatorPart = separatorPart;
        this.leadIn = leadIn;
        this.leadOut = leadOut;
    }

    @Override
    public void render(Object model, Writer outputWriter, Map<String, Object> userData) throws IOException, IllegalAccessException {
        var collection = nameResolver.resolveComplexNameOnModel(model, name);
        var iterator = iteratorFactory.toIterator(collection, userData);
        var hasItems = iterator.hasNext();

        if (hasItems && leadIn != null) {
            leadIn.render(model, outputWriter, userData);
        }

        var printSeparator = false;
        while (iterator.hasNext()) {
            var listElement = iterator.next();
            if (printSeparator && separatorPart != null) {
                separatorPart.render(listElement, outputWriter, userData);
            }
            printSeparator = true;
            mainPart.render(listElement, outputWriter, userData);
        }
        if (hasItems && leadOut != null) {
            leadOut.render(model, outputWriter, userData);
        }
    }

    @Override
    public String toString() {
        return '❰' + name +
                (leadIn != null ? "❚" + leadIn : "") +
                "❚" + mainPart +
                (separatorPart != null ? "❚" + separatorPart : "") +
                (leadOut != null ? "❚" + leadOut : "")
                + '❱';
    }
}

class TextRenderer extends Renderer {
    final String text;

    TextRenderer(String text, Position position) {
        super(position);
        this.text = text;
    }

    @Override
    public void render(Object model, Writer outputWriter, Map<String, Object> userData) throws IOException {
        outputWriter.append(text);
    }

    @Override
    public String toString() {
        return text;
    }
}

