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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Concrete implementations of this abstract base class express a directed relationship between two
 * cdm entities ( {@link IRelated} ). The most important properties of the relationship are:
 * <ul>
 * <li>{@link #getRelatedFrom(IRelated)}</li>
 * <li>{@link #getRelatedTo(IRelated)}</li>
 * <li>The {@code <TYPE>}, a RelationshipTermBase which specifies the kind of the relationship</li>
 * </ul>
 * A relationship thus is forming a directed graph consisting of two nodes and an edge:
 * <pre>
     relatedFrom -----[TYPE]----> relatedTo
   </pre>
 * Whereas the direction of the relation can be valid for the direct (everted) and also for the inverted {@link Direction} direction.
 * This directional validity is defined by {@link RelationshipTermBase#isSymmetric()}
 *
 *
 *
 * @author m.doering
 * @author a.kohlbecker (Documentation)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RelationshipBase")
@XmlRootElement(name = "RelationshipBase")
@MappedSuperclass
@Audited
public abstract class RelationshipBase<FROM extends IRelated, TO extends IRelated, TYPE extends RelationshipTermBase> extends ReferencedEntityBase implements Cloneable {
    private static final long serialVersionUID = -5030154633820061997L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RelationshipBase.class);

    @XmlAttribute(name = "isDoubtful")
    private boolean doubtful;


    /**
     * Enumeration and String representation of the <code>relatedFrom</code> (invers) and
     * <code>relatedTo</code> (direct, everted) bean properties. Intended to be used in the
     * persistence layer only.( But also used in the service layer now - a.kohlbecker )
     *
     * See also {@link RelationshipBase} for an explanation on relationships in general.
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
     * objects themselves. E.g. when marking a taxon relationship as doubtful
     * this means that it is doubtful that the 2 taxon concept are related in
     * such a way. It does NOT mean that any of the taxa itself is doubtful.
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

 /*   public Set getDeletedObjects(){
        return this.deletedObjects;
    }
*/
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

        //no changes to: doubtful
        return result;
    }
}
