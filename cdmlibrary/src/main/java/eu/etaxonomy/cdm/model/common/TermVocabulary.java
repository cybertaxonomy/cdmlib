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
public class TermVocabulary extends DefinedTermBase {
	static Logger logger = Logger.getLogger(TermVocabulary.class);
	//The order of the enumeration list is a linear order that can be used for statistical purposes. Measurement scale =
	//ordinal
	private boolean isOrdinal;
	protected List<OrderedTermBase> terms = new ArrayList();
	//The enumeration/vocabulary source (e.g. ontology) defining the terms to be loaded when a database is created for the first time.  
	// Software can go and grap these terms incl labels and description. 
	// UUID needed? Furhter vocs can be setup through our own ontology.
	private String enumerationUri;

	
	public TermVocabulary(String term, String label, String enumerationUri) {
		super(term, label);
		setEnumerationUri(enumerationUri);
	}

	
	public boolean isOrdinal(){
		return this.isOrdinal;
	}
	public void setOrdinal(boolean isOrdinal){
		this.isOrdinal = isOrdinal;
	}

	
	@OneToMany(mappedBy="enumeration")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public List<OrderedTermBase> getTerms() {
		return terms;
	}
	protected void setTerms(List<OrderedTermBase> terms) {
		this.terms = terms;
	}
	public void addTerm(OrderedTermBase term) {
		term.setEnumeration(this);
	}
	public void removeTerm(OrderedTermBase term) {
		term.setEnumeration(null);
	}

	public List<OrderedTermBase> getPrecedingTerms(OrderedTermBase etb) {
		return terms.subList(0, terms.indexOf(etb));
	}
	public List<OrderedTermBase> getSucceedingTerms(OrderedTermBase etb) {
		return terms.subList(terms.indexOf(etb), terms.size());
	}
	public OrderedTermBase getPreviousTerm(OrderedTermBase etb) {
		int idx = terms.indexOf(etb)-1;
		return terms.get(idx);
	}
	public OrderedTermBase getNextTerm(OrderedTermBase etb) {
		int idx = terms.indexOf(etb)+1;
		return terms.get(idx);
	}

	
	public String getEnumerationUri() {
		return enumerationUri;
	}
	public void setEnumerationUri(String enumerationUri) {
		this.enumerationUri = enumerationUri;
	}
}