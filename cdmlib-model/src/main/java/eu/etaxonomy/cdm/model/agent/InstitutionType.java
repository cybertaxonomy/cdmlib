/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
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
@Audited
public class InstitutionType extends DefinedTermBase<InstitutionType> {
	private static final long serialVersionUID = 8714866112728127219L;
	public static final Logger logger = Logger.getLogger(InstitutionType.class);

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty institution type.
	 * 
	 * @see #InstitutionType(String, String, String)
	 */
	public InstitutionType() {
		super();
		// TODO Auto-generated constructor stub
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
		super(term, label, labelAbbrev);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<InstitutionType> termVocabulary) {
		// TODO Auto-generated method stub
		
	}

	public int compareTo(Object o) {
		return 0;
	}
}