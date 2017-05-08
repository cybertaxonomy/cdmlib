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
// * The taxon name class for animals.
// * <P>
// * This class corresponds to: NameZoological according to the ABCD schema.
// *
// * @author m.doering
// * @created 08-Nov-2007 13:07:03
// * @see NonViralName
// */
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "ZoologicalName", propOrder = {
//
//})
//@XmlRootElement(name = "ZoologicalName")
//@Entity
//@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonName")
//@Audited
//@Configurable
//public class ZoologicalName4{
////            extends NonViralName<ZoologicalName>{
////	private static final long serialVersionUID = 845745609734814484L;
////	@SuppressWarnings("unused")
////	private static final Logger logger = Logger.getLogger(ZoologicalName.class);
////
////    private static final NomenclaturalCode code = NomenclaturalCode.ICZN;
//
//
//	// ************* CONSTRUCTORS *************/
//	/**
//	 * Class constructor: creates a new zoological taxon name instance
//	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
//	 *
//	 * @see #ZoologicalName(Rank, HomotypicalGroup)
//	 * @see #ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
//	 * @see eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
//	 */
////	protected ZoologicalName() {
////		super(code);
////	    this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
////	}
//
//	/**
//	 * Class constructor: creates a new zoological taxon name instance
//	 * only containing its {@link Rank rank},
//	 * its {@link HomotypicalGroup homotypical group} and
//	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
//	 * The new zoological taxon name instance will be also added to the set of
//	 * zoological taxon names belonging to the given homotypical group.
//	 *
//	 * @param	rank  the rank to be assigned to <i>this</i> zoological taxon name
//	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> zoological taxon name belongs
//	 * @see 	#ZoologicalName()
//	 * @see 	#ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
//	 */
////	protected ZoologicalName(Rank rank, HomotypicalGroup homotypicalGroup) {
////		super(code, rank, homotypicalGroup);
////		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
////	}
//
//	/**
//	 * Class constructor: creates a new zoological taxon name instance
//	 * containing its {@link Rank rank},
//	 * its {@link HomotypicalGroup homotypical group},
//	 * its scientific name components, its {@link eu.etaxonomy.cdm.agent.TeamOrPersonBase author(team)},
//	 * its {@link eu.etaxonomy.cdm.reference.INomenclaturalReference nomenclatural reference} and
//	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
//	 * The new zoological taxon name instance will be also added to the set of
//	 * zoological taxon names belonging to the given homotypical group.
//	 *
//	 * @param	rank  the rank to be assigned to <i>this</i> zoological taxon name
//	 * @param	genusOrUninomial the string for <i>this</i> zoological taxon name
//	 * 			if its rank is genus or higher or for the genus part
//	 * 			if its rank is lower than genus
//	 * @param	infraGenericEpithet  the string for the first epithet of
//	 * 			<i>this</i> zoological taxon name if its rank is lower than genus
//	 * 			and higher than species aggregate
//	 * @param	specificEpithet  the string for the first epithet of
//	 * 			<i>this</i> zoological taxon name if its rank is species aggregate or lower
//	 * @param	infraSpecificEpithet  the string for the second epithet of
//	 * 			<i>this</i> zoological taxon name if its rank is lower than species
//	 * @param	combinationAuthorship  the author or the team who published <i>this</i> zoological taxon name
//	 * @param	nomenclaturalReference  the nomenclatural reference where <i>this</i> zoological taxon name was published
//	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
//	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> zoological taxon name belongs
//	 * @see 	#ZoologicalName()
//	 * @see 	#ZoologicalName(Rank, HomotypicalGroup)
//	 * @see		#NewZoologicalInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
//	 */
////	protected ZoologicalName (Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
////		super(code, rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
////		this.cacheStrategy = ZooNameDefaultCacheStrategy.NewInstance();
////	}
//
//
//	//********* METHODS **************************************/
//
//
//	/**
//	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
//	 * the construction of <i>this</i> zoological taxon name, that is the
//	 * International Code of Zoological Nomenclature. This method overrides
//	 * the getNomenclaturalCode method from {@link INonViralName NonViralName}.
//	 *
//	 * @return  the nomenclatural code for animals
//	 * @see  	NonViralName#isCodeCompliant()
//	 * @see  	NonViralName#getNomenclaturalCode()
//	 * @see  	TaxonName#getHasProblem()
//	 */
////	@Override
////	public NomenclaturalCode getNomenclaturalCode(){
////		return code;
////	}
//
//
////*********************** CLONE ********************************************************/
//
//	/**
//	 * Clones <i>this</i> zoological name. This is a shortcut that enables to create
//	 * a new instance that differs only slightly from <i>this</i> zoological name by
//	 * modifying only some of the attributes.
//	 *
//	 * @see eu.etaxonomy.cdm.model.name.NonViralName#clone()
//	 * @see java.lang.Object#clone()
//	 */
//	@Override
//	public Object clone() {
//		IZoologicalName result = (IZoologicalName)super.clone();
//		//no changes to: breed, publicationYear, originalPublicationYear
//		return result;
//	}
//}
