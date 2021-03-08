package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static java.util.stream.Collectors.joining;

abstract class Renderer {
    final Position position;

    Renderer(Position position) {
        this.position = position;
    }

    abstract void render(Object model, Scope scope, Writer outputWriter);
}

class NameRenderer extends Renderer {
    private final String name;
    private final NameResolver nameResolver;
    private final CustomNameRenderer customNameRenderer;
    private final CustomTypeRenderer customTypeRenderer;

    NameRenderer(String name, Position position, NameResolver nameResolver, CustomNameRenderer customNameRenderer, CustomTypeRenderer customTypeRenderer) {
        super(position);
        this.name = name;
        this.customNameRenderer = customNameRenderer;
        this.nameResolver = nameResolver;
        this.customTypeRenderer = customTypeRenderer;
    }

    @Override
    void render(Object model, Scope scope, Writer outputWriter) {
        try {
            if (customNameRenderer.render(model, name, scope, outputWriter)) {
                return;
            }
            NameResolver.Result subModelResult = nameResolver.resolveComplexNameOnModel(model, name);
            if (subModelResult.failed) {
                throw new SoutException(position, subModelResult.message);
            }
            var subModel = subModelResult.value;
            if (customTypeRenderer.write(subModel, scope, outputWriter)) {
                return;
            }
            if (subModel == null) {
                throw new SoutException(position, "Null value.");
            }
            outputWriter.append(subModel.toString());
        } catch (IOException e) {
            throw new SoutException(position, e);
        }
    }

    @Override
    public String toString() {
        return "❰" + name + "❱";
    }
}

class ContainerRenderer extends Renderer {
    private final List<Renderer> children;

    ContainerRenderer(Position position, List<Renderer> children) {
        super(position);
        this.children = children;
    }

    @Override
    void render(Object model, Scope scope, Writer outputWriter) {
        for (var child : children) {
            child.render(model, scope, outputWriter);
        }
    }

    @Override
    public String toString() {
        return children.stream().map(Object::toString).collect(joining());
    }
}

class NestedRenderer extends Renderer {
    private final String name;
    private final NameResolver nameResolver;
    private final IteratorFactory iteratorFactory;
    private final ContainerRenderer mainPart;
    private final ContainerRenderer separatorPart;
    private final ContainerRenderer leadIn;
    private final ContainerRenderer leadOut;
    private final ContainerRenderer truePart;
    private final ContainerRenderer falsePart;

    NestedRenderer(String name, Position position, NameResolver nameResolver, IteratorFactory iteratorFactory,
                   ContainerRenderer mainPart, ContainerRenderer separatorPart, ContainerRenderer leadIn, ContainerRenderer leadOut,
                   ContainerRenderer truePart, ContainerRenderer falsePart) {
        super(position);
        this.name = name;
        this.nameResolver = nameResolver;
        this.iteratorFactory = iteratorFactory;
        this.mainPart = mainPart;
        this.separatorPart = separatorPart;
        this.leadIn = leadIn;
        this.leadOut = leadOut;
        this.truePart = truePart;
        this.falsePart = falsePart;
    }

    @Override
    public void render(Object model, Scope scope, Writer outputWriter) {
        var nestedModelResult = nameResolver.resolveComplexNameOnModel(model, name);
        if (nestedModelResult.failed) {
            throw new SoutException(position, nestedModelResult.message);
        }
        var nestedModel = nestedModelResult.value;
        var nestedScope = new Scope(scope);
        if (nestedModel instanceof Boolean) {
            boolean b = (boolean) nestedModel;
            if (truePart == null) {
                throw new SoutException("Wrong amount of parts for rendering a boolean.");
            }
            if (b) {
                truePart.render(model, nestedScope, outputWriter);
            } else if (falsePart != null) {
                falsePart.render(model, nestedScope, outputWriter);
            }
            return;
        }
        var iterator = iteratorFactory.toIterator(nestedModel, scope, position);
        if (!iterator.hasNext()) {
            // Empty collection, nothing to do.
            return;
        }

        if (leadIn != null) {
            leadIn.render(model, nestedScope, outputWriter);
        }

        var printSeparator = false;
        while (iterator.hasNext()) {
            var listElement = iterator.next();
            if (printSeparator && separatorPart != null) {
                separatorPart.render(listElement, nestedScope, outputWriter);
            }
            printSeparator = true;
            mainPart.render(listElement, nestedScope, outputWriter);
        }
        if (leadOut != null) {
            leadOut.render(model, nestedScope, outputWriter);
        }
    }

    @Override
    public String toString() {
        return '❰' + name +
                (leadIn != null ? "❚" + leadIn : "") +
                "❚" + mainPart +
                (separatorPart != null ? "❚" + separatorPart : "") +
                (leadOut != null ? "❚" + leadOut : "")
                + '❱';
    }
}

class TextRenderer extends Renderer {
    final String text;

    TextRenderer(String text, Position position) {
        super(position);
        this.text = text;
    }

    @Override
    public void render(Object model, Scope scope, Writer outputWriter) {
        try {
            outputWriter.append(text);
        } catch (IOException e) {
            throw new SoutException(position, e);
        }
    }

    @Override
    public String toString() {
        return text;
    }
}

