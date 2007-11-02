/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

/**
 * This class aims to make available some "flags" for identifiable entities in a
 * flexible way. Application developers (and even users) can define their own
 * "flags" as a MarkerType.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:26
 */
public class Marker extends VersionableEntity {
	static Logger logger = Logger.getLogger(Marker.class);

	@Description("")
	private boolean flag;
	private MarkerType type;

	public MarkerType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(MarkerType newVal){
		type = newVal;
	}

	public boolean getFlag(){
		return flag;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFlag(boolean newVal){
		flag = newVal;
	}

}