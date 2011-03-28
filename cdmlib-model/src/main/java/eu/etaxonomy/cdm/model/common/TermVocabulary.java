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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
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
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;


/**
 * A single enumeration must only contain DefinedTerm instances of one kind
 * (this means a subclass of DefinedTerm).
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermVocabulary", propOrder = {
    "termSourceUri",
    "terms"
})
@XmlRootElement(name = "TermVocabulary")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.TermVocabulary")
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class TermVocabulary<T extends DefinedTermBase> extends TermBase implements Iterable<T> {
	private static final long serialVersionUID = 1925052321596648672L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermVocabulary.class);

	//The vocabulary source (e.g. ontology) defining the terms to be loaded when a database is created for the first time.  
	// Software can go and grap these terms incl labels and description. 
	// UUID needed? Further vocs can be setup through our own ontology.
	@XmlElement(name = "TermSourceURI")
	@Field(index=org.hibernate.search.annotations.Index.UN_TOKENIZED)
	private String termSourceUri;
	

	//TODO Changed
	@XmlElementWrapper(name = "Terms")
	@XmlElement(name = "Term")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="vocabulary", fetch=FetchType.LAZY, targetEntity = DefinedTermBase.class)
	@Type(type="DefinedTermBase")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	@IndexedEmbedded(depth = 2)
	protected Set<T> terms = getNewTermSet();
	
// ********************************* FACTORY METHODS *****************************************/

	public static TermVocabulary NewInstance(String description, String label, String abbrev, String termSourceUri){
		return new TermVocabulary(description, label, abbrev, termSourceUri);
	}
	
// ************************* CONSTRUCTOR *************************************************	

	protected TermVocabulary() {
	}
	
	protected TermVocabulary(String term, String label, String labelAbbrev, String termSourceUri) {
		super(term, label, labelAbbrev);
		setTermSourceUri(termSourceUri);
	}
	

// ******************* METHODS *************************************************/	
	
	public T findTermByUuid(UUID uuid){
		for(T t : terms) {
			if(t.getUuid().equals(uuid)) {
				return t;
			}
		}
		return null;
	}

	@Transient
	Set<T> getNewTermSet() {
		return new HashSet<T>();
	}

	public Set<T> getTerms() {
		return terms;
	}
	
	public void addTerm(T term) {
		term.setVocabulary(this);
		this.terms.add(term);
	}
	public void removeTerm(T term) {
		this.terms.remove(term);
		term.setVocabulary(null);
	}

	public String getTermSourceUri() {
		return termSourceUri;
	}
	public void setTermSourceUri(String vocabularyUri) {
		this.termSourceUri = vocabularyUri;
	}
	
	
	public Iterator<T> iterator() {
		return terms.iterator();  // OLD: new TermIterator<T>(this.terms);
	}
	
	public int size(){
		return terms.size();
	}
	
	
	/**
	 * Returns all terms of this vocabulary sorted by their representation defined by the given language.
	 * If such an representation does not exist, the representation of the default language is testing instead for ordering.
	 * @param language
	 * @return
	 */
	public SortedSet<T> getTermsOrderedByLabels(Language language){
		TermLanguageComparator<T> comp = new TermLanguageComparator<T>();
		comp.setCompareLanguage(language);
		
		SortedSet<T> result = new TreeSet<T>(comp);
		result.addAll(getTerms());
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ILoadableTerm#readCsvLine(java.util.List)
	 */
	public TermVocabulary<T> readCsvLine(List<String> csvLine) {
		return readCsvLine(csvLine, Language.CSV_LANGUAGE());
	}
	
	public TermVocabulary<T> readCsvLine(List<String> csvLine, Language lang) {
		this.setUuid(UUID.fromString(csvLine.get(0)));
		this.setUri(csvLine.get(1));
		//this.addRepresentation(Representation.NewInstance(csvLine.get(3), csvLine.get(2).trim(), lang) );
		return this;
	}
	
//*********************** CLONE ********************************************************/
	
	/** 
	 * Clones <i>this</i> TermVocabulary. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> TermVocabulary.
	 * The terms of the original vocabulary are cloned
	 * 
	 * @see eu.etaxonomy.cdm.model.common.TermBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		TermVocabulary result;
		try {
			result = (TermVocabulary) super.clone();
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
		result.terms = new HashSet<T>();
		
		return result;
	}
    
}