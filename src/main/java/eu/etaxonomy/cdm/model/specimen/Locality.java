/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.specimen;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:06
 */
@Entity
public class Locality extends VersionableEntity {
	static Logger logger = Logger.getLogger(Locality.class);

	private String name;
	private Region region;

	public String getName(){
		return name;
	}

	public Region getRegion(){
		return region;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(String newVal){
		name = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRegion(Region newVal){
		region = newVal;
	}

}