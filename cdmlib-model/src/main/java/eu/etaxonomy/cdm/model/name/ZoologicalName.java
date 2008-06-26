/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The taxon name class for animals.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:03
 * @see NonViralName
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "breed",
    "publicationYear",
    "originalPublicationYear"
})
@XmlRootElement(name = "ZoologicalName")
@Entity
public class ZoologicalName extends NonViralName {
	
	static Logger logger = Logger.getLogger(ZoologicalName.class);

	//Name of the breed of an animal
	@XmlElement(name = "Breed")
	private String breed;
	
	@XmlElement(name = "PublicationYear")
	private Integer publicationYear;
	
	@XmlElement(name = "OriginalPublicationYear")
	private Integer originalPublicationYear;

	static private INonViralNameParser nameParser = new NonViralNameParserImpl();

	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new zoological taxon name instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #ZoologicalName(Rank, HomotypicalGroup)
	 * @see #ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy
	 */
	protected ZoologicalName() {
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}
	
	/** 
	 * Class constructor: creates a new zoological taxon name instance
	 * only containing its {@link common.Rank rank},
	 * its {@link common.HomotypicalGroup homotypical group} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank  the rank to be assigned to this non viral taxon name
	 * @param	homotypicalGroup  the homotypical group to which this non viral taxon name belongs
	 * @see 	#ZoologicalName()
	 * @see 	#ZoologicalName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy
	 */
	protected ZoologicalName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}

	/** 
	 * Class constructor: creates a new zoological taxon name instance
	 * containing its {@link common.Rank rank},
	 * its {@link common.HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link agent.TeamOrPersonBase author(team)},
	 * its {@link reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank  the rank to be assigned to this zoological taxon name
	 * @param	genusOrUninomial the string for this zoological taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			this zoological taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			this zoological taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			this zoological taxon name if its rank is lower than species
	 * @param	combinationAuthorTeam  the author or the team who published this zoological taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where this zoological taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which this zoological taxon name belongs
	 * @see 	#ZoologicalName()
	 * @see 	#ZoologicalName(Rank, HomotypicalGroup)
	 * @see		#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.IIdentifiableEntityCacheStrategy
	 */
	protected ZoologicalName (Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		super(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}
	
	
	//********* METHODS **************************************/
	
	/** 
	 * Creates a new zoological taxon name instance
	 * only containing its {@link common.Rank rank} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank	the rank to be assigned to this zoological taxon name
	 * @see 			#ZoologicalName(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 			eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy
	 */
	public static ZoologicalName NewInstance(Rank rank){
		return new ZoologicalName(rank, null);
	}

	/** 
	 * Creates a new zoological taxon name instance
	 * only containing its {@link common.Rank rank} and
	 * its {@link common.HomotypicalGroup homotypical group} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy default cache strategy}.
	 * The new zoological taxon name instance will be also added to the set of
	 * zoological taxon names belonging to this homotypical group. If the homotypical 
	 * group does not exist a new instance will be created for it.
	 * 
	 * @param  rank  the rank to be assigned to this zoological taxon name
	 * @param  homotypicalGroup  the homotypical group to which this zoological taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see    #ZoologicalName(Rank, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy
	 */
	public static ZoologicalName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new ZoologicalName(rank, homotypicalGroup);
	}
	/** 
	 * Creates a new zoological taxon name instance
	 * containing its {@link common.Rank rank},
	 * its {@link common.HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link agent.TeamOrPersonBase author(team)},
	 * its {@link reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank  the rank to be assigned to this zoological taxon name
	 * @param	genusOrUninomial the string for this zoological taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			this zoological taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			this zoological taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			this zoological taxon name if its rank is lower than species
	 * @param	combinationAuthorTeam  the author or the team who published this zoological taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where this zoological taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which this zoological taxon name belongs
	 * @see 	#NewInstance(Rank)
	 * @see 	#NewInstance(Rank, HomotypicalGroup)
	 * @see		#ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.ZooNameDefaultCacheStrategy
	 */
	public static ZoologicalName NewInstance(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		return new ZoologicalName(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
	}	
	
	
	/**
	 * Returns a zoological taxon name based on parsing a string composed of
	 * just one word (uninomial) and supposing that it is compliant to the ICZN.
	 * 
	 * @param 	fullNameString  string representing the scientific name of a
	 * 							uninomial zoological taxon name 
	 * @return					the new zoological taxon name
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