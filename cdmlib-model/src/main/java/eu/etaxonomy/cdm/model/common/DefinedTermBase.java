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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.agent.InstitutionType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.RightsTerm;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;


/**
 * walkaround for enumerations, base type according to TDWG.  For linear ordering
 * use partOf relation and BreadthFirst. Default iterator order should therefore
 * be BreadthFirst (not DepthFirst)
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:19
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DefinedTermBase", propOrder = {
    "media",
    "vocabulary"
})
@XmlRootElement(name = "DefinedTermBase")
@XmlSeeAlso({
	AnnotationType.class,
	DerivationEventType.class,
	ExtensionType.class,
    Feature.class,
    InstitutionType.class,
    Language.class,
    MarkerType.class,
    MeasurementUnit.class,
    NamedAreaType.class,
    NomenclaturalCode.class,
    PreservationMethod.class,
    ReferenceSystem.class,
    RightsTerm.class,
    StatisticalMeasure.class,
    TextFormat.class
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DefinedTermBase<T extends DefinedTermBase> extends TermBase implements ILoadableTerm<T>, IDefinedTerm<T> {
	private static final long serialVersionUID = 2931811562248571531L;
	private static final Logger logger = Logger.getLogger(DefinedTermBase.class);
		
//	@XmlElement(name = "KindOf")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
	@XmlTransient
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DefinedTermBase.class)
    @Cascade(CascadeType.SAVE_UPDATE)
	private T kindOf;
	/**
	 * FIXME - Hibernate retuns this as a collection of CGLibProxy$$DefinedTermBase objects 
	 * which can't be cast to instances of T - can we explicitly initialize these terms using 
	 * Hibernate.initialize(), does this imply a distinct load, and find methods in the dao?
	 */
//	@XmlElementWrapper(name = "Generalizations")
//	@XmlElement(name = "GeneralizationOf")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
	@XmlTransient
    @OneToMany(fetch=FetchType.LAZY, mappedBy = "kindOf", targetEntity = DefinedTermBase.class)
	@Cascade({CascadeType.SAVE_UPDATE})
	private Set<T> generalizationOf = new HashSet<T>();
	
//	@XmlElement(name = "PartOf")
//	@XmlIDREF
//  @XmlSchemaType(name = "IDREF")
	@XmlTransient
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = DefinedTermBase.class)
	@Cascade(CascadeType.SAVE_UPDATE)
	protected T partOf;
	
	/**
	 * FIXME - Hibernate retuns this as a collection of CGLibProxy$$DefinedTermBase objects 
	 * which can't be cast to instances of T - can we explicitly initialize these terms using 
	 * Hibernate.initialize(), does this imply a distinct load, and find methods in the dao?
	 */
//	@XmlElementWrapper(name = "Includes")
//	@XmlElement(name = "Include")
//	@XmlIDREF
//    @XmlSchemaType(name = "IDREF")
	@XmlTransient
	@OneToMany(fetch=FetchType.LAZY, mappedBy = "partOf", targetEntity = DefinedTermBase.class)
	@Cascade({CascadeType.SAVE_UPDATE})
	private Set<T> includes = new HashSet<T>();
	
	@XmlElementWrapper(name = "Media")
	@XmlElement(name = "Medium")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private Set<Media> media = new HashSet<Media>();
	
	@XmlElement(name = "TermVocabulary")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch=FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	protected TermVocabulary<T> vocabulary;	
	
//***************************** CONSTRUCTOR *******************************************/	
	
	public DefinedTermBase() {
		super();
	}
	public DefinedTermBase(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

//******************************* METHODS ******************************************************/
	
	public abstract void resetTerms();

	protected abstract void setDefaultTerms(TermVocabulary<T> termVocabulary);
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ILoadableTerm#readCsvLine(java.util.List)
	 */
	public T readCsvLine(Class<T> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		try {
			T newInstance = termClass.newInstance();
		    return readCsvLine(newInstance, csvLine, Language.CSV_LANGUAGE());
		} catch (Exception e) {
			logger.error(e);
			for(StackTraceElement ste : e.getStackTrace()) {
				logger.error(ste);
			}
		}
		
	    return null;
	}

	protected static <TERM extends DefinedTermBase> TERM readCsvLine(TERM newInstance, List<String> csvLine, Language lang) {
			newInstance.setUuid(UUID.fromString(csvLine.get(0)));
			newInstance.setUri(csvLine.get(1));
			String label = csvLine.get(2).trim();
			String text = csvLine.get(3);
			String abbreviatedLabel = csvLine.get(4);
			newInstance.addRepresentation(Representation.NewInstance(text, label, abbreviatedLabel, lang) );
			return newInstance;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ILoadableTerm#writeCsvLine(au.com.bytecode.opencsv.CSVWriter)
	 */
	public void writeCsvLine(CSVWriter writer, T term) {
		String [] line = new String[4];
		line[0] = term.getUuid().toString();
		line[1] = term.getUri();
		line[2] = term.getLabel();
		line[3] = term.getDescription();
		writer.writeNext(line);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getByUuid(java.util.UUID)
	 */
	@Transient
	public T getByUuid(UUID uuid){
		return this.vocabulary.findTermByUuid(uuid);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getKindOf()
	 */
	public T getKindOf(){
		return (T)DefinedTermBase.deproxy(this.kindOf, this.getClass());
	}

	public void setKindOf(T kindOf){
		this.kindOf = kindOf;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getGeneralizationOf()
	 */
	public Set<T> getGeneralizationOf(){
		return this.generalizationOf;
	}
	
	protected void setGeneralizationOf(Set<T> value) {
		this.generalizationOf = value;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#addGeneralizationOf(T)
	 */
	public void addGeneralizationOf(T generalization) {
		generalization.setKindOf(this);
		this.generalizationOf.add(generalization);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#removeGeneralization(T)
	 */
	public void removeGeneralization(T generalization) {
		if(generalizationOf.contains(generalization)){
			generalization.setKindOf(null);
		    this.generalizationOf.remove(generalization);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getPartOf()
	 */
	public T getPartOf(){
		return (T)DefinedTermBase.deproxy(this.partOf, this.getClass());
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#setPartOf(T)
	 */
	public void setPartOf(T partOf){
		this.partOf = partOf;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getIncludes()
	 */
	public Set<T> getIncludes(){
		return this.includes;
	}
	
	protected void setIncludes(Set<T> includes) {
		this.includes = includes;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#addIncludes(T)
	 */
	public void addIncludes(T includes) {
		includes.setPartOf(this);
		this.includes.add(includes);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#removeIncludes(T)
	 */
	public void removeIncludes(T includes) {
		if(this.includes.contains(includes)) {
			includes.setPartOf(null);
		    this.includes.remove(includes);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#getMedia()
	 */
	public Set<Media> getMedia(){
		return this.media;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefinedTerm#addMedia(eu.etaxonomy.cdm.model.media.Media)
	 */
	public void addMedia(Media media) {
		this.media.add(media);
	}
	public void removeMedia(Media media) {
		this.media.remove(media);
	}

	/**
	 * @return
	 */
	public TermVocabulary<T> getVocabulary() {
		return this.vocabulary;
	}

	//for bedirectional use only, use vocabulary.addTerm instead
	/**
	 * @param newVocabulary
	 */
	protected void setVocabulary(TermVocabulary<T> newVocabulary) {
		this.vocabulary = newVocabulary;		
	}	
}