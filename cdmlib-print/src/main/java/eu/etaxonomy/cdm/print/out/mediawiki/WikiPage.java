/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.print.out.mediawiki;

/**
 * @author l.morris
 * @since Sep 19, 2013
 *
 */
public class WikiPage {

	// default object params
	protected String title;
	protected String text;
	protected String summary;

	/**
	 * Default class constructor
	 * 
	 * @param title
	 *            page title
	 * @param text
	 *            page text
	 * @param summary
	 *            page summary
	 */
	public WikiPage(String title, String text, String summary) {
		this.title = title;
		this.text = text;
		this.summary = summary;
	}
}
