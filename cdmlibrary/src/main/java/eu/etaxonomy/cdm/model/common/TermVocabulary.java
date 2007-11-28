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
public class TermVocabulary extends TermBase {
	static Logger logger = Logger.getLogger(TermVocabulary.class);
	//The order of the enumeration list is a linear order that can be used for statistical purposes. Measurement scale =
	//ordinal
	private boolean isOrdinal;
	protected List<DefinedTermBase> terms = new ArrayList();
	//The vocabulary source (e.g. ontology) defining the terms to be loaded when a database is created for the first time.  
	// Software can go and grap these terms incl labels and description. 
	// UUID needed? Further vocs can be setup through our own ontology.
	private String termSourceUri;
	private Class termClass;

	
	public TermVocabulary() {
		super();
	}
	public TermVocabulary(String term, String label, String termSourceUri) {
		super(term, label);
		setTermSourceUri(termSourceUri);
	}


	public boolean isOrdinal(){
		return this.isOrdinal;
	}
	public void setOrdinal(boolean isOrdinal){
		this.isOrdinal = isOrdinal;
	}

	
	@OneToMany(mappedBy="vocabulary")
	@Cascade({CascadeType.SAVE_UPDATE})
	public List<DefinedTermBase> getTerms() {
		return terms;
	}
	protected void setTerms(List<DefinedTermBase> terms) {
		this.terms = terms;
	}
	public void addTerm(DefinedTermBase term) throws WrongTermTypeException {
		if (terms.size()<1){
			// no term yet in the list. First term defines the vocabulary kind
			termClass=term.getClass();
		}else if (term.getClass()!=termClass){
				// check if new term in this vocabulary matches the previous ones
				throw new WrongTermTypeException(term.getClass().getCanonicalName());
		}
		term.setVocabulary(this);
	}
	public void removeTerm(DefinedTermBase term) {
		term.setVocabulary(null);
	}

	
	public List<DefinedTermBase> getPrecedingTerms(OrderedTermBase otb) {
		// FIXME: need to return only OrderedTermBase lists
		return terms.subList(0, terms.indexOf(otb));
	}
	public List<DefinedTermBase> getSucceedingTerms(OrderedTermBase otb) {
		// FIXME: need to return only OrderedTermBase lists
		return terms.subList(terms.indexOf(otb), terms.size());
	}
	public OrderedTermBase getPreviousTerm(OrderedTermBase otb) {
		int idx = terms.indexOf(otb)-1;
		return (OrderedTermBase)terms.get(idx);
	}
	public OrderedTermBase getNextTerm(OrderedTermBase otb) {
		int idx = terms.indexOf(otb)+1;
		return (OrderedTermBase)terms.get(idx);
	}

	
	public String getTermSourceUri() {
		return termSourceUri;
	}
	public void setTermSourceUri(String vocabularyUri) {
		this.termSourceUri = vocabularyUri;
	}
	
	
	protected Class getTermClass() {
		return termClass;
	}
	protected void setTermClass(Class termClass) {
		this.termClass = termClass;
	}
}