package nl.openweb.jcr.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


import static org.junit.Assert.*;

/**
 * @author Ebrahim Aharpour
 * @since 9/2/2017
 */
public class ReflectionUtilsTest {
    public static final String MOCK_VALUE = "Test";
    private List<String> obj = new ArrayList<>();
    @Test
    public void getMethod() throws Exception {
        Method method = ReflectionUtils.getMethod(obj, "add", Object.class);
        assertNotNull(method);
        Method nonExitingMethod = ReflectionUtils.getMethod(obj, "nonExiting", String.class);
        assertNull(nonExitingMethod);
    }

    @Test
    public void invokeMethod() throws Exception {
        Method method = ReflectionUtils.getMethod(obj, "add", Object.class);
        ReflectionUtils.invokeMethod(method, obj, MOCK_VALUE);
        assertEquals(MOCK_VALUE, obj.get(0));
    }

    @Test(expected=RuntimeReflectionException.class)
    public void invokeMethodException() {
        TestClass testObj = new TestClass();
        Method method = ReflectionUtils.getMethod(testObj, "method");
        ReflectionUtils.invokeMethod(method, testObj);
    }

    static class TestClass {
        public  void method() {
            throw new IllegalArgumentException();
        }
    }


}