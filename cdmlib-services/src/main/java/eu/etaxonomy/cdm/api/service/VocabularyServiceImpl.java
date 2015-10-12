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
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(readOnly = true)
public class VocabularyServiceImpl extends IdentifiableServiceBase<TermVocabulary,ITermVocabularyDao>  implements IVocabularyService {

	@Override
    @Autowired
	protected void setDao(ITermVocabularyDao dao) {
		this.dao = dao;
	}


	@Override
	@Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends TermVocabulary> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TermVocabulary> cacheStrategy, IProgressMonitor monitor) {
		if (clazz == null){
			clazz = TermVocabulary.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
	}


    @Override
    public List<TermVocabulary> listByTermType(TermType termType, boolean includeSubTypes,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return dao.listByTermType(termType, includeSubTypes, limit, start, orderHints, propertyPaths);
    }

    @Override
	public <T extends DefinedTermBase> List<TermVocabulary<T>> findByTermType(TermType termType) {
		return dao.findByTermType(termType);
	}
	/**
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getLanguageVocabulary()
	 * FIXME candidate for harmonization
	 * is this the same as getVocabulary(VocabularyEnum.Language)
	 */
	@Override
	public TermVocabulary<Language> getLanguageVocabulary() {
		String uuidString = "45ac7043-7f5e-4f37-92f2-3874aaaef2de";
		UUID uuid = UUID.fromString(uuidString);
		TermVocabulary<Language> languageVocabulary = dao.findByUuid(uuid);
		return languageVocabulary;
	}

	@Override
	public Pager<DefinedTermBase> getTerms(TermVocabulary vocabulary, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
        Integer numberOfResults = dao.countTerms(vocabulary);

		List<DefinedTermBase> results = new ArrayList<DefinedTermBase>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getTerms(vocabulary, pageSize, pageNumber,orderHints,propertyPaths);
		}

		return new DefaultPagerImpl<DefinedTermBase>(pageNumber, numberOfResults, pageSize, results);
	}

}
