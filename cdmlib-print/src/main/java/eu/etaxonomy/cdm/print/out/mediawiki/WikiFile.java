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



/**
 * @author n.paule
 * @date Sep 19, 2013
 *
 */
public class WikiFile {

	protected File file;
	protected String fileName;
	protected String text;
	protected String comment;

	/**
	 * @param file
	 *            the file
	 */
	public WikiFile(File file) {
		this.file = file;
		this.fileName = file.getName();
		this.text = "";
	}

	public WikiFile(File file, String fileName, String text, String comment) {
		this.file = file;
		this.fileName = fileName;
		this.text = text;
		this.comment = comment;
	}
	
}
