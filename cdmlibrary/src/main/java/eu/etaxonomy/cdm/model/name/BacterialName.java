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
 * Taxon name class for bacteria
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:35:56
 */
@Entity
public class BacterialName extends NonViralName {
	static Logger logger = Logger.getLogger(BacterialName.class);

	//Author team and year of the subgenus name
	@Description("Author team and year of the subgenus name")
	private String subGenusAuthorship;
	//Approbation of name according to approved list, validation list,or validly published, paper in IJSB after 1980
	@Description("Approbation of name according to approved list, validation list,or validly published, paper in IJSB after 1980")
	private String nameApprobation;

	public String getSubGenusAuthorship(){
		return subGenusAuthorship;
	}

	/**
	 * 
	 * @param subGenusAuthorship
	 */
	public void setSubGenusAuthorship(String subGenusAuthorship){
		;
	}

	public String getNameApprobation(){
		return nameApprobation;
	}

	/**
	 * 
	 * @param nameApprobation
	 */
	public void setNameApprobation(String nameApprobation){
		;
	}

}