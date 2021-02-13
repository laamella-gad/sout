package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static com.laamella.sout.DataTraveller.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

abstract class SoutNode {
    final Position position;

    SoutNode(Position position) {
        this.position = position;
    }

    abstract void render(Object data, Writer output) throws IOException;
}

class NameNode extends SoutNode {
    final String name;

    NameNode(String name, Position position) {
        super(position);
        this.name = name;
    }

    @Override
    void render(Object data, Writer output) throws IOException {
        String text = convertValueToText(findValueOf(data, name));
        output.append(text);
    }

    @Override
    public String toString() {
        return "❰" + name + "❱";
    }
}

abstract class ContainerNode extends SoutNode {
    final List<SoutNode> children = new ArrayList<>();

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
    public void render(Object data, Writer output) throws IOException {
        var listData = valueIterator(findValueOf(data, name));

        var hasItems = listData.hasNext();

        if (hasItems && leadIn != null) {
            leadIn.render(data, output);
        }

        var printSeparator = false;
        while (listData.hasNext()) {
            var listElement = listData.next();
            if (printSeparator && separatorPart != null) {
                separatorPart.render(listElement, output);
            }
            printSeparator = true;
            mainPart.render(listElement, output);
        }
        if (hasItems && leadOut != null) {
            leadOut.render(data, output);
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
    void render(Object data, Writer output) throws IOException {
        for (var child : children) {
            child.render(data, output);
        }
    }

    @Override
    public String toString() {
        return children.stream().map(Object::toString).collect(joining());
    }
}

class TextNode extends SoutNode {
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
