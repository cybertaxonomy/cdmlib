/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:44
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
	 * @param type
	 */
	public void setType(ConceptRelationshipType type){
		;
	}

	public Taxon getFromTaxon(){
		return fromTaxon;
	}

	/**
	 * 
	 * @param fromTaxon
	 */
	public void setFromTaxon(Taxon fromTaxon){
		;
	}

	public Taxon getToTaxon(){
		return toTaxon;
	}

	/**
	 * 
	 * @param toTaxon
	 */
	public void setToTaxon(Taxon toTaxon){
		;
	}

}