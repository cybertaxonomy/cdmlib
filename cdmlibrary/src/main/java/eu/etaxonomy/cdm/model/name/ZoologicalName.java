/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.name;


import org.apache.log4j.Logger;

/**
 * Taxon name class for animals
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:27
 */
public class ZoologicalName extends NonViralName {
	static Logger logger = Logger.getLogger(ZoologicalName.class);

	//Name of the breed of an animal
	@Description("Name of the breed of an animal")
	private String breed;

	public String getBreed(){
		return breed;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setBreed(String newVal){
		breed = newVal;
	}

	@Transient
	public String getFullAuthorship(){
		return "";
	}

}