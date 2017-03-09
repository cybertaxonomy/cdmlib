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

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
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
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
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


    private enum CDM_INTEXT_CLASS{
        REFERENCE("reference"),
        TAXONNAME("name"),
        AGENT("agent"),
        MEDIA("media"),
        OCCURRENCE("occurrence"),
        TAXON("taxon")
        ;
        String tag;

         private CDM_INTEXT_CLASS(String tag) {
            this.tag = tag;
        }

        public String tag(){
            return this.tag;
        }
    }



// ***************** FACTORY METHOD ***********************************

    public static IntextReference NewInstance(IIntextReferenceTarget target,
            IIntextReferencable referencedEntity, int start, int end){
        IntextReference result = new IntextReference(target, referencedEntity, start, end);
        return result;
    }

//	public static IntextReference NewTaxonNameInstance(TaxonNameBase<?,?> taxonName, LanguageStringBase languageString, int start, int end){
//		return new IntextReference(taxonName, null, null, null, null, null, languageString, start, end);
//	}
//
//	public static IntextReference NewTaxonInstance(TaxonBase<?> taxon, LanguageStringBase languageString, int start, int end){
//		return new IntextReference(null, taxon, null, null, null, null, languageString, start, end);
//	}
//
//	public static IntextReference NewOccurrenceInstance(SpecimenOrObservationBase<?> occurrence, LanguageStringBase languageString, int start, int end){
//		return new IntextReference(null, null, occurrence, null, null, null, languageString, start, end);
//	}
//
//	public static IntextReference NewAgentInstance(AgentBase<?> agent, LanguageStringBase languageString, int start, int end){
//		return new IntextReference(null, null, null, agent, null, null, languageString, start, end);
//	}
//
//	public static IntextReference NewReferenceInstance(Reference reference, LanguageStringBase languageString, int start, int end){
//		return new IntextReference(null, null, null, null, reference, null, languageString, start, end);
//	}
//
//	public static IntextReference NewMediaInstance(Media media, LanguageStringBase languageString, int start, int end){
//		return new IntextReference(null, null, null, null, null, media, languageString, start, end);
//	}

	public static LanguageString NewReferencedLanguageString(IIntextReferenceTarget target, String pre, String middle, String post, Language language){
        LanguageString result = LanguageString.NewInstance(null, language);
        IntextReference intextReference = IntextReference.NewInstance(target, result, 0, 0);
	    result.addIntextReference(intextReference);
	    result.setText(pre + intextReference.toInlineString(middle) + post);
        return result;
    }

    public static LanguageString NewReferencedLanguageString(IIntextReferenceTarget target, String text, int start, int end, Language language){
        if (start < 0 || end < 0 || start > end || end > text.length()){
            throw new IndexOutOfBoundsException("Start and end must be within bounds");
        }
        LanguageString result = LanguageString.NewInstance(text, language);
        IntextReference intextReference = IntextReference.NewInstance(target, result, start, end);
        result.addIntextReference(intextReference);
        String intext = text.substring(0, start) +
                intextReference.toInlineString(text.substring(start, end)) + text.substring(end);
        result.setText(intext);
        return result;
    }

//********************** CONSTRUCTOR ********************************************/

	/**
	 * @deprecated for internal use only
	 */
	@Deprecated //for hibernate use only
	private IntextReference(){}

//	private IntextReference(TaxonNameBase<?, ?> taxonName, TaxonBase<?> taxon,
//				SpecimenOrObservationBase<?> occurrence, AgentBase<?> agent,
//				Reference reference, Media media, LanguageStringBase languageString, int start, int end) {
//			super();
//			this.taxonName = taxonName;
//			this.taxon = taxon;
//			this.occurrence = occurrence;
//			this.agent = agent;
//			this.reference = reference;
//			this.media = media;
//			if (languageString != null && languageString.isInstanceOf(LanguageString.class)){
//				this.languageString = CdmBase.deproxy(languageString, LanguageString.class);
//				this.languageString.addIntextReference(this);
//			}else if (languageString != null && languageString.isInstanceOf(Annotation.class)){
//				this.annotation = CdmBase.deproxy(languageString, Annotation.class);
//				this.annotation.addIntextReference(this);
//			}
//			this.startPos = start;
//			this.endPos = end;
//	}

	   private IntextReference(IIntextReferenceTarget target, IIntextReferencable referencedEntity, int start, int end) {
           super();
           setTarget(target);
           setReferencedEntity(referencedEntity);

           this.startPos = start;
           this.endPos = end;
   }


    private CDM_INTEXT_CLASS myClass(){
        if (agent != null){
            return CDM_INTEXT_CLASS.AGENT;
        }else if (media != null){
            return CDM_INTEXT_CLASS.MEDIA;
        }else if (taxonName != null){
            return CDM_INTEXT_CLASS.TAXONNAME;
        }else if (taxon != null){
            return CDM_INTEXT_CLASS.TAXON;
        }else if (reference != null){
            return CDM_INTEXT_CLASS.REFERENCE;
        }else if (occurrence != null){
            return CDM_INTEXT_CLASS.OCCURRENCE;
        }else{
            throw new IllegalStateException("Intext reference has no target object defined");
        }
    }

// ****************    GETTER / SETTER ******************************************/

   /**
    * Returns the target object. Throws an {@link IllegalStateException} if no target
    * is defined.
    *
    * @return
    */
   public IIntextReferenceTarget getTarget() {
       if (agent != null){
           return agent;
       }else if (media != null){
           return media;
       }else if (taxonName != null){
           return taxonName;
       }else if (taxon != null){
           return taxon;
       }else if (reference != null){
           return reference;
       }else if (occurrence != null){
           return occurrence;
       }else{
           throw new IllegalStateException("Intext reference has no target object defined");
       }
   }

   /**
     * @param target
     */
    private void setTarget(IIntextReferenceTarget target) {
        target = CdmBase.deproxy(target);
        if (target instanceof TaxonNameBase){
            this.taxonName = (TaxonNameBase<?,?>)target;
        }else if (target instanceof TaxonBase){
            this.taxon = (TaxonBase<?>)target;
        }else if (target instanceof SpecimenOrObservationBase){
            this.occurrence = (SpecimenOrObservationBase<?>)target;
        }else if (target instanceof AgentBase){
            this.agent = (AgentBase<?>)target;
        }else if (target instanceof Reference){
            this.reference = (Reference)target;
        }else if (target instanceof Media){
            this.media = (Media)target;
        }else{
            throw new IllegalArgumentException("Target entity not yet handled: " + target.getClass().getName());
        }
    }

   public IIntextReferencable getReferencedEntity() {
       if (languageString != null){
           return languageString;
       }else if (annotation != null){
           return annotation;
       }else{
           return null;
       }
   }

   /**
    * @param referencedEntity
    */
   public void setReferencedEntity(IIntextReferencable referencedEntity) {
       if (referencedEntity == null){
           this.annotation = null;
           this.languageString = null;
       }else if (this.getReferencedEntity() == referencedEntity){
           //do nothing
       }else{
           referencedEntity = CdmBase.deproxy(referencedEntity);
           if (referencedEntity instanceof LanguageString){
               this.languageString = (LanguageString)referencedEntity;
               this.annotation = null;
           }else if (referencedEntity instanceof Annotation){
               this.annotation = (Annotation)referencedEntity;
               this.languageString = null;
           }else{
               throw new IllegalArgumentException("Referenced entity type not yet supported: " + referencedEntity.getClass().getName());
           }
           if (!referencedEntity.getIntextReferences().contains(this)){
               referencedEntity.addIntextReference(this);
           }
       }
   }

//	public TaxonNameBase<?, ?> getTaxonName() {
//		return taxonName;
//	}
//	public void setTaxonName(TaxonNameBase<?, ?> taxonName) {
//		this.taxonName = taxonName;
//	}
//
//
//	public TaxonBase<?> getTaxon() {
//		return taxon;
//	}
//	public void setTaxon(TaxonBase<?> taxon) {
//		this.taxon = taxon;
//	}
//
//	public SpecimenOrObservationBase<?> getOccurrence() {
//		return occurrence;
//	}
//	public void setOccurrence(SpecimenOrObservationBase<?> occurrence) {
//		this.occurrence = occurrence;
//	}
//
//	public AgentBase<?> getAgent() {
//		return agent;
//	}
//	public void setAgent(AgentBase<?> agent) {
//		this.agent = agent;
//	}
//
//	public Reference getReference() {
//		return reference;
//	}
//	public void setReference(Reference reference) {
//		this.reference = reference;
//	}
//
//	public Media getMedia() {
//		return media;
//	}
//	public void setMedia(Media media) {
//		this.media = media;
//	}

//	public LanguageString getLanguageString() {
//		return languageString;
//	}
//	protected void setLanguageString(LanguageString languageString) {
//		this.languageString = languageString;
//	}
//
//	public Annotation getAnnotation() {
//		return annotation;
//	}
//
//	public void setAnnotation(Annotation annotation) {
//		this.annotation = annotation;
//	}

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

	private static final String CDM_PREFIX = "cdm:";
	public String toInlineString(String innerText){
	    String tag = CDM_PREFIX + myClass().tag();
	    IIntextReferenceTarget entity = getTarget();
	    String attributes = " cdmId='" + entity.getUuid() + "' intextId='" + this.getUuid() + "'" + otherAttributes(entity);
	    String result;
	    if (StringUtils.isNotEmpty(innerText)){
	        result = "<" + tag + attributes + ">" + innerText + "</" + tag + ">";
	    }else{
            result = "<" + tag + attributes + "/>";
	    }
	    return result;
	}

    /**
     * Entity class dependent attributes
     * @param entity
     * @return
     */
    private String otherAttributes(IIntextReferenceTarget entity) {
        return "";
    }


}
