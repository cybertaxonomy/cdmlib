/**
 *
 */
package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * This class represents a link to another CDM Base class from within a text.
 * If a text, e.g. a LanguageString links parts of its text to an other CDM Base
 * one may create a tag around the according text including id and uuid as attributes
 * and adding an IntextReference to the LanguageString. The IntextReference then points
 * to the according CdmBase.
 * This way we may keep referential integrity and we may also support correct
 * deduplication or deletion of the referenced objects.
 *
 * @see #4706
 *
 * @author a.mueller
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IntextReference", propOrder = {
    "taxonName",
    "taxon",
    "occurrence",
    "agent",
    "reference",
    "media",
    "languageString",
    "annotation",
    "startPos",
    "endPos"
})
@Entity
@Audited
public class IntextReference extends VersionableEntity {
	private static final long serialVersionUID = -7002541566256975424L;

    @XmlElement(name = "TaxonName")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private TaxonNameBase<?,?> taxonName;

    @XmlElement(name = "Taxon")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private TaxonBase<?> taxon;

    @XmlElement(name = "Occurrence")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private SpecimenOrObservationBase<?> occurrence;

    @XmlElement(name = "Agent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private AgentBase<?> agent;

    @XmlElement(name = "Reference")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private Reference reference;

    @XmlElement(name = "Media")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private Media media;

    //TODO or do we want to link to LanguageString Base??
    @XmlElement(name = "LanguageString")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private LanguageString languageString;

    //TODO or do we want to link to LanguageString Base??
    @XmlElement(name = "Annotation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private Annotation annotation;

	private int startPos;

	private int endPos;

// ***************** FACTORY METHOD ***********************************

	public static IntextReference NewTaxonNameInstance(TaxonNameBase<?,?> taxonName, LanguageStringBase languageString, int start, int end){
		return new IntextReference(taxonName, null, null, null, null, null, languageString, start, end);
	}

	public static IntextReference NewTaxonInstance(TaxonBase<?> taxon, LanguageStringBase languageString, int start, int end){
		return new IntextReference(null, taxon, null, null, null, null, languageString, start, end);
	}

	public static IntextReference NewOccurrenceInstance(SpecimenOrObservationBase<?> occurrence, LanguageStringBase languageString, int start, int end){
		return new IntextReference(null, null, occurrence, null, null, null, languageString, start, end);
	}

	public static IntextReference NewAgentInstance(AgentBase<?> agent, LanguageStringBase languageString, int start, int end){
		return new IntextReference(null, null, null, agent, null, null, languageString, start, end);
	}

	public static IntextReference NewReferenceInstance(Reference reference, LanguageStringBase languageString, int start, int end){
		return new IntextReference(null, null, null, null, reference, null, languageString, start, end);
	}

	public static IntextReference NewReferenceInstance(Media media, LanguageStringBase languageString, int start, int end){
		return new IntextReference(null, null, null, null, null, media, languageString, start, end);
	}

//********************** CONSTRUCTOR ********************************************/

	/**
	 * @deprecated for internal use only
	 */
	@Deprecated //for hibernate use only
	private IntextReference(){}

	private IntextReference(TaxonNameBase<?, ?> taxonName, TaxonBase<?> taxon,
				SpecimenOrObservationBase<?> occurrence, AgentBase<?> agent,
				Reference reference, Media media, LanguageStringBase languageString, int start, int end) {
			super();
			this.taxonName = taxonName;
			this.taxon = taxon;
			this.occurrence = occurrence;
			this.agent = agent;
			this.reference = reference;
			if (languageString != null && languageString.isInstanceOf(LanguageString.class)){
				this.languageString = CdmBase.deproxy(languageString, LanguageString.class);
				this.languageString.addIntextReference(this);
			}else if (languageString != null && languageString.isInstanceOf(Annotation.class)){
				this.annotation = CdmBase.deproxy(languageString, Annotation.class);
				this.annotation.addIntextReference(this);
			}
			this.startPos = start;
			this.endPos = end;
	}


// ****************    GETTER / SETTER ******************************************/

	public TaxonNameBase<?, ?> getTaxonName() {
		return taxonName;
	}
	public void setTaxonName(TaxonNameBase<?, ?> taxonName) {
		this.taxonName = taxonName;
	}


	public TaxonBase<?> getTaxon() {
		return taxon;
	}
	public void setTaxon(TaxonBase<?> taxon) {
		this.taxon = taxon;
	}

	public SpecimenOrObservationBase<?> getOccurrence() {
		return occurrence;
	}
	public void setOccurrence(SpecimenOrObservationBase<?> occurrence) {
		this.occurrence = occurrence;
	}

	public AgentBase<?> getAgent() {
		return agent;
	}
	public void setAgent(AgentBase<?> agent) {
		this.agent = agent;
	}

	public Reference getReference() {
		return reference;
	}
	public void setReference(Reference reference) {
		this.reference = reference;
	}



	public Media getMedia() {
		return media;
	}
	public void setMedia(Media media) {
		this.media = media;
	}

	public LanguageString getLanguageString() {
		return languageString;
	}
	public void setLanguageString(LanguageString languageString) {
		this.languageString = languageString;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	public int getStartPos() {
		return startPos;
	}
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public int getEndPos() {
		return endPos;
	}
	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

}
