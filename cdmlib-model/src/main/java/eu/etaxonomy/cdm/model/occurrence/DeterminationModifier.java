/**
* Copyright (C) 2009 EDIT
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

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Modifier;

/**
 * modifier for a determination.
 * can be cf. det. rev. conf. for example
 * @author m.doering
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeterminationModifier")
@XmlRootElement(name = "DeterminationModifier")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class DeterminationModifier extends Modifier {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeterminationModifier.class);

	private static final UUID uuidUnknown = UUID.fromString("00000000-0000-0000-0000-000000000000");

	protected static Map<UUID, DeterminationModifier> termMap = null;		
	
	/**
	 * Factory method
	 * @return
	 */
	public static DeterminationModifier NewInstance() {
		return new DeterminationModifier();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static DeterminationModifier NewInstance(String term, String label, String labelAbbrev) {
		return new DeterminationModifier(term, label, labelAbbrev);
	}
	
	
	/**
	 * Constructor
	 */
	public DeterminationModifier() {
	}

	
	/**
	 * Constructor
	 */
	protected DeterminationModifier(String term, String label, String labelAbbrev) {
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

	
	@Override
	protected void setDefaultTerms(TermVocabulary<Modifier> termVocabulary) {
		termMap = new HashMap<UUID, DeterminationModifier>();
		for (Modifier term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), (DeterminationModifier)term);  //TODO casting
		}
	}
}
