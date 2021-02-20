package com.laamella.sout;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Function;

/**
 * Tries to go from object to object by evaluating names to fields, getters, maps, ...
 */
@SuppressWarnings("unchecked")
class ModelTraveller {

    Object evaluateNameOnModel(Object model, String complexName) throws IllegalAccessException {
        int dotIndex = complexName.indexOf('.');
        if (dotIndex >= 0) {
            Object nestedValue = innerEvaluateNameOnModel(model, complexName.substring(0, dotIndex));
            return evaluateNameOnModel(nestedValue, complexName.substring(dotIndex + 1));
        }
        return innerEvaluateNameOnModel(model, complexName);
    }

    private Object innerEvaluateNameOnModel(Object target, String name) throws IllegalAccessException {
        if (target == null) {
            throw new NullPointerException(String.format("%s not found on null object.", name));
        }
        // If the name is empty, the value is the target itself.
        if (name.isBlank()) {
            return target;
        }
        // Find name in the keys of a map.
        if (target instanceof Map) {
            var map = (Map<String, Object>) target;
            var value = map.get(name);
            if (value == null) {
                throw new IllegalArgumentException(String.format("%s not found in map %s", name, target));
            }
            return value;
        }
        // Find value by applying the target function to the key.
        if (target instanceof Function) {
            return ((Function<Object, Object>) target).apply(name);
        }
        // Find the value of a field called name.
        var fieldValue = getFieldValue(target, target.getClass(), name);
        if (fieldValue != null) {
            return fieldValue;
        }
        // Get the value from getName()
        var getterValue = getMethodValue(target, target.getClass(), "get" + capitalize(name));
        if (getterValue != null) {
            return getterValue;
        }
        // Get the value from isName()
        var isserValue = getMethodValue(target, target.getClass(), "is" + capitalize(name));
        if (isserValue != null) {
            return isserValue;
        }
        // Get the value from name()
        var plainMethodValue = getMethodValue(target, target.getClass(), name);
        if (plainMethodValue != null) {
            return plainMethodValue;
        }
        // Give up.
        throw new IllegalArgumentException(String.format("%s not found on %s", name, target));
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static Object getFieldValue(Object target, Class<?> type, String fieldName) throws IllegalAccessException {
        try {
            var field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException e) {
            // go on
        }

        var superclass = type.getSuperclass();
        if (superclass == null) {
            return null;
        }
        return getFieldValue(target, superclass, fieldName);
    }

    static Object getMethodValue(Object target, Class<?> type, String fieldName) throws IllegalAccessException {
        try {
            var method = type.getDeclaredMethod(fieldName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (NoSuchMethodException | InvocationTargetException e) {
            // go on
        }

        var superclass = type.getSuperclass();
        if (superclass == null) {
            return null;
        }
        return getMethodValue(target, superclass, fieldName);
    }
}
