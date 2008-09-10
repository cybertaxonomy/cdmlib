/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;


public interface IDefinedTermDao extends ICdmEntityDao<DefinedTermBase>, ITitledDao<DefinedTermBase>{
	
	/**
	 * @param iso639 a two or three letter language code according to iso639-1 or iso639-2
	 * @return the Language or null
	 */
	//TODO refactor typo:
	public Language getLanguageByIso(String iso639);
	
	public List<Language> getLanguagesByIso(List<String> iso639List);
	
	public List<Language> getLanguagesByLocale(Enumeration<Locale> locales);
	
	public WaterbodyOrCountry getCountryByIso(String iso639);
	
	public List<? extends DefinedTermBase> getDefinedTermByRepresentationText(String text, Class clazz );

	
}
