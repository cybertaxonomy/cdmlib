/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.molecular;


import etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

/**
 * The region name of a DNA string. E.g. 18S, COX, etc.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:58
 */
public class Locus extends VersionableEntity {
	static Logger logger = Logger.getLogger(Locus.class);

	@Description("")
	private String name;
	@Description("")
	private String description;

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(String newVal){
		name = newVal;
	}

	public String getDescription(){
		return description;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDescription(String newVal){
		description = newVal;
	}

}