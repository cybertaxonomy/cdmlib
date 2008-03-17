/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 */
@MappedSuperclass
public abstract class RelationshipBase<TO extends IRelated, FROM extends IRelated, TYPE extends RelationshipTermBase> extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(RelationshipBase.class);
	private FROM relationFrom;
	private TO relationTo;
	private TYPE type;

	/**
	 * creates a relationship between 2 names and adds this relationship object to the respective name relation sets
	 * @param toName
	 * @param fromName
	 * @param type
	 * @param ruleConsidered
	 */
	protected RelationshipBase(FROM from, TO to, TYPE type, ReferenceBase citation, String citationMicroReference) {
		super(citation, citationMicroReference, null);
		setRelationFrom(from);
		setRelationTo(to);
		setType(type);
		from.addRelationship(this);
		to.addRelationship(this);
	}
	
	@ManyToOne
	public TYPE getType(){
		return this.type;
	}
	private void setType(TYPE type){
		this.type = type;
	}
	
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public FROM getRelationFrom() {
		return relationFrom;
	}
	private void setRelationFrom(FROM relationFrom) {
		this.relationFrom = relationFrom;
	}

	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TO getRelationTo() {
		return relationTo;
	}
	private void setRelationTo(TO relationTo) {
		this.relationTo = relationTo;
	}

}