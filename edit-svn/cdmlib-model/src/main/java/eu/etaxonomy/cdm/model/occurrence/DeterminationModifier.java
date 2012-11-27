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
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
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

	public static final UUID uuidConfer = UUID.fromString("20db670a-2db2-49cc-bbdd-eace33694b7f");
	public static final UUID uuidAffinis = UUID.fromString("128f0b54-73e2-4efb-bfda-a6243185a562");

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
	
	protected static DeterminationModifier getTermByUuid(UUID uuid){
		if (termMap == null){
			return null;
		}else{
			return (DeterminationModifier)termMap.get(uuid);
		}
	}

	
	public static final DeterminationModifier AFFINIS(){
		return getTermByUuid(uuidAffinis);
	}

	public static final DeterminationModifier CONFER(){
		return getTermByUuid(uuidConfer);
	}

	
	
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
