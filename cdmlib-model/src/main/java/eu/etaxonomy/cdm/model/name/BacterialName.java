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
 * Taxon name class for bacteria
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:11
 */
@Entity
public class BacterialName extends NonViralName {
	static Logger logger = Logger.getLogger(BacterialName.class);

	//Author team and year of the subgenus name
	private String subGenusAuthorship;
	//Approbation of name according to approved list, validation list,or validly published, paper in IJSB after 1980
	private String nameApprobation;

	public static BacterialName NewInstance(Rank rank){
		return new BacterialName(rank, null);
	}

	public static BacterialName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new BacterialName(rank, homotypicalGroup);
	}

	protected BacterialName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
	}
	
	public String getSubGenusAuthorship(){
		return this.subGenusAuthorship;
	}

	/**
	 * 
	 * @param subGenusAuthorship    subGenusAuthorship
	 */
	public void setSubGenusAuthorship(String subGenusAuthorship){
		this.subGenusAuthorship = subGenusAuthorship;
	}

	public String getNameApprobation(){
		return this.nameApprobation;
	}

	/**
	 * 
	 * @param nameApprobation    nameApprobation
	 */
	public void setNameApprobation(String nameApprobation){
		this.nameApprobation = nameApprobation;
	}
	
	
	@Transient
	@Override
	public NomenclaturalCode getNomeclaturalCode(){
		return NomenclaturalCode.BACTERIOLOGICAL();

	}

}