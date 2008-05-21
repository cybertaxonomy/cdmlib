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

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


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

	static private INonViralNameParser nameParser = new NonViralNameParserImpl();

	
	public static ZoologicalName NewInstance(Rank rank){
		return new ZoologicalName(rank, null);
	}

	public static ZoologicalName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new ZoologicalName(rank, homotypicalGroup);
	}
	public static ZoologicalName NewInstance(Rank rank, String genusOrUninomial, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		return new ZoologicalName(rank, genusOrUninomial, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
	}	
	
	
	/**
	 * Returns a parsed Name
	 * @param fullName
	 * @return
	 */
	public static ZoologicalName PARSED_NAME(String fullNameString){
		return PARSED_NAME(fullNameString, Rank.GENUS());
	}
	
	/**
	 * Returns a parsed Name
	 * @param fullName
	 * @return
	 */
	public static ZoologicalName PARSED_NAME(String fullNameString, Rank rank){
		if (nameParser == null){
			nameParser  = new NonViralNameParserImpl();
		}
		return (ZoologicalName)nameParser.parseFullName(fullNameString, NomenclaturalCode.ICZN(), rank);
	}
	
	protected ZoologicalName() {
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}
	
	protected ZoologicalName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}

	protected ZoologicalName (Rank rank, String genusOrUninomial, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		super(rank, genusOrUninomial, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}
	
	
	@Transient
	@Override
	public NomenclaturalCode getNomeclaturalCode(){
		return NomenclaturalCode.ICZN();
	}
	
/* ***************** GETTER / SETTER ***************************/
	
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