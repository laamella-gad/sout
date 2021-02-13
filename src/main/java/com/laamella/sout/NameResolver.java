package com.laamella.sout;

@FunctionalInterface
public interface NameResolver {
    Object resolve(Object target, String name);
}
