/**
 * 
 */
package eu.etaxonomy.cdm.print.out.odf;

import java.io.File;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.transform.XSLTransformException;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.print.Transformator;
import eu.etaxonomy.cdm.print.out.PublishOutputModuleBase;

/**
 * This output module will transform the given document into <a href="http://www.oasis-open.org/committees/office/">ODF</a> format for further editing in 
 * <a href="http://www.openoffice.org/">OpenOffice</a> or any other tool that understands ODF.
 * 
 * @author n.hoffmann
 * @since Apr 20, 2010
 * @version 1.0
 */
public class OdfOutputModule extends PublishOutputModuleBase {
	private static final Logger logger = Logger
		.getLogger(OdfOutputModule.class);
	
	public static String STYLESHEET_RESOURCE_DEFAULT = "/stylesheets/odf/cdmToOdfText.xsl";
	
	private DocumentCreator documentCreator;
	private Transformator transformator;
	
	public OdfOutputModule() {
		InputStream xslt = getXsltInputStream();
		documentCreator = new DocumentCreator();
		try {
			transformator = new Transformator(xslt);	
		} 
		catch (XSLTransformException e) {
			logger.error("XSLTransformException while creating ODF output module", e);
		}
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.out.IPublishOutputModule#output(org.jdom.Document, java.io.File)
	 */
	public void output(Document document, File exportFolder, IProgressMonitor progressMonitor) {
		
		super.output(document, exportFolder, progressMonitor);
		
		Document transformedDocument;
		String filePath = getFilePath(exportFolder);
			
		try{
			transformedDocument = transformator.transform(document);
				
			OdfTextDocument odfTextDocument = documentCreator.create(transformedDocument);
							
			odfTextDocument.save(filePath);
			logger.warn("ODF output written to disk: " + filePath);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.out.IPublishOutputModule#getOutputFileSuffix()
	 */
	public String getOutputFileSuffix() {
		return "odf";
	}
	
	@Override
	protected InputStream getDefaultXsltInputStream() {
		return OdfOutputModule.class.getResourceAsStream(STYLESHEET_RESOURCE_DEFAULT);
	}
}
