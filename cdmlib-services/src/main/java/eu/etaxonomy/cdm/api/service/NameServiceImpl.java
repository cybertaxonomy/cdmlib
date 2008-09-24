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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.common.IReferencedEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.INomenclaturalStatusDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;


@Service
@Transactional(readOnly = true)
public class NameServiceImpl extends IdentifiableServiceBase<TaxonNameBase> implements INameService {
	static private final Logger logger = Logger.getLogger(NameServiceImpl.class);
	
	private ITaxonNameDao nameDao;
	@Autowired
	protected ITermVocabularyDao vocabularyDao;
	@Autowired
	protected IOrderedTermVocabularyDao orderedVocabularyDao;
	@Autowired
	@Qualifier("refEntDao")
    protected IReferencedEntityDao<ReferencedEntityBase> referencedEntityDao;
	@Autowired
	private INomenclaturalStatusDao nomStatusDao;
	@Autowired
	private ITypeDesignationDao typeDesignationDao;
	@Autowired
	private IHomotypicalGroupDao homotypicalGroupDao;

	
	@Autowired
	protected void setDao(ITaxonNameDao dao) {
		//set the base class dao
		super.dao = dao;
		this.nameDao = dao;
	}


	/**
	 * Constructor
	 */
	public NameServiceImpl(){
		logger.debug("Load NameService Bean");
	}

//********************* METHODS ****************************************************************//	
	
	public List getNamesByName(String name){
		return super.findCdmObjectsByTitle(name);
	}
	
	public List getNamesByName(String name, CdmBase sessionObject){
		return super.findCdmObjectsByTitle(name, sessionObject);
	}

	public TaxonNameBase<?,?> getTaxonNameByUuid(UUID uuid) {
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
	public Map<UUID, HomotypicalGroup> saveAllHomotypicalGroups(Collection<HomotypicalGroup> homotypicalGroups){
		return homotypicalGroupDao.saveAll(homotypicalGroups);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, TypeDesignationBase> saveTypeDesignationAll(Collection<TypeDesignationBase> typeDesignationCollection){
		return typeDesignationDao.saveAll(typeDesignationCollection);
	}

	@Transactional(readOnly = false)
	public Map<UUID, ReferencedEntityBase> saveReferencedEntitiesAll(Collection<ReferencedEntityBase> referencedEntityCollection){
		return referencedEntityDao.saveAll(referencedEntityCollection);
	}
	
	@Transactional(readOnly = false)
	public UUID removeTaxonName(TaxonNameBase taxonName) {
		return super.removeCdmObject(taxonName);
	}

	public List<TaxonNameBase> getAllNames(int limit, int start){
		return nameDao.list(limit, start);
	}

	public List<NomenclaturalStatus> getAllNomenclaturalStatus(int limit, int start){
		return nomStatusDao.list(limit, start);
	}
	 
	public List<TypeDesignationBase> getAllTypeDesignations(int limit, int start){
		return typeDesignationDao.getAllTypeDesignations(limit, start);
	}
 	
	public List<HomotypicalGroup> getAllHomotypicalGroups(int limit, int start){
		return homotypicalGroupDao.list(limit, start);
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
