///**
//* Copyright (C) 2007 EDIT
//* European Distributed Institute of Taxonomy
//* http://www.e-taxonomy.eu
//*
//* The contents of this file are subject to the Mozilla Public License Version 1.1
//* See LICENSE.TXT at the top of this package for the full license terms.
//*/
//
//package eu.etaxonomy.cdm.model.name;
//
//
//import javax.persistence.Entity;
//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import javax.xml.bind.annotation.XmlRootElement;
//import javax.xml.bind.annotation.XmlType;
//
//import org.hibernate.envers.Audited;
//import org.hibernate.search.annotations.Indexed;
//import org.springframework.beans.factory.annotation.Configurable;
//
///**
// * The taxon name class for plants and fungi.
// * <P>
// * This class corresponds to: NameBotanical according to the ABCD schema.
// *
// * @author m.doering
// * @created 08-Nov-2007 13:06:15
// */
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "BotanicalName", propOrder = {
//        "anamorphic"
//})
//@XmlRootElement(name = "BotanicalName")
//@Entity
//@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
//@Audited
//@Configurable
//public class BotanicalName4
////            extends NonViralName<IBotanicalName>
//            /*, IMergable*/ {
////    private static final long serialVersionUID = -3484146190510367749L;
////    @SuppressWarnings("unused")
////	private static final Logger logger = Logger.getLogger(IBotanicalName.class);
////
////    private static final NomenclaturalCode code = NomenclaturalCode.ICNAFP;
//
////    IBotanicalName,
//
//    //ICNAFP
//
//
//
//    // ************* ICNAFP Names
//
////    /**
////     * Returns the boolean value of the flag indicating whether the specimen
////     * type of <i>this</i> botanical taxon name for a fungus is asexual (true) or not
////     * (false). This applies only in case of fungi. The Article 59 of the ICBN
////     * permits mycologists to give asexually reproducing fungi (anamorphs)
////     * separate names from their sexual states (teleomorphs).
////     *
////     * @return  the boolean value of the isAnamorphic flag
////     */
////    public boolean isAnamorphic(){
////        return this.anamorphic;
////    }
////
////    /**
////     * @see  #isAnamorphic()
////     */
////    public void setAnamorphic(boolean anamorphic){
////        this.anamorphic = anamorphic;
////    }
//
//	// ************* CONSTRUCTORS *************/
//	//needed by hibernate
//	/**
//	 * Class constructor: creates a new botanical taxon name instance
//	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
//	 *
//	 * @see #BotanicalName(Rank, HomotypicalGroup)
//	 * @see #BotanicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
//	 * @see eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
//	 */
////	protected BotanicalName(){
////		super(code);
////		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
////	}
////	protected BotanicalName(NomenclaturalCode code){
////        super(code);
////        this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
////    }
//
//	/**
//	 * Class constructor: creates a new botanical taxon name instance
//	 * only containing its {@link Rank rank},
//	 * its {@link HomotypicalGroup homotypical group} and
//	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
//	 * The new botanical taxon name instance will be also added to the set of
//	 * botanical taxon names belonging to this homotypical group.
//	 *
//	 * @param	rank  the rank to be assigned to <i>this</i> botanical taxon name
//	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
//	 * @see 	#BotanicalName()
//	 * @see 	#BotanicalName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
//	 */
////	protected BotanicalName(Rank rank, HomotypicalGroup homotypicalGroup) {
////		super(code, rank, homotypicalGroup);
////		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
////	}
////    protected BotanicalName(NomenclaturalCode code, Rank rank, HomotypicalGroup homotypicalGroup) {
////        super(code, rank, homotypicalGroup);
////        this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
////    }
//
//	/**
//	 * Class constructor: creates a new botanical taxon name instance
//	 * containing its {@link Rank rank},
//	 * its {@link HomotypicalGroup homotypical group},
//	 * its scientific name components, its {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author(team)},
//	 * its {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} and
//	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
//	 * The new botanical taxon name instance will be also added to the set of
//	 * botanical taxon names belonging to this homotypical group.
//	 *
//	 * @param	rank  the rank to be assigned to <i>this</i> botanical taxon name
//	 * @param	genusOrUninomial the string for <i>this</i> botanical taxon name
//	 * 			if its rank is genus or higher or for the genus part
//	 * 			if its rank is lower than genus
//	 * @param	infraGenericEpithet  the string for the first epithet of
//	 * 			<i>this</i> botanical taxon name if its rank is lower than genus
//	 * 			and higher than species aggregate
//	 * @param	specificEpithet  the string for the first epithet of
//	 * 			<i>this</i> botanical taxon name if its rank is species aggregate or lower
//	 * @param	infraSpecificEpithet  the string for the second epithet of
//	 * 			<i>this</i> botanical taxon name if its rank is lower than species
//	 * @param	combinationAuthorship  the author or the team who published <i>this</i> botanical taxon name
//	 * @param	nomenclaturalReference  the nomenclatural reference where <i>this</i> botanical taxon name was published
//	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
//	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
//	 * @see 	#BotanicalName()
//	 * @see 	#BotanicalName(Rank, HomotypicalGroup)
//	 * @see		#NewBotanicalInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
//	 */
////	protected BotanicalName(NomenclaturalCode code, Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
////		super(code, rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
////		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
////	}
////    protected BotanicalName(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
////        super(code, rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
////        this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
////    }
//
//
////*************************
//
//
//
//	/**
//	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
//	 * the construction of <i>this</i> botanical taxon name, that is the
//	 * International Code of Botanical Nomenclature. This method overrides
//	 * the getNomenclaturalCode method from {@link INonViralName NonViralName}.
//	 *
//	 * @return  the nomenclatural code for plants
//	 * @see  	NonViralName#isCodeCompliant()
//	 * @see  	TaxonNameBase#getHasProblem()
//	 */
////
////	public NomenclaturalCode getNomenclaturalCode(){
////		return code;
////	}
//
//
//	/**
//	 * Checks if this name is an autonym.<BR>
//	 * An autonym is a taxon name that has equal specific and infra specific epithets.<BR>
//	 * {@link http://ibot.sav.sk/icbn/frameset/0010Ch2Sec1a006.htm#6.8. Vienna Code §6.8}
//	 * or a taxon name that has equal generic and infrageneric epithets (A22.2)
//	 * @return true, if name has Rank, Rank is below species and species epithet equals infraSpeciesEpithtet, else false
//	 */
////	@Override
////	public boolean isAutonym(){
////		if (this.getRank() != null && this.getSpecificEpithet() != null && this.getInfraSpecificEpithet() != null &&
////				this.isInfraSpecific() && this.getSpecificEpithet().trim().equals(this.getInfraSpecificEpithet().trim())){
////			return true;
////		}else if (this.getRank() != null && this.getGenusOrUninomial() != null && this.getInfraGenericEpithet() != null &&
////				this.isInfraGeneric() && this.getGenusOrUninomial().trim().equals(this.getInfraGenericEpithet().trim())){
////			return true;
////		}else{
////			return false;
////		}
////	}
//
////*********************** CLONE ********************************************************/
//
//	/**
//	 * Clones <i>this</i> botanical name. This is a shortcut that enables to create
//	 * a new instance that differs only slightly from <i>this</i> botanical name by
//	 * modifying only some of the attributes.
//	 *
//	 * @see eu.etaxonomy.cdm.model.name.NonViralName#clone()
//	 * @see java.lang.Object#clone()
//	 */
//	@Override
//    public Object clone() {
//		IBotanicalName result = (IBotanicalName)super.clone();
//		//no changes to:
//		return result;
//	}
//
//
//}
