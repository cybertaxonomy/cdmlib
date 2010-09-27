/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * The class representing restrictions for the validity of
 * {@link TaxonDescription taxon descriptions}. This could include not only {@link Stage life stage}
 * or {@link Sex sex} but also for instance particular organism parts or seasons.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:50
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Scope")
@XmlRootElement(name = "Scope")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class Scope extends Modifier {
	private static final long serialVersionUID = 4479960075363470677L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Scope.class);
	
	protected static Map<UUID, Scope> termMap = null;
	
	// ************* CONSTRUCTORS *************/	

	/** 
	 * Class constructor: creates a new empty scope instance.
	 * 
	 * @see #Scope(String, String, String)
	 */
	public Scope() {
	}

	/** 
	 * Class constructor: creates a new scope instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new scope to be created 
	 * @param	label  		 the string identifying the new scope to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new scope to be created
	 * @see 				 #Scope()
	 */
	protected Scope(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	
	//********* METHODS **************************************/

	/** 
	 * Creates a new empty scope instance.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static Scope NewInstance(){
		return new Scope();
	}
	
	/** 
	 * Creates a new scope instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new scope to be created 
	 * @param	label  		 the string identifying the new scope to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new scope to be created
	 * @see 				 #NewInstance()
	 */
	public static Scope NewInstance(String term, String label, String labelAbbrev){
		return new Scope(term, label, labelAbbrev);
	}
	

	
//************************** METHODS ********************************
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}

	
	@Override
	protected void setDefaultTerms(TermVocabulary<Modifier> termVocabulary) {
		termMap = new HashMap<UUID, Scope>();
		for (Modifier term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), (Scope)term);  //TODO casting
		}
	}
}