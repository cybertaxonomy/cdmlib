/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.Language;
import org.apache.log4j.Logger;

/**
 * only valid for Taxa, not specimen/occurrences. Check DescriptionBase relation.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:05
 */
public class CommonTaxonName extends FeatureBase {
	static Logger logger = Logger.getLogger(CommonTaxonName.class);

	@Description("")
	private String name;
	private Language language;

	public Language getLanguage(){
		return language;
	}

	/**
	 * 
	 * @param language
	 */
	public void setLanguage(Language language){
		;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name){
		;
	}

}