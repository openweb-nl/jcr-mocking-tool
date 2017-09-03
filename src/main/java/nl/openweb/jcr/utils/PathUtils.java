package nl.openweb.jcr.utils;

/**
 * @author Ebrahim Aharpour
 * @since 9/3/2017
 */
public class PathUtils {

    private PathUtils() {
        // to prevent initialization
    }

    public static String normalizePath(String path) {
        String result = path;
        if (path != null) {
            result = path.substring(path.startsWith("/") ? 1 : 0,
                    path.endsWith("/") && path.length() > 1 ? path.length() - 1 : path.length());
        }
        return result;
    }
}
