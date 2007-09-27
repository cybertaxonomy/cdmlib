/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.Media;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:35:59
 */
@Entity
public class AcceptedTaxon extends TaxonBase {
	static Logger logger = Logger.getLogger(AcceptedTaxon.class);

	private ArrayList facts;
	private ArrayList inverseSynonymRelations;
	private ArrayList medias;
	private ArrayList taxonRelations;
	private ArrayList inverseTaxonRelations;

	public ArrayList getFacts(){
		return facts;
	}

	public ArrayList getInverseSynonymRelations(){
		return inverseSynonymRelations;
	}

	public ArrayList getInverseTaxonRelations(){
		return inverseTaxonRelations;
	}

	public ArrayList getMedias(){
		return medias;
	}

	public ArrayList getTaxonRelations(){
		return taxonRelations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFacts(ArrayList newVal){
		facts = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInverseSynonymRelations(ArrayList newVal){
		inverseSynonymRelations = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInverseTaxonRelations(ArrayList newVal){
		inverseTaxonRelations = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setMedias(ArrayList newVal){
		medias = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTaxonRelations(ArrayList newVal){
		taxonRelations = newVal;
	}

}