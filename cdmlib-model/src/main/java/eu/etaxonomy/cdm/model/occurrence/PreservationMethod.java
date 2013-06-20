/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * http://rs.tdwg.org/ontology/voc/Collection.rdf#SpecimenPreservationMethodTypeTerm
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PreservationMethod")
@XmlRootElement(name = "PreservationMethod")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class PreservationMethod extends DefinedTermBase<PreservationMethod> {
	private static final long serialVersionUID = -6597303767771121540L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PreservationMethod.class);
	
	protected static Map<UUID, PreservationMethod> termMap = null;
	
	/**
	 * Factory method
	 * @return
	 */
	public static PreservationMethod NewInstance(){
		return new PreservationMethod();
	}
	
	/**
	 * Constructor
	 */
	public PreservationMethod() {
	}
	
	/**
	 * Factory method
	 * @return
	 */
	public static PreservationMethod NewInstance(String term, String label, String labelAbbrev) {
		return new PreservationMethod(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	protected PreservationMethod(String term, String label, String labelAbbrev) {
		super(TermType.PreservationMethod, term, label, labelAbbrev);
	}

// *************************** METHODS ******************************************************/	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}

	
	@Override
	protected void setDefaultTerms(TermVocabulary<PreservationMethod> termVocabulary){
		termMap = new HashMap<UUID, PreservationMethod>();
		for (PreservationMethod term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term); 
		}
	}
}
