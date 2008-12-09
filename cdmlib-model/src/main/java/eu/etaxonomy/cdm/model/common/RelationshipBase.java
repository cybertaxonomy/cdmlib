/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author m.doering
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RelationshipBase", propOrder = { })
@XmlRootElement(name = "RelationshipBase")
@MappedSuperclass
public abstract class RelationshipBase<FROM extends IRelated, TO extends IRelated, TYPE extends RelationshipTermBase> extends ReferencedEntityBase {
	private static final long serialVersionUID = -5030154633820061997L;
	static Logger logger = Logger.getLogger(RelationshipBase.class);

	protected RelationshipBase(){
		super();
	}
	
	/**
	 * Creates a relationship between 2 objects and adds it to the respective
	 * relation sets of both objects.
	 * 
	 * @param from
	 * @param to
	 * @param type
	 * @param citation
	 * @param citationMicroReference
	 */
	protected RelationshipBase(FROM from, TO to, TYPE type, ReferenceBase citation, String citationMicroReference) {
		super(citation, citationMicroReference, null);
		setRelatedFrom(from);
		setRelatedTo(to);
		setType(type);
		from.addRelationship(this);
		to.addRelationship(this);
	}
	
	@Transient
	public abstract TYPE getType();
	
	protected abstract void setType(TYPE type);
	
	@Transient
	protected abstract FROM getRelatedFrom();
	
	protected abstract void setRelatedFrom(FROM relatedFrom);
	
	@Transient
	protected abstract TO getRelatedTo();
	
	protected abstract void setRelatedTo(TO relatedTo);
	
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