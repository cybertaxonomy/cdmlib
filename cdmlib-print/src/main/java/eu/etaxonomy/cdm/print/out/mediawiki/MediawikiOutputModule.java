/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.print.out.mediawiki;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMSource;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.print.PublishConfigurator;
import eu.etaxonomy.cdm.print.out.PublishOutputModuleBase;


/**
 * @author l.morris, s.buers
 * @since Sep 11, 2013
 * 
 */
public class MediawikiOutputModule extends PublishOutputModuleBase {

	private static final Logger logger = Logger
			.getLogger(MediawikiOutputModule.class);

	public static String STYLESHEET_RESOURCE_DEFAULT = "src/main/resources/stylesheets/mediawiki/multipages.xsl";
	
	
	// default wiki - exportparameter
	public String prefix="";
	public String sourceUrl="";
	public String username="CDM Mediawiki Exporter"; 
	
	public MediawikiOutputModule(String mediaWikiPagePrefix, String sourceUrl){
		super();
		this.sourceUrl = sourceUrl;
		this.prefix=mediaWikiPagePrefix+":";
	}
	
	
	public MediawikiOutputModule(String sourceUrl) {
		super();
		this.sourceUrl = sourceUrl;
	}


	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public void output(Document document, File exportFolder,
			IProgressMonitor progressMonitor) {

		
		super.output(document, exportFolder, progressMonitor);
		
		
		
		try {

			// URL xslURL = new
			// URL("file:///C:/Users/l.morris/workspace/cdmlib/cdmlib-print/src/main/resources/stylesheets/mediawiki/multipages.xsl");
			URL xslURL = (new java.io.File(STYLESHEET_RESOURCE_DEFAULT))
					.toURI().toURL();

			String xslID = xslURL.toString();

			// Setup output
			String filePath = getFilePath(exportFolder);
			OutputStream out = new FileOutputStream(filePath);
			out = new java.io.BufferedOutputStream(out);

			// validate namespace
			
//			System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
//		             "net.sf.saxon.om.DocumentBuilderFactoryImpl");
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			//			factory.setValidating(true);
			DocumentBuilder builder = factory.newDocumentBuilder();

			org.w3c.dom.Document namespDocument = builder.parse(new File(
					STYLESHEET_RESOURCE_DEFAULT));


			// get StreamSource out of namespDocument

			DOMSource domSource = new DOMSource(namespDocument);
			StringWriter xmlAsWriter = new StringWriter();
			StreamResult domResult = new StreamResult(xmlAsWriter);
			TransformerFactory.newInstance().newTransformer()
			.transform(domSource, domResult);
			StringReader xmlReader = new StringReader(xmlAsWriter.toString());
			StreamSource inputSource = new StreamSource(xmlReader);

			try {

				// Setup XSLT
				TransformerFactory tfactory = new net.sf.saxon.TransformerFactoryImpl();
				Transformer transformer = tfactory.newTransformer(inputSource);// (xslt));

				// new
				// DOMSource source = new DOMSource(builder.parse(document));

//				 JDOMBuilder jdomBuilder= new JDOMBuilder();
//				 Document doc = domBuilder.build(document);

				JDOMSource source = new JDOMSource(document);
				Source xmlSource = new DOMSource();

				// Setup input for XSLT transformation
				// JDOMSource source = new JDOMSource(namespDocument);

				// Resulting SAX events (the generated FO) must be piped through
				// to FOP
				// Result result = new SAXResult(fop.getDefaultHandler());
				// Result result = new SAXResult();
				StreamResult result = new StreamResult(out);

				// Run XSLT transformation 
				transformer.setParameter("prefix", prefix);
				transformer.setParameter("username", username);
				transformer.setParameter("cdm-url", sourceUrl);
				transformer.transform(source, result);

				// get the wikitext of the generated file
				// use fr_jussieu_snv_lis.wiki.bot.WikiBot to upload the file
				// and images

			} finally {
				out.close();
//				logger.info("Mediawiki XML file created: " + filePath);
			}

		} catch (TransformerException te) {

			logger.error(te.getLocalizedMessage());
			logger.error(te.getCause());
			logger.error("TransformerException. Could not generate document",
					te);

		} catch (Exception e) {
			logger.error("Could not generate document", e);
		}

	}

	/**
	 * @return
	 */
	private static Document useExternalXMLSource() {
		SAXBuilder testbuilder = new SAXBuilder();
		  File xmlFile = new File("/home/sybille/development/mediawiki/kick_off/ericaceae_source.xml");
		  Document document1=null;
		try {
			document1 = (Document) testbuilder.build(xmlFile);
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return document1;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.cdm.print.out.IPublishOutputModule#getOutputFileSuffix()
	 */
	@Override
	public String getOutputFileSuffix() {
		return "xml";
	}

	public InputStream getDefaultXsltInputStream() {
		return MediawikiOutputModule.class
				.getResourceAsStream(STYLESHEET_RESOURCE_DEFAULT);
	}

	public static void main(String[] args) {

		MediawikiOutputModule outputModule = new MediawikiOutputModule("");
		PublishConfigurator configurator = PublishConfigurator
				.NewRemoteInstance();
		configurator.setExportFolder(new File("src/main/resources/tmp"));
		// try out my own xml:
				
		Document document = useExternalXMLSource();

		outputModule.output(document, configurator.getExportFolder(),
				configurator.getProgressMonitor());

	}

	// like this or change modifier in abstract superclass?
	public String generateFilenameWithDate(String name) {
		return super.generateFilenameWithDate(name);
		
	}

}
