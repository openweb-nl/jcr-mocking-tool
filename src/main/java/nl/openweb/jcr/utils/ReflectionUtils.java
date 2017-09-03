package nl.openweb.jcr.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Ebrahim Aharpour
 * @since 9/2/2017
 */
public class ReflectionUtils {

    private ReflectionUtils() {
        // to prevent initialization
    }

    public static Method getMethod(Object obj, String methodName, Class<?>... parameterTypes) {
        Method result;
        Class<?> type = obj.getClass();
        try {
            result = type.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            result = null;
        }
        return result;
    }

    public static Object invokeMethod(Method method, Object obj, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeReflectionException(e);
        }
    }
}
