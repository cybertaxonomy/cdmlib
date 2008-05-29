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

/**
 * use name attribute for the entire virus name!  examples see ICTVdb: http://www.
 * ncbi.nlm.nih.gov/ICTVdb/Ictv/vn_indxA.htm
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:02
 */
@Entity
public class ViralName extends TaxonNameBase<ViralName>  {
	static Logger logger = Logger.getLogger(ViralName.class);

	//The accepted acronym for the Virus, e.g. PCV for Peanut Clump Virus
	private String acronym;

	public ViralName(Rank rank) {
		super(rank);
	}

	
	public String getAcronym(){
		return this.acronym;
	}
	public void setAcronym(String acronym){
		this.acronym = acronym;
	}

	@Override
	public String generateTitle(){
		logger.warn("not yet implemented");
		return this.toString();
	}

	@Override
	@Transient
	public boolean isCodeCompliant() {
		logger.warn("not yet implemented");
		return false;
	}
	
	
	@Transient
	@Override
	public NomenclaturalCode getNomeclaturalCode(){
		return NomenclaturalCode.VIRAL();
	}

}