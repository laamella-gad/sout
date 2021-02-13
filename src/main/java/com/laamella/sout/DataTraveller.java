package com.laamella.sout;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

class DataTraveller {

    static <T> Iterator<T> valueIterator(Object value) {
        if (value instanceof List) {
            return ((List<T>) value).iterator();
        } else if (value instanceof Object[]) {
            return stream((T[]) value).iterator();
        } else if (value instanceof int[]) {
            return (Iterator<T>) stream((int[]) value).boxed().iterator();
        } else if (value instanceof Stream) {
            return (Iterator<T>) ((Stream<?>) value).iterator();
        } else if (value instanceof Iterator) {
            return (Iterator<T>) value;
        } else if (value instanceof Iterable) {
            return ((Iterable<T>) value).iterator();
        }
        // TODO and so on, and so on...
        return (Iterator<T>) Arrays.asList(value).iterator();
    }

    static String convertValueToText(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    static <T> T findValueOf(Object target, String complexName) {
        int dotIndex = complexName.indexOf('.');
        if (dotIndex >= 0) {
            Object nestedValue = simpleFindValueOf(target, complexName.substring(0, dotIndex));
            return findValueOf(nestedValue, complexName.substring(dotIndex + 1));
        }
        return simpleFindValueOf(target, complexName);
    }

    private static <T> T simpleFindValueOf(Object target, String name) {
        if (target == null) {
            throw new NullPointerException(String.format("%s not found on null object.", name));
        }
        // If the name is empty, the value is the target itself.
        if (name.isBlank()) {
            return (T) target;
        }
        // Find name in the keys of a map.
        if (target instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) target;
            Object value = map.get(name);
            if (value == null) {
                throw new IllegalArgumentException(String.format("%s not found in map %s", name, target));
            }
            return (T) value;
        }
        // Find value by applying the target function to the key.
        if (target instanceof Function) {
            return ((Function<Object, T>) target).apply(name);
        }
        // Find the value of a field called name.
        T fieldValue = getFieldValue(target, target.getClass(), name);
        if (fieldValue != null) {
            return fieldValue;
        }
        // Get the value from getName()
        T getterValue = getMethodValue(target, target.getClass(), "get" + capitalize(name));
        if (getterValue != null) {
            return getterValue;
        }
        // Get the value from isName()
        T isserValue = getMethodValue(target, target.getClass(), "is" + capitalize(name));
        if (isserValue != null) {
            return isserValue;
        }
        // Give up.
        throw new IllegalArgumentException(String.format("%s not found on %s", name, target));
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static <T> T getFieldValue(Object target, Class<?> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            return (T) field.get(target);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // go on
        }

        Class<?> superclass = type.getSuperclass();
        if (superclass == null) {
            return null;
        }
        return getFieldValue(target, superclass, fieldName);
    }

    static <T> T getMethodValue(Object target, Class<?> type, String fieldName) {
        try {
            Method method = type.getDeclaredMethod(fieldName);
            return (T) method.invoke(target);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            // go on
        }

        Class<?> superclass = type.getSuperclass();
        if (superclass == null) {
            return null;
        }
        return getMethodValue(target, superclass, fieldName);
    }
}
