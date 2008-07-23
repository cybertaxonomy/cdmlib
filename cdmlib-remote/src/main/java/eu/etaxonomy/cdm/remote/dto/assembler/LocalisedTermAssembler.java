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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
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

	public static final int LABEL = 0;
	public static final int ABBREVIATED_LABEL = 1;
	public static final int DESCRIPTION = 2;
	public static final int TEXT = 3; 
	
	@Autowired
	private IDefinedTermDao languageDao;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	LocalisedTermSTO getSTO(TermBase term, Enumeration<Locale> locales) {
		return this.getSTO(term, locales, LABEL);
	}
	
	LocalisedTermSTO getSTO(TermBase term, Enumeration<Locale> locales, int termVariant) {
		LocalisedTermSTO localisedTermSto = new LocalisedTermSTO();
		List<Language> languages = languageDao.getLanguagesByLocale(locales);
		Representation representation = term.getPreferredRepresentation(languages);
		
		switch(termVariant){
			case LABEL: 
				localisedTermSto.setTerm(representation.getLabel());
				break;
			case ABBREVIATED_LABEL: 
				localisedTermSto.setTerm(representation.getAbbreviatedLabel());
				break;
			case DESCRIPTION: 
				localisedTermSto.setTerm(representation.getDescription());
				break;
			case TEXT: 
				localisedTermSto.setTerm(representation.getText());
				break;
		}		
		
		localisedTermSto.setLanguage(representation.getLanguage().toString());
		return localisedTermSto;
	}
	
	
	
	/**
	 * Method not implemented since class <code>LocalisedTermTO</code> does not exist.
	 */
	@Deprecated
	BaseTO getTO(TermBase cdmObj, Enumeration<Locale> locales) {
		throw new RuntimeException("not implemented, class LocalisedTermTO does not exist.");
	}
	

}
