/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * type figures are observations with at least a figure object in media
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:41
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecimenOrObservationBase", propOrder = {
	"sex",
    "individualCount",
    "lifeStage",
    "description",
    "descriptions",
    "determinations",
    "derivationEvents"
})
@XmlRootElement(name = "SpecimenOrObservationBase")
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(appliesTo="SpecimenOrObservationBase", indexes = { @Index(name = "specimenOrObservationBaseTitleCacheIndex", columnNames = { "titleCache" }) })
public abstract class SpecimenOrObservationBase<S extends IIdentifiableEntityCacheStrategy> extends IdentifiableMediaEntity<S> {
	
	private static final Logger logger = Logger.getLogger(SpecimenOrObservationBase.class);
	
	@XmlElementWrapper(name = "Descriptions")
	@XmlElement(name = "Description")
	@ManyToMany(fetch = FetchType.LAZY,mappedBy="describedSpecimenOrObservations",targetEntity=DescriptionBase.class)
	@Cascade(CascadeType.SAVE_UPDATE)
	@NotNull
	private Set<DescriptionBase> descriptions = new HashSet<DescriptionBase>();
	
	@XmlElementWrapper(name = "Determinations")
	@XmlElement(name = "Determination")
	@OneToMany(mappedBy="identifiedUnit")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN})
	@IndexedEmbedded(depth = 2)
	@NotNull
	private Set<DeterminationEvent> determinations = new HashSet<DeterminationEvent>();
	
	@XmlElement(name = "Sex")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private Sex sex;
	
	@XmlElement(name = "LifeStage")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private Stage lifeStage;
	
	@XmlElement(name = "IndividualCount")
	@Field(index=org.hibernate.search.annotations.Index.UN_TOKENIZED)
	@Min(0)
	private Integer individualCount;
	
	// the verbatim description of this occurrence. Free text usable when no atomised data is available.
	// in conjunction with titleCache which serves as the "citation" string for this object
	@XmlElement(name = "Description")
	@XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
	@OneToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN})
	@IndexedEmbedded
	@NotNull
	protected Map<Language,LanguageString> description = new HashMap<Language,LanguageString>();
	
	// events that created derivedUnits from this unit
	@XmlElementWrapper(name = "DerivationEvents")
	@XmlElement(name = "DerivationEvent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN})
    @NotNull
	protected Set<DerivationEvent> derivationEvents = new HashSet<DerivationEvent>();

	/**
	 * Constructor
	 */
	protected SpecimenOrObservationBase(){
		super();
	}
	
	/**
	 * The descriptions this specimen or observation is part of.<BR>
	 * A specimen can not only have it's own {@link SpecimenDescription specimen description }
	 * but can also be part of a {@link TaxonDescription taxon description} or a 
	 * {@link TaxonNameDescription taxon name description}.<BR>
	 * @see #getSpecimenDescriptions()
	 * @return
	 */
	public Set<DescriptionBase> getDescriptions() {
		if(descriptions == null) {
			this.descriptions = new HashSet<DescriptionBase>();
		}
		return this.descriptions;
	}
	
	/**
	 * Returns the {@link SpecimenDescription specimen descriptions} this specimen is part of.
	 * @see #getDescriptions()
	 * @return
	 */
	public Set<SpecimenDescription> getSpecimenDescriptions() {
		return getSpecimenDescriptions(true);
	}
	
	/**
	 * Returns the {@link SpecimenDescription specimen descriptions} this specimen is part of.
	 * @see #getDescriptions()
	 * @return
	 */
	@Transient
	public Set<SpecimenDescription> getSpecimenDescriptions(boolean includeImageGallery) {
		Set<SpecimenDescription> specimenDescriptions = new HashSet<SpecimenDescription>();
		for (DescriptionBase descriptionBase : getDescriptions()){
			if (descriptionBase.isInstanceOf(SpecimenDescription.class)){
				if (includeImageGallery || descriptionBase.isImageGallery() == false){
					specimenDescriptions.add(descriptionBase.deproxy(descriptionBase, SpecimenDescription.class));
				}
				
			}
		}
		return specimenDescriptions;
	}
	/**
	 * Returns the {@link SpecimenDescription specimen descriptions} this specimen is part of.
	 * @see #getDescriptions()
	 * @return
	 */
	@Transient
	public Set<SpecimenDescription> getSpecimenDescriptionImageGallery() {
		Set<SpecimenDescription> specimenDescriptions = new HashSet<SpecimenDescription>();
		for (DescriptionBase descriptionBase : getDescriptions()){
			if (descriptionBase.isInstanceOf(SpecimenDescription.class)){
				if (descriptionBase.isImageGallery() == true){
					specimenDescriptions.add(descriptionBase.deproxy(descriptionBase, SpecimenDescription.class));
				}
			}
		}
		return specimenDescriptions;
	}

	/**
	 * Adds a new description to this specimen or observation
	 * @param description
	 */
	public void addDescription(DescriptionBase description) {
		this.descriptions.add(description);
		if (! description.getDescribedSpecimenOrObservations().contains(this)){
			description.addDescribedSpecimenOrObservation(this);
		}
//		Method method = ReflectionUtils.findMethod(SpecimenDescription.class, "addDescribedSpecimenOrObservation", new Class[] {SpecimenOrObservationBase.class});
//		ReflectionUtils.makeAccessible(method);
//		ReflectionUtils.invokeMethod(method, description, new Object[] {this});
	}
	
	/**
	 * Removes a specimen from a description (removes a description from this specimen)
	 * @param description
	 */
	public void removeDescription(DescriptionBase description) {
		this.descriptions.remove(description);
		if (description.getDescribedSpecimenOrObservations().contains(this)){
			description.removeDescribedSpecimenOrObservation(this);
		}
//		Method method = ReflectionUtils.findMethod(SpecimenDescription.class, "removeDescribedSpecimenOrObservations", new Class[] {SpecimenOrObservationBase.class});
//		ReflectionUtils.makeAccessible(method);
//		ReflectionUtils.invokeMethod(method, description, new Object[] {this});
	}
	
	public Set<DerivationEvent> getDerivationEvents() {
		if(derivationEvents == null) {
			this.derivationEvents = new HashSet<DerivationEvent>();
		}
		return this.derivationEvents;
	}
	
	public void addDerivationEvent(DerivationEvent derivationEvent) {
		if (! this.derivationEvents.contains(derivationEvent)){
			this.derivationEvents.add(derivationEvent);
			derivationEvent.addOriginal(this);
		}
	}
	
	public void removeDerivationEvent(DerivationEvent derivationEvent) {
		this.derivationEvents.remove(derivationEvent);
	}
	
	public Set<DeterminationEvent> getDeterminations() {
		if(determinations == null) {
			this.determinations = new HashSet<DeterminationEvent>();
		}
		return this.determinations;
	}

	public void addDetermination(DeterminationEvent determination) {
		// FIXME bidirectional integrity. Use protected Determination setter
		this.determinations.add(determination);
	}
	
	public void removeDetermination(DeterminationEvent determination) {
		// FIXME bidirectional integrity. Use protected Determination setter
		this.determinations.remove(determination);
	}
	
	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public Stage getLifeStage() {
		return lifeStage;
	}

	public void setLifeStage(Stage lifeStage) {
		this.lifeStage = lifeStage;
	}
	
	@Override
	public String generateTitle(){
		return getCacheStrategy().getTitleCache(this);
	}
	
	public Integer getIndividualCount() {
		return individualCount;
	}

	public void setIndividualCount(Integer individualCount) {
		this.individualCount = individualCount;
	}

	public Map<Language,LanguageString> getDefinition(){
		return this.description;
	}
	
	public void addDefinition(LanguageString description){
		this.description.put(description.getLanguage(),description);
	}
	
	public void addDefinition(String text, Language language){
		this.description.put(language, LanguageString.NewInstance(text, language));
	}
	public void removeDefinition(Language lang){
		this.description.remove(lang);
	}
	
	/**
	 * for derived units get the single next higher parental/original unit.
	 * If multiple original units exist throw error
	 * @return
	 */
	@Transient
	public SpecimenOrObservationBase getOriginalUnit(){
		logger.warn("GetOriginalUnit not yet implemented");
		return null;
	}

	
//******************** CLONE **********************************************/
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see eu.etaxonomy.cdm.model.common.IdentifiableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		SpecimenOrObservationBase result = null;
		result = (SpecimenOrObservationBase)super.clone();
		
		//defininion (description, languageString)
		result.description = new HashMap<Language,LanguageString>();
		for(LanguageString languageString : this.description.values()) {
			LanguageString newLanguageString = (LanguageString)languageString.clone();
			result.addDefinition(newLanguageString);
		} 

		//sex
		result.setSex(this.sex);
		//life stage
		result.setLifeStage(this.lifeStage);
		
		//Descriptions
		for(DescriptionBase description : this.descriptions) {
			result.addDescription((SpecimenDescription)description);
		}
		
		//DeterminationEvent FIXME should clone() the determination
		// as the relationship is OneToMany
		for(DeterminationEvent determination : this.determinations) {
			result.addDetermination(determination);
		}
		
		//DerivationEvent
		for(DerivationEvent derivationEvent : this.derivationEvents) {
			result.addDerivationEvent(derivationEvent);
		}
		
		//no changes to: individualCount
		return result;
	}
}