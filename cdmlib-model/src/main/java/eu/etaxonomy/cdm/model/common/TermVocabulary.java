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
import org.hibernate.annotations.Type;
import java.util.*;

import javax.persistence.*;


/**
 * A single enumeration must only contain DefinedTerm instances of one kind
 * (=class)
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class TermVocabulary<T extends DefinedTermBase> extends TermBase implements Iterable<T> {
	static Logger logger = Logger.getLogger(TermVocabulary.class);
	
	private static final UUID uuidLanguage = UUID.fromString("17ba1c02-256d-47cf-bed0-2964ec1108ba");
	private static final UUID uuidRank = UUID.fromString("b17451eb-4278-4179-af68-44f27aa3d151");
	private static final UUID uuidContinent = UUID.fromString("ed4e5948-172a-424c-83d6-6fc7c7da70ed");
	
	
	public static TermVocabulary findByUuid(UUID uuid){
		//in tests tems may no be initialised by database access
//		if (!isInitialized()){
//			initTermList(null);
//		}
//		return termVocabularyMap.get(uuid);
		//TODO
		logger.error("Not yet implemented");
		return null;
	}
	public static final TermVocabulary getUUID(UUID uuid){
		return (TermVocabulary)findByUuid(uuid);
	}
	public static final TermVocabulary LANGUAGE(){
		return getUUID(uuidLanguage);
	}

	//The vocabulary source (e.g. ontology) defining the terms to be loaded when a database is created for the first time.  
	// Software can go and grap these terms incl labels and description. 
	// UUID needed? Further vocs can be setup through our own ontology.
	private String termSourceUri;
	protected Class termClass;

	//TODO Changed
	public Set<T> terms = getNewTermSet();
	
	//to be overriden by subclasses, e.g. OrderedTermVocabulary
	@Transient
	protected Set<T> getNewTermSet(){
		return new HashSet<T>();
	}
	
	public TermVocabulary() {
		super();
	}
	public TermVocabulary(String term, String label, String termSourceUri) {
		super(term, label);
		setTermSourceUri(termSourceUri);
	}
	
	@OneToMany(mappedBy="vocabulary")
	@Type(type="DefinedTermBase")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<T> getTerms() {
		//Set<T> result = getNewTermSet();
		//result.addAll(terms);
		//return result;
		return terms;
	}
	protected void setTerms(Set<T> terms) {
		this.terms = terms;
	}
	public void addTerm(T term) throws WrongTermTypeException {
		if (terms.size()<1){
			// no term yet in the list. First term defines the vocabulary kind
			termClass=term.getClass();
		}else if (term.getClass()!=termClass){
				// check if new term in this vocabulary matches the previous ones
				throw new WrongTermTypeException(term.getClass().getCanonicalName());
		}
		term.setVocabulary(this);
	}
	public void removeTerm(T term) {
		term.setVocabulary(null);
	}

	public String getTermSourceUri() {
		return termSourceUri;
	}
	public void setTermSourceUri(String vocabularyUri) {
		this.termSourceUri = vocabularyUri;
	}
	
	
	public Class getTermClass() {
		return termClass;
	}
	private void setTermClass(Class termClass) {
		this.termClass = termClass;
	} 
	
	
//	// inner iterator class for the iterable interface
//	private class TermIterator<T> implements Iterator<T> {
//		   // FIXME: using a list here is probably not safe. Sth passed by value, an array, would be better
//		   // but arrays cause generics problems: http://forum.java.sun.com/thread.jspa?threadID=651276&messageID=3832182
//		   // hack for now ;(
//		   private Set<T> array;
//		   private int i= 0;
//		   // ctor
//		   public TermIterator(Set<T> array) {
//		      // check for null being passed in etc.
//		      this.array= array;
//		   }
//		   // interface implementation
//		   public boolean hasNext() { return i < array.size(); }
//		   public T next() { return array.get(i++); }
//		   public void remove() { throw new UnsupportedOperationException(); }
//	}

	public Iterator<T> iterator() {
		return terms.iterator();  // OLD: new TermIterator<T>(this.terms);
	}
	
	public int size(){
		return terms.size();
	}
    
}