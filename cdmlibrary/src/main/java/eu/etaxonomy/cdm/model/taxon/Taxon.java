/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.description.Description;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:56
 */
public class Taxon extends TaxonBase {
	static Logger logger = Logger.getLogger(Taxon.class);
	private ArrayList descriptions;
	private ArrayList inverseSynonymRelations;
	private ArrayList taxonInSource;
	private ArrayList taxonRelations;
	private ArrayList inverseTaxonRelations;

	public ArrayList getTaxonInSource(){
		return this.taxonInSource;
	}

	/**
	 * 
	 * @param taxonInSource    taxonInSource
	 */
	public void setTaxonInSource(ArrayList taxonInSource){
		this.taxonInSource = taxonInSource;
	}

	public ArrayList getDescriptions(){
		return this.descriptions;
	}

	/**
	 * 
	 * @param descriptions    descriptions
	 */
	public void setDescriptions(ArrayList descriptions){
		this.descriptions = descriptions;
	}

	public ArrayList getInverseSynonymRelations(){
		return this.inverseSynonymRelations;
	}

	/**
	 * 
	 * @param inverseSynonymRelations    inverseSynonymRelations
	 */
	public void setInverseSynonymRelations(ArrayList inverseSynonymRelations){
		this.inverseSynonymRelations = inverseSynonymRelations;
	}

	public ArrayList getTaxonRelations(){
		return this.taxonRelations;
	}

	/**
	 * 
	 * @param taxonRelations    taxonRelations
	 */
	public void setTaxonRelations(ArrayList taxonRelations){
		this.taxonRelations = taxonRelations;
	}

	public ArrayList getInverseTaxonRelations(){
		return this.inverseTaxonRelations;
	}

	/**
	 * 
	 * @param inverseTaxonRelations    inverseTaxonRelations
	 */
	public void setInverseTaxonRelations(ArrayList inverseTaxonRelations){
		this.inverseTaxonRelations = inverseTaxonRelations;
	}

	@Override
	public String generateTitle(){
		return "";
	}

}