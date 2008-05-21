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

import java.util.*;
import javax.persistence.*;

/**
 * use name attribute for the entire virus name!  examples see ICTVdb: http://www.
 * ncbi.nlm.nih.gov/ICTVdb/Ictv/vn_indxA.htm
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:02
 */
@Entity
public class ViralName extends TaxonNameBase {
	static Logger logger = Logger.getLogger(ViralName.class);

	//The accepted acronym for the Virus, e.g. PCV for Peanut Clump Virus
	private String acronym;

	public ViralName(Rank rank) {
		super(rank);
		// TODO Auto-generated constructor stub
	}

	
	public String getAcronym(){
		return this.acronym;
	}
	public void setAcronym(String acronym){
		this.acronym = acronym;
	}

	@Override
	public String generateTitle(){
		return "";
	}

	@Override
	@Transient
	public boolean isCodeCompliant() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	@Transient
	@Override
	public NomenclaturalCode getNomeclaturalCode(){
		return NomenclaturalCode.VIRAL();
	}
}