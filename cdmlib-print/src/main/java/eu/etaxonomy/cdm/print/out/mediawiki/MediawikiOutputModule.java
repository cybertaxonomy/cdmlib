// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.print.out.mediawiki;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.security.auth.login.LoginException;
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
import org.jdom.transform.JDOMSource;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.print.PublishConfigurator;
import eu.etaxonomy.cdm.print.out.PublishOutputModuleBase;


/**
 * @author l.morris
 * @date Sep 11, 2013
 * 
 */
public class MediawikiOutputModule extends PublishOutputModuleBase {

	private static final Logger logger = Logger
			.getLogger(MediawikiOutputModule.class);

	public static String STYLESHEET_RESOURCE_DEFAULT = "src/main/resources/stylesheets/mediawiki/multipages.xsl";

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


			// get StreamSource out of namespDocument!!!

			DOMSource domSource = new DOMSource(namespDocument);
			StringWriter xmlAsWriter = new StringWriter();
			StreamResult domResult = new StreamResult(xmlAsWriter);
			TransformerFactory.newInstance().newTransformer()
			.transform(domSource, domResult);
			StringReader xmlReader = new StringReader(xmlAsWriter.toString());
			StreamSource inputSource = new StreamSource(xmlReader);

			try {

				// Setup XSLT
				// InputStream xslt = getXsltInputStream();
//				TransformerFactory tfactory = TransformerFactory.newInstance();
				TransformerFactory tfactory = new net.sf.saxon.TransformerFactoryImpl();
				Transformer transformer = tfactory.newTransformer(inputSource);// (xslt));

				// new
				// DOMSource source = new DOMSource(builder.parse(document));

				// JDOMBuilder jdomBuilder = new JDOMBuilder();
				// Document doc = domBuilder.build(document);

				JDOMSource source = new JDOMSource(document);
				Source xmlSource = new DOMSource();

				// Setup input for XSLT transformation
				// JDOMSource source = new JDOMSource(namespDocument);

				// Resulting SAX events (the generated FO) must be piped through
				// to FOP
				// Result result = new SAXResult(fop.getDefaultHandler());
				// Result result = new SAXResult();
				StreamResult result = new StreamResult(out);

				// Start XSLT transformation and FOP processing
				transformer.transform(source, result);

				// get the wikitext of the generated file
				// use fr_jussieu_snv_lis.wiki.bot.WikiBot to upload the file
				// and images

			} finally {
				out.close();
				logger.info("Mediawiki XML file created: " + filePath);
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

	/*
	 * @author l.morris
	 */
	private void uploadToMediwiki() {

		// export via API
		String urlWiki = "http://biowikifarm.net/testwiki";
		String loginWiki = "Lorna Morris";
		boolean uploadImagesWiki = true;

		WikiBot myBot = new WikiBot(urlWiki, loginWiki, "dolfin_69");

		try {

			//myBot.login();
			if (!myBot.login()) {
				System.out.println("Login failed");
				return;
			}

			//myBot.edit("lornatest", "lorna text", "lorna summary");

			FileInputStream fstream = new FileInputStream("C:\\Users\\l.morris\\Documents\\prin_pub_test2\\Mediwiki7.xml");

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String wikitext = "";
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				//System.out.println (strLine);
				wikitext = wikitext + strLine;
			}
			//Close the input stream
			in.close();	 

			URL url = new URL("http://media.e-taxonomy.eu/palmae/photos/palm_tc_14566_1.jpg");
			

			//File file = new File("C:\\Users\\l.morris\\Documents\\prin_pub_test2\\palm_tc_14568_4.jpg");//palm_tc_14494_1.jpg //mediwiki8.xml");
			File file2 = new File("C:\\Users\\l.morris\\Documents\\prin_pub_test2\\Mediwiki7.xml");

			// this works but all the XML metadata is added to a single page, we want to import the XML file.
			// this one works
			//TODO: Try to call edit() for each page from XML file 
			//myBot.edit("lornatest2", wikitext, "lorna summary"); 
			myBot.importPages("Mediwiki7b.xml", file2, "lorna summary");//problems
			//myBot.importPages("Internal:Cichorium", file2, "lorna summary");

			//this uploadAFile works to upload an image
			//you need to have the image e.g. palm_tc_14566_1.jpg
			//so http://media.e-taxonomy.eu/palmae/photos/palm_tc_14566_1.jpg doesn't work
			File file = new File("http://media.e-taxonomy.eu/palmae/photos/palm_tc_14566_1.jpg");
			//File file = new File(url.toURI());
			myBot.uploadAFile(file, "palm_tc_14566_1.jpg", "my text", "my comment");

			//need to get a list of image paths. Xper2 use Base.getAllResources to get these.
			//myBot.uploadAFile(file, filename, text, comment);
			//List<WikiPage>
			//create Wiki page - name, content, comments
			// e.g. new WikiPage('taxonName', wikitext, comments)

			/*for (int i = 0; i < listOfpages.size(); i++) {

					if (myBot.importPage(listOfpages.get(i))) {
						pagesOK++;
					} else {
						pagesKO++;
					}
			}*/

		} catch (LoginException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			return;
		} 

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

		MediawikiOutputModule outputModule = new MediawikiOutputModule();
		PublishConfigurator configurator = PublishConfigurator
				.NewRemoteInstance();
		configurator.setExportFolder(new File("src/main/resources/tmp"));
		Document document = new Document();

		outputModule.output(document, configurator.getExportFolder(),
				configurator.getProgressMonitor());

	}

}
