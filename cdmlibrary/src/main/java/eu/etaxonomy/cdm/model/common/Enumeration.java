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
import eu.etaxonomy.cdm.model.Description;
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
public class Enumeration extends DefinedTermBase {
	static Logger logger = Logger.getLogger(Enumeration.class);
	//The order of the enumeration list is a linear order that can be used for statistical purposes. Measurement scale =
	//ordinal
	private boolean isOrdinal;
	protected List<EnumeratedTermBase> terms = new ArrayList();
	//The enumeration/vocabulary source (e.g. ontology) defining the terms to be loaded when a database is created for the first time.  
	// Software can go and grap these terms incl labels and description. 
	// UUID needed? Furhter vocs can be setup through our own ontology.
	private String enumerationUri;

	
	public boolean isOrdinal(){
		return this.isOrdinal;
	}
	public void setOrdinal(boolean isOrdinal){
		this.isOrdinal = isOrdinal;
	}

	
	public List<EnumeratedTermBase> getTerms() {
		return terms;
	}
	protected void setTerms(List<EnumeratedTermBase> terms) {
		this.terms = terms;
	}
	public void addTerm(EnumeratedTermBase term) {
		term.setEnumeration(this);
	}
	public void removeTerm(EnumeratedTermBase term) {
		term.setEnumeration(null);
	}
	
	
	public String getEnumerationUri() {
		return enumerationUri;
	}
	public void setEnumerationUri(String enumerationUri) {
		this.enumerationUri = enumerationUri;
	}

	/**
	 * add new terms from a vocabulary to which uri points.
	 * 
	 * @param uri    uri
	 */
	public void loadTerms(String uri){

	}

	
}