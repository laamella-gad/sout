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

    abstract void render(Object data, Writer output) throws IOException, IllegalAccessException;
}

class NameNode extends Node {
    private final String name;
    private final List<NameRenderer> nameRenderers;
    private final ModelTraveller modelTraveller;
    private final DataConverter dataConverter;

    NameNode(String name, Position position, List<NameRenderer> nameRenderers, ModelTraveller modelTraveller, DataConverter dataConverter) {
        super(position);
        this.name = name;
        this.nameRenderers = nameRenderers;
        this.modelTraveller = modelTraveller;
        this.dataConverter = dataConverter;
    }

    @Override
    void render(Object data, Writer output) throws IOException, IllegalAccessException {
        for (NameRenderer nameRenderer : nameRenderers) {
            if (nameRenderer.render(data, name, output)) {
                return;
            }
        }
        Object value = modelTraveller.evaluateNameOnModel(data, name);
        dataConverter.renderAsText(value, output);
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
    private final ModelTraveller modelTraveller;
    private final DataConverter dataConverter;
    LoopPartNode mainPart = null;
    LoopPartNode separatorPart = null;
    LoopPartNode leadIn = null;
    LoopPartNode leadOut = null;

    LoopNode(String name, Position position, ModelTraveller modelTraveller, DataConverter dataConverter) {
        super(position);
        this.name = name;
        this.modelTraveller = modelTraveller;
        this.dataConverter = dataConverter;
    }

    @Override
    public void render(Object data, Writer outputWriter) throws IOException, IllegalAccessException {
        var listData = dataConverter.toIterator(modelTraveller.evaluateNameOnModel(data, name));

        var hasItems = listData.hasNext();

        if (hasItems && leadIn != null) {
            leadIn.render(data, outputWriter);
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
            leadOut.render(data, outputWriter);
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
    void render(Object data, Writer output) throws IOException, IllegalAccessException {
        for (var child : children) {
            child.render(data, output);
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
    public void render(Object data, Writer output) throws IOException {
        output.append(text);
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
    public void render(Object data, Writer output) throws IOException, IllegalAccessException {
        for (var child : children) {
            child.render(data, output);
        }
    }

    @Override
    public String toString() {
        return children.stream().map(Object::toString).collect(joining());
    }
}