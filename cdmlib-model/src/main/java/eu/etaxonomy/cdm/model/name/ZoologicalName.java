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

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAgent;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.BotanicNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.ZooNameDefaultCacheStrategy;

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
	private Integer publicationYear;
	private Integer originalPublicationYear;

	private ZoologicalName() {
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}
	
	public ZoologicalName(Rank rank) {
		super(rank);
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}
	public ZoologicalName(Rank rank, String genusOrUninomial, String specificEpithet, String infraSpecificEpithet, INomenclaturalAgent combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef) {
		super(rank, genusOrUninomial, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef);
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}
	
	public String getBreed(){
		return this.breed;
	}
	public void setBreed(String breed){
		this.breed = breed;
	}

	public Integer getPublicationYear() {
		return publicationYear;
	}
	public void setPublicationYear(Integer publicationYear) {
		this.publicationYear = publicationYear;
	}

	public Integer getOriginalPublicationYear() {
		return originalPublicationYear;
	}
	public void setOriginalPublicationYear(Integer originalPublicationYear) {
		this.originalPublicationYear = originalPublicationYear;
	}

}