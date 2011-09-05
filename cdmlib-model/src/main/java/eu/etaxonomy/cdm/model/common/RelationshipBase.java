/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author m.doering
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RelationshipBase")
@XmlRootElement(name = "RelationshipBase")
@MappedSuperclass
public abstract class RelationshipBase<FROM extends IRelated, TO extends IRelated, TYPE extends RelationshipTermBase> extends ReferencedEntityBase implements Cloneable {
	private static final long serialVersionUID = -5030154633820061997L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(RelationshipBase.class);

	@XmlAttribute(name = "isDoubtful")
	private boolean doubtful;
	
	//this set is used only for persistence (CdmDeleteListener) to delete savely relationships and update their former related objects
	@XmlTransient
	@Transient
	protected Set<IRelated> deletedObjects = new HashSet<IRelated>();
	
	
	/**
	 * enumeration and String representation of the <code>relatedFrom</code> and
	 * <code>relatedTo</code> bean propertys. Intended to be used in the
	 * persistence layer only.
	 */
	@XmlEnum
	public enum Direction {
		@XmlEnumValue("relatedFrom")
		relatedFrom, 
		@XmlEnumValue("relatedTo")
		relatedTo
	}
	
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
	protected RelationshipBase(FROM from, TO to, TYPE type, Reference citation, String citationMicroReference) {
		super(citation, citationMicroReference, null);
		setRelatedFrom(from);
		setRelatedTo(to);
		setType(type);
		from.addRelationship(this);
		to.addRelationship(this);
	}
	
	public abstract TYPE getType();
	
	public abstract void setType(TYPE type);
	
	protected abstract FROM getRelatedFrom();
	
	protected abstract void setRelatedFrom(FROM relatedFrom);
	
	protected abstract TO getRelatedTo();
	
	protected abstract void setRelatedTo(TO relatedTo);
	
	/**
	 * A boolean flag that marks the relationship between two objects as doubtful
	 * Please be aware that this flag should not be used to mark any status of the 
	 * objects themselfs. E.g. when marking a synonym relationship as doubtful
	 * this means that it is doubtful that the synonym is really a synonym to the
	 * taxon. It does not mean that the synonym is doubtfully a synonym.
	 * @return true, if the relationship is doubtful, false otherwise
	 */
	public boolean isDoubtful(){
		return this.doubtful;
	}
	
	public void setDoubtful(boolean doubtful){
		this.doubtful = doubtful;
	}
	
	public boolean isRemoved(){
		if ( this.getRelatedFrom() == null ^ this.getRelatedTo() == null){
			throw new IllegalStateException("A relationship may have only both related object as null or none. But just one is null!");
		}
		return this.getRelatedFrom() == null || this.getRelatedTo() == null;
	}
	
	public Set getDeletedObjects(){
		return this.deletedObjects;
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
	
	
//*********************** CLONE ********************************************************/
		
	/** 
	 * Clones <i>this</i> relationship. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> relationship by
	 * modifying only some of the attributes.
	 * @throws CloneNotSupportedException 
	 * 
	 * @see eu.etaxonomy.cdm.model.common.RelationshipBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		RelationshipBase<FROM,TO,TYPE> result = (RelationshipBase<FROM,TO,TYPE>)super.clone();
		//no changes to: doubtful, deletedObjects
		return result;
	}	
}