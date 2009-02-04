/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.search.annotations.Indexed;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.common.init.ITermInitializer;
import eu.etaxonomy.cdm.model.media.Media;
import java.lang.reflect.Field;
import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


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
    "kindOf",
    "generalizationOf",
    "partOf",
    "includes",
    "media",
    "vocabulary"
})
@XmlRootElement(name = "DefinedTermBase")
@Entity
//@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Indexed
public abstract class DefinedTermBase<T extends DefinedTermBase> extends TermBase implements ILoadableTerm<T> {
	private static final long serialVersionUID = 2931811562248571531L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DefinedTermBase.class);
	
	@XmlElement(name = "KindOf")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private T kindOf;
	/**
	 * FIXME - Hibernate retuns this as a collection of CGLibProxy$$DefinedTermBase objects 
	 * which can't be cast to instances of T - can we explicitly initialize these terms using 
	 * Hibernate.initialize(), does this imply a distinct load, and find methods in the dao?
	 */
	@XmlElement(name = "GeneralizationOf")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Set<T> generalizationOf = new HashSet<T>();
	
	@XmlElement(name = "PartOf")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private T partOf;
	
	/**
	 * FIXME - Hibernate retuns this as a collection of CGLibProxy$$DefinedTermBase objects 
	 * which can't be cast to instances of T - can we explicitly initialize these terms using 
	 * Hibernate.initialize(), does this imply a distinct load, and find methods in the dao?
	 */
	@XmlElementWrapper(name = "Includes")
	@XmlElement(name = "Include")
    private Set<T> includes = new HashSet<T>();
	
	@XmlElementWrapper(name = "Media")
	@XmlElement(name = "Medium")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Set<Media> media = new HashSet<Media>();
	
	@XmlElement(name = "TermVocabulary")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected TermVocabulary<T> vocabulary;	
	
	public DefinedTermBase() {
		super();
	}
	public DefinedTermBase(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	protected abstract void setDefaultTerms(TermVocabulary<T> termVocabulary);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ILoadableTerm#readCsvLine(java.util.List)
	 */
	public T readCsvLine(Class<T> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		try {
			T newInstance = termClass.newInstance();
		    return readCsvLine(newInstance, csvLine, Language.ENGLISH());
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
	
	@Transient
	public T getByUuid(UUID uuid){
		return this.vocabulary.findTermByUuid(uuid);
	}
	
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = DefinedTermBase.class)
	@Cascade({CascadeType.SAVE_UPDATE})
	public T getKindOf(){
		return this.kindOf;
	}
	
	public void setKindOf(T kindOf){
		this.kindOf = kindOf;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy = "kindOf", targetEntity = DefinedTermBase.class)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<T> getGeneralizationOf(){
		return this.generalizationOf;
	}
	
	public void setGeneralizationOf(Set<T> generalizationOf) {
		this.generalizationOf = generalizationOf;
	}
	
	public void addGeneralizationOf(T generalization) {
		generalization.setKindOf(this);
		this.generalizationOf.add(generalization);
	}
	
	public void removeGeneralization(T generalization) {
		if(generalizationOf.contains(generalization)){
			generalization.setKindOf(null);
		    this.generalizationOf.remove(generalization);
		}
	}


	@ManyToOne(fetch = FetchType.LAZY, targetEntity = DefinedTermBase.class)
	@Cascade({CascadeType.SAVE_UPDATE})
	public T getPartOf(){
		return this.partOf;
	}
	public void setPartOf(T partOf){
		this.partOf = partOf;
	}

	
	@OneToMany(fetch=FetchType.LAZY, mappedBy = "partOf", targetEntity = DefinedTermBase.class)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<T> getIncludes(){
		return this.includes;
	}
	public void setIncludes(Set<T> includes) {
		this.includes = includes;
	}
	public void addIncludes(T includes) {
		includes.setPartOf(this);
		this.includes.add(includes);
	}
	public void removeIncludes(T includes) {
		if(this.includes.contains(includes)) {
			includes.setPartOf(null);
		    this.includes.remove(includes);
		}
	}


	@OneToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Media> getMedia(){
		return this.media;
	}
	public void setMedia(Set<Media> media) {
		this.media = media;
	}
	public void addMedia(Media media) {
		this.media.add(media);
	}
	public void removeMedia(Media media) {
		this.media.remove(media);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#getVocabulary()
	 */
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@Cascade( { CascadeType.SAVE_UPDATE })
	public TermVocabulary<T> getVocabulary() {
		return this.vocabulary;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#setVocabulary(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	public void setVocabulary(TermVocabulary<T> newVocabulary) {
		this.vocabulary = newVocabulary;		
	}	
}