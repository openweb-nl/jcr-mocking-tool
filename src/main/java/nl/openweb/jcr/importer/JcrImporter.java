package nl.openweb.jcr.importer;

import javax.jcr.Node;
import java.io.InputStream;


/**
 * Imports JCR nodes from an outside source
 * @author Ivor Boers
 */
public interface JcrImporter {
    String JCR_PRIMARY_TYPE = "jcr:primaryType";
    String JCR_MIXIN_TYPES = "jcr:mixinTypes";
    String JCR_UUID = "jcr:uuid";

    JcrImporter addMixins(boolean addMixins);

    JcrImporter addUuid(boolean addUuid);

    JcrImporter setProtectedProperties(boolean setProtectedProperties);

    JcrImporter saveSession(boolean saveSession);

    JcrImporter addUnknownTypes(boolean addUnknownTypes);

    Node getRootNode();

    /**
     * Create nodes from source on the root
     * @param source the textual representation of the nodes
     * @return the created (top)node
     */
    Node createNodes(String source);

    /**
     * Create nodes from source at a specified path without a specified intermediate nodetype
     * @param source the textual representation of the nodes
     * @param path the path where the topnode should be imported
     * @return the created (top)node
     */
    Node createNodes(String source, String path);

    /**
     *
     * @param source the textual representation of the nodes
     * @param path the path where the topnode should be imported
     * @param intermediateNodeType the type of the nodes between the exisiting path and the path where to create the nodes
     * @return the created (top)node
     */
    Node createNodes(String source, String path, String intermediateNodeType);

    /**
     * Create nodes from source on the root
     * @param inputStream the stream representation of the nodes
     * @return the created (top)node
     */
    Node createNodes(InputStream inputStream);

    /**
     * Create nodes from source at a specified path without a specified intermediate nodetype
     * @param inputStream the stream representation of the nodes
     * @param path the path where the topnode should be imported
     * @return the created (top)node
     */
    Node createNodes(InputStream inputStream, String path);

    /**
     *
     * @param inputStream the stream representation of the nodes
     * @param path the path where the topnode should be imported
     * @param intermediateNodeType the type of the nodes between the exisiting path and the path where to create the nodes
     * @return the created (top)node
     */
    Node createNodes(InputStream inputStream, String path, String intermediateNodeType);

}
