package com.laamella.sout;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class Template {
    private final RootNode rootNode;

    public Template(Reader template) throws IOException {
        rootNode = new TemplateParser().parse(template);
    }

    public void apply(Object data, Writer output) throws IOException {
        for (Node child : rootNode.children) {
            render(child, data, output);
        }
    }

    private void render(Node node, Object data, Writer output) throws IOException {
        if (node != null) {
            if (node instanceof LoopNode) {
                LoopNode loopNode = (LoopNode) node;
                List<Object> listData = convertValueToList(findValueOf(data, loopNode.name));

                if (!listData.isEmpty()) {
                    render(loopNode.leadIn, data, output);
                }

                boolean printSeparator = false;
                for (Object listElement : listData) {
                    if (printSeparator) {
                        render(loopNode.separatorPart, listElement, output);
                    }
                    printSeparator = true;
                    if(loopNode.mainPart.children.isEmpty()) {
                    }else {
                        render(loopNode.mainPart, listElement, output);
                    }
                }
                if (!listData.isEmpty()) {
                    render(loopNode.leadOut, data, output);
                }
            } else if (node instanceof LoopPartNode) {
                for (Node child : ((LoopPartNode) node).children) {
                    render(child, data, output);
                }
            } else if (node instanceof TextNode) {
                output.append(((TextNode) node).text);
            } else if (node instanceof NameNode) {
                String text = convertValueToText(findValueOf(data, ((NameNode) node).name));
                output.append(text);
            }
        }
    }

    private List<Object> convertValueToList(Object value) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        // TODO arrays
        // TODO streams?
        throw new IllegalArgumentException(String.format("%s is not a list", value));
    }

    private String convertValueToText(Object value) {
        return value.toString();
    }

    private Object findValueOf(Object target, String name) {
        if (target instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) target;
            Object value = map.get(name);
            if (value == null) {
                throw new IllegalArgumentException(String.format("%s not found in map %s", name, target));
            }
            return value;
        }
        // TODO instrospect getters
        // TODO introspect fields
        // TODO apply function?
        throw new IllegalArgumentException(String.format("%s not found on %s", name, target));
    }
}
