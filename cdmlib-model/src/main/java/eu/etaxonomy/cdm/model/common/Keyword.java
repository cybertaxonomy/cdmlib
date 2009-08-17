/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

/**
 * simple keywords. could be taxonomic scope/skill , geographic scope or anything else
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:31
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Keyword")
@Entity
@Audited
public class Keyword extends OrderedTermBase<Keyword> {
	private static final long serialVersionUID = -4907208557010483263L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Keyword.class);

	public static Keyword NewInstance(){
		return new Keyword();
	}


	public static Keyword NewInstance(String term, String label, String labelAbbrev){
		return new Keyword(term, label, labelAbbrev);
	}

	
	public Keyword() {
		super();
	}

	public Keyword(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}


	@Override
	protected void setDefaultTerms(TermVocabulary<Keyword> termVocabulary) {
		// TODO Auto-generated method stub
	}

}