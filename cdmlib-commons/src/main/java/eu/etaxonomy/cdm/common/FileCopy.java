/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 20.11.2008
 * @version 1.0
 */
public class FileCopy {
	private static final Logger logger = Logger.getLogger(FileCopy.class);

	// overwrite constants
	public static final int DO_OVERWRITE = 1;
	public static final int NO_OVERWRITE = 2;

	// default values
	private static int bufferSize = 4 * 1024;
	private static int overwrite = DO_OVERWRITE;
		
	/**
	 * Copies a File to another directory 
	 * @param sourceFile
	 * @param destinationDirectory
	 * @param destFileName
	 * @return
	 * @throws IOException
	 */
	public static boolean copy(File sourceFile, File destinationDirectory, String destFileName)
			throws IOException {
		if (sourceFile == null){
			logger.debug("No sourcefile defined");
			throw new IOException("No sourcefile defined");
		}
		if (!sourceFile.isFile() || !sourceFile.canRead()) {
			logger.debug("Not a readable file: " + sourceFile.getName());
			throw new IOException("Not a readable file: " + sourceFile.getName());
		}
		if (destFileName == null || destFileName.equals("")){
			destFileName = sourceFile.getName();
		}
		InputStream in = new FileInputStream(sourceFile);
		copy(in, destinationDirectory, destFileName);
		
		if (!destinationDirectory.isDirectory()) {
			logger.warn("Not a directory: " + destinationDirectory.getName());
			return false;
		}
		File destinationFile = new File(destinationDirectory, destFileName);

		OutputStream out = new FileOutputStream(destinationFile);
		
		return copy(in, out);
	}
	
	public static boolean copy(InputStream in, File destinationDirectory, String destFileName)
			throws IOException {
		
		if (!destinationDirectory.isDirectory()) {
			throw new IOException("Destination is not a directory");
		}
		if (destFileName == null || destFileName.equals("")){
			throw new IOException("No destination file name specified");
		}
		File destinationFile = new File(destinationDirectory, destFileName);
		OutputStream out = new FileOutputStream(destinationFile);
		
		return copy(in, out);
}
	
	public static boolean copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[bufferSize];
		int bytesRead;
		while ((bytesRead = in.read(buffer)) >= 0) {
			out.write(buffer, 0, bytesRead);
		}
		out.close();
		in.close();
		logger.debug("File copied");
		return true;
	}

	
	public static boolean copy(String source, String destDirectory, String destFileName)
			throws IOException {
		File sourceFile = new File(source);
		File destinationDir = new File(destDirectory);
		return copy(sourceFile, destinationDir, destFileName);
	}
	
	public static boolean copy(String source, String destDirectory)
			throws IOException {
		return copy(source, destDirectory, null);
	}


	/**
	 * True if file 
	 * @param file File destination
	 * @return true if data can be copied, false otherwise
	 */
	public static boolean doCopy(File file) {
		boolean exists = file.exists();
		if (overwrite == DO_OVERWRITE || !exists) {
			return true;
		} else if (overwrite == NO_OVERWRITE) {
			return false;
		} else{
			return false;
		}
	}


}
	
	


