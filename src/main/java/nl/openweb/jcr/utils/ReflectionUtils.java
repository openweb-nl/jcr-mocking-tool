/*
 * Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.openweb.jcr.utils;

import javax.jcr.Node;
import javax.jcr.Session;
import java.lang.reflect.Field;
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

    public static Session unwrapSessionDecorator(Session session) {
        Session result = session;
        if (session != null && "org.hippoecm.repository.impl.SessionDecorator".equals(session.getClass().getName())) {
            Class<?> clazz = session.getClass().getSuperclass();
            return (Session) getPrivateField(clazz, session, "session");
        }
        return result;
    }


    public static Node unwrapNodeDecorator(Node node) {
        Node result = node;
        if (node != null && "org.hippoecm.repository.impl.NodeDecorator".equals(node.getClass().getName())) {
            Class<?> clazz = node.getClass().getSuperclass();
            result = (Node) getPrivateField(clazz, node, "node");
        }
        return result;
    }

    private static Object getPrivateField(Class<?> clazz, Object object, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeReflectionException(e);
        }
    }
}
