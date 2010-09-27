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
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * This class represents terms describing different types of absence
 * (like "extinct" or just "absent") of a {@link Taxon taxon} in a {@link NamedArea particular area}.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:08
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbsenceTerm")
@XmlRootElement(name = "AbsenceTerm")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class AbsenceTerm extends PresenceAbsenceTermBase<AbsenceTerm> {
	private static final long serialVersionUID = -7125360212309512860L;
	private static final Logger logger = Logger.getLogger(AbsenceTerm.class);
	
	private static Map<UUID, AbsenceTerm> termMap = null;
	
	private static final UUID uuidAbsence=UUID.fromString("59709861-f7d9-41f9-bb21-92559cedd598");
	private static final UUID uuidNF=UUID.fromString("61cee840-801e-41d8-bead-015ad866c2f1");
	private static final UUID uuidIF=UUID.fromString("aeec2947-2700-4623-8e32-9e3a430569d1");
	private static final UUID uuidCF=UUID.fromString("9d4d3431-177a-4abe-8e4b-1558573169d6");
	

	/** 
	 * Creates a new empty absence term.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static AbsenceTerm NewInstance(){
		logger.debug("NewInstance");
		return new AbsenceTerm();
	}
	
	/** 
	 * Creates a new absence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new absence term to be created 
	 * @param	label  		 the string identifying the new absence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new absence term to be created
	 * @see 				 #NewInstance()
	 */
	public static AbsenceTerm NewInstance(String term, String label, String labelAbbrev){
		return new AbsenceTerm(term, label, labelAbbrev);
	}
		
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty absence term.
	 * 
	 * @see #AbsenceTerm(String, String, String)
	 */
	public AbsenceTerm() {
	}

	/** 
	 * Class constructor: creates a new absence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new absence term to be created 
	 * @param	label  		 the string identifying the new absence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new absence term to be created
	 * @see 				 #AbsenceTerm()
	 */
	public AbsenceTerm(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	
//************************** METHODS ********************************
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}

	protected static AbsenceTerm getTermByUuid(UUID uuid){
		if (termMap == null){
			return null;
		}else{
			return (AbsenceTerm)termMap.get(uuid);
		}
	}

	
	public static final AbsenceTerm ABSENT(){
		return getTermByUuid(uuidAbsence);
	}
	
	public static final AbsenceTerm NATIVE_REPORTED_IN_ERROR(){
		return getTermByUuid(uuidNF);
	}
	
	public static final AbsenceTerm CULTIVATED_REPORTED_IN_ERROR(){
		return getTermByUuid(uuidCF);
	}

	public static final AbsenceTerm INTRODUCED_REPORTED_IN_ERROR(){
		return getTermByUuid(uuidIF);
	}

	//TODO make automatic like in TDWGArea
	public static AbsenceTerm getPresenceTermByAbbreviation(String abbrev) { 
		if (abbrev == null) { throw new NullPointerException("abbrev is 'null' in getPresenceTermByAbbreviation");
		} else if (abbrev.equalsIgnoreCase("cf")) { return AbsenceTerm.CULTIVATED_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("if")) { return AbsenceTerm.INTRODUCED_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("nf")) { return AbsenceTerm.NATIVE_REPORTED_IN_ERROR();
		} else {
			logger.warn("Unknown absence status term: " + abbrev);
			return null;
		}
	}

	
	@Override
	protected void setDefaultTerms(TermVocabulary<AbsenceTerm> termVocabulary) {
		termMap = new HashMap<UUID, AbsenceTerm>();
		for (AbsenceTerm term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), (AbsenceTerm)term);  //TODO casting
		}
	}

}