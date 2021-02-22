package com.laamella.sout;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Scope {
    private final Scope parentScope;
    private final Map<String, Object> variables = new HashMap<>();

    Scope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    @SuppressWarnings("unchecked")
    public <T> T getVariable(String name, T def) {
        var nameScope = findScopeContainingName(requireNonNull(name));
        if (nameScope == null) {
            variables.put(name, def);
            return def;
        }
        return (T) nameScope.variables.get(name);
    }

    private Scope findScopeContainingName(String name) {
        if (variables.containsKey(requireNonNull(name))) {
            return this;
        }
        if (parentScope == null) {
            return null;
        }
        return parentScope.findScopeContainingName(name);
    }

    public <T> T updateVariable(String name, T newValue) {
        var scope = findScopeContainingName(requireNonNull(name));
        if (scope == null) {
            throw new SoutException("Variable %s not found.", name);
        }
        scope.variables.put(name, newValue);
        return newValue;
    }

    public void setLocalVariable(String name, Object value) {
        variables.put(requireNonNull(name), value);
    }

    public void setGlobalVariable(String name, Object value) {
        requireNonNull(name);
        if (parentScope == null) {
            variables.put(name, value);
        } else {
            parentScope.setGlobalVariable(name, value);
        }
    }
}
