// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print.out;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.jdom.Document;

import eu.etaxonomy.cdm.common.IProgressMonitor;

/**
 * This abstract implementation of the {@link IPublishOutputModule} contains convenience methods for file 
 * path generation. Clients should consider extending this class.
 * 
 * @author n.hoffmann
 * @created Apr 20, 2010
 * @version 1.0
 */
public abstract class AbstractPublishOutputModule implements IPublishOutputModule {
	private static final Logger logger = Logger
			.getLogger(AbstractPublishOutputModule.class);
	
	/**
	 * The date format used by {@link #generateFilenameWithDate(String, String)}
	 */
	public static final String DATE_FORMAT_NOW = "yyyyMMdd-HHmm";
		
	/**
	 * Generates a string containing the current date followed by the given name.
	 * 
	 * @param name a string.
	 * @return a string containing the current date followed by the given name.
	 */
	protected String generateFilenameWithDate(String name){
		StringBuffer buffer = new StringBuffer();
		
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		buffer.append(sdf.format(cal.getTime()));
		
		buffer.append("-");
		
		buffer.append(name);//"xml_export"
		buffer.append("." + getOutputFileSuffix());
		
		return buffer.toString();
	}
	
	/**
	 * Generates the complete path to the output file based on the given output folder.
	 * 
	 * @param outputFolder the folder to store the output file in.
	 * @return a string containing the full path to the output file.
	 */
	public String getFilePath(File outputFolder) {
		return outputFolder.getPath() + File.separator + generateFilenameWithDate("output");
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.out.IPublishOutputModule#output(org.jdom.Document, java.io.File)
	 */
	public void output(Document document, File exportFolder, IProgressMonitor progressMonitor) {
		if(progressMonitor == null){
			throw new IllegalArgumentException("ProgressMonitor may not be null");
		}
		
		String message = "Running output module: " + this.getClass().getSimpleName();
		logger.trace(message);
		progressMonitor.subTask(message);
	}
}
