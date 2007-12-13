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

import eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy;
import eu.etaxonomy.cdm.strategy.ZooNameCacheStrategy;

import java.util.*;
import javax.persistence.*;

/**
 * Taxon name class for animals
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:03
 */
@Entity
public class ZoologicalName extends NonViralName {
	static Logger logger = Logger.getLogger(ZoologicalName.class);

	//Name of the breed of an animal
	private String breed;

	private ZoologicalName() {
		this.cacheStrategy = ZooNameCacheStrategy.NewInstance();
	}
	
	public ZoologicalName(Rank rank) {
		super(rank);
		this.cacheStrategy = ZooNameCacheStrategy.NewInstance();
	}

	
	public String getBreed(){
		return this.breed;
	}
	public void setBreed(String breed){
		this.breed = breed;
	}

}