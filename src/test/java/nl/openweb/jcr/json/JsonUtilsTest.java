package nl.openweb.jcr.json;


import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import nl.openweb.jcr.domain.Node;

public class JsonUtilsTest {


    @Test
    public void test() throws Exception {
        URI uri = getClass().getClassLoader().getResource("node.json").toURI();
        String json = Files.readAllLines(Paths.get(uri)).stream().collect(Collectors.joining("\n"));
        Node node = JsonUtils.parseJson(json);
        Assert.assertEquals(json, JsonUtils.toJson(node).replaceAll("\\r\\n", "\n"));
    }

}