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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import javax.security.auth.login.LoginException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
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
	
	public static String STYLESHEET_RESOURCE_DEFAULT = "/stylesheets/mediawiki/multipages.xsl";
	
	public void output(Document document, File exportFolder, IProgressMonitor progressMonitor) {
		
		super.output(document, exportFolder, progressMonitor);
		
		try{		
			
			URL xslURL = new URL("file:///C:/Users/l.morris/workspace/cdmlib/cdmlib-print/src/main/resources/stylesheets/mediawiki/multipages.xsl");


	         String xslID = xslURL.toString();
		
			// Setup output
			String filePath = getFilePath(exportFolder);
			OutputStream out = new FileOutputStream(filePath);
			out = new java.io.BufferedOutputStream(out);
		
			try{

	            // Setup XSLT
	            //InputStream xslt = getXsltInputStream();
	            TransformerFactory factory = TransformerFactory.newInstance();
	            Transformer transformer = factory.newTransformer(new StreamSource(xslID));//(xslt));


	            // Setup input for XSLT transformation
	            JDOMSource source = new JDOMSource(document);

	            // Resulting SAX events (the generated FO) must be piped through to FOP
	            //Result result = new SAXResult(fop.getDefaultHandler());
	            Result result = new SAXResult();
	          

	            // Start XSLT transformation and FOP processing
	            transformer.transform(source, result);
	            
	            //get the wikitext of the generated file
	            //use fr_jussieu_snv_lis.wiki.bot.WikiBot to upload the file and images
	            
	            
	            
            } finally {
                out.close();
                logger.info("PDF file created: " + filePath);
            }
			
		}catch (TransformerException te) {
			
			logger.error(te.getLocalizedMessage());
			logger.error(te.getCause());
			logger.error("TransformerException. Could not generate document", te);
			
		} catch (Exception e) {
			logger.error("Could not generate document", e);
		}
		
		
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.print.out.IPublishOutputModule#getOutputFileSuffix()
	 */
	@Override
	public String getOutputFileSuffix() {
		return "xml";
	}
	
	public InputStream getDefaultXsltInputStream(){
		return MediawikiOutputModule.class.getResourceAsStream(STYLESHEET_RESOURCE_DEFAULT);
	}
	
	
	public static void main(String[] args) {

		MediawikiOutputModule outputModule = new MediawikiOutputModule();
		PublishConfigurator configurator = PublishConfigurator.NewRemoteInstance();
		configurator.setExportFolder(new File("/Users/l.morris/Documents")); 
		Document document = new Document();
		
		outputModule.output(document, configurator.getExportFolder(), configurator.getProgressMonitor());

	}


}
