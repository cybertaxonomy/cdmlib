/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.taxon;


import etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:23
 */
public class TaxonRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(TaxonRelationship.class);

	private ConceptRelationshipType type;
	private Taxon fromTaxon;
	private Taxon toTaxon;

	public ConceptRelationshipType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(ConceptRelationshipType newVal){
		type = newVal;
	}

	public Taxon getFromTaxon(){
		return fromTaxon;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFromTaxon(Taxon newVal){
		fromTaxon = newVal;
	}

	public Taxon getToTaxon(){
		return toTaxon;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setToTaxon(Taxon newVal){
		toTaxon = newVal;
	}

}