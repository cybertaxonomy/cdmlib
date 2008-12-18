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

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.common.init.DefaultVocabularyStore;
import eu.etaxonomy.cdm.model.common.init.IVocabularyStore;
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
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DefinedTermBase<T extends DefinedTermBase> extends TermBase implements ILoadableTerm<T> {
	private static final long serialVersionUID = 2931811562248571531L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DefinedTermBase.class);
	
	static protected IVocabularyStore vocabularyStore = new DefaultVocabularyStore();

	public static void setVocabularyStore(IVocabularyStore vocabularyStore){
		DefinedTermBase.vocabularyStore = vocabularyStore;
	}
	
	@XmlElement(name = "KindOf")
	private DefinedTermBase<T> kindOf;
	
	@XmlElement(name = "GeneralizationOf")
	private Set<DefinedTermBase<T>> generalizationOf = new HashSet<DefinedTermBase<T>>();
	
	@XmlElement(name = "PartOf")
	private DefinedTermBase<T> partOf;
	
	@XmlElementWrapper(name = "Includes")
	@XmlElement(name = "Include")
	private Set<DefinedTermBase<T>> includes = new HashSet<DefinedTermBase<T>>();
	
	@XmlElementWrapper(name = "Media")
	@XmlElement(name = "Medium")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Set<Media> media = new HashSet<Media>();
	
	@XmlElement(name = "TermVocabulary")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected TermVocabulary<T> vocabulary;
	

	public static DefinedTermBase findByUuid(UUID uuid){
		return vocabularyStore.getTermByUuid(uuid);
	}
	
	public DefinedTermBase() {
		super();
	}
	public DefinedTermBase(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ILoadableTerm#readCsvLine(java.util.List)
	 */
	public ILoadableTerm readCsvLine(List<String> csvLine) {
		return readCsvLine(csvLine, Language.ENGLISH());
	}

	public ILoadableTerm readCsvLine(List<String> csvLine, Language lang) {
		this.setUuid(UUID.fromString(csvLine.get(0)));
		this.setUri(csvLine.get(1));
		String label = csvLine.get(2).trim();
		String text = csvLine.get(3);
		String abbreviatedLabel = null;
		this.addRepresentation(Representation.NewInstance(text, label, abbreviatedLabel, lang) );
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ILoadableTerm#writeCsvLine(au.com.bytecode.opencsv.CSVWriter)
	 */
	public void writeCsvLine(CSVWriter writer) {
		String [] line = new String[4];
		line[0] = getUuid().toString();
		line[1] = getUri();
		line[2] = getLabel();
		line[3] = getDescription();
		writer.writeNext(line);
	}
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	public DefinedTermBase<T> getKindOf(){
		return this.kindOf;
	}
	public void setKindOf(DefinedTermBase<T> kindOf){
		this.kindOf = kindOf;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy = "kindOf")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DefinedTermBase<T>> getGeneralizationOf(){
		return this.generalizationOf;
	}
	
	public void setGeneralizationOf(Set<DefinedTermBase<T>> generalizationOf) {
		this.generalizationOf = generalizationOf;
	}
	
	public void addGeneralizationOf(DefinedTermBase<T> generalization) {
		generalization.setKindOf(this);
		this.generalizationOf.add(generalization);
	}
	
	public void removeGeneralization(DefinedTermBase<T> generalization) {
		if(generalizationOf.contains(generalization)){
			generalization.setKindOf(null);
		    this.generalizationOf.remove(generalization);
		}
	}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public DefinedTermBase<T> getPartOf(){
		return this.partOf;
	}
	public void setPartOf(DefinedTermBase<T> partOf){
		this.partOf = partOf;
	}

	
	@OneToMany(fetch=FetchType.LAZY, mappedBy = "partOf")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DefinedTermBase<T>> getIncludes(){
		return this.includes;
	}
	public void setIncludes(Set<DefinedTermBase<T>> includes) {
		this.includes = includes;
	}
	public void addIncludes(DefinedTermBase<T> includes) {
		includes.setPartOf(this);
		this.includes.add(includes);
	}
	public void removeIncludes(DefinedTermBase<T> includes) {
		if(this.includes.contains(includes)) {
			includes.setPartOf(null);
		    this.includes.remove(includes);
		}
	}


	@OneToMany
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
	@Transient
	@XmlTransient
	public TermVocabulary getVocabulary() {
		return this.vocabulary;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#setVocabulary(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	public void setVocabulary(TermVocabulary newVocabulary) {
		// Hibernate bidirectional cascade hack: 
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-1054
		if(this.vocabulary == newVocabulary){ return;}
		if (this.vocabulary != null) { 
			this.vocabulary.terms.remove(this);
		}
		if (newVocabulary!= null) { 
			newVocabulary.terms.add(this);
		}
		this.vocabulary = newVocabulary;		
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#getVocabulary()
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@Cascade( { CascadeType.SAVE_UPDATE })
	protected TermVocabulary getPersistentVocabulary() {
		return this.vocabulary;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#setVocabulary(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	protected void setPersistentVocabulary(TermVocabulary newVocabulary) {
		// Hibernate bidirectional cascade hack: 
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-1054
		if(this.vocabulary == newVocabulary){ return;}
		if (this.vocabulary != null) { 
			this.vocabulary.terms.remove(this);
		}
		if (newVocabulary!= null) { 
			try {
				Field fieldInitializing = AbstractPersistentCollection.class.getDeclaredField("initializing");
				fieldInitializing.setAccessible(true);
				if (AbstractPersistentCollection.class.isAssignableFrom(newVocabulary.terms.getClass())){
					boolean initValue = fieldInitializing.getBoolean(newVocabulary.terms);
					if (initValue == false){
						newVocabulary.terms.add(this);
					}else{
						//nothing
					}
				}else{
					newVocabulary.terms.add(this);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		this.vocabulary = newVocabulary;		
	}
	
}