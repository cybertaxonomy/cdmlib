/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:58
 */
public class TaxonRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(TaxonRelationship.class);
	private ConceptRelationshipType type;
	private IdentifiableEntity fromTaxon;
	private IdentifiableEntity toTaxon;

	public ConceptRelationshipType getType(){
		return this.type;
	}

	/**
	 * 
	 * @param type    type
	 */
	public void setType(ConceptRelationshipType type){
		this.type = type;
	}

	public IdentifiableEntity getFromTaxon(){
		return this.fromTaxon;
	}

	/**
	 * 
	 * @param fromTaxon    fromTaxon
	 */
	public void setFromTaxon(IdentifiableEntity fromTaxon){
		this.fromTaxon = fromTaxon;
	}

	public IdentifiableEntity getToTaxon(){
		return this.toTaxon;
	}

	/**
	 * 
	 * @param toTaxon    toTaxon
	 */
	public void setToTaxon(IdentifiableEntity toTaxon){
		this.toTaxon = toTaxon;
	}

}