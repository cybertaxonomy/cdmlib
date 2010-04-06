// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringBaseDao;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringDao;
import eu.etaxonomy.cdm.persistence.dao.common.IRepresentationDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TermServiceImpl extends IdentifiableServiceBase<DefinedTermBase,IDefinedTermDao> implements ITermService{
	private static final Logger logger = Logger.getLogger(TermServiceImpl.class);
	private ILanguageStringDao languageStringDao;
	@Autowired
	@Qualifier("langStrBaseDao")
	private ILanguageStringBaseDao languageStringBaseDao;
	private IRepresentationDao representationDao;
	
	@Autowired
	public void setLanguageStringDao(ILanguageStringDao languageStringDao) {
		this.languageStringDao = languageStringDao;
	} 
	
	@Autowired
	public void setRepresentationDao(IRepresentationDao representationDao) {
		this.representationDao = representationDao;
	}
	
	@Autowired
	protected void setDao(IDefinedTermDao dao) {
		this.dao = dao;
	}
		
	public <TERM extends DefinedTermBase> List<TERM> listByTermClass(Class<TERM> clazz, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		return dao.listByTermClass(clazz, limit, start, orderHints, propertyPaths);
	}	
	
	/**
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getTermByUri(java.lang.String)
	 */
	public DefinedTermBase getByUri(String uri) {
		//FIXME transformation from URI to UUID
		return dao.findByUri(uri);
	}
	
	public Language getLanguageByIso(String iso639) {
		return dao.getLanguageByIso(iso639);
	}
	
	public List<Language> getLanguagesByLocale(Enumeration<Locale> locales){
		return dao.getLanguagesByLocale(locales);
	}

	/**
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getAreaByTdwgAbbreviation(java.lang.String)
	 */
	public NamedArea getAreaByTdwgAbbreviation(String tdwgAbbreviation) {
		//FIXME this is just a placeholder until it is decided where to implement this method 
		//(see also FIXMEs in TdwgArea)
		return TdwgArea.getAreaByTdwgAbbreviation(tdwgAbbreviation);
	}

	public <T extends DefinedTermBase> Pager<T> getGeneralizationOf(T definedTerm, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countGeneralizationOf(definedTerm);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getGeneralizationOf(definedTerm, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public <T extends DefinedTermBase> Pager<T> getIncludes(Set<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countIncludes(definedTerms);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getIncludes(definedTerms, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber) {
        Integer numberOfResults = dao.countMedia(definedTerm);
		
		List<Media> results = new ArrayList<Media>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getMedia(definedTerm, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
	}

	public <T extends DefinedTermBase> Pager<T> getPartOf(Set<T> definedTerms,Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countPartOf(definedTerms);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getPartOf(definedTerms, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber,
			List<OrderHint> orderHints, List<String> propertyPaths) {
		Integer numberOfResults = dao.count(level, type);

		List<NamedArea> results = new ArrayList<NamedArea>();
		if (numberOfResults > 0) { // no point checking again
			results = dao.list(level, type, pageSize, pageNumber, orderHints, propertyPaths);
		}

		return new DefaultPagerImpl<NamedArea>(pageNumber, numberOfResults, pageSize, results);
	}

	public <T extends DefinedTermBase> Pager<T> findByRepresentationText(String label, Class<T> clazz, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countDefinedTermByRepresentationText(label,clazz);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getDefinedTermByRepresentationText(label, clazz, pageSize, pageNumber);
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public List<LanguageString> getAllLanguageStrings(int limit, int start) {
		return languageStringDao.list(limit, start);
	}

	public List<Representation> getAllRepresentations(int limit, int start) {
		return representationDao.list(limit,start);
	}

	public UUID saveLanguageData(LanguageStringBase languageData) {
		return languageStringBaseDao.save(languageData);
	}

	public void generateTitleCache() {
		// TODO Auto-generated method stub
		
	}	
}
