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
// * The taxon name class for bacteria.
// * <P>
// * This class corresponds to: NameBacterial according to the ABCD schema.
// *
// * @author m.doering
// * @created 08-Nov-2007 13:06:11
// */
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "", propOrder = {
//})
//@XmlRootElement(name = "BacterialName")
//@Entity
//@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
//@Audited
//@Configurable
//public class BacterialName4{
////        extends NonViralName<IBacterialName>{
//
////    private static final long serialVersionUID = 5161176481172718843L;
////    @SuppressWarnings("unused")
////	private static final Logger logger = Logger.getLogger(IBacterialName.class);
////
////    private static final NomenclaturalCode code = NomenclaturalCode.ICNB;
//
//
//	// ************* CONSTRUCTORS *************/
//
////	protected BacterialName(){
////		super(code);
////		this.cacheStrategy = BacterialNameDefaultCacheStrategy.NewInstance();
////	}
//
//	/**
//	 * Class constructor: creates a new bacterial taxon name instance
//	 * only containing its {@link Rank rank},
//	 * its {@link HomotypicalGroup homotypical group} and
//	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
//	 * The new bacterial taxon name instance will be also added to the set of
//	 * bacterial taxon names belonging to this homotypical group.
//	 *
//	 * @param	rank  the rank to be assigned to <i>this</i> bacterial taxon name
//	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> bacterial taxon name belongs
//	 * @see 	#NewInstance(Rank)
//	 * @see 	#NewInstance(Rank, HomotypicalGroup)
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
//	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
//	 */
////	protected BacterialName(Rank rank, HomotypicalGroup homotypicalGroup) {
////		super(code, rank, homotypicalGroup);
////		this.cacheStrategy = BacterialNameDefaultCacheStrategy.NewInstance();
////	}
//
//	//********* METHODS **************************************/
//
//
//
//
//	/**
//	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
//	 * the construction of <i>this</i> bacterial taxon name, that is the
//	 * International Code of Nomenclature of Bacteria. This method overrides
//	 * the getNomenclaturalCode method from {@link INonViralName NonViralName}.
//	 *
//	 * @return  the nomenclatural code for bacteria
//	 * @see  	NonViralName#isCodeCompliant()
//	 * @see  	TaxonNameBase#getHasProblem()
//	 */
////	public NomenclaturalCode getNomenclaturalCode(){
////		return code;
////
////	}
//
////*********************** CLONE ********************************************************/
//
//	/**
//	 * Clones <i>this</i> bacterial name. This is a shortcut that enables to create
//	 * a new instance that differs only slightly from <i>this</i> bacterial name by
//	 * modifying only some of the attributes.
//	 *
//	 * @see eu.etaxonomy.cdm.model.name.NonViralName#clone()
//	 * @see java.lang.Object#clone()
//	 */
//	@Override
//    public Object clone() {
//		IBacterialName result = (IBacterialName)super.clone();
//		//no changes to:
//		return result;
//	}
//
//}
