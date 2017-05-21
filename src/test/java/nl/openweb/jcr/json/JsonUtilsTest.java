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
import nl.openweb.jcr.domain.PropertyBean;

public class JsonUtilsTest {


    @Test
    public void twoWayConversionTest() throws Exception {
        String json = loadJson("node.json");
        NodeBean node = JsonUtils.parseJson(json);
        Assert.assertEquals(json, JsonUtils.toJson(node).replaceAll("\\r\\n", "\n"));
    }

    @Test
    public void sameNameSiblingsTests() throws IOException, URISyntaxException {
        String json = loadJson("samplenode.json");
        NodeBean node = JsonUtils.parseJson(json);
        NodeBean sameNameSibling1 = (NodeBean) node.getNodeOrProperty().get(9);
        Assert.assertEquals("subnode", sameNameSibling1.getName());
        Assert.assertEquals("stringValue1", ((PropertyBean) sameNameSibling1.getNodeOrProperty().get(1)).getValue().get(0));
        NodeBean sameNameSibling2 = (NodeBean) node.getNodeOrProperty().get(10);
        Assert.assertEquals("subnode", sameNameSibling2.getName());
        Assert.assertEquals("stringValue2", ((PropertyBean) sameNameSibling2.getNodeOrProperty().get(1)).getValue().get(0));
        PropertyBean multivalueDate = (PropertyBean) node.getNodeOrProperty().get(8);
        Assert.assertEquals("Date", multivalueDate.getType());
        Assert.assertTrue(multivalueDate.isMultiple());
        Assert.assertArrayEquals(new String[]{"2016-11-03T10:06:35.101+01:00", "2017-05-20T8:28:35.101+01:00"},
                multivalueDate.getValue().toArray(new String[multivalueDate.getValue().size()]));
        Assert.assertEquals(json, JsonUtils.toJson(node).replaceAll("\\r\\n", "\n"));
    }

    private String loadJson(String file) throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource(file).toURI();
        return Files.readAllLines(Paths.get(uri)).stream().collect(Collectors.joining("\n"));
    }


}