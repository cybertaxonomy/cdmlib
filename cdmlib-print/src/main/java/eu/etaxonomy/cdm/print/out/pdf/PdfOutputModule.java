/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print.out.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.transform.JDOMSource;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.print.out.PublishOutputModuleBase;

/**
 * This output module will create a <a href="http://www.adobe.com/products/acrobat/adobepdf.html">PDF</a> document 
 * with a predefined output for quick printing.
 * 
 * @author n.hoffmann
 * @since Jul 20, 2010
 * @version 1.0
 */
public class PdfOutputModule extends PublishOutputModuleBase {
	private static final Logger logger = Logger
			.getLogger(PdfOutputModule.class);
	
	public static String STYLESHEET_RESOURCE_DEFAULT = "/stylesheets/pdf/cdmToPdf.xsl";
		
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.out.IPublishOutputModule#output(org.jdom.Document, java.io.File, eu.etaxonomy.printpublisher.NotificationMediator)
	 */
	public void output(Document document, File exportFolder, IProgressMonitor progressMonitor) {
		
		super.output(document, exportFolder, progressMonitor);
		
		try{
		
			 // configure fopFactory as desired
			FopFactory fopFactory = FopFactory.newInstance();
				
			FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
			 // configure foUserAgent as desired
		
			// Setup output
			String filePath = getFilePath(exportFolder);
			OutputStream out = new FileOutputStream(filePath);
			out = new java.io.BufferedOutputStream(out);
		
			try{
			
				// Construct fop with desired output format
	            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

	            // Setup XSLT
	            InputStream xslt = getXsltInputStream();
	            TransformerFactory factory = TransformerFactory.newInstance();
	            Transformer transformer = factory.newTransformer(new StreamSource(xslt));

	            // Setup input for XSLT transformation
	            JDOMSource source = new JDOMSource(document);

	            // Resulting SAX events (the generated FO) must be piped through to FOP
	            Result result = new SAXResult(fop.getDefaultHandler());

	            // Start XSLT transformation and FOP processing
	            transformer.transform(source, result);
            } finally {
                out.close();
                logger.info("PDF file created: " + filePath);
            }
			
		}catch (Exception e) {
			logger.error("Could not generate document", e);
		}
	}

	public String getOutputFileSuffix() {
		return "pdf";
	}
	
	public InputStream getDefaultXsltInputStream(){
		return PdfOutputModule.class.getResourceAsStream(STYLESHEET_RESOURCE_DEFAULT);
	}
}
