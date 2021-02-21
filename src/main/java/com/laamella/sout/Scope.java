package com.laamella.sout;

import java.util.Map;

public class Scope {
    public final Scope parentScope;
    private final Map<String, Object> variables;

    Scope(Scope parentScope, Map<String, Object> variables) {
        this.parentScope = parentScope;
        this.variables = variables;
    }

    @SuppressWarnings("unchecked")
    public <T> T getVariable(String name, T def) {
        var nameScope = findScopeContainingName(name);
        if (nameScope == null) {
            variables.put(name, def);
            return def;
        }
        return (T) nameScope.variables.get(name);
    }

    private Scope findScopeContainingName(String name) {
        if (variables.containsKey(name)) {
            return this;
        }
        if (parentScope == null) {
            return null;
        }
        return parentScope.findScopeContainingName(name);
    }

    public <T> T updateVariable(String name, T newValue) {
        var scope = findScopeContainingName(name);
        if (scope == null) {
            throw new IllegalArgumentException(String.format("Variable %s not found.", name));
        }
        scope.variables.put(name, newValue);
        return newValue;
    }

    public void setLocalVariable(String name, Object value) {
        variables.put(name, value);
    }

    public void setGlobalVariable(String name, Object value) {
        if (parentScope == null) {
            variables.put(name, value);
        } else {
            parentScope.setGlobalVariable(name, value);
        }
    }
}
