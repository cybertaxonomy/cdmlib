/**
 *
 */
package eu.etaxonomy.cdm.remote.dto.assembler;
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.remote.dto.BaseTO;
import eu.etaxonomy.cdm.remote.dto.LocalisedTermSTO;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 23.05.2008 22:08:28
 *
 */

@Component
public class LocalisedTermAssembler extends AssemblerBase <LocalisedTermSTO, BaseTO, TermBase>  {
	
	enum TermType{
		TEXT,
		LABEL,
		ABBREVLABEL;
	}

	@Autowired
	private IDefinedTermDao languageDao;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	public LocalisedTermSTO getSTO(TermBase term, Enumeration<Locale> locales) {
		return getSTO(term, locales, TermType.LABEL);
	}
	
	public LocalisedTermSTO getSTO(TermBase term, Enumeration<Locale> locales, TermType termtype ) {
		LocalisedTermSTO sto = new LocalisedTermSTO();
		List<Language> languages = languageDao.getLanguagesByLocale(locales);
		Representation r = term.getPreferredRepresentation(languages);
		switch(termtype){
		case LABEL: 
			sto.setTerm(r.getLabel()); 
			break;
		case TEXT: 
			sto.setTerm(r.getText()); 
			break;
		case ABBREVLABEL: 
			sto.setTerm(r.getAbbreviatedLabel()); 
			break;
		}
		sto.setLanguage(r.getLanguage().toString());
		return sto;
	}
	
	public LocalisedTermSTO getSTO(RelationshipTermBase term, Enumeration<Locale> locales, boolean inverseRepresenation) {
		LocalisedTermSTO sto = new LocalisedTermSTO();
		
		//Representation r = term.getRepresentation(null);
		sto.setTerm(inverseRepresenation ? term.getInverseLabel() : term.getLabel());
		
		
		return sto;
	}

	/**
	 * Method not implemented since class <code>LocalisedTermTO</code> does not exist.
	 */
	@Deprecated
	BaseTO getTO(TermBase cdmObj, Enumeration<Locale> locales) {
		throw new RuntimeException("not implemented, class LocalisedTermTO does not exist.");
	}
	

}
