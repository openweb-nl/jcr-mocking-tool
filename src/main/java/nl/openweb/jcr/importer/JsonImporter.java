package nl.openweb.jcr.importer;

import nl.openweb.jcr.JcrImporterException;
import nl.openweb.jcr.NodeBeanUtils;
import nl.openweb.jcr.domain.NodeBean;
import nl.openweb.jcr.json.JsonUtils;

import javax.jcr.Node;
import java.io.IOException;
import java.io.InputStream;

/**
 * Importer for JSON files
 * @author Ivor Boers
 */
public class JsonImporter extends AbstractJcrImporter {
    public static final String FORMAT = "json";

    public JsonImporter(Node rootNode) {
        super(rootNode);
    }

    @Override
    public Node createNodes(String json, String path, String intermediateNodeType) {
        try {
            return createNodeFromNodeBean(JsonUtils.parseJsonMap(json), path, intermediateNodeType);
        } catch (IOException e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }

    @Override
    public Node createNodes(InputStream inputStream, String path, String intermediateNodeType) {
        try {
            validate(inputStream);
            NodeBean json = JsonUtils.parseJson(inputStream);
            return createNodeFromNodeBean(NodeBeanUtils.nodeBeanToMap(json), path, intermediateNodeType);
        } catch (IOException e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }

    private void validate(InputStream inputStream) {
        if (inputStream == null) {
            throw new JcrImporterException("InputSteam may not be null.");
        }
    }
}
