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
import eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy;
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
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #ZoologicalName(Rank, HomotypicalGroup)
	 * @see #ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy
	 */
	protected ZoologicalName() {
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}
	
	/** 
	 * Class constructor: creates a new zoological taxon name instance
	 * only containing its {@link common.Rank rank},
	 * its {@link common.HomotypicalGroup homotypical group} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank  the rank to be assigned to this non viral taxon name
	 * @param	homotypicalGroup  the homotypical group to which this non viral taxon name belongs
	 * @see 	#ZoologicalName()
	 * @see 	#ZoologicalName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy
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
	 * the {@link eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy default cache strategy}.
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
	 * @see 	eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy
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
	 * the {@link eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank	the rank to be assigned to this zoological taxon name
	 * @see 			#ZoologicalName(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 			eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy
	 */
	public static ZoologicalName NewInstance(Rank rank){
		return new ZoologicalName(rank, null);
	}

	/** 
	 * Creates a new zoological taxon name instance
	 * only containing its {@link common.Rank rank} and
	 * its {@link common.HomotypicalGroup homotypical group} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy default cache strategy}.
	 * The new zoological taxon name instance will be also added to the set of
	 * zoological taxon names belonging to this homotypical group. If the homotypical 
	 * group does not exist a new instance will be created for it.
	 * 
	 * @param  rank  the rank to be assigned to this zoological taxon name
	 * @param  homotypicalGroup  the homotypical group to which this zoological taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see    #ZoologicalName(Rank, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy
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
	 * the {@link eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy default cache strategy}.
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
	 * @see 	eu.etaxonomy.cdm.strategy.cache.BookDefaultCacheStrategy
	 */
	public static ZoologicalName NewInstance(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		return new ZoologicalName(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
	}	
	
	
	/**
	 * Returns a zoological taxon name based on parsing a string representing
	 * all elements (according to the ICZN) of a zoological taxon name in which
	 * the scientific name is an uninomial.
	 * 
	 * @param	fullNameString  the string to be parsed 
	 * @return					the new zoological taxon name
	 */
	public static ZoologicalName PARSED_NAME(String fullNameString){
		return PARSED_NAME(fullNameString, Rank.GENUS());
	}
	
	/**
	 * Returns a zoological taxon name based on parsing a string representing
	 * all elements (according to the ICZN) of a zoological taxon name. The
	 * parsing result depends on the given rank of the zoological taxon name
	 * to be created.
	 * 
	 * @param 	fullNameString  the string to be parsed 
	 * @param   rank			the rank of the taxon name
	 * @return					the new zoological taxon name
	 */
	public static ZoologicalName PARSED_NAME(String fullNameString, Rank rank){
		if (nameParser == null){
			nameParser  = new NonViralNameParserImpl();
		}
		return (ZoologicalName)nameParser.parseFullName(fullNameString, NomenclaturalCode.ICZN(), rank);
	}
	
	/**
	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of this zoological taxon name, that is the
	 * International Code of Zoological Nomenclature. This method overrides
	 * the getNomeclaturalCode method from {@link TaxonNameBase#getNomeclaturalCode() TaxonNameBase}.
	 *
	 * @return  the nomenclatural code for animals
	 * @see  	NonViralName#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Transient
	@Override
	public NomenclaturalCode getNomenclaturalCode(){
		return NomenclaturalCode.ICZN();
	}
	
/* ***************** GETTER / SETTER ***************************/
	
	/**
	 * Returns the breed name string for this animal (zoological taxon name).
	 * 
	 * @return  the string containing the breed name for this zoological taxon name
	 */
	public String getBreed(){
		return this.breed;
	}
	/**
	 * @see  #getBreed()
	 */
	public void setBreed(String breed){
		this.breed = breed;
	}

	/**
	 * Returns the publication year (as an integer) for this zoological taxon
	 * name. If this attribute is null and a nomenclatural reference exists
	 * the year could be computed from the {@link reference.INomenclaturalReference#getYear() nomenclatural reference}.
	 * 
	 * @return  the integer representing the publication year for this zoological taxon name
	 * @see  	#getOriginalPublicationYear()
	 */
	public Integer getPublicationYear() {
		return publicationYear;
	}
	/**
	 * @see  #getPublicationYear()
	 */
	public void setPublicationYear(Integer publicationYear) {
		this.publicationYear = publicationYear;
	}

	/**
	 * Returns the publication year (as an integer) of the original validly
	 * published species epithet for this zoological taxon name. This only
	 * applies for zoological taxon names that are no {@link TaxonNameBase#isOriginalCombination() original combinations}.
	 * If the originalPublicationYear attribute is null the year could be taken
	 * from the publication year of the corresponding original name (basionym)
	 * or from the {@link reference.INomenclaturalReference#getYear() nomenclatural reference} of the basionym
	 * if it exists.
	 * 
	 * @return  the integer representing the publication year for this zoological taxon name
	 * @see  	#getPublicationYear()
	 */
	public Integer getOriginalPublicationYear() {
		return originalPublicationYear;
	}
	/**
	 * @see  #getOriginalPublicationYear()
	 */
	public void setOriginalPublicationYear(Integer originalPublicationYear) {
		this.originalPublicationYear = originalPublicationYear;
	}
}