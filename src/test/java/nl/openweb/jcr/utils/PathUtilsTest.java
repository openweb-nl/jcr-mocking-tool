package nl.openweb.jcr.utils;

import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ebrahim Aharpour
 * @since 9/3/2017
 */
public class PathUtilsTest {
    @Test
    public void normalizePath() throws Exception {
        assertEquals("some/path", PathUtils.normalizePath("/some/path/"));
        assertEquals("some/path", PathUtils.normalizePath("/some/path"));
        assertEquals("some/path", PathUtils.normalizePath("some/path"));
        assertEquals("some/path", PathUtils.normalizePath("some/path/"));
        assertEquals("", PathUtils.normalizePath("//"));
        assertEquals("", PathUtils.normalizePath("/"));
        assertEquals("", PathUtils.normalizePath(""));
        assertNull(PathUtils.normalizePath(null));
    }

}