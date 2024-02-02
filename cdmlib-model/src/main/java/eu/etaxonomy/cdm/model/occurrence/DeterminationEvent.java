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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.term.DefinedTerm;

/**
 * @author m.doering
 * @since 08-Nov-2007 13:06:21
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeterminationEvent", propOrder = {
    "identifiedUnit",
    "taxon",
    "taxonName",
    "modifier",
    "preferredFlag",
    "references"
})
@XmlRootElement(name = "DeterminationEvent")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed
@Audited
public class DeterminationEvent extends EventBase {

	private static final long serialVersionUID = 5065341354427569773L;

	private static final Logger logger = LogManager.getLogger();

	@XmlElement(name = "IdentifiedUnit")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private SpecimenOrObservationBase<?> identifiedUnit;

	@XmlElement(name = "Taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded(includeEmbeddedObjectId=true)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private TaxonBase<?> taxon;

//	#4518
	@XmlElement(name = "TaxonName")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded(includeEmbeddedObjectId=true)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private TaxonName taxonName;

	@XmlElement(name = "Modifier")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private DefinedTerm modifier;

	@XmlElement(name = "PreferredFlag")
	private boolean preferredFlag;

	@XmlElementWrapper(name = "References")
	@XmlElement(name = "Reference")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Set<Reference> references = new HashSet<>();

//******************************* FACTORY ********************************/

	public static DeterminationEvent NewInstance(){
		return new DeterminationEvent();
	}

	public static DeterminationEvent NewInstance(Taxon taxon, SpecimenOrObservationBase identifiedUnit ){
		DeterminationEvent result = new DeterminationEvent();
		result.setTaxon(taxon);
		result.setIdentifiedUnit(identifiedUnit);
		identifiedUnit.addDetermination(result);
		return result;
	}

	public static DeterminationEvent NewInstance(TaxonName name, SpecimenOrObservationBase identifiedUnit ){
		DeterminationEvent result = new DeterminationEvent();
		result.setTaxonName(name);
		result.setIdentifiedUnit(identifiedUnit);
		identifiedUnit.addDetermination(result);
		return result;
	}

    public static DeterminationEvent NewInstance(Synonym synonym, SpecimenOrObservationBase identifiedUnit ){
        DeterminationEvent result = new DeterminationEvent();
        result.setTaxon(synonym);
        result.setIdentifiedUnit(identifiedUnit);
        identifiedUnit.addDetermination(result);
        return result;
    }

//*********************** CONSTRUCTOR ********************************/

    //for hibernate use only, *packet* private required by bytebuddy
	DeterminationEvent() {
		super();
	}

//************************* GETTER / SETTER *************************/

	public DefinedTerm getModifier() {
		return modifier;
	}
	public void setModifier(DefinedTerm modifier) {
		this.modifier = modifier;
	}

	public void setTaxonName(TaxonName taxonName){
		this.taxonName = taxonName;
	}
	public TaxonName getTaxonName(){
		return this.taxonName;
	}

    public TaxonBase getTaxon(){
        return this.taxon;
    }
	public void setTaxon(TaxonBase taxon){
		this.taxon = taxon;
	}

	@Transient
	public Partial getIdentificationDate(){
		return this.getTimeperiod().getStart();
	}
	public void setIdentificationDate(Partial identificationDate){
		this.getTimeperiod().setStart(identificationDate);
	}

	@Transient
	public AgentBase getDeterminer() {
		return this.getActor();
	}
	public void setDeterminer(AgentBase determiner) {
		this.setActor(determiner);
	}

	public SpecimenOrObservationBase getIdentifiedUnit() {
		return identifiedUnit;
	}
	public void setIdentifiedUnit(SpecimenOrObservationBase identifiedUnit) {
		this.identifiedUnit = identifiedUnit;
	}

	public boolean getPreferredFlag() {
		return preferredFlag;
	}
	public void setPreferredFlag(boolean preferredFlag) {
		this.preferredFlag = preferredFlag;
	}

	public Set<Reference> getReferences() {
		return references;
	}
	public void setReferences(Set<Reference> references) {
		this.references = references;
	}
	public void addReference(Reference reference) {
		this.references.add(reference);
	}

//*********** CLONE **********************************/

	/**
	 * Clones <i>this</i> determination event. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> determination event
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link EventBase EventBase}.
	 *
	 * @see EventBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DeterminationEvent clone(){
		try{
			DeterminationEvent result = (DeterminationEvent)super.clone();
			//type
			result.setIdentifiedUnit(this.getIdentifiedUnit());
			//modifier
			result.setModifier(this.getModifier());
			//taxon
			result.setTaxon(this.getTaxon()); //TODO
			//no changes to: preferredFlag
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}
