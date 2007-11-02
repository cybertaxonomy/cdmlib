/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.taxon;


import etaxonomy.cdm.model.description.Description;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:20
 */
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
	 * @param newVal
	 */
	public void setTaxonInSource(ArrayList newVal){
		taxonInSource = newVal;
	}

	public ArrayList getDescriptions(){
		return descriptions;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDescriptions(ArrayList newVal){
		descriptions = newVal;
	}

	public ArrayList getInverseSynonymRelations(){
		return inverseSynonymRelations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInverseSynonymRelations(ArrayList newVal){
		inverseSynonymRelations = newVal;
	}

	public ArrayList getTaxonRelations(){
		return taxonRelations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTaxonRelations(ArrayList newVal){
		taxonRelations = newVal;
	}

	public ArrayList getInverseTaxonRelations(){
		return inverseTaxonRelations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInverseTaxonRelations(ArrayList newVal){
		inverseTaxonRelations = newVal;
	}

}