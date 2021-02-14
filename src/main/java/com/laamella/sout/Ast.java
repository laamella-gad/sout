package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

abstract class Node {
    final Position position;

    Node(Position position) {
        this.position = position;
    }

    abstract void render(Object data, Writer output, SoutConfiguration configuration) throws IOException, IllegalAccessException;
}

class NameNode extends Node {
    final String name;

    NameNode(String name, Position position) {
        super(position);
        this.name = name;
    }

    @Override
    void render(Object data, Writer output, SoutConfiguration configuration) throws IOException, IllegalAccessException {
        Object value = configuration.dataTraveller.findValueOf(data, name);
        if (value instanceof Renderable) {
            ((Renderable) value).render(data, output);
        } else {
            configuration.dataConverter.renderAsText(value, output);
        }
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
    final String name;
    LoopPartNode mainPart = null;
    LoopPartNode separatorPart = null;
    LoopPartNode leadIn = null;
    LoopPartNode leadOut = null;

    LoopNode(String name, Position position) {
        super(position);
        this.name = name;
    }

    void validate() {
        int parts = children.size();
        switch (parts) {
            case 1 -> mainPart = (LoopPartNode) children.get(0);
            case 2 -> {
                mainPart = (LoopPartNode) children.get(0);
                separatorPart = (LoopPartNode) children.get(1);
            }
            case 4 -> {
                leadIn = (LoopPartNode) children.get(0);
                mainPart = (LoopPartNode) children.get(1);
                separatorPart = (LoopPartNode) children.get(2);
                leadOut = (LoopPartNode) children.get(3);
            }
            // TODO 6 = special separator after the first and before the last element?
            default -> throw new IllegalArgumentException(format("Wrong amount of parts (%d) for loop %s.", parts, name));
        }
    }

    @Override
    public void render(Object data, Writer output, SoutConfiguration configuration) throws IOException, IllegalAccessException {
        var listData = configuration.dataConverter.toIterator(configuration.dataTraveller.findValueOf(data, name));

        var hasItems = listData.hasNext();

        if (hasItems && leadIn != null) {
            leadIn.render(data, output, configuration);
        }

        var printSeparator = false;
        while (listData.hasNext()) {
            var listElement = listData.next();
            if (printSeparator && separatorPart != null) {
                separatorPart.render(listElement, output, configuration);
            }
            printSeparator = true;
            mainPart.render(listElement, output, configuration);
        }
        if (hasItems && leadOut != null) {
            leadOut.render(data, output, configuration);
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
    void render(Object data, Writer output, SoutConfiguration configuration) throws IOException, IllegalAccessException {
        for (var child : children) {
            child.render(data, output, configuration);
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
    public void render(Object data, Writer output, SoutConfiguration configuration) throws IOException {
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
    public void render(Object data, Writer output, SoutConfiguration configuration) throws IOException, IllegalAccessException {
        for (var child : children) {
            child.render(data, output, configuration);
        }
    }

    @Override
    public String toString() {
        return children.stream().map(Object::toString).collect(joining());
    }
}