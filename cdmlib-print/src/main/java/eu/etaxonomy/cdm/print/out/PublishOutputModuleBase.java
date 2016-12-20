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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

/**
 * This abstract implementation of the {@link IPublishOutputModule} contains
 * convenience methods for file path generation. Clients should consider
 * extending this class.
 * 
 * @author n.hoffmann
 * @created Apr 20, 2010
 * @version 1.0
 */
public abstract class PublishOutputModuleBase implements IPublishOutputModule {
	private static final Logger logger = Logger
			.getLogger(PublishOutputModuleBase.class);

	/**
	 * The date format used by {@link #generateFilenameWithDate(String, String)}
	 */
	public static final String DATE_FORMAT_NOW = "yyyyMMdd-HHmm";

	private FilenameFilter filter = new FilenameFilter() {

		@Override
		public boolean accept(File dir, String fileName) {
			return fileName.substring(fileName.length() - 3).equals("xsl");
		}
	};

	private File xslt;
	private String filePath = null;
	private Document inputDocument=null;

	/**
	 * Generates a string containing the current date followed by the given
	 * name.
	 * 
	 * @param name
	 *            a string.
	 * @return a string containing the current date followed by the given name.
	 */
	protected String generateFilenameWithDate(String name) {
		StringBuffer buffer = new StringBuffer();

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		buffer.append(sdf.format(cal.getTime()));

		buffer.append("-");

		buffer.append(name);// "xml_export"
		buffer.append("." + getOutputFileSuffix());

		return buffer.toString();
	}

	/**
	 * return the complete path to the output file based on the given output
	 * folder. if it was not created before getNewFilePath() is called to create
	 * it
	 * @param outputFolder
	 *            the folder to store the output file in.
	 * @return a string containing the full path to the output file.
	 */
	public String getFilePath(File outputFolder) {
		/*
		 * @author s.buers: changed to: the filepath is only created once
		 */
		if (filePath == null) {
			filePath = getNewFilePath(outputFolder);
		} 
			return filePath;
	}

	/**
	 * @return the complete path to the output file 
	 * @author s.buers
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * creates new name for the output file based on the given output
	 * folder and actual timestamp. 
	 * @author s.buers
	 * @param outputFolder
	 * @return
	 */
	public String getNewFilePath(File outputFolder) {
		return outputFolder.getPath() + File.separator
				+ generateFilenameWithDate("output");
	}

	public Document getInputDocument() {
		return inputDocument;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.out.IPublishOutputModule#output(org.jdom.
	 * Document, java.io.File)
	 */
	public void output(Document document, File exportFolder,
			IProgressMonitor progressMonitor) {
		if (progressMonitor == null) {
			throw new IllegalArgumentException(
					"ProgressMonitor may not be null");
		}
		inputDocument=document;
		String message = "Running output module: "
				+ this.getClass().getSimpleName();
		logger.trace(message);
		progressMonitor.subTask(message);
	}

	@Override
	public List<File> getStylesheets() throws IOException {
		List<File> stylesheets = new ArrayList<File>();

		for (File directory : getStylesheetLocations()) {
			if (directory.exists() && directory.isDirectory()) {
				stylesheets.addAll(getStylesheetsByLocation(directory));
			} else {
				logger.info(String
						.format("Tried to read styleshets from '%s', but it does not exist or is not a directory",
								directory));
			}
		}

		return stylesheets;

	}

	private List<File> getStylesheetLocations() throws IOException {
		List<File> locationList = new ArrayList<File>();

		String l = File.separator;

		URL shippedStylesheetsResource = PublishOutputModuleBase.class
				.getResource("/stylesheets/pdf/");
		File shippedStylesheetsDir = new File(
				shippedStylesheetsResource.getFile());
		locationList.add(shippedStylesheetsDir);

		// TODO this should be configured in a central place, see #2387
		File userdir = new File(CdmUtils.perUserCdmFolder + l + "stylesheets"
				+ l + getOutputFileSuffix());
		locationList.add(userdir);

		return locationList;
	}

	@Override
	public File getXslt() {
		return xslt;
	}

	@Override
	public void setXslt(File xslt) {
		this.xslt = xslt;
	}

	public InputStream getXsltInputStream() {
		if (getXslt() == null) {
			return getDefaultXsltInputStream();
		}

		try {
			return new FileInputStream(getXslt());
		} catch (FileNotFoundException e) {
			logger.error(e);
		}
		return null;
	}

	protected InputStream getDefaultXsltInputStream() {
		return null;
	}

	public List<File> getStylesheetsByLocation(File stylesheetFolder) {

		File[] stylesheets = stylesheetFolder.listFiles(filter);

		return Arrays.asList(stylesheets);
	}
}
