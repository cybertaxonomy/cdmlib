/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import eu.etaxonomy.cdm.model.agent.Institution;
import org.apache.log4j.Logger;

/**
 * publisher is "institution" in BibTex ???
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:41
 */
public class Report extends PublicationBase {
	static Logger logger = Logger.getLogger(Report.class);

	private Institution institution;

	public Institution getInstitution(){
		return institution;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInstitution(Institution newVal){
		institution = newVal;
	}

}