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

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * The taxon name class for plants and fungi.
 * <P>
 * This class corresponds to: NameBotanical according to the ABCD schema.
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:15
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BotanicalName", propOrder = {
    "anamorphic"
})
@XmlRootElement(name = "BotanicalName")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
@Audited
@Configurable
public class BotanicalName
            extends NonViralName<BotanicalName>
            implements IBotanicalName /*, IMergable*/ {
	private static final long serialVersionUID = 6818651572463497727L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BotanicalName.class);

	//Only for fungi: to indicate that the type of the name is asexual or not
    @XmlElement(name ="IsAnamorphic")
	private boolean anamorphic;

	static private INonViralNameParser<?> nameParser = new NonViralNameParserImpl();

	// ************* CONSTRUCTORS *************/
	//needed by hibernate
	/**
	 * Class constructor: creates a new botanical taxon name instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 *
	 * @see #BotanicalName(Rank, HomotypicalGroup)
	 * @see #BotanicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	protected BotanicalName(){
		super();
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}
	/**
	 * Class constructor: creates a new botanical taxon name instance
	 * only containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 *
	 * @param	rank  the rank to be assigned to <i>this</i> botanical taxon name
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
	 * @see 	#BotanicalName()
	 * @see 	#BotanicalName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	protected BotanicalName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}
	/**
	 * Class constructor: creates a new botanical taxon name instance
	 * containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author(team)},
	 * its {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 *
	 * @param	rank  the rank to be assigned to <i>this</i> botanical taxon name
	 * @param	genusOrUninomial the string for <i>this</i> botanical taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			<i>this</i> botanical taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			<i>this</i> botanical taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			<i>this</i> botanical taxon name if its rank is lower than species
	 * @param	combinationAuthorship  the author or the team who published <i>this</i> botanical taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where <i>this</i> botanical taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
	 * @see 	#BotanicalName()
	 * @see 	#BotanicalName(Rank, HomotypicalGroup)
	 * @see		#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	protected BotanicalName(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		super(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}


	//********* METHODS **************************************/

	/**
	 * Creates a new botanical taxon name instance
	 * only containing its {@link Rank rank} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 *
	 * @param	rank	the rank to be assigned to <i>this</i> botanical taxon name
	 * @see 			#BotanicalName(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 			eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	public static BotanicalName NewInstance(Rank rank){
		return new BotanicalName(rank, null);
	}
	/**
	 * Creates a new botanical taxon name instance
	 * only containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 *
	 * @param  rank  the rank to be assigned to <i>this</i> botanical taxon name
	 * @param  homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see    #BotanicalName(Rank, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	public static BotanicalName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new BotanicalName(rank, homotypicalGroup);
	}
	/**
	 * Creates a new botanical taxon name instance
	 * containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author(team)},
	 * its {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 *
	 * @param	rank  the rank to be assigned to <i>this</i> botanical taxon name
	 * @param	genusOrUninomial the string for <i>this</i> botanical taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			<i>this</i> botanical taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			<i>this</i> botanical taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			<i>this</i> botanical taxon name if its rank is lower than species
	 * @param	combinationAuthorship  the author or the team who published <i>this</i> botanical taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where <i>this</i> botanical taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
	 * @see 	#NewInstance(Rank)
	 * @see 	#NewInstance(Rank, HomotypicalGroup)
	 * @see		ZoologicalName#ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	public static  BotanicalName NewInstance(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		return new BotanicalName(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
	}

	/**
	 * Returns a botanical taxon name based on parsing a string representing
	 * all elements (according to the ICBN) of a botanical taxon name (where
	 * the scientific name is an uninomial) including authorship but without
	 * nomenclatural reference. If the {@link Rank rank} is not "Genus" it should be
	 * set afterwards with the {@link TaxonNameBase#setRank(Rank) setRank} methode.
	 *
	 * @param	fullNameString  the string to be parsed
	 * @return					the new botanical taxon name
	 */
	public static BotanicalName PARSED_NAME(String fullNameString){
		return PARSED_NAME(fullNameString, Rank.GENUS());
	}

	/**
	 * Returns a botanical taxon name based on parsing a string representing
	 * all elements (according to the ICBN) of a botanical taxon name including
	 * authorship but without nomenclatural reference. The parsing result
	 * depends on the given rank of the botanical taxon name to be created.
	 *
	 * @param 	fullNameString  the string to be parsed
	 * @param   rank			the rank of the taxon name
	 * @return					the new botanical taxon name
	 */
	public static BotanicalName PARSED_NAME(String fullNameString, Rank rank){
		if (nameParser == null){
			nameParser = new NonViralNameParserImpl();
		}
		return (BotanicalName)nameParser.parseFullName(fullNameString, NomenclaturalCode.ICNAFP,  rank);
	}

	/**
	 * Returns a botanical taxon name based on parsing a string representing
	 * all elements (according to the ICBN) of a botanical taxon name (where
	 * the scientific name is an uninomial) including authorship and
	 * nomenclatural reference. Eventually a new {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference}
	 * instance will also be created. If the {@link Rank rank} is not "Genus" it should be
	 * set afterwards with the {@link TaxonNameBase#setRank(Rank) setRank} methode.
	 *
	 * @param	fullNameAndReferenceString  the string to be parsed
	 * @return								the new botanical taxon name
	 */
	public static BotanicalName PARSED_REFERENCE(String fullNameAndReferenceString){
		return PARSED_REFERENCE(fullNameAndReferenceString, Rank.GENUS());
	}

	/**
	 * Returns a botanical taxon name based on parsing a string representing
	 * all elements (according to the ICBN) of a botanical taxon name including
	 * authorship and nomenclatural reference. The parsing result depends on
	 * the given rank of the botanical taxon name to be created.
	 * Eventually a new {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference}
	 * instance will also be created.
	 *
	 * @param	fullNameAndReferenceString  the string to be parsed
	 * @param   rank						the rank of the taxon name
	 * @return								the new botanical taxon name
	 */
	public static BotanicalName PARSED_REFERENCE(String fullNameAndReferenceString, Rank rank){
		if (nameParser == null){
			nameParser = new NonViralNameParserImpl();
		}
		return (BotanicalName)nameParser.parseReferencedName(fullNameAndReferenceString, NomenclaturalCode.ICNAFP, rank);
	}

//***********************

	private static Map<String, java.lang.reflect.Field> allFields = null;
	@Override
    protected Map<String, java.lang.reflect.Field> getAllFields(){
    	if (allFields == null){
			allFields = CdmUtils.getAllFields(this.getClass(), CdmBase.class, false, false, false, true);
		}
    	return allFields;
    }

//*************************

	/**
	 * Returns the boolean value of the flag indicating whether the specimen
	 * type of <i>this</i> botanical taxon name for a fungus is asexual (true) or not
	 * (false). This applies only in case of fungi. The Article 59 of the ICBN
	 * permits mycologists to give asexually reproducing fungi (anamorphs)
	 * separate names from their sexual states (teleomorphs).
	 *
	 * @return  the boolean value of the isAnamorphic flag
	 */
	@Override
	public boolean isAnamorphic(){
		return this.anamorphic;
	}

	/**
	 * @see  #isAnamorphic()
	 */
	@Override
    public void setAnamorphic(boolean anamorphic){
		this.anamorphic = anamorphic;
	}


	/**
	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of <i>this</i> botanical taxon name, that is the
	 * International Code of Botanical Nomenclature. This method overrides
	 * the getNomenclaturalCode method from {@link NonViralName NonViralName}.
	 *
	 * @return  the nomenclatural code for plants
	 * @see  	NonViralName#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Override
	public NomenclaturalCode getNomenclaturalCode(){
		return NomenclaturalCode.ICNAFP;
	}


	/**
	 * Checks if this name is an autonym.<BR>
	 * An autonym is a taxon name that has equal specific and infra specific epithets.<BR>
	 * {@link http://ibot.sav.sk/icbn/frameset/0010Ch2Sec1a006.htm#6.8. Vienna Code §6.8}
	 * or a taxon name that has equal generic and infrageneric epithets (A22.2)
	 * @return true, if name has Rank, Rank is below species and species epithet equals infraSpeciesEpithtet, else false
	 */
	@Override
	public boolean isAutonym(){
		if (this.getRank() != null && this.getSpecificEpithet() != null && this.getInfraSpecificEpithet() != null &&
				this.isInfraSpecific() && this.getSpecificEpithet().trim().equals(this.getInfraSpecificEpithet().trim())){
			return true;
		}else if (this.getRank() != null && this.getGenusOrUninomial() != null && this.getInfraGenericEpithet() != null &&
				this.isInfraGeneric() && this.getGenusOrUninomial().trim().equals(this.getInfraGenericEpithet().trim())){
			return true;
		}else{
			return false;
		}
	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> botanical name. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> botanical name by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.name.NonViralName#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		BotanicalName result = (BotanicalName)super.clone();
		//no changes to: title, authorTeam, hasProblem, nomenclaturallyRelevant, uri
		return result;
	}


}
