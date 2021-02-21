package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

class SoutTemplateParser {
    private final int openChar;
    private final int separatorChar;
    private final int closeChar;
    private final int escapeChar;
    private final NameResolver nameResolver = new NameResolver();
    private final IteratorFactory iteratorFactory;
    private final CustomNameRenderer customNameRenderer;
    private final CustomTypeRenderer customTypeRenderer;

    public SoutTemplateParser(int openChar, int separatorChar, int closeChar, int escapeChar,
                              IteratorFactory iteratorFactory,
                              CustomNameRenderer customNameRenderer,
                              CustomTypeRenderer customTypeRenderer) {
        this.openChar = openChar;
        this.separatorChar = separatorChar;
        this.closeChar = closeChar;
        this.escapeChar = escapeChar;
        this.iteratorFactory = iteratorFactory;
        this.customNameRenderer = customNameRenderer;
        this.customTypeRenderer = customTypeRenderer;
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

    ContainerRenderer parseTemplate(Reader template) throws IOException {
        var renderers = new ArrayList<Renderer>();
        int c = parseRenderersIntoList(renderers, false, new Context(template));
        if (c == closeChar) {
            throw new IOException(String.format("Unexpected closing %c at top level.", c));
        }
        return new ContainerRenderer(new Position(0, 0), renderers);
    }

    private int parseRenderersIntoList(List<Renderer> renderers, boolean terminateOnSeparator, Context context) throws IOException {
        State state = State.READING_TEXT;
        boolean nextCharIsLiteral = false;
        TextBuffer text = new TextBuffer();
        int c;
        while (true) {
            c = context.read();
            if (c == -1) {
                switch (state) {
                    case READING_NAME -> throw new IOException(String.format("Name %s was not closed before end of file.", text.consume()));
                    case READING_TEXT -> renderers.add(new TextRenderer(text.consume(), context.lastPosition()));
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
                                renderers.add(new TextRenderer(text.consume(), context.lastPosition()));
                            }
                            state = State.READING_NAME;
                        } else if (c == separatorChar) {
                            if (terminateOnSeparator) {
                                if (text.isNotEmpty()) {
                                    renderers.add(new TextRenderer(text.consume(), context.lastPosition()));
                                }
                                return c;
                            } else {
                                text.append(c);
                            }
                        } else if (c == closeChar) {
                            if (text.isNotEmpty()) {
                                renderers.add(new TextRenderer(text.consume(), context.lastPosition()));
                            }
                            return c;
                        } else {
                            text.append(c);
                        }
                    }
                    case READING_NAME -> {
                        if (c == separatorChar) {
                            LoopRenderer loopNode = parseLoopRenderer(text.consume(), context);
                            renderers.add(loopNode);
                            state = State.READING_TEXT;
                        } else if (c == openChar) {
                            throw new IOException(String.format("Unexpected open %c in name.", c));
                        } else if (c == closeChar) {
                            renderers.add(new NameRenderer(text.consume(), context.lastPosition(), nameResolver, customNameRenderer, customTypeRenderer));
                            state = State.READING_TEXT;
                        } else {
                            text.append(c);
                        }
                    }
                }
            }
        }

    }

    private LoopRenderer parseLoopRenderer(String name, Context context) throws IOException {
        int closeChar;
        var loopParts = new ArrayList<ContainerRenderer>();
        do {
            var renderersInLoopPart = new ArrayList<Renderer>();
            closeChar = parseRenderersIntoList(renderersInLoopPart, true, context);
            loopParts.add(new ContainerRenderer(context.lastPosition(), renderersInLoopPart));
        } while (closeChar == separatorChar);
        if (closeChar == -1) {
            throw new IOException("End of template while reading a loop.");
        }
        int parts = loopParts.size();
        ContainerRenderer mainPart, leadIn = null, separatorPart = null, leadOut = null;
        switch (parts) {
            case 1 -> mainPart = loopParts.get(0);
            case 2 -> {
                mainPart = loopParts.get(0);
                separatorPart = loopParts.get(1);
            }
            case 4 -> {
                leadIn = loopParts.get(0);
                mainPart = loopParts.get(1);
                separatorPart = loopParts.get(2);
                leadOut = loopParts.get(3);
            }
            // TODO 6 = special separator after the first and before the last element?
            default -> throw new IllegalArgumentException(String.format("Wrong amount of parts (%d) for loop %s.", parts, name));
        }
        return new LoopRenderer(name, context.lastPosition(), nameResolver, iteratorFactory, mainPart, separatorPart, leadIn, leadOut);
    }
}
