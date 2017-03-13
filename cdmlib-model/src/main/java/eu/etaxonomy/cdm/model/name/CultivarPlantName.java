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

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The taxon name class for cultivars (cultivated plants). The only possible
 * {@link Rank ranks} for cultivars are CULTIVAR, GREX, CONVAR, CULTIVAR_GROUP,
 * GRAFT_CHIMAERA or DENOMINATION_CLASS.
 * <P>
 * This class corresponds partially to: NameBotanical according to the
 * ABCD schema.
 *
 * @author m.doering
 * @created 08-Nov-2007 13:06:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
@XmlRootElement(name = "CultivarPlantName")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
@Audited
@Configurable
public class CultivarPlantName
        extends BotanicalName {
    private static final long serialVersionUID = -7948375817971980004L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CultivarPlantName.class);


	// ************* CONSTRUCTORS *************/
	/**
	 * Class constructor: creates a new cultivar taxon name instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 *
	 * @see #CultivarPlantName(Rank, HomotypicalGroup)
	 * @see eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	public CultivarPlantName(){
		super();
	}

	/**
	 * Class constructor: creates a new cultivar taxon name instance
	 * only containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new cultivar taxon name instance will be also added to the set of
	 * cultivar taxon names belonging to this homotypical group.
	 *
	 * @param	rank  the rank to be assigned to <i>this</i> cultivar taxon name
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> cultivar taxon name belongs
	 * @see 	#CultivarPlantName()
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	protected CultivarPlantName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
	}


//*************************


	/**
	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of <i>this</i> cultivar taxon name, that is the
	 * International Code of Nomenclature for Cultivated Plants. This method
	 * overrides the getNomenclaturalCode method from {@link NonViralName#getNomenclaturalCode() NonViralName}.
	 *
	 * @return  the nomenclatural code for cultivated plants
	 * @see  	NonViralName#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Override
	public NomenclaturalCode getNomenclaturalCode(){
		return NomenclaturalCode.ICNCP;
	}


//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> cultivar plant name. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> cultivar plant name by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.name.BotanicalName#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		CultivarPlantName result = (CultivarPlantName)super.clone();
		//no changes to:
		return result;
	}

}
