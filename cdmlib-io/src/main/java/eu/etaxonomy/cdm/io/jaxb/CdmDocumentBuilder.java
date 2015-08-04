/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.jaxb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.UncategorizedMappingException;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import eu.etaxonomy.cdm.jaxb.CdmNamespacePrefixMapper;
import eu.etaxonomy.cdm.jaxb.FormattedText;
import eu.etaxonomy.cdm.jaxb.MultilanguageTextElement;

/**
 * Initializes a JaxbContext with one class (eu.etaxonomy.cdm.model.DataSet).
 *
 * @author a.babadshanjan, ben.clark
 */
//Binds it to XML schemas found in /src/main/resources/schema/cdm (cdm.xsd, common.xsd, name.xsd).
//There is a bit of magic with a resource resolver in eu.etaxonomy.cdm.io.jaxb
//which allows to package the schemas into a jar file.
public class CdmDocumentBuilder extends Jaxb2Marshaller  {

    private static final Logger logger = Logger.getLogger(CdmDocumentBuilder.class);
    private boolean formattedOutput = Boolean.TRUE;
    private String encoding = "UTF-8";

    public static String CDM_NAMESPACE = "eu.etaxonomy.cdm.model";
    public static String[] CDM_SCHEMA_FILES = { "/schema/cdm/agent.xsd",
        "/schema/cdm/cdm.xsd",
        "/schema/cdm/common.xsd",
        "/schema/cdm/description.xsd",
        "/schema/cdm/location.xsd",
        "/schema/cdm/media.xsd",
        "/schema/cdm/molecular.xsd",
        "/schema/cdm/name.xsd",
        "/schema/cdm/occurrence.xsd",
        "/schema/cdm/reference.xsd",
    "/schema/cdm/taxon.xsd"};
    public static Class[] CONTEXT_CLASSES = {DataSet.class,FormattedText.class,MultilanguageTextElement.class};

    private Resource schemas[];

    protected String[] getSchemaFiles() {
        return CDM_SCHEMA_FILES;
    }

    protected Class[] getContextClasses() {
        return CONTEXT_CLASSES;
    }

    public CdmDocumentBuilder()	{
        schemas = new Resource[CDM_SCHEMA_FILES.length];

        for(int i = 0; i < CDM_SCHEMA_FILES.length; i++) {
            schemas[i] = new ClassPathResource(CDM_SCHEMA_FILES[i]);
        }

        super.setSchemas(schemas);
        super.setClassesToBeBound(CONTEXT_CLASSES);
        super.setSchemaLanguage("http://www.w3.org/2001/XMLSchema");
    }

    public CdmDocumentBuilder(boolean formattedOutput, String encoding) {
        this.formattedOutput = formattedOutput;
        this.encoding = encoding;
    }


    @Override
    protected void initJaxbMarshaller(Marshaller marshaller) throws JAXBException {
        try {
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CdmNamespacePrefixMapper() );

            // For test purposes insert newlines to make the XML output readable
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
            //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,"http://etaxonomy.eu/cdm/model/1.0 schema/cdm/cdm.xsd");
            //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,"http://etaxonomy.eu/cdm/model/1.0 cdm.xsd");
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);

            CdmMarshallerListener marshallerListener = new CdmMarshallerListener();
            marshaller.setListener(marshallerListener);
            marshaller.setEventHandler(new WarningTolerantValidationEventHandler());
        } catch(PropertyException pe) {
            throw new JAXBException(pe.getMessage(),pe);
        }
    }

    protected <T> T unmarshal(Class<T> clazz, InputSource input) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        Schema schema;
        try {
            schema = createSchema();
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setXIncludeAware(true);
            saxParserFactory.setValidating(true);
            saxParserFactory.setSchema(schema);

            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setEntityResolver(new CatalogResolver());
            xmlReader.setErrorHandler(new DefaultErrorHandler());
            SAXSource saxSource = new SAXSource( xmlReader, input);
            saxSource.setSystemId(input.getSystemId());

            return (T)super.unmarshal(saxSource);
        } catch (IOException e) {
            throw new UncategorizedMappingException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new UncategorizedMappingException(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new UncategorizedMappingException(e.getMessage(), e);
        }
    }

    private Schema createSchema() throws SAXException, IOException {
        //method created to avoid dependency of spring-xml like in earlier versions
        //old implementation was schema = SchemaLoaderUtils.loadSchema(schemas, "http://www.w3.org/2001/XMLSchema");
        //maybe we can improve this loading in future

        String schemaLanguage = "http://www.w3.org/2001/XMLSchema";
        Source[] schemaSources = new Source[schemas.length];
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        for (int i = 0; i < schemas.length; i++) {
            schemaSources[i] = makeSchemaSource(reader, schemas[i]);//  new ResourceSource(reader, schemas[i]);
        }
        SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
        return schemaFactory.newSchema(schemaSources);
    }

    private Source makeSchemaSource(XMLReader reader, Resource resource) throws IOException {
        Source result = new SAXSource(reader, createInputSource(resource));
        return result;
    }

    private static InputSource createInputSource(Resource resource) throws IOException {
        InputSource inputSource = new InputSource(resource.getInputStream());
        inputSource.setSystemId(getSystemId(resource));
        return inputSource;
    }

    /** Retrieves the URL from the given resource as System ID. Returns <code>null</code> if it cannot be opened. */
    private static String getSystemId(Resource resource) {
        try {
            return new URI(resource.getURL().toExternalForm()).toString();
        }
        catch (IOException ex) {
            logger.debug("Could not get System ID from [" + resource + "], ex");
            return null;
        }
        catch (URISyntaxException e) {
            logger.debug("Could not get System ID from [" + resource + "], ex");
            return null;
        }
    }


    public <T> T unmarshal(Class<T> clazz,Reader reader) throws XmlMappingException {
        InputSource source = new InputSource(reader);
        return unmarshal(clazz,source);
    }

    public <T> T unmarshal(Class<T> clazz,Reader reader, String systemId) throws XmlMappingException {
        InputSource input = new InputSource(reader);
        input.setSystemId(systemId);
        return unmarshal(clazz,input);
    }

    public <T> T unmarshal(Class<T> clazz, File file) throws XmlMappingException {

        InputSource input;
        try {
            input = new InputSource(new InputStreamReader(new FileInputStream(file),encoding));
            return unmarshal(clazz,input);
        } catch (UnsupportedEncodingException e) {
            throw new UncategorizedMappingException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new UncategorizedMappingException(e.getMessage(), e);
        }

    }

    public void marshal(DataSet dataSet, Writer writer) throws XmlMappingException {
        logger.info("Start marshalling");
        super.marshal(dataSet, new StreamResult(writer));
    }

    public void marshal(DataSet dataSet, StreamResult result) throws XmlMappingException {
        logger.info("Start marshalling");
        super.marshal(dataSet, result);
    }

    public void marshal(DataSet dataSet, SAXResult result) throws XmlMappingException {
        logger.info("Start marshalling");
        super.marshal(dataSet, result);
    }

}


