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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

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
 * @created 08-Nov-2007 13:07:02
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
@XmlRootElement(name = "ViralName")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
@Audited
@Configurable
public class ViralName
            extends TaxonNameBase<ViralName, INameCacheStrategy> {
    private static final long serialVersionUID = -6201649691028218290L;

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
	protected ViralName(Rank rank) {
		super(rank);
	}

// ************************* METHODS **************************/

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
		//no changes to:
		return result;
	}
}
