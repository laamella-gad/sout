package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

abstract class Node {
    final Position position;

    Node(Position position) {
        this.position = position;
    }

    abstract void render(Object model, Writer outputWriter) throws IOException, IllegalAccessException;
}

class NameNode extends Node {
    private final String name;
    private final CustomNameRenderer customNameRenderer;
    private final NameResolver nameResolver;
    private final CustomTypeRenderer customTypeRenderer;

    NameNode(String name, Position position, CustomNameRenderer customNameRenderer, NameResolver nameResolver, CustomTypeRenderer customTypeRenderer) {
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

abstract class ContainerNode extends Node {
    final List<Node> children = new ArrayList<>();

    ContainerNode(Position position) {
        super(position);
    }
}

class LoopNode extends ContainerNode {
    private final String name;
    private final NameResolver nameResolver;
    private final DataConverter dataConverter;
    LoopPartNode mainPart = null;
    LoopPartNode separatorPart = null;
    LoopPartNode leadIn = null;
    LoopPartNode leadOut = null;

    LoopNode(String name, Position position, NameResolver nameResolver, DataConverter dataConverter) {
        super(position);
        this.name = name;
        this.nameResolver = nameResolver;
        this.dataConverter = dataConverter;
    }

    @Override
    public void render(Object model, Writer outputWriter) throws IOException, IllegalAccessException {
        var listData = dataConverter.toIterator(nameResolver.evaluateNameOnModel(model, name));

        var hasItems = listData.hasNext();

        if (hasItems && leadIn != null) {
            leadIn.render(model, outputWriter);
        }

        var printSeparator = false;
        while (listData.hasNext()) {
            var listElement = listData.next();
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

class LoopPartNode extends ContainerNode {
    LoopPartNode(Position position) {
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

class TextNode extends Node {
    final String text;

    TextNode(String text, Position position) {
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

class RootNode extends ContainerNode {
    RootNode() {
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