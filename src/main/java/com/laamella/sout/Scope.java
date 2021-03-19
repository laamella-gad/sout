package com.laamella.sout;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A scope that holds user-defined variables.
 */
public class Scope {
    private final Scope parentScope;
    private final Map<String, Object> variables = new HashMap<>();

    Scope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    /**
     * @return the value of variable "name".
     * If it wasn't defined yet, it is defined in this scope, and set to "def".
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String name, T def) {
        var nameScope = findScopeContainingName(requireNonNull(name));
        if (nameScope == null) {
            variables.put(name, def);
            return def;
        }
        return (T) nameScope.variables.get(name);
    }

    /**
     * @return the scope where name is defined.
     * If it hasn't been defined anywhere, null is returned.
     */
    private Scope findScopeContainingName(String name) {
        if (variables.containsKey(requireNonNull(name))) {
            return this;
        }
        if (parentScope == null) {
            return null;
        }
        return parentScope.findScopeContainingName(name);
    }

    /**
     * Set a variable to a new value in the scope where it is defined.
     *
     * @throws SoutException if name isn't defined in any scope.
     */
    public <T> T updateVariable(String name, T newValue) {
        var scope = findScopeContainingName(requireNonNull(name));
        if (scope == null) {
            throw new SoutException("Variable %s not found.", name);
        }
        scope.variables.put(name, newValue);
        return newValue;
    }

    /**
     * Set a variable to a value in this scope.
     */
    public void setVariable(String name, Object value) {
        variables.put(requireNonNull(name), value);
    }

    /**
     * @return the scope above this scope. null if this is the global scope.
     */
    public Scope getParentScope() {
        return parentScope;
    }

    /**
     * @return the global scope, where global variables can be stored.
     */
    public Scope getGlobalScope() {
        if (parentScope == null) {
            return this;
        }
        return parentScope.getGlobalScope();
    }
}
