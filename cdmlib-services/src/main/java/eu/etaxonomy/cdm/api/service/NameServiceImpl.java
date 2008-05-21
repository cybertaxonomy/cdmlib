/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class NameServiceImpl extends IdentifiableServiceBase<TaxonNameBase> implements INameService {
	static Logger logger = Logger.getLogger(NameServiceImpl.class);
	
	protected ITermVocabularyDao vocabularyDao;
	
	@Autowired
	protected void setDao(ITaxonNameDao dao) {
		this.dao = dao;
	}
	
	@Autowired
	protected void setVocabularyDao(ITermVocabularyDao vocabularyDao) {
		this.vocabularyDao = vocabularyDao;
	}

	public NameServiceImpl(){
		logger.info("Load NameService Bean");
	}

	public List getNamesByName(String name){
		return super.findCdmObjectsByTitle(name);
	}

	public TaxonNameBase getTaxonNameByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid);
	}

	@Transactional(readOnly = false)
	public UUID saveTaxonName(TaxonNameBase taxonName) {
		return super.saveCdmObject(taxonName);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, TaxonNameBase> saveTaxonNameAll(Collection<TaxonNameBase> taxonNameCollection){
		return saveCdmObjectAll(taxonNameCollection);
	}
	
	@Transactional(readOnly = false)
	public UUID removeTaxonName(TaxonNameBase taxonName) {
		return super.removeCdmObject(taxonName);
	}

	public List<TaxonNameBase> getAllNames(int limit, int start){
		return dao.list(limit, start);
	}

	public OrderedTermVocabulary<Rank> getRankVocabulary() {
		String uuidRank = "ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b";
		UUID rankUuid = UUID.fromString(uuidRank);
		OrderedTermVocabulary<Rank> rankVocabulary = (OrderedTermVocabulary)vocabularyDao.findByUuid(rankUuid);
		return rankVocabulary;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getNameRelationshipTypeVocabulary()
	 */
	public TermVocabulary<NameRelationshipType> getNameRelationshipTypeVocabulary() {
		String uuidRank = "6878cb82-c1a4-4613-b012-7e73b413c8cd";
		UUID rankUuid = UUID.fromString(uuidRank);
		TermVocabulary<NameRelationshipType> nameRelTypeVocabulary = (TermVocabulary)vocabularyDao.findByUuid(rankUuid);
		return nameRelTypeVocabulary;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getStatusTypeVocabulary()
	 */
	public TermVocabulary<NomenclaturalStatusType> getStatusTypeVocabulary() {
		String uuidRank = "bb28cdca-2f8a-4f11-9c21-517e9ae87f1f";
		UUID rankUuid = UUID.fromString(uuidRank);
		TermVocabulary<NomenclaturalStatusType> nomStatusTypeVocabulary = (TermVocabulary)vocabularyDao.findByUuid(rankUuid);
		return nomStatusTypeVocabulary;
	}

	public void generateTitleCache() {
		logger.warn("Not yet implemented");
		// TODO Auto-generated method stub
	}

}
