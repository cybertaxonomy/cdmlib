/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.common.IReferencedEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;


@Service
@Transactional(readOnly = true)
public class NameServiceImpl extends IdentifiableServiceBase<TaxonNameBase> implements INameService {
	static private final Logger logger = Logger.getLogger(NameServiceImpl.class);
	
	private ITaxonNameDao nameDao;
	protected ITermVocabularyDao vocabularyDao;
	protected IOrderedTermVocabularyDao orderedVocabularyDao;
	protected IReferencedEntityDao referencedEntityDao;

	
	@Autowired
	protected void setDao(ITaxonNameDao dao) {
		this.dao = dao;
		this.nameDao = dao;
	}
	
	@Autowired
	protected void setVocabularyDao(ITermVocabularyDao vocabularyDao) {
		this.vocabularyDao = vocabularyDao;
	}
	
	@Autowired
	protected void setOrderedVocabularyDao(IOrderedTermVocabularyDao orderedVocabularyDao) {
		this.orderedVocabularyDao = orderedVocabularyDao;
	}

	@Autowired
	protected void setReferencedEntityDao(IReferencedEntityDao referencedEntityDao) {
		this.referencedEntityDao = referencedEntityDao;
	}

	public NameServiceImpl(){
		logger.debug("Load NameService Bean");
	}

	public List getNamesByName(String name){
		return super.findCdmObjectsByTitle(name);
	}
	
	public List getNamesByName(String name, CdmBase sessionObject){
		return super.findCdmObjectsByTitle(name, sessionObject);
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
	public Map<UUID, ReferencedEntityBase> saveTypeDesignationAll(Collection<ReferencedEntityBase> typeDesignationCollection){
		return referencedEntityDao.saveAll(typeDesignationCollection);
	}
	
	@Transactional(readOnly = false)
	public UUID removeTaxonName(TaxonNameBase taxonName) {
		return super.removeCdmObject(taxonName);
	}

	public List<TaxonNameBase> getAllNames(int limit, int start){
		return nameDao.list(limit, start);
		//return dao.list(limit, start);
	}

	public List<ReferencedEntityBase> getAllNomenclaturalStatus(int limit, int start){
		return nameDao.getAllNomenclaturalStatus(limit, start);
	}
	 
	public List<ReferencedEntityBase> getAllTypeDesignations(int limit, int start){
		return nameDao.getAllTypeDesignations(limit, start);
	}
	
	public OrderedTermVocabulary<Rank> getRankVocabulary() {
		String uuidRank = "ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b";
		UUID rankUuid = UUID.fromString(uuidRank);
		OrderedTermVocabulary<Rank> rankVocabulary = (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(rankUuid);
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
