/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.publication;


import org.apache.log4j.Logger;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:05
 */
public class Journal extends PublicationBase {
	static Logger logger = Logger.getLogger(Journal.class);

	private String issn;

	public String getIssn(){
		return issn;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setIssn(String newVal){
		issn = newVal;
	}

}