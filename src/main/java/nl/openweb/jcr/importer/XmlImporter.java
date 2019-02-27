package nl.openweb.jcr.importer;

import nl.openweb.jcr.JcrImporterException;
import nl.openweb.jcr.NodeBeanUtils;
import nl.openweb.jcr.domain.NodeBean;
import nl.openweb.jcr.domain.PropertyBean;

import javax.jcr.Node;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Ivor
 */
public class XmlImporter extends AbstractJcrImporter {
    public static final String FORMAT = "xml";
    private final JAXBContext jaxbContext;

    public XmlImporter(Node rootNode){
        super(rootNode);
        this.jaxbContext = createJxbContext();
    }

    private JAXBContext createJxbContext() {
        try {
            return JAXBContext.newInstance(NodeBean.class, PropertyBean.class);
        } catch (JAXBException e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }

    @Override
    public Node createNodes(String xml, String path, String intermediateNodeType) {
        return this.createNodes(new ByteArrayInputStream(xml.getBytes()), path, intermediateNodeType);
    }

    @Override
    public Node createNodes(InputStream inputStream, String path, String intermediateNodeType) {
        try {
            validate(inputStream);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Object unmarshaled = unmarshaller.unmarshal(inputStream);
            if (unmarshaled instanceof NodeBean) {
                return createNodeFromNodeBean(NodeBeanUtils.nodeBeanToMap((NodeBean) unmarshaled), path, intermediateNodeType);
            } else {
                throw new JcrImporterException("The given XML file is not of the right format");
            }
        } catch (JAXBException e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }

    private void validate(InputStream inputStream) {
        if (inputStream == null) {
            throw new JcrImporterException("InputSteam may not be null.");
        }
    }
}
