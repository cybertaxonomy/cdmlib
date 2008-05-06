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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@Entity
public class SynonymRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(SynonymRelationship.class);
	private Synonym synonym;
	private Taxon acceptedTaxon;
	private SynonymRelationshipType type;

	
	//for hibernate, don't use
	@Deprecated
	private SynonymRelationship(){
	}
	
	protected SynonymRelationship(Synonym synoynm, Taxon taxon, SynonymRelationshipType type) {
		super();
		this.synonym = synoynm;
		taxon.addSynonymRelation(this);
		synoynm.addSynonymRelation(this);
		this.acceptedTaxon = taxon;
		this.type = type;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Taxon getAcceptedTaxon(){
		return this.acceptedTaxon;
	}

	private void setAcceptedTaxon(Taxon acceptedTaxon){
		this.acceptedTaxon = acceptedTaxon;
	}

	
	@ManyToOne
	public SynonymRelationshipType getType(){
		return this.type;
	}
	private void setType(SynonymRelationshipType type){
		this.type = type;
	}

	
	@ManyToOne(fetch=FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Synonym getSynonym(){
		return this.synonym;
	}
	private void setSynonym(Synonym synoynm){
		this.synonym = synoynm;
	}

}