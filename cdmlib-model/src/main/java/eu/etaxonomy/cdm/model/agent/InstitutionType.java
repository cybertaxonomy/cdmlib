/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * Represents an element of a controlled {@link eu.etaxonomy.cdm.model.common.TermVocabulary vocabulary} for different kinds of institutions.
 * Each {@link DefinedTermBase element} belongs to one vocabulary.
 * <p>
 * This class corresponds to: InstitutionTypeTerm according to the TDWG ontology.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:30
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstitutionType")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class InstitutionType extends DefinedTermBase<InstitutionType> {
	private static final long serialVersionUID = 8714866112728127219L;
	public static final Logger logger = Logger.getLogger(InstitutionType.class);

	protected static Map<UUID, InstitutionType> termMap = null;	
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty institution type.
	 * 
	 * @see #InstitutionType(String, String, String)
	 */
	public InstitutionType() {
	}

	/** 
	 * Class constructor using a description (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 *
	 * @param	term   		 the string describing this new vocabulary element
	 * @param	label  		 the string which identifies this new vocabulary element
	 * @param	labelAbbrev  the string identifying (in abbreviated form) this
	 * 						 new vocabulary element
	 * @see           		 #InstitutionType()
	 * @see           		 eu.etaxonomy.cdm.model.common.Representation
	 * @see           		 eu.etaxonomy.cdm.model.common.TermBase#TermBase(String, String, String)
	 */
	public InstitutionType(String term, String label, String labelAbbrev) {
		super(TermType.InstitutionType, term, label, labelAbbrev);
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}

	
	protected static InstitutionType getTermByUuid(UUID uuid){
		if (termMap == null){
			DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
			vocabularyStore.initialize();
		}
		return (InstitutionType)termMap.get(uuid);
	}

	
	
	@Override
	protected void setDefaultTerms(TermVocabulary<InstitutionType> termVocabulary){
		termMap = new HashMap<UUID, InstitutionType>();
		for (InstitutionType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term); 
		}
	}
}