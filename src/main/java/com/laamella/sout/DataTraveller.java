package com.laamella.sout;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

class DataTraveller {

    static Iterator<?> valueIterator(Object value) {
        if (value instanceof List) {
            return ((List<?>) value).iterator();
        } else if (value instanceof Object[]) {
            return stream((Object[]) value).iterator();
        } else if (value instanceof int[]) {
            return stream((int[]) value).boxed().iterator();
        } else if (value instanceof Stream) {
            return ((Stream<?>) value).iterator();
        } else if (value instanceof Iterator) {
            return (Iterator<?>) value;
        } else if (value instanceof Iterable) {
            return ((Iterable<?>) value).iterator();
        }
        // TODO and so on, and so on...
        return asList(value).iterator();
    }

    static void renderValueAsText(Object value, Writer output, SoutConfiguration configuration) throws IOException {
        if (value == null) {
            return;
        }
        for (TypeHandler typeHandler : configuration.typeHandlers) {
            if (typeHandler.render(value, output)) {
                return;
            }
        }
        output.append(value.toString());
    }

    static Object findValueOf(Object target, String complexName, SoutConfiguration configuration) throws IllegalAccessException {
        int dotIndex = complexName.indexOf('.');
        if (dotIndex >= 0) {
            Object nestedValue = simpleFindValueOf(target, complexName.substring(0, dotIndex), configuration);
            return findValueOf(nestedValue, complexName.substring(dotIndex + 1), configuration);
        }
        return simpleFindValueOf(target, complexName, configuration);
    }

    private static Object simpleFindValueOf(Object target, String name, SoutConfiguration configuration) throws IllegalAccessException {
        if (target == null) {
            throw new NullPointerException(String.format("%s not found on null object.", name));
        }
        // If the name is empty, the value is the target itself.
        if (name.isBlank()) {
            return target;
        }
        // See if there is a custom resolver to handle the name.
        for (NameResolver nameResolver : configuration.nameResolvers) {
            Object value = nameResolver.resolve(target, name);
            if (value != null) {
                return value;
            }
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
