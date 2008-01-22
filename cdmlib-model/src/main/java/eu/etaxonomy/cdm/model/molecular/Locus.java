/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * The region name of a DNA string. E.g. 18S, COX, etc.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:32
 */
@Entity
public class Locus extends VersionableEntity {
	static Logger logger = Logger.getLogger(Locus.class);
	private String name;
	private String description;
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

	public String getDescription(){
		return this.description;
	}

	/**
	 * 
	 * @param description    description
	 */
	public void setDescription(String description){
		this.description = description;
	}

}