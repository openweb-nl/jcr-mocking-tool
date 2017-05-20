package nl.openweb.jcr.json;

import org.junit.Assert;
import org.junit.Test;

import nl.openweb.jcr.domain.Node;

/**
 * Created by Ebrahim on 5/20/2017.
 */
public class JsonUtilsTest {
    @Test
    public void toJson() throws Exception {

    }

    @Test
    public void parseJson() throws Exception {
        String json = "{\"hipposys:passkey\":\"jvm://\",\"hipposys:active\":true,\"hipposys:system\":true,\"hipposys:securityprovider\":\"internal\",\"jcr:primaryType\":\"hipposys:user\",\"subnode\":{\"test\":\"something\",\"booleanPropertiy\":false}}";
        Node node = JsonUtils.parseJson(json);
        Assert.assertEquals(json, JsonUtils.toJson(node));

    }

}