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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * The region name of a DNA string. E.g. 18S, COX, etc.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:14
 */
@Entity
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
	 * @param name
	 */
	public void setName(String name){
		;
	}

	public String getDescription(){
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description){
		;
	}

}