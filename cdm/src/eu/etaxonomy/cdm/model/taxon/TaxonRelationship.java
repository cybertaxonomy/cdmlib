/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.publication.PublicationBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:16
 */
@Entity
public class TaxonRelationship extends VersionableEntity {
	static Logger logger = Logger.getLogger(TaxonRelationship.class);

	private PublicationBase citation;
	private AcceptedTaxon fromTaxon;
	private AcceptedTaxon toTaxon;
	private ConceptRelationshipType type;

	public PublicationBase getCitation(){
		return citation;
	}

	public AcceptedTaxon getFromTaxon(){
		return fromTaxon;
	}

	public AcceptedTaxon getToTaxon(){
		return toTaxon;
	}

	public ConceptRelationshipType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCitation(PublicationBase newVal){
		citation = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFromTaxon(AcceptedTaxon newVal){
		fromTaxon = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setToTaxon(AcceptedTaxon newVal){
		toTaxon = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(ConceptRelationshipType newVal){
		type = newVal;
	}

}