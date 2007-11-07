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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * Taxon name class for cultivars.
 * {only possible for  CULTIVAR, GREX, CONVAR, CULTIVAR_GROUP, GRAFT_CHIMAERA and
 * DENOMINATION_CLASS ranks}
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:02
 */
@Entity
public class CultivarPlantName extends BotanicalName {
	static Logger logger = Logger.getLogger(CultivarPlantName.class);

	//the caracteristical name of the cultivar
	@Description("the caracteristical name of the cultivar")
	private String cultivarName;

	public CultivarPlantName(Rank rank) {
		super(rank);
	}

	public String getCultivarName(){
		return cultivarName;
	}

	/**
	 * 
	 * @param cultivarName
	 */
	public void setCultivarName(String cultivarName){
		;
	}

}