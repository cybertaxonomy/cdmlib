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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;

/**
 * The taxon name class for viral taxa. The scientific name will be stored
 * as a string (consisting eventually of several words even combined also with
 * non alphabetical characters) in the inherited {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#setTitleCache(String) titleCache} attribute.
 * Classification has no influence on the names of viral taxon names and no
 * viral taxon must be taxonomically included in another viral taxon with
 * higher rank. For examples see ICTVdb:
 * "http://www.ncbi.nlm.nih.gov/ICTVdb/Ictv/vn_indxA.htm"
 * <P>
 * This class corresponds to: NameViral according to the ABCD schema.
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:02
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "acronym"
})
@XmlRootElement(name = "ViralName")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
@Audited
@Configurable
public class ViralName extends TaxonNameBase<ViralName, INameCacheStrategy<ViralName>> implements Cloneable {
	private static final long serialVersionUID = 4516625507432071817L;
	private static final Logger logger = Logger.getLogger(ViralName.class);

//    @XmlTransient
//    @Transient
//	protected INameCacheStrategy<ViralName> cacheStrategy;

	@XmlElement(name = "Acronym")
	@Field
    //TODO Val #3379
//	@NullOrNotEmpty
	@Column(length=255)
	private String acronym;

	// ************* CONSTRUCTORS *************/

	protected ViralName(){
		super();
	}

	/**
	 * Class constructor: creates a new viral taxon name instance
	 * only containing its {@link Rank rank}.
	 *
	 * @param	rank  the rank to be assigned to <i>this</i> viral taxon name
	 * @see 	TaxonNameBase#TaxonNameBase(Rank)
	 */
	public ViralName(Rank rank) {
		super(rank);
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

	//********* METHODS **************************************/

	/**
	 * Creates a new viral taxon name instance only containing its {@link Rank rank}.
	 *
	 * @param	rank  the rank to be assigned to <i>this</i> viral taxon name
	 * @see 	#ViralName(Rank)
	 */
	public static ViralName NewInstance(Rank rank){
		return new ViralName(rank);
	}

	/**
	 * Returns the accepted acronym (an assigned abbreviation) string for <i>this</i>
	 * viral taxon name. For instance PCV stays for Peanut Clump Virus.
	 *
	 * @return  the string containing the accepted acronym of <i>this</i> viral taxon name
	 */
	public String getAcronym(){
		return this.acronym;
	}
	/**
	 * @see  #getAcronym()
	 */
	public void setAcronym(String acronym){
		this.acronym = StringUtils.isBlank(acronym)? null : acronym;
	}

	/**
	 * Generates and returns the string with the scientific name of <i>this</i>
	 * viral taxon name. This string may be stored in the inherited
	 * {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} attribute.
	 * This method overrides the generic and inherited
	 * method from {@link TaxonNameBase TaxonNameBase} .
	 *
	 * @return  the string with the composed name of <i>this</i> viral taxon name with authorship (and maybe year)
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
	 * @see  	TaxonNameBase#generateTitle()
	 */
//	@Override
//	public String generateTitle(){
//		logger.warn("not yet implemented");
//		return this.toString();
//	}

	@Override
	public String generateFullTitle(){
		logger.warn("not yet implemented");
		return this.toString();
	}

	/**
	 * Returns the boolean value "true" if the components of <i>this</i> viral taxon name
	 * follow the rules of the corresponding
	 * {@link NomenclaturalCode International Code of Virus Classification and Nomenclature},
	 * "false" otherwise.
	 * This method overrides and implements the isCodeCompliant method from
	 * the abstract {@link TaxonNameBase#isCodeCompliant() TaxonNameBase} class.
	 *
	 * @return  the boolean value expressing the compliance of <i>this</i> viral taxon name to its nomenclatural code
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
	 * the construction of <i>this</i> viral taxon name, that is the
	 * International Code of Virus Classification and Nomenclature.
	 * This method overrides the getNomenclaturalCode method from {@link TaxonNameBase TaxonNameBase}.
	 *
	 * @return  the nomenclatural code for viruses
	 * @see  	#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 * @see  	TaxonNameBase#getNomenclaturalCode()
	 */
	@Override
	public NomenclaturalCode getNomenclaturalCode(){
		return NomenclaturalCode.ICVCN;
	}


	/**
	 * Returns the {@link eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy cache strategy} used to generate
	 * several strings corresponding to <i>this</i> viral taxon name.
	 *
	 * @return  the cache strategy used for <i>this</i> viral taxon name
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see     eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
//	@Override
//	@Transient
//	public INameCacheStrategy getCacheStrategy() {
//		return cacheStrategy;
//	}


	/**
	 * @see  #getCacheStrategy()
	 */
//	@Override
//	public void setCacheStrategy(INameCacheStrategy cacheStrategy) {
//		this.cacheStrategy = cacheStrategy;
//	}


//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> viral name. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> viral name by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.name.TaxonNameBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		ViralName result = (ViralName)super.clone();
		//no changes to: acronym
		return result;
	}
}
