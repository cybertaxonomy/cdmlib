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
import eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The taxon name class for viral taxa. The scientific name will be stored
 * as a string (consisting eventually of several words even combined also with
 * non alphabetical characters) in the inherited {@link common.IdentifiableEntity#setTitleCache(String) titleCache} attribute.
 * Classification has no influence on the names of viral taxon names and no
 * viral taxon must be taxonomically included in another viral taxon with
 * higher rank. For examples see ICTVdb:
 * http://www.ncbi.nlm.nih.gov/ICTVdb/Ictv/vn_indxA.htm
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:02
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cacheStrategy",
    "acronym"
})
@XmlRootElement(name = "ViralName")
@Entity
public class ViralName extends TaxonNameBase<ViralName, INameCacheStrategy>  {
	
	private static final Logger logger = Logger.getLogger(ViralName.class);

	@XmlElement(name = "CacheStrategy")
	protected INameCacheStrategy cacheStrategy;
	@XmlElement(name = "Acronym")
	private String acronym;

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new viral taxon name instance
	 * only containing its {@link common.Rank rank}.
	 * 
	 * @param	rank  the rank to be assigned to this viral taxon name
	 * @see 	TaxonNameBase#TaxonNameBase(Rank)
	 */
	public ViralName(Rank rank) {
		super(rank);
	}

	
	//********* METHODS **************************************/

	/** 
	 * Creates a new viral taxon name instance only containing its {@link common.Rank rank}.
	 * 
	 * @param	rank  the rank to be assigned to this viral taxon name
	 * @see 	#ViralName(Rank)
	 */
	public static ViralName NewInstance(Rank rank){
		return new ViralName(rank);
	}

	/**
	 * Returns the accepted acronym (an assigned abbreviation) string for this
	 * viral taxon name. For instance PCV stays for Peanut Clump Virus.
	 * 
	 * @return  the string containing the accepted acronym of this viral taxon name
	 */
	public String getAcronym(){
		return this.acronym;
	}
	/**
	 * @see  #getAcronym()
	 */
	public void setAcronym(String acronym){
		this.acronym = acronym;
	}

	/**
	 * Generates and returns the string with the scientific name of this
	 * viral taxon name. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.
	 * This method overrides the generic and inherited
	 * TaxonNameBase#generateTitle() method.
	 *
	 * @return  the string with the composed name of this viral taxon name with authorship (and maybe year)
	 * @see  	common.IdentifiableEntity#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	TaxonNameBase#generateTitle()
	 */
	@Override
	public String generateTitle(){
		logger.warn("not yet implemented");
		return this.toString();
	}

	/**
	 * Returns the boolean value "true" if the components of this viral taxon name
	 * follow the rules of the corresponding
	 * {@link NomenclaturalCode International Code of Virus Classification and Nomenclature},
	 * "false" otherwise.
	 * This method overrides and implements the isCodeCompliant method from
	 * the abstract {@link TaxonNameBase#isCodeCompliant() TaxonNameBase} class.
	 *  
	 * @return  the boolean value expressing the compliance of this viral taxon name to its nomenclatural code
	 * @see	   	TaxonNameBase#isCodeCompliant()
	 */
	@Override
	@Transient
	public boolean isCodeCompliant() {
		logger.warn("not yet implemented");
		return false;
	}
	
	
	/**
	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of this viral taxon name, that is the
	 * International Code of Virus Classification and Nomenclature.
	 * This method overrides the getNomeclaturalCode method from {@link TaxonNameBase#getNomeclaturalCode() TaxonNameBase}.
	 *
	 * @return  the nomenclatural code for viruses
	 * @see  	#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Transient
	@Override
	public NomenclaturalCode getNomeclaturalCode(){
		return NomenclaturalCode.VIRAL();
	}


	/**
	 * Returns the {@link eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy cache strategy} used to generate
	 * several strings corresponding to this viral taxon name.
	 * 
	 * @return  the cache strategy used for this viral taxon name
	 * @see 	eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy
	 * @see     eu.etaxonomy.cdm.strategy.cache.IIdentifiableEntityCacheStrategy
	 */
	@Transient
	@Override
	public INameCacheStrategy getCacheStrategy() {
		return cacheStrategy;
	}


	/**
	 * @see  #getCacheStrategy()
	 */
	@Override
	public void setCacheStrategy(INameCacheStrategy cacheStrategy) {
		this.cacheStrategy = cacheStrategy;
	}

}
