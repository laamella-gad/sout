package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * The base of the Abstract Syntax Tree for the template.
 * In other words: this is an easy to use structure that represents a template after we've parsed it.
 * Its main purpose is to render the template.
 */
abstract class Renderer implements Renderable {
    final Position position;

    Renderer(Position position) {
        this.position = position;
    }

    public abstract void render(Object model, Scope scope, Writer outputWriter);
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
    public void render(Object model, Scope scope, Writer outputWriter) {
        try {
            if (customNameRenderer.render(name, null, model, scope, position, outputWriter)) {
                return;
            }
            NameResolver.Result subModelResult = nameResolver.resolveComplexNameOnModel(model, name);
            if (subModelResult.failed) {
                throw new SoutException(position, subModelResult.message);
            }
            var subModel = subModelResult.value;
            if (customTypeRenderer.render(name, null, null, null, subModel, scope, position, outputWriter)) {
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
    public void render(Object model, Scope scope, Writer outputWriter) {
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
    private final CustomNameRenderer customNameRenderer;
    private final CustomTypeRenderer customTypeRenderer;
    private final Renderable[] parts;
    private final BooleanRenderer booleanRenderer = new BooleanRenderer();
    private final CollectionRenderer collectionRenderer;
    private final SimpleNestingRenderer simpleNestingRenderer = new SimpleNestingRenderer();

    NestedRenderer(String name, Position position, NameResolver nameResolver, CustomNameRenderer customNameRenderer, CustomTypeRenderer customTypeRenderer, IteratorFactory iteratorFactory, Renderable[] parts) {
        super(position);
        this.name = name;
        this.nameResolver = nameResolver;
        this.customNameRenderer = customNameRenderer;
        this.customTypeRenderer = customTypeRenderer;
        this.parts = parts;
        this.collectionRenderer = new CollectionRenderer(iteratorFactory);
    }

    @Override
    public void render(Object model, Scope scope, Writer outputWriter) {
        try {
            if (customNameRenderer.render(name, parts, model, scope, position, outputWriter)) {
                return;
            }

            var nestedModelResult = nameResolver.resolveComplexNameOnModel(model, name);
            if (nestedModelResult.failed) {
                throw new SoutException(position, nestedModelResult.message);
            }
            var nestedModel = nestedModelResult.value;
            var nestedScope = new Scope(scope);

            if (customTypeRenderer.render(name, parts, nestedModel, nestedScope, model, scope, position, outputWriter)) {
                return;
            }
            if (booleanRenderer.render(name, parts, nestedModel, nestedScope, model, scope, position, outputWriter)) {
                return;
            }
            if (collectionRenderer.render(name, parts, nestedModel, nestedScope, model, scope, position, outputWriter)) {
                return;
            }
            if (simpleNestingRenderer.render(name, parts, nestedModel, nestedScope, model, scope, position, outputWriter)) {
                return;
            }
            throw new SoutException(position, "Don't know how to render %s.", name);
        } catch (IOException e) {
            throw new SoutException(position, e);
        }
    }

    @Override
    public String toString() {
        return '❰' + name + "❚" + Arrays.stream(parts).map(Object::toString).collect(joining("❚")) + '❱';
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

