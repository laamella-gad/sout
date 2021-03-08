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
        final Reader reader;
        int row = 1, column = 0;
        private Position lastPosition = new Position(1, 1);

        Context(Reader reader) {
            this.reader = reader;
        }

        public int read() throws IOException {
            int c = reader.read();
            if (c == '\n') {
                row++;
                column = 1;
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

        public Position thisPosition() {
            return new Position(column, row);
        }
    }

    ContainerRenderer parseTemplate(Reader template) {
        var renderers = new ArrayList<Renderer>();
        var context = new Context(template);
        int c = parseRenderersIntoList(renderers, false, context);
        if (c == closeChar) {
            throw new SoutException(context.thisPosition(), "Unexpected closing %c at top level.", c);
        }
        return new ContainerRenderer(new Position(0, 0), renderers);
    }

    private int parseRenderersIntoList(List<Renderer> renderers, boolean terminateOnSeparator, Context context) {
        State state = State.READING_TEXT;
        boolean nextCharIsLiteral = false;
        TextBuffer text = new TextBuffer();
        int c;
        while (true) {
            try {
                c = context.read();
            } catch (IOException e) {
                throw new SoutException(context.thisPosition(), e);
            }
            if (c == -1) {
                switch (state) {
                    case READING_NAME -> throw new SoutException(context.thisPosition(), "Name %s was not closed before end of file.", text.consume());
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
                            NestedRenderer nestedNode = parseNestingRenderer(text.consume(), context);
                            renderers.add(nestedNode);
                            state = State.READING_TEXT;
                        } else if (c == openChar) {
                            throw new SoutException(context.thisPosition(), "Unexpected open %c in name.", c);
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

    private NestedRenderer parseNestingRenderer(String name, Context context) {
        int closeChar;
        var nestedParts = new ArrayList<ContainerRenderer>();
        do {
            var renderersInNestedPart = new ArrayList<Renderer>();
            closeChar = parseRenderersIntoList(renderersInNestedPart, true, context);
            nestedParts.add(new ContainerRenderer(context.lastPosition(), renderersInNestedPart));
        } while (closeChar == separatorChar);
        if (closeChar == -1) {
            throw new SoutException(context.thisPosition(), "End of template while reading a nesting.");
        }
        int parts = nestedParts.size();
        ContainerRenderer mainPart, leadIn = null, separatorPart = null, leadOut = null, truePart = null, falsePart = null;
        switch (parts) {
            case 1 -> {
                mainPart = nestedParts.get(0);
                truePart = mainPart;
            }
            case 2 -> {
                mainPart = nestedParts.get(0);
                separatorPart = nestedParts.get(1);
                truePart = mainPart;
                falsePart = separatorPart;
            }
            case 4 -> {
                leadIn = nestedParts.get(0);
                mainPart = nestedParts.get(1);
                separatorPart = nestedParts.get(2);
                leadOut = nestedParts.get(3);
            }
            // TODO 6 = special separator after the first and before the last element?
            default -> throw new SoutException(context.lastPosition(), "Wrong amount of parts (%d) for nesting %s.", parts, name);
        }
        return new NestedRenderer(name, context.lastPosition(), nameResolver, iteratorFactory, mainPart, separatorPart, leadIn, leadOut, truePart, falsePart);
    }
}
