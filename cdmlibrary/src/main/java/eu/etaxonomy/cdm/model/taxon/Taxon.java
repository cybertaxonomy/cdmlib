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
 * @created 02-Nov-2007 19:36:35
 */
@Entity
public class Taxon extends TaxonBase {
	static Logger logger = Logger.getLogger(Taxon.class);

	private ArrayList descriptions;
	private ArrayList inverseSynonymRelations;
	private ArrayList taxonInSource;
	private ArrayList taxonRelations;
	private ArrayList inverseTaxonRelations;

	public ArrayList getTaxonInSource(){
		return taxonInSource;
	}

	/**
	 * 
	 * @param taxonInSource
	 */
	public void setTaxonInSource(ArrayList taxonInSource){
		;
	}

	public ArrayList getDescriptions(){
		return descriptions;
	}

	/**
	 * 
	 * @param descriptions
	 */
	public void setDescriptions(ArrayList descriptions){
		;
	}

	public ArrayList getInverseSynonymRelations(){
		return inverseSynonymRelations;
	}

	/**
	 * 
	 * @param inverseSynonymRelations
	 */
	public void setInverseSynonymRelations(ArrayList inverseSynonymRelations){
		;
	}

	public ArrayList getTaxonRelations(){
		return taxonRelations;
	}

	/**
	 * 
	 * @param taxonRelations
	 */
	public void setTaxonRelations(ArrayList taxonRelations){
		;
	}

	public ArrayList getInverseTaxonRelations(){
		return inverseTaxonRelations;
	}

	/**
	 * 
	 * @param inverseTaxonRelations
	 */
	public void setInverseTaxonRelations(ArrayList inverseTaxonRelations){
		;
	}

	@Override
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}