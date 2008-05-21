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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@Entity
public class SynonymRelationship extends RelationshipBase<Synonym, Taxon, SynonymRelationshipType> {
	static Logger logger = Logger.getLogger(SynonymRelationship.class);
//	private Synonym synonym;
//	private Taxon acceptedTaxon;
//	private SynonymRelationshipType type;

	
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
	
//	@ManyToOne(fetch=FetchType.EAGER)
//	@Cascade({CascadeType.SAVE_UPDATE})
	@Transient
	public Taxon getAcceptedTaxon(){
		return super.getRelatedTo();
	}

	protected void setAcceptedTaxon(Taxon acceptedTaxon){
		super.setRelatedTo(acceptedTaxon);
	}

//	@ManyToOne(fetch=FetchType.EAGER)
//	@Cascade({CascadeType.SAVE_UPDATE})
	@Transient
	public Synonym getSynonym(){
		return super.getRelatedFrom();
	}
	protected void setSynonym(Synonym synoynm){
		super.setRelatedFrom(synoynm);
	}

}