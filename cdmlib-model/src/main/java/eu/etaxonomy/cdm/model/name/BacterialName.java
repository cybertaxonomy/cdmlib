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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The taxon name class for bacteria.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:11
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "subGenusAuthorship",
    "nameApprobation"
})
@XmlRootElement(name = "BacterialName")
@Entity
public class BacterialName extends NonViralName {
	
	static Logger logger = Logger.getLogger(BacterialName.class);

	//Author team and year of the subgenus name
	@XmlElement(name = "SubGenusAuthorship")
	private String subGenusAuthorship;
	
	//Approbation of name according to approved list, validation list, or validly published, paper in IJSB after 1980
	@XmlElement(name = "NameApprobation")
	private String nameApprobation;

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new bacterial taxon name instance
	 * only containing its {@link common.Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * The new bacterial taxon name instance will be also added to the set of
	 * bacterial taxon names belonging to this homotypical group.
	 * 
	 * @param	rank  the rank to be assigned to this bacterial taxon name
	 * @param	homotypicalGroup  the homotypical group to which this bacterial taxon name belongs
	 * @see 	#NewInstance(Rank)
	 * @see 	#NewInstance(Rank, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	protected BacterialName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
	}

	//********* METHODS **************************************/
	/** 
	 * Creates a new bacterial taxon name instance
	 * only containing its {@link common.Rank rank} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param  rank  the rank to be assigned to this bacterial taxon name
	 * @see    #NewInstance(Rank, HomotypicalGroup)
	 * @see    #BacterialName(Rank, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	public static BacterialName NewInstance(Rank rank){
		return new BacterialName(rank, null);
	}

	/** 
	 * Creates a new bacterial taxon name instance
	 * only containing its {@link common.Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * The new bacterial taxon name instance will be also added to the set of
	 * bacterial taxon names belonging to this homotypical group.
	 * 
	 * @param  rank  the rank to be assigned to this bacterial taxon name
	 * @param  homotypicalGroup  the homotypical group to which this bacterial taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #BacterialName(Rank, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	public static BacterialName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new BacterialName(rank, homotypicalGroup);
	}
	
	/**
	 * Returns the string containing the authorship with the year and details
	 * of the reference in which the subgenus included in the scientific name
	 * of this bacterial taxon name was published.
	 * For instance if the bacterial taxon name is
	 * 'Bacillus (subgen. Aerobacillus Donker 1926, 128) polymyxa' the subgenus
	 * authorship string is 'Donker 1926, 128'. 
	 * 
	 * @return  the string containing the complete subgenus' authorship
	 * 			included in this bacterial taxon name
	 */
	public String getSubGenusAuthorship(){
		return this.subGenusAuthorship;
	}

	/**
	 * @see  #getSubGenusAuthorship()
	 */
	public void setSubGenusAuthorship(String subGenusAuthorship){
		this.subGenusAuthorship = subGenusAuthorship;
	}

	/**
	 * Returns the string representing the reason for the approbation of this
	 * bacterial taxon name. Bacterial taxon names are valid or approved
	 * according to:
	 * - the approved list, c.f.r. IJSB 1980 (AL)
	 * - the validation list, in IJSB after 1980 (VL)
	 * or
	 * - are validly published as paper in IJSB after 1980 (VP).
	 * IJSB is the acronym for International Journal of Systematic Bacteriology.
	 * 
	 * @return  the string with the source of the approbation for this bacterial taxon name
	 */
	public String getNameApprobation(){
		return this.nameApprobation;
	}

	/**
	 * @see  #getNameApprobation()
	 */
	public void setNameApprobation(String nameApprobation){
		this.nameApprobation = nameApprobation;
	}
	
	
	/**
	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of this bacterial taxon name, that is the
	 * International Code of Nomenclature of Bacteria. This method overrides
	 * the getNomeclaturalCode method from {@link NonViralName#getNomeclaturalCode() NonViralName}.
	 *
	 * @return  the nomenclatural code for bacteria
	 * @see  	NonViralName#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Transient
	@Override
	public NomenclaturalCode getNomenclaturalCode(){
		return NomenclaturalCode.ICNB();

	}

}