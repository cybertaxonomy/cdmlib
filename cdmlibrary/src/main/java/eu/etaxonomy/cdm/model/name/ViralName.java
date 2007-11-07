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
 * use name attribute for the entire virus name!
 * 
 * examples see ICTVdb:
 * http://www.ncbi.nlm.nih.gov/ICTVdb/Ictv/vn_indxA.htm
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:41
 */
@Entity
public class ViralName extends TaxonNameBase {
	static Logger logger = Logger.getLogger(ViralName.class);

	//The accepted acronym for the Virus, e.g. PCV for Peanut Clump Virus
	@Description("The accepted acronym for the Virus, e.g. PCV for Peanut Clump Virus")
	private String acronym;

	public ViralName(Rank rank) {
		super(rank);
	}

	public String getAcronym(){
		return acronym;
	}

	/**
	 * 
	 * @param acronym
	 */
	public void setAcronym(String acronym){
		;
	}

	@Override
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}