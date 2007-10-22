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
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:04
 */
@MappedSuperclass
public abstract class IdentifiableEntity extends VersionableEntity {
	static Logger logger = Logger.getLogger(IdentifiableEntity.class);

	private String lsid;

	public String getLsid(){
		return lsid;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLsid(String newVal){
		lsid = newVal;
	}

}