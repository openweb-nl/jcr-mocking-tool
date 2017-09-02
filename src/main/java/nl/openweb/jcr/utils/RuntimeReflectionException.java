package nl.openweb.jcr.utils;

/**
 * @author Ebrahim Aharpour
 * @since 9/2/2017
 */
public class RuntimeReflectionException extends RuntimeException {

    public RuntimeReflectionException(Exception e) {
        super(e.getMessage(), e);
    }
}
