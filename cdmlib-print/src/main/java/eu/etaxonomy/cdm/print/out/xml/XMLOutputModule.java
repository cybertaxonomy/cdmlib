/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print.out.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.print.out.PublishOutputModuleBase;

/**
 * Simple output module that writes the harvested xml data to disk without further processing. 
 * 
 * @author n.hoffmann
 * @created Apr 8, 2010
 * @version 1.0
 */
public class XMLOutputModule extends PublishOutputModuleBase{
	private static final Logger logger = Logger
			.getLogger(XMLOutputModule.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IPublishOutputModule#output(org.jdom.Document)
	 */
	public void output(Document document, File exportFolder, IProgressMonitor progressMonitor) {
		
		super.output(document, exportFolder, progressMonitor);
		
		try {
			String filePath = getFilePath(exportFolder);
			FileOutputStream outputStream = new FileOutputStream(filePath);
			//XMLOutputter xmlOutputter = new XMLOutputter();
						
			Format format = Format.getPrettyFormat();
			format.setEncoding("UTF-8");
			XMLOutputter xmlOutputter = new XMLOutputter(format);			
			
			xmlOutputter.output(document, outputStream);
			logger.warn("XML output written to disk: " + filePath);
		} catch (FileNotFoundException e) {
			logger.error("A FileNotFoundException occured", e);
		} catch (IOException e) {
			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.out.IPublishOutputModule#getOutputFileSuffix()
	 */
	public String getOutputFileSuffix() {
		return "xml";
	}
}
