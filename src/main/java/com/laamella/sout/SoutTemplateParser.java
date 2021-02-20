package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;

class SoutTemplateParser {
    private final int openChar;
    private final int separatorChar;
    private final int closeChar;
    private final int escapeChar;
    private final NameResolver nameResolver;
    private final DataConverter dataConverter;
    private final NameRenderer nameRenderer;
    private final TypeRenderer typeRenderer;

    public SoutTemplateParser(int openChar, int separatorChar, int closeChar, int escapeChar,
                              NameResolver nameResolver, DataConverter dataConverter,
                              NameRenderer nameRenderer, TypeRenderer typeRenderer) {
        this.openChar = openChar;
        this.separatorChar = separatorChar;
        this.closeChar = closeChar;
        this.escapeChar = escapeChar;
        this.nameResolver = nameResolver;
        this.dataConverter = dataConverter;
        this.nameRenderer = nameRenderer;
        this.typeRenderer = typeRenderer;
    }

    enum State {READING_NAME, READING_TEXT}

    static class Context {
        int row = 0, column = 0;
        Reader reader;
        private Position lastPosition = new Position(0, 0);

        Context(Reader reader) {
            this.reader = reader;
        }

        public int read() throws IOException {
            int c = reader.read();
            if (c == '\n') {
                row++;
                column = 0;
            } else {
                column++;
            }
            return c;
        }

        public Position lastPosition() {
            Position lastPosition = this.lastPosition;
            this.lastPosition = new Position(column, row);
            return lastPosition;
        }
    }

    RootNode parse(Reader template) throws IOException {
        RootNode soutTemplate = new RootNode();
        parseContainerNode(soutTemplate, new Context(template));
        return soutTemplate;
    }

    private int parseContainerNode(ContainerNode node, Context context) throws IOException {
        State state = State.READING_TEXT;
        boolean nextCharIsLiteral = false;
        TextBuffer text = new TextBuffer();
        int c;
        while (true) {
            c = context.read();
            if (c == -1) {
                switch (state) {
                    case READING_NAME -> throw new IOException(String.format("Name %s was not closed before end of file.", text.consume()));
                    case READING_TEXT -> node.children.add(new TextNode(text.consume(), context.lastPosition()));
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
                                node.children.add(new TextNode(text.consume(), context.lastPosition()));
                            }
                            state = State.READING_NAME;
                        } else if (c == separatorChar) {
                            if (node instanceof LoopPartNode) {
                                if (text.isNotEmpty()) {
                                    node.children.add(new TextNode(text.consume(), context.lastPosition()));
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
                                    node.children.add(new TextNode(text.consume(), context.lastPosition()));
                                }
                                return c;
                            }
                        } else {
                            text.append(c);
                        }
                    }
                    case READING_NAME -> {
                        if (c == separatorChar) {
                            String name = text.consume();
                            LoopNode loopNode = new LoopNode(name, context.lastPosition(), nameResolver, dataConverter);
                            parseLoopNode(loopNode, context);
                            node.children.add(loopNode);
                            int parts = loopNode.children.size();
                            switch (parts) {
                                case 1 -> loopNode.mainPart = (LoopPartNode) loopNode.children.get(0);
                                case 2 -> {
                                    loopNode.mainPart = (LoopPartNode) loopNode.children.get(0);
                                    loopNode.separatorPart = (LoopPartNode) loopNode.children.get(1);
                                }
                                case 4 -> {
                                    loopNode.leadIn = (LoopPartNode) loopNode.children.get(0);
                                    loopNode.mainPart = (LoopPartNode) loopNode.children.get(1);
                                    loopNode.separatorPart = (LoopPartNode) loopNode.children.get(2);
                                    loopNode.leadOut = (LoopPartNode) loopNode.children.get(3);
                                }
                                // TODO 6 = special separator after the first and before the last element?
                                default -> throw new IllegalArgumentException(String.format("Wrong amount of parts (%d) for loop %s.", parts, name));
                            }
                            state = State.READING_TEXT;
                        } else if (c == openChar) {
                            throw new IOException(String.format("Unexpected open %c in name.", c));
                        } else if (c == closeChar) {
                            node.children.add(new NameNode(text.consume(), context.lastPosition(), nameRenderer, nameResolver, typeRenderer));
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
            var loopPartNode = new LoopPartNode(context.lastPosition());
            loopNode.children.add(loopPartNode);
            closeChar = parseContainerNode(loopPartNode, context);
        } while (closeChar == separatorChar);
        if (closeChar == -1) {
            throw new IOException("End of template while reading a loop.");
        }
    }
}
