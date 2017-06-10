/*
 * Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.openweb.jcr.domain;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the nl.openweb.hippo.domain package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Value_QNAME = new QName("http://www.jcp.org/jcr/sv/1.0", "value");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: nl.openweb.hippo.domain
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link NodeBean }
     * 
     */
    public NodeBean createNode() {
        return new NodeBean();
    }

    /**
     * Create an instance of {@link PropertyBean }
     * 
     */
    public PropertyBean createProperty() {
        return new PropertyBean();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.jcp.org/jcr/sv/1.0", name = "value")
    public JAXBElement<String> createValue(String value) {
        return new JAXBElement<>(_Value_QNAME, String.class, null, value);
    }

}
