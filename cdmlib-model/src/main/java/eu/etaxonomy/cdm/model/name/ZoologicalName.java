/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.cache.name.CacheUpdate;
import eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

/**
 * The taxon name class for animals.
 * <P>
 * This class corresponds to: NameZoological according to the ABCD schema.
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:03
 * @see NonViralName
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ZoologicalName", propOrder = {
    "breed",
    "publicationYear",
    "originalPublicationYear"
})
@XmlRootElement(name = "ZoologicalName")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
@Audited
@Configurable
public class ZoologicalName
            extends NonViralName<ZoologicalName>
            implements IZoologicalName{
	private static final long serialVersionUID = 845745609734814484L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ZoologicalName.class);

	//Name of the breed of an animal
	@XmlElement(name = "Breed")
	@Field
	@NullOrNotEmpty
	@Column(length=255)
	private String breed;

	@XmlElement(name = "PublicationYear")
	@Field(analyze = Analyze.NO)
	@CacheUpdate(value ="authorshipCache")
	@Min(0)
    private Integer publicationYear;

	@XmlElement(name = "OriginalPublicationYear")
	@Field(analyze = Analyze.NO)
	@CacheUpdate(value ="authorshipCache")
	@Min(0)
    private Integer originalPublicationYear;

	static private INonViralNameParser nameParser = new NonViralNameParserImpl();


	// ************* CONSTRUCTORS *************/
	/**
	 * Class constructor: creates a new zoological taxon name instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
	 *
	 * @see #ZoologicalName(Rank, HomotypicalGroup)
	 * @see #ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
	 */
	protected ZoologicalName() {
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}

	/**
	 * Class constructor: creates a new zoological taxon name instance
	 * only containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
	 * The new zoological taxon name instance will be also added to the set of
	 * zoological taxon names belonging to the given homotypical group.
	 *
	 * @param	rank  the rank to be assigned to <i>this</i> zoological taxon name
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> zoological taxon name belongs
	 * @see 	#ZoologicalName()
	 * @see 	#ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
	 */
	protected ZoologicalName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}

	/**
	 * Class constructor: creates a new zoological taxon name instance
	 * containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link eu.etaxonomy.cdm.agent.TeamOrPersonBase author(team)},
	 * its {@link eu.etaxonomy.cdm.reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
	 * The new zoological taxon name instance will be also added to the set of
	 * zoological taxon names belonging to the given homotypical group.
	 *
	 * @param	rank  the rank to be assigned to <i>this</i> zoological taxon name
	 * @param	genusOrUninomial the string for <i>this</i> zoological taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			<i>this</i> zoological taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			<i>this</i> zoological taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			<i>this</i> zoological taxon name if its rank is lower than species
	 * @param	combinationAuthorship  the author or the team who published <i>this</i> zoological taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where <i>this</i> zoological taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> zoological taxon name belongs
	 * @see 	#ZoologicalName()
	 * @see 	#ZoologicalName(Rank, HomotypicalGroup)
	 * @see		#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	protected ZoologicalName (Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		super(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
	}


	//********* METHODS **************************************/

	/**
	 * Creates a new zoological taxon name instance
	 * only containing its {@link Rank rank} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
	 *
	 * @param	rank	the rank to be assigned to <i>this</i> zoological taxon name
	 * @see 			#ZoologicalName(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 			eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
	 */
	public static ZoologicalName NewInstance(Rank rank){
		return new ZoologicalName(rank, null);
	}

	/**
	 * Creates a new zoological taxon name instance
	 * only containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
	 * The new zoological taxon name instance will be also added to the set of
	 * zoological taxon names belonging to the given homotypical group.
	 *
	 * @param  rank  the rank to be assigned to <i>this</i> zoological taxon name
	 * @param  homotypicalGroup  the homotypical group to which <i>this</i> zoological taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see    #ZoologicalName(Rank, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
	 */
	public static ZoologicalName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new ZoologicalName(rank, homotypicalGroup);
	}
	/**
	 * Creates a new zoological taxon name instance
	 * containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link eu.etaxonomy.cdm.agent.TeamOrPersonBase author(team)},
	 * its {@link eu.etaxonomy.cdm.reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
	 * The new zoological taxon name instance will be also added to the set of
	 * zoological taxon names belonging to the given homotypical group.
	 *
	 * @param	rank  the rank to be assigned to <i>this</i> zoological taxon name
	 * @param	genusOrUninomial the string for <i>this</i> zoological taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			<i>this</i> zoological taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			<i>this</i> zoological taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			<i>this</i> zoological taxon name if its rank is lower than species
	 * @param	combinationAuthorship  the author or the team who published <i>this</i> zoological taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where <i>this</i> zoological taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> zoological taxon name belongs
	 * @see 	#NewInstance(Rank)
	 * @see 	#NewInstance(Rank, HomotypicalGroup)
	 * @see		#ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
	 */
	public static ZoologicalName NewInstance(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		return new ZoologicalName(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
	}


	/**
	 * Returns a zoological taxon name based on parsing a string representing
	 * all elements (according to the {@link NomenclaturalCode#ICZN() ICZN}) of a zoological taxon name (where
	 * the scientific name is an uninomial) including authorship but without
	 * nomenclatural reference.
	 *
	 * @param	fullNameString  the string to be parsed
	 * @return					the new zoological taxon name
	 */
	public static ZoologicalName PARSED_NAME(String fullNameString){
		return PARSED_NAME(fullNameString, Rank.GENUS());
	}

	/**
	 * Returns a zoological taxon name based on parsing a string representing
	 * all elements (according to the {@link NomenclaturalCode#ICZN() ICZN})) of a zoological taxon name including
	 * authorship but without nomenclatural reference. The parsing result
	 * depends on the given rank of the zoological taxon name to be created.
	 *
	 * @param 	fullNameString  the string to be parsed
	 * @param   rank			the rank of the taxon name
	 * @return					the new zoological taxon name
	 */
	public static ZoologicalName PARSED_NAME(String fullNameString, Rank rank){
		if (nameParser == null){
			nameParser  = new NonViralNameParserImpl();
		}
		return (ZoologicalName)nameParser.parseFullName(fullNameString, NomenclaturalCode.ICZN, rank);
	}

	/**
	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of <i>this</i> zoological taxon name, that is the
	 * International Code of Zoological Nomenclature. This method overrides
	 * the getNomenclaturalCode method from {@link NonViralName NonViralName}.
	 *
	 * @return  the nomenclatural code for animals
	 * @see  	NonViralName#isCodeCompliant()
	 * @see  	NonViralName#getNomenclaturalCode()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Override
	public NomenclaturalCode getNomenclaturalCode(){
		return NomenclaturalCode.ICZN;
	}

//*************** ***************************

	private static Map<String, java.lang.reflect.Field> allFields = null;
	@Override
    protected Map<String, java.lang.reflect.Field> getAllFields(){
    	if (allFields == null){
			allFields = CdmUtils.getAllFields(this.getClass(), CdmBase.class, false, false, false, true);
		}
    	return allFields;
    }

/* ***************** GETTER / SETTER ***************************/



	/**
	 * Returns the breed name string for <i>this</i> animal (zoological taxon name).
	 *
	 * @return  the string containing the breed name for <i>this</i> zoological taxon name
	 */
	@Override
    public String getBreed(){
		return this.breed;
	}
	/**
	 * @see  #getBreed()
	 */
	@Override
    public void setBreed(String breed){
		this.breed = StringUtils.isBlank(breed) ? null : breed;
	}

	/**
	 * Returns the publication year (as an integer) for <i>this</i> zoological taxon
	 * name. If the publicationYear attribute is null and a nomenclatural
	 * reference exists the year could be computed from the
	 * {@link eu.etaxonomy.cdm.reference.INomenclaturalReference nomenclatural reference}.
	 *
	 * @return  the integer representing the publication year for <i>this</i> zoological taxon name
	 * @see  	#getOriginalPublicationYear()
	 */
	@Override
    public Integer getPublicationYear() {
		return publicationYear;
	}
	/**
	 * @see  #getPublicationYear()
	 */
	@Override
    public void setPublicationYear(Integer publicationYear) {
		this.publicationYear = publicationYear;
	}

	/**
	 * Returns the publication year (as an integer) of the original validly
	 * published species epithet for <i>this</i> zoological taxon name. This only
	 * applies for zoological taxon names that are no {@link TaxonNameBase#isOriginalCombination() original combinations}.
	 * If the originalPublicationYear attribute is null the year could be taken
	 * from the publication year of the corresponding original name (basionym)
	 * or from the {@link eu.etaxonomy.cdm.reference.INomenclaturalReference nomenclatural reference} of the basionym
	 * if it exists.
	 *
	 * @return  the integer representing the publication year of the original
	 * 			species epithet corresponding to <i>this</i> zoological taxon name
	 * @see  	#getPublicationYear()
	 */
	@Override
    public Integer getOriginalPublicationYear() {
		return originalPublicationYear;
	}
	/**
	 * @see  #getOriginalPublicationYear()
	 */
	@Override
    public void setOriginalPublicationYear(Integer originalPublicationYear) {
		this.originalPublicationYear = originalPublicationYear;
	}


//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> zoological name. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> zoological name by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.name.NonViralName#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		ZoologicalName result = (ZoologicalName)super.clone();
		//no changes to: breed, publicationYear, originalPublicationYear
		return result;
	}
}
