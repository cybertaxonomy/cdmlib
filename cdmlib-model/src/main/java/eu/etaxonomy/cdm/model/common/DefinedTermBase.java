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
import org.hibernate.collection.PersistentSet;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.common.init.DefaultVocabularyStore;
import eu.etaxonomy.cdm.model.common.init.TermLoader;
import eu.etaxonomy.cdm.model.common.init.IVocabularyStore;

import java.lang.reflect.Field;
import java.util.*;

import javax.persistence.*;


/**
 * workaround for enumerations, base type according to TDWG.  For linear ordering
 * use partOf relation and BreadthFirst. Default iterator order should therefore
 * be BreadthFirst (not DepthFirst)
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:19
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DefinedTermBase<T extends DefinedTermBase> extends TermBase implements ILoadableTerm{
	static Logger logger = Logger.getLogger(DefinedTermBase.class);
	
	static protected IVocabularyStore vocabularyStore = new DefaultVocabularyStore();

	public static void setVocabularyStore(IVocabularyStore vocabularyStore){
		DefinedTermBase.vocabularyStore = vocabularyStore;
	}
	
	private DefinedTermBase kindOf;
	private Set<DefinedTermBase> generalizationOf = new HashSet<DefinedTermBase>();
	private DefinedTermBase partOf;
	private Set<DefinedTermBase> includes = new HashSet<DefinedTermBase>();
	private Set<Media> media = new HashSet<Media>();
	protected TermVocabulary<T> vocabulary;
	

	public static DefinedTermBase findByUuid(UUID uuid){
		return vocabularyStore.getTermByUuid(uuid);
	}
	
	public DefinedTermBase() {
		super();
	}
	public DefinedTermBase(String term, String label) {
		super(term, label);
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
		this.addRepresentation(Representation.NewInstance(csvLine.get(3), csvLine.get(2).trim(), lang) );
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
	
	@Transient
	//@ManyToOne
	//@Cascade({CascadeType.SAVE_UPDATE})
	public DefinedTermBase getKindOf(){
		return this.kindOf;
	}
	public void setKindOf(DefinedTermBase kindOf){
		this.kindOf = kindOf;
	}

	@Transient
	//@OneToMany(fetch=FetchType.LAZY)
	//@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DefinedTermBase> getGeneralizationOf(){
		return this.generalizationOf;
	}
	public void setGeneralizationOf(Set<DefinedTermBase> generalizationOf) {
		this.generalizationOf = generalizationOf;
	}


	@Transient
	//@ManyToOne
	//@Cascade({CascadeType.SAVE_UPDATE})
	public DefinedTermBase getPartOf(){
		return this.partOf;
	}
	public void setPartOf(DefinedTermBase partOf){
		this.partOf = partOf;
	}

	@Transient
	//@OneToMany(fetch=FetchType.LAZY)
	//@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DefinedTermBase> getIncludes(){
		return this.includes;
	}
	public void setIncludes(Set<DefinedTermBase> includes) {
		this.includes = includes;
	}
	public void addIncludes(DefinedTermBase includes) {
		this.includes.add(includes);
	}
	public void removeIncludes(TermBase includes) {
		this.includes.remove(includes);
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
	@ManyToOne(fetch=FetchType.EAGER)
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