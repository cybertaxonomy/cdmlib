/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.EventBase;

/**
 * @author a.mueller
 \* @since 17.05.2010
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DerivationEvent", propOrder = {
    "originals",
    "derivatives",
    "institution",
    "type"
})
@XmlRootElement(name = "DerivationEvent")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed
@Audited
public class DerivationEvent extends EventBase implements Cloneable{
	private static final long serialVersionUID = 3661673673962819395L;
	private static final Logger logger = Logger.getLogger(DerivationEvent.class);

	@XmlElementWrapper(name = "Originals")
	@XmlElement(name = "Original")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	@ManyToMany(fetch = FetchType.LAZY,mappedBy="derivationEvents")
	@IndexedEmbedded(depth = 3)
	protected Set<SpecimenOrObservationBase> originals = new HashSet<>();

	@XmlElementWrapper(name = "Derivatives")
	@XmlElement(name = "Derivative")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@OneToMany(fetch=FetchType.LAZY, mappedBy="derivedFrom")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	protected Set<DerivedUnit> derivatives = new HashSet<>();

	@XmlElement(name = "Institution")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@JoinColumn(name="institution_id")
	private Institution institution;

	@XmlElement(name = "DerivationEventType")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private DerivationEventType type;

// ********************* FACTORY  ****************************/

	/**
	 * Factory method
	 * @deprecated Use {@link #NewInstance(DerivationEventType)} or any other
	 * factory method instead to make sure,
	 * the derivation event type is always set.
	 */
	@Deprecated
	public static DerivationEvent NewInstance(){
		return new DerivationEvent();
	}

	// ********************* FACTORY  ****************************/

	/**
	 * Factory method
	 * @return
	 */
	public static DerivationEvent NewInstance(DerivationEventType type){
		DerivationEvent result = new DerivationEvent();
		result.setType(type);
		return result;
	}

	/**
	 * Factory method
	 * @return
	 */
	public static DerivationEvent NewSimpleInstance(SpecimenOrObservationBase original,
	        DerivedUnit derivative, DerivationEventType type){
		DerivationEvent result = NewInstance(type);
		result.addOriginal(original);
		result.addDerivative(derivative);
		return result;
	}

// ************************* CONSTRUCTOR ****************************/

	/**
	 * Constructor
	 */
	protected DerivationEvent() {
		super();
	}

// ********************* GETTER / SETTER / ADDER **********************/

	/**
	 * The specimen or observations that are the input for this derviation event.
	 * @return
	 */
	public Set<SpecimenOrObservationBase> getOriginals() {
		return originals;
	}


	/**
	 * Adds a new input specimen or observation for this derviation event.
	 * @see #getOriginals()
	 * @return
	 */
	public void addOriginal(SpecimenOrObservationBase original) {
		if (! this.originals.contains(original)){
			this.originals.add(original);
			original.addDerivationEvent(this);
		}
	}
	/**
	 * Removes an input specimen or observation for this derviation event.
	 * @see #getOriginals()
	 * @return
	 */
	public void removeOriginal(SpecimenOrObservationBase original) {
	    if (this.originals.contains(original)){
	        this.originals.remove(original);
	        original.removeDerivationEvent(this);
	    }
	}


	/**
	 * The specimen or observations that are the output for this derviation event.
	 * @return
	 */
	public Set<DerivedUnit> getDerivatives() {
		return derivatives;
	}


	/**
	 * Adds a new output specimen or observation for this derivation event.
	 * @see #getDerivatives()
	 * @return
	 */
	public void addDerivative(DerivedUnit derivative) {
		if (derivative != null){
			boolean notExisting = derivatives.add(derivative);
			if (notExisting){
				derivative.setDerivedFrom(this);
			}
		}
	}
	/**
	 * Removes an output specimen or observation for this derviation event.
	 * @see #getDerivatives()
	 * @return
	 */
	public void removeDerivative(DerivedUnit derivative) {
		if (derivative != null){
			derivative.setDerivedFrom(null);
		}
		derivatives.remove(derivative);
	}

    /**
     * #4498
     * @return
     */
    public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	/**
	 * Returns the derivation event type
	 * @return
	 */
	public DerivationEventType getType() {
		return type;
	}

	public void setType(DerivationEventType type) {
		this.type = type;
	}

//*********** CLONE **********************************/

	/**
	 * Clones <i>this</i> derivation event. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> derivation event
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link EventBase EventBase}.
	 *
	 * @see EventBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		try{
			DerivationEvent result = (DerivationEvent)super.clone();
			//type
			result.setType(this.getType());
			//derivates
			result.derivatives = new HashSet<DerivedUnit>();
			for(DerivedUnit derivative : this.derivatives) {
				result.addDerivative(derivative);
			}
			//originals
			result.originals = new HashSet<>();
			for(SpecimenOrObservationBase<?> original : this.originals) {
				result.addOriginal(original);
			}
			//no changes to: -
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}
