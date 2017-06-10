package nl.openweb.jcr.json;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import nl.openweb.jcr.domain.NodeBean;

public class JsonUtilsTest {


    @Test
    public void twoWayConversionTest() throws Exception {
        String json = loadJson("nodes.json");
        NodeBean node = JsonUtils.parseJson(json);
        Assert.assertEquals(json, JsonUtils.toJson(node).replaceAll("\\r\\n", "\n"));
    }

    private String loadJson(String file) throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource(file).toURI();
        return Files.readAllLines(Paths.get(uri)).stream().collect(Collectors.joining("\n"));
    }


}