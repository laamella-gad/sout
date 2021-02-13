package com.laamella.sout;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.*;

abstract class Node {
    final int row, column;

    Node(int row, int column) {
        this.row = row;
        this.column = column;
    }
}

class NameNode extends Node {
    final String name;

    NameNode(String name, int row, int column) {
        super(row, column);
        this.name = name;
    }
}

abstract class ContainerNode extends Node {
    final List<Node> children = new ArrayList<>();

    ContainerNode(int row, int column) {
        super(row, column);
    }
}

class LoopNode extends ContainerNode {
    final String name;
    LoopPartNode mainPart = null;
    LoopPartNode separatorPart = null;
    LoopPartNode leadIn = null;
    LoopPartNode leadOut = null;

    LoopNode(String name, int row, int column) {
        super(row, column);
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
}

class RootNode extends ContainerNode {
    RootNode() {
        super(0, 0);
    }
}

class LoopPartNode extends ContainerNode {
    LoopPartNode(int row, int column) {
        super(row, column);
    }
}

class TextNode extends Node {
    final String text;

    TextNode(String text, int row, int column) {
        super(row, column);
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
