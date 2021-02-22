package com.laamella.sout;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Function;

import static com.laamella.sout.NameResolver.Result.fail;
import static com.laamella.sout.NameResolver.Result.succeed;

/**
 * Tries to go from object to object by evaluating names to fields, getters, maps, ...
 */
@SuppressWarnings("unchecked")
class NameResolver {

    static class Result {
        final String message;
        final boolean failed;
        final Object value;

        private Result(Object value, String message, boolean failed) {
            this.value = value;
            this.message = message;
            this.failed = failed;
        }

        static Result succeed(Object value) {
            return new Result(value, "", false);
        }

        public static Result fail(String message, Object... params) {
            return new Result(null, String.format(message, params), true);
        }
    }

    Result resolveComplexNameOnModel(Object model, String complexName) {
        int dotIndex = complexName.indexOf('.');
        if (dotIndex >= 0) {
            Result nestedResult = resolveSimpleNameOnModel(model, complexName.substring(0, dotIndex));
            if (!nestedResult.failed) {
                return resolveComplexNameOnModel(nestedResult.value, complexName.substring(dotIndex + 1));
            } else {
                return nestedResult;
            }
        }
        return resolveSimpleNameOnModel(model, complexName);
    }

    private Result resolveSimpleNameOnModel(Object target, String name) {
        // If the name is empty, the value is the target itself.
        if (name.isBlank()) {
            return succeed(target);
        }
        // If we're trying to resolve a name on a null object, it will always fail.
        if (target == null) {
            return fail("%s not found on null object.", name);
        }
        // Find name in the keys of a map.
        if (target instanceof Map) {
            var map = (Map<String, Object>) target;
            if (map.containsKey(name)) {
                return succeed(map.get(name));
            }
            return fail("%s not found in map %s.", name, target);
        }
        // Find value by applying the target function to the key.
        if (target instanceof Function) {
            return succeed(((Function<Object, Object>) target).apply(name));
        }
        // Find the value of a field called name.
        var fieldResult = getFieldValue(target, target.getClass(), name);
        if (!fieldResult.failed) {
            return fieldResult;
        }
        // Get the value from getName()
        var getterResult = getMethodValue(target, target.getClass(), "get" + capitalize(name));
        if (!getterResult.failed) {
            return getterResult;
        }
        // Get the value from isName()
        var isserResult = getMethodValue(target, target.getClass(), "is" + capitalize(name));
        if (!isserResult.failed) {
            return isserResult;
        }
        // Get the value from name()
        var plainMethodResult = getMethodValue(target, target.getClass(), name);
        if (!plainMethodResult.failed) {
            return plainMethodResult;
        }
        // Give up.
        return fail("%s not found on %s", name, target);
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static Result getFieldValue(Object target, Class<?> type, String fieldName) {
        try {
            var field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return succeed(field.get(target));
        } catch (NoSuchFieldException e) {
            // go on
        } catch (IllegalAccessException e) {
            return fail("");
        }

        var superclass = type.getSuperclass();
        if (superclass == null) {
            return fail("");
        }
        return getFieldValue(target, superclass, fieldName);
    }

    static Result getMethodValue(Object target, Class<?> type, String fieldName) {
        try {
            var method = type.getDeclaredMethod(fieldName);
            method.setAccessible(true);
            return succeed(method.invoke(target));
        } catch (NoSuchMethodException | InvocationTargetException e) {
            // go on
        } catch (IllegalAccessException e) {
            return fail("");
        }

        var superclass = type.getSuperclass();
        if (superclass == null) {
            return fail("");
        }
        return getMethodValue(target, superclass, fieldName);
    }
}
