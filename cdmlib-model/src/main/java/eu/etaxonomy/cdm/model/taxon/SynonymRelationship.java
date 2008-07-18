/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A synonym to an ("accepted/correct") taxon must have the same
 * {@link TaxonBase#getSec() concept reference} as the taxon itself.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SynonymRelationship")
@XmlRootElement(name = "SynonymRelationship")
@Entity
public class SynonymRelationship extends RelationshipBase<Synonym, Taxon, SynonymRelationshipType> {
	private static final Logger logger = Logger.getLogger(SynonymRelationship.class);

	
	//for hibernate, don't use
	@Deprecated
	private SynonymRelationship(){
	}
	
	/**
	 * Constructor that adds immediately a relationship instance to both 
	 * the synonym and taxon instance!
	 * @param synoynm
	 * @param taxon
	 * @param type
	 */
	protected SynonymRelationship(Synonym synonym, Taxon taxon, SynonymRelationshipType type, ReferenceBase citation, String citationMicroReference) {
		super(synonym, taxon, type, citation, citationMicroReference);
	}
	
	@Transient
	public Taxon getAcceptedTaxon(){
		return super.getRelatedTo();
	}

	protected void setAcceptedTaxon(Taxon acceptedTaxon){
		super.setRelatedTo(acceptedTaxon);
	}

	@Transient
	public Synonym getSynonym(){
		return super.getRelatedFrom();
	}
	protected void setSynonym(Synonym synoynm){
		super.setRelatedFrom(synoynm);
	}

}