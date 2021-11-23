/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.occurrence;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.SingleSourcedEntityBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.term.DefinedTerm;

/**
 * The class representing the assignment of a occurrence status to a
 * {@link DerivedUnit derived unit}. This includes an {@link OccurrenceStatusType occurrence status type}
 * (for instance "destroyed", "lost" or "not seen by ...").
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:39
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OccurrenceStatus", propOrder = {
    "unit",
    "type"
})
@Entity
@Audited
public class OccurrenceStatus
        extends SingleSourcedEntityBase {

    private static final long serialVersionUID = 623891726208046243L;
    private static Logger logger = Logger.getLogger(OccurrenceStatus.class);

	@XmlElement(name = "OccurrenceStatusType")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private DefinedTerm type;

    @XmlElement(name = "Name")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private DerivedUnit unit;

// ************************** FACTORY *********************************/

    /**
	 * Creates a new occurrence status instance with a given
	 * occurrence status type.
	 *
	 * @see #OccurrenceStatus()
	 */
	public static OccurrenceStatus NewInstance(DefinedTerm occurrenceStatusType){
		return NewInstance(occurrenceStatusType, null, null);
	}


	/**
	 * Creates a new occurrence status instance with a given
	 * occurrence status type.
	 *
	 * @see #OccurrenceStatus()
	 */
	public static OccurrenceStatus NewInstance(DefinedTerm occurrenceStatusType, Reference citation, String microCitation){
		OccurrenceStatus status = new OccurrenceStatus();
		status.setType(occurrenceStatusType);
		status.setCitation(citation);
		status.setCitationMicroReference(microCitation);
		return status;
	}

// ************************ CONSTRUCTOR *************************/

	protected OccurrenceStatus() {
        super();
    }

// ************************ GETTER / SETTER ************************/

    public DerivedUnit getUnit() {
        return unit;
    }
    protected void setUnit(DerivedUnit unit) {
        if (this.unit != null && !this.unit.equals(unit)){
            this.unit.removeStatus(this);
        }
        this.unit = unit;
        if (unit != null){
            unit.addStatus(this);
        }
    }

	/**
	 * Returns the occurrence status type of <i>this</i>
	 * occurrence status.
	 */
	public DefinedTerm getType(){
		return this.type;
	}
	/**
	 * @see  #getType()
	 */
	public void setType(DefinedTerm type){
		this.type = type;
	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> occurrence status. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> occurrence status by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.common.SingleSourcedEntityBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public OccurrenceStatus clone() {
		try {
			OccurrenceStatus result = (OccurrenceStatus)super.clone();
	        //no changes to: type
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}