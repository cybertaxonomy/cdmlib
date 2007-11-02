/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import org.apache.log4j.Logger;

/**
 * Taxon name class for bacteria
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:07
 */
public class BacterialName extends NonViralName {
	static Logger logger = Logger.getLogger(BacterialName.class);

	//Author team and year of the subgenus name
	@Description("Author team and year of the subgenus name")
	private String subGenusAuthorship;
	//Approbation of name according to approved list, validation list,or validly published, paper in IJSB after 1980
	@Description("Approbation of name according to approved list, validation list,or validly published, paper in IJSB after 1980")
	private String nameApprobation;

	public String getSubGenusAuthorship(){
		return subGenusAuthorship;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSubGenusAuthorship(String newVal){
		subGenusAuthorship = newVal;
	}

	public String getNameApprobation(){
		return nameApprobation;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNameApprobation(String newVal){
		nameApprobation = newVal;
	}

}