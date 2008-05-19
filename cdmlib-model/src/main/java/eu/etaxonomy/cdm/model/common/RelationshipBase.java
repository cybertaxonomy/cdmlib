/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import java.util.UUID;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;

/**
 * @author m.doering
 */
@MappedSuperclass
public abstract class RelationshipBase<FROM extends IRelated, TO extends IRelated, TYPE extends RelationshipTermBase> extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(RelationshipBase.class);
	private FROM relationFrom;
	private TO relationTo;
	private TYPE type;

	protected RelationshipBase(){
		super();
	}
	
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
	protected void setType(TYPE type){
		this.type = type;
	}
	
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	protected FROM getRelationFrom() {
		return relationFrom;
	}
	protected void setRelationFrom(FROM relationFrom) {
		this.relationFrom = relationFrom;
	}

	
	@ManyToOne(fetch=FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	protected TO getRelationTo() {
		return relationTo;
	}
	protected void setRelationTo(TO relationTo) {
		this.relationTo = relationTo;
	}
	
// TODO
//	UUID toUuid; 
//	UUID fromUuid;
//	
//	@Transient
//	public UUID getToUuidCache(){
//		return relationTo.getUuid();
//	}
//	protected void setToUuid(UUID uuid){
//		toUuid = uuid;
//	}
//	
//	public UUID getFromUuid(){
//		return relationTo.getUuid();
//	}
//	protected void setFromUuid(UUID uuid){
//		fromUuid = uuid;
//	}
}