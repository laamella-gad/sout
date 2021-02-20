package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;

class SoutTemplateParser {
    private final int openChar;
    private final int separatorChar;
    private final int closeChar;
    private final int escapeChar;
    private final NameResolver customNameResolver;
    private final IteratorFactory iteratorFactory;
    private final CustomNameRenderer customNameRenderer;
    private final CustomTypeRenderer customTypeRenderer;
    private final CustomIteratorFactory customIteratorFactory;

    public SoutTemplateParser(int openChar, int separatorChar, int closeChar, int escapeChar,
                              NameResolver customNameResolver, IteratorFactory iteratorFactory,
                              CustomNameRenderer customNameRenderer,
                              CustomTypeRenderer customTypeRenderer,
                              CustomIteratorFactory customIteratorFactory) {
        this.openChar = openChar;
        this.separatorChar = separatorChar;
        this.closeChar = closeChar;
        this.escapeChar = escapeChar;
        this.customNameResolver = customNameResolver;
        this.iteratorFactory = iteratorFactory;
        this.customNameRenderer = customNameRenderer;
        this.customTypeRenderer = customTypeRenderer;
        this.customIteratorFactory = customIteratorFactory;
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

    RootRenderer parse(Reader template) throws IOException {
        RootRenderer soutTemplate = new RootRenderer();
        parseContainerNode(soutTemplate, new Context(template));
        return soutTemplate;
    }

    private int parseContainerNode(ContainerRenderer node, Context context) throws IOException {
        State state = State.READING_TEXT;
        boolean nextCharIsLiteral = false;
        TextBuffer text = new TextBuffer();
        int c;
        while (true) {
            c = context.read();
            if (c == -1) {
                switch (state) {
                    case READING_NAME -> throw new IOException(String.format("Name %s was not closed before end of file.", text.consume()));
                    case READING_TEXT -> node.children.add(new TextRenderer(text.consume(), context.lastPosition()));
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
                                node.children.add(new TextRenderer(text.consume(), context.lastPosition()));
                            }
                            state = State.READING_NAME;
                        } else if (c == separatorChar) {
                            if (node instanceof LoopPartRenderer) {
                                if (text.isNotEmpty()) {
                                    node.children.add(new TextRenderer(text.consume(), context.lastPosition()));
                                }
                                return c;
                            } else {
                                text.append(c);
                            }
                        } else if (c == closeChar) {
                            if (node instanceof RootRenderer) {
                                throw new IOException(String.format("Unexpected closing %c at top level.", c));
                            } else {
                                if (text.isNotEmpty()) {
                                    node.children.add(new TextRenderer(text.consume(), context.lastPosition()));
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
                            LoopRenderer loopNode = new LoopRenderer(name, context.lastPosition(), customNameResolver, iteratorFactory, customIteratorFactory);
                            parseLoopNode(loopNode, context);
                            node.children.add(loopNode);
                            int parts = loopNode.children.size();
                            switch (parts) {
                                case 1 -> loopNode.mainPart = (LoopPartRenderer) loopNode.children.get(0);
                                case 2 -> {
                                    loopNode.mainPart = (LoopPartRenderer) loopNode.children.get(0);
                                    loopNode.separatorPart = (LoopPartRenderer) loopNode.children.get(1);
                                }
                                case 4 -> {
                                    loopNode.leadIn = (LoopPartRenderer) loopNode.children.get(0);
                                    loopNode.mainPart = (LoopPartRenderer) loopNode.children.get(1);
                                    loopNode.separatorPart = (LoopPartRenderer) loopNode.children.get(2);
                                    loopNode.leadOut = (LoopPartRenderer) loopNode.children.get(3);
                                }
                                // TODO 6 = special separator after the first and before the last element?
                                default -> throw new IllegalArgumentException(String.format("Wrong amount of parts (%d) for loop %s.", parts, name));
                            }
                            state = State.READING_TEXT;
                        } else if (c == openChar) {
                            throw new IOException(String.format("Unexpected open %c in name.", c));
                        } else if (c == closeChar) {
                            node.children.add(new NameRenderer(text.consume(), context.lastPosition(), customNameRenderer, customNameResolver, customTypeRenderer));
                            state = State.READING_TEXT;
                        } else {
                            text.append(c);
                        }
                    }
                }
            }
        }
    }

    private void parseLoopNode(LoopRenderer loopNode, Context context) throws IOException {
        int closeChar;
        do {
            var loopPartNode = new LoopPartRenderer(context.lastPosition());
            loopNode.children.add(loopPartNode);
            closeChar = parseContainerNode(loopPartNode, context);
        } while (closeChar == separatorChar);
        if (closeChar == -1) {
            throw new IOException("End of template while reading a loop.");
        }
    }
}
