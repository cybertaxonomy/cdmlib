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
 * Taxon name class for cultivars. {only possible for  CULTIVAR, GREX, CONVAR,
 * CULTIVAR_GROUP, GRAFT_CHIMAERA and DENOMINATION_CLASS ranks}
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

	public CultivarPlantName(){
	}

	public static CultivarPlantName NewInstance(Rank rank){
		return new CultivarPlantName(rank, null);
	}

	public static CultivarPlantName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new CultivarPlantName(rank, homotypicalGroup);
	}
	
	protected CultivarPlantName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
	}
	
	public String getCultivarName(){
		return this.cultivarName;
	}

	/**
	 * 
	 * @param cultivarName    cultivarName
	 */
	public void setCultivarName(String cultivarName){
		this.cultivarName = cultivarName;
	}
	
	
	@Transient
	@Override
	public NomenclaturalCode getNomenclaturalCode(){
		return NomenclaturalCode.ICNCP();
	}

}