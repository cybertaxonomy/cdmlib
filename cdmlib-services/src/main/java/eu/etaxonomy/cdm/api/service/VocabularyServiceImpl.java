/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class VocabularyServiceImpl extends IdentifiableServiceBase<TermVocabulary,ITermVocabularyDao>  implements IVocabularyService {

	@Autowired
	protected void setDao(ITermVocabularyDao dao) {
		this.dao = dao;
	}

	public void generateTitleCache() {
		// TODO Auto-generated method stub
	}
	
	public TermVocabulary<DefinedTermBase> getVocabulary(VocabularyEnum vocabularyType){
		return dao.findByUuid(vocabularyType.getUuid());
	}
	
	public <TERM extends DefinedTermBase> List<TermVocabulary<TERM>> listByTermClass(Class<TERM> clazz, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		return dao.listByTermClass(clazz, limit, start, orderHints, propertyPaths);
	}	
	
	/** 
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getLanguageVocabulary()
	 * FIXME candidate for harmonization
	 * is this the same as getVocabulary(VocabularyEnum.Language)
	 */
	public TermVocabulary<Language> getLanguageVocabulary() {
		String uuidString = "45ac7043-7f5e-4f37-92f2-3874aaaef2de";
		UUID uuid = UUID.fromString(uuidString);
		TermVocabulary<Language> languageVocabulary = (TermVocabulary)dao.findByUuid(uuid);
		return languageVocabulary;
	}

	public Pager<DefinedTermBase> getTerms(TermVocabulary vocabulary, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
        Integer numberOfResults = dao.countTerms(vocabulary);
		
		List<DefinedTermBase> results = new ArrayList<DefinedTermBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getTerms(vocabulary, pageSize, pageNumber,orderHints,propertyPaths); 
		}
			
		return new DefaultPagerImpl<DefinedTermBase>(pageNumber, numberOfResults, pageSize, results);
	}

}
