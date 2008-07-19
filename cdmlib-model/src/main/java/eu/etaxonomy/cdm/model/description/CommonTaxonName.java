/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;

/**
 * only valid for Taxa, not specimen/occurrences. Check DescriptionBase relation.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:17
 */
@Entity
public class CommonTaxonName extends DescriptionElementBase {
	static Logger logger = Logger.getLogger(CommonTaxonName.class);
	
	private String name;
	private Language language;

	/**
	 * Factory method. Sets the feature (type) of this description element to COMMON_NAME
	 * and sets the name and language
	 * @param name
	 * @param language
	 * @return
	 */
	public static CommonTaxonName NewInstance(String name, Language language){
		CommonTaxonName result = new CommonTaxonName();
		result.setName(name);
		result.setLanguage(language);
		return result;
	}
	
	protected CommonTaxonName(){
		super(Feature.COMMON_NAME());
	}
	
	
	
	
	/** Deprecated because feature should always be <code>COMMON_NAME</code> for class CommonTaxonName
	*/
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#setFeature(eu.etaxonomy.cdm.model.description.Feature)
	 */
	@Override
	@Deprecated 
	public void setFeature(Feature feature) {
		super.setFeature(feature);
	}

	@ManyToOne
	public Language getLanguage(){
		return this.language;
	}
	public void setLanguage(Language language){
		this.language = language;
	}

	/**
	 * @return
	 */
	public String getName(){
		return this.name;
	}

	/**
	 * 
	 * @param name    name
	 */
	public void setName(String name){
		this.name = name;
	}

}