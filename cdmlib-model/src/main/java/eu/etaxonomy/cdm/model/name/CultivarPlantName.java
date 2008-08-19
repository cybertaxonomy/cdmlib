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
 * The taxon name class for cultivars (cultivated plants). The only possible
 * {@link Rank ranks} for cultivars are CULTIVAR, GREX, CONVAR, CULTIVAR_GROUP,
 * GRAFT_CHIMAERA or DENOMINATION_CLASS.
 * <P>
 * This class corresponds partially to: NameBotanical according to the
 * ABCD schema.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cultivarName"
})
@XmlRootElement(name = "CultivarPlantName")
@Entity
public class CultivarPlantName extends BotanicalName {
	static Logger logger = Logger.getLogger(CultivarPlantName.class);
	
	//the characteristical name of the cultivar
    @XmlElement(name = "CultivarName", required = true)
	private String cultivarName;

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
	
	//********* METHODS **************************************/
	
	/** 
	 * Creates a new cultivar taxon name instance
	 * only containing its {@link Rank rank} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank	the rank to be assigned to <i>this</i> cultivar taxon name
	 * @see 			#CultivarPlantName(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, HomotypicalGroup)
	 * @see 			eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	public static CultivarPlantName NewInstance(Rank rank){
		return new CultivarPlantName(rank, null);
	}

	/** 
	 * Creates a new cultivar taxon name instance
	 * only containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new cultivar taxon name instance will be also added to the set of
	 * cultivar taxon names belonging to this homotypical group.
	 * 
	 * @param  rank  the rank to be assigned to <i>this</i> cultivar taxon name
	 * @param  homotypicalGroup  the homotypical group to which <i>this</i> cultivar taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #CultivarPlantName(Rank, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	public static CultivarPlantName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new CultivarPlantName(rank, homotypicalGroup);
	}
	
	/** 
	 * Returns the characteristical cultivar name part string assigned to <i>this</i>
	 * cultivar taxon name. In the scientific name "Clematis alpina 'Ruby'" for
	 * instance this characteristical string is "Ruby". This part of the name is
	 * governed by the International Code for the Nomenclature of Cultivated
	 * Plants and the string should include neither quotes nor + signs
	 * (these elements of the name cache string will be generated by the
	 * {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}).
	 */
	public String getCultivarName(){
		return this.cultivarName;
	}

	/**
	 * @see  #getCultivarName()
	 */
	public void setCultivarName(String cultivarName){
		this.cultivarName = cultivarName;
	}
	
	
	/**
	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of <i>this</i> cultivar taxon name, that is the
	 * International Code of Nomenclature for Cultivated Plants. This method
	 * overrides the getNomeclaturalCode method from {@link NonViralName#getNomeclaturalCode() NonViralName}.
	 *
	 * @return  the nomenclatural code for cultivated plants
	 * @see  	NonViralName#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Transient
	@Override
	public NomenclaturalCode getNomenclaturalCode(){
		return NomenclaturalCode.ICNCP();
	}

}