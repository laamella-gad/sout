package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;

class TemplateParser {
    private final int escapeChar;
    private final int openChar;
    private final int closeChar;
    private final int separatorChar;

    enum State {READING_NAME, READING_TEXT}

    static class Context {
        int row = 0, col = 0;
        Reader reader;

        public Context(Reader reader) {
            this.reader = reader;
        }
    }

    TemplateParser(char escapeChar, char openChar, char closeChar, char separatorChar) {
        this.escapeChar = escapeChar;
        this.openChar = openChar;
        this.closeChar = closeChar;
        this.separatorChar = separatorChar;
    }

    RootNode parse(Reader template) throws IOException {
        RootNode rootNode = new RootNode();
        parseContainerNode(rootNode, new Context(template));
        return rootNode;
    }

    private int parseContainerNode(ContainerNode node, Context context) throws IOException {
        State state = State.READING_TEXT;
        boolean nextCharIsLiteral = false;
        Str text = new Str();
        int c;
        while (true) {
            c = context.reader.read();
            if (c == -1) {
                switch (state) {
                    case READING_NAME -> throw new IOException(String.format("Name %s was not closed before end of file.", text.consume()));
                    case READING_TEXT -> node.children.add(new TextNode(text.consume(), context.row, context.col));
                }
                return c;
            } else if (c == escapeChar && !nextCharIsLiteral) {
                nextCharIsLiteral = true;
            } else if (nextCharIsLiteral) {
                if (c == escapeChar || c == openChar || c == closeChar || c == separatorChar) {
                    text.append(c);
                } else {
                    text.append('\\');
                    text.append(c);
                }
                nextCharIsLiteral = false;
            } else {
                switch (state) {
                    case READING_TEXT -> {
                        if (c == openChar) {
                            if (text.isNotEmpty()) {
                                node.children.add(new TextNode(text.consume(), context.row, context.col));
                            }
                            state = State.READING_NAME;
                        } else if (c == separatorChar) {
                            if (node instanceof LoopPartNode) {
                                if (text.isNotEmpty()) {
                                    node.children.add(new TextNode(text.consume(), context.row, context.col));
                                }
                                return c;
                            } else {
                                text.append(c);
                            }
                        } else if (c == closeChar) {
                            if (node instanceof RootNode) {
                                throw new IOException(String.format("Unexpected closing %c at top level.", c));
                            } else {
                                if (text.isNotEmpty()) {
                                    node.children.add(new TextNode(text.consume(), context.row, context.col));
                                }
                                return c;
                            }
                        } else {
                            text.append(c);
                        }
                    }
                    case READING_NAME -> {
                        if (c == separatorChar) {
                            LoopNode loopNode = new LoopNode(text.consume(), context.row, context.col);
                            parseLoopNode(loopNode, context);
                            node.children.add(loopNode);
                            loopNode.validate();
                            state = State.READING_TEXT;
                        } else if (c == openChar) {
                            throw new IOException(String.format("Unexpected open %c in name.", c));
                        } else if (c == closeChar) {
                            node.children.add(new NameNode(text.consume(), context.row, context.col));
                            state = State.READING_TEXT;
                        } else {
                            text.append(c);
                        }
                    }
                }
            }
        }
    }

    private void parseLoopNode(LoopNode loopNode, Context context) throws IOException {
        int closeChar;
        do {
            LoopPartNode rootNode = new LoopPartNode(context.row, context.col);
            loopNode.children.add(rootNode);
            closeChar = parseContainerNode(rootNode, context);
        } while (closeChar == separatorChar);
        if (closeChar == -1) {
            // TODO
            throw new IOException();
        }
    }
}
