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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.format.ReferencingObjectFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.SingleSourcedEntityBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.taxon.SecundumSource;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IOriginalSourceDao;
import eu.etaxonomy.cdm.persistence.dto.ReferencingObjectDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.match.MatchStrategyConfigurator;
import eu.etaxonomy.cdm.strategy.match.MatchStrategyConfigurator.MatchStrategy;
import eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

@Service
@Transactional(readOnly = true)
public class CommonServiceImpl
        /*extends ServiceBase<OriginalSourceBase,IOriginalSourceDao>*/
        implements ICommonService {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CommonServiceImpl.class);

    @Autowired
    private IOriginalSourceDao originalSourceDao;

    @Autowired
    private ICdmGenericDao genericDao;

    @Override
    public <T extends CdmBase> T findWithUpdate(Class<T> clazz, int id){
        return genericDao.find(clazz, id);
    }

    @Override
    public <T extends CdmBase> T find(Class<T> clazz, int id){
        return genericDao.find(clazz, id);
    }

    @Override
    public <T extends CdmBase> T find(Class<T> clazz, int id, List<String> propertyPaths){
        return  genericDao.find(clazz, id, propertyPaths);
    }

    @Override
    public <T extends CdmBase> T find(Class<T> clazz, UUID uuid) {
        return uuid == null ? null : genericDao.find(clazz, uuid);
    }

    @Override
    public <T extends CdmBase> T find(Class<T> clazz, UUID uuid, List<String> propertyPaths) {
        return uuid == null ? null : genericDao.find(clazz, uuid, propertyPaths);
    }

    @Override
    public <S extends ISourceable> Map<String, S> getSourcedObjectsByIdInSourceC(Class<S> clazz, Set<String> idInSourceSet, String idNamespace){
        Map<String, S> list = originalSourceDao.findOriginalSourcesByIdInSource(clazz, idInSourceSet, idNamespace);
        return list;
    }

    @Override
    public <S extends ISourceable> S getSourcedObjectByIdInSource(Class<S> clazz, String idInSource, String idNamespace) {
        S result = null;
        List<S> list = originalSourceDao.findOriginalSourceByIdInSource(clazz, idInSource, idNamespace);
        if (! list.isEmpty()){
            result = list.get(0);
        }return result;
    }

    @Override
    public Set<ReferencingObjectDto> getReferencingObjectDtos(CdmBase referencedCdmBase){
        return this.genericDao.getReferencingObjectsDto(referencedCdmBase);
    }

    @Override
    public Set<CdmBase> getReferencingObjects(CdmBase referencedCdmBase){
        return this.genericDao.getReferencingObjects(referencedCdmBase);
    }

    @Override
    public long getReferencingObjectsCount(CdmBase referencedCdmBase){
        return this.genericDao.getReferencingObjectsCount(referencedCdmBase);
    }

    @Override
    public Set<CdmBase> getReferencingObjectsForDeletion(CdmBase referencedCdmBase){
        return this.genericDao.getReferencingObjectsForDeletion(referencedCdmBase);
    }


    @Override
    public Set<ReferencingObjectDto> initializeReferencingObjectDtos(Set<ReferencingObjectDto> dtos,
            boolean doReferencingEntity, boolean doTargetEntity, boolean doDescription, Language language) {

        for (ReferencingObjectDto dto : dtos){
            //TODO or load()?
            CdmBase entity = this.genericDao.find(dto.getType(), dto.getUuid());
            entity = CdmBase.deproxy(entity); //TODO necessary here or should we only do this in called methods below
            if (doReferencingEntity){
                dto.setReferencedEntity(entity);
            }
            if (doTargetEntity){
                UuidAndTitleCache<CdmBase> target = getReferencingObjectTarget(entity);
                dto.setOpenInTarget(target);
            }
            if (doDescription){
                String targetString = dto.getOpenInTarget() == null ? null : dto.getOpenInTarget().getTitleCache();
                String description = getReferencingObjectDescription(entity, targetString, language);
                dto.setTitleCache(description);
            }
        }
        return dtos;
    }

    private UuidAndTitleCache<CdmBase> getReferencingObjectTarget(CdmBase entity) {
        CdmBase targetEntity;
        entity = CdmBase.deproxy(entity);
        if (entity instanceof SecundumSource){
            targetEntity = ((SecundumSource) entity).getSourcedTaxon();
        }else if (entity instanceof NomenclaturalSource){
            targetEntity = ((NomenclaturalSource) entity).getSourcedName();
        }else if (entity instanceof DescriptionElementSource){
            DescriptionElementBase element = ((DescriptionElementSource) entity).getSourcedElement();
            targetEntity = getTarget(element);
        }else if (entity instanceof DescriptionElementBase){
           targetEntity = getTarget((DescriptionElementBase)entity);
        }else if (entity instanceof IdentifiableSource){
            IdentifiableSource source = ((IdentifiableSource) entity);
            targetEntity = originalSourceDao.findIdentifiableBySourceId(IdentifiableEntity.class, source.getId());
        }else if (entity instanceof NamedSource){
            NamedSource source = ((NamedSource) entity);
            SingleSourcedEntityBase singleSourced = originalSourceDao.findSingleSourceBySourceId(SingleSourcedEntityBase.class, source.getId());
            if (singleSourced != null){
                targetEntity = singleSourced;
            }else{
                //TODO
                targetEntity = entity;
            }
        }else if (entity instanceof DescriptionBase){
            targetEntity = getTarget((DescriptionBase)entity);
        }else{
            targetEntity = entity;
        }
        targetEntity = CdmBase.deproxy(targetEntity);
        String targetLabel = targetEntity instanceof IdentifiableEntity ? ((IdentifiableEntity)targetEntity).getTitleCache() : null;
        UuidAndTitleCache<CdmBase> result = new UuidAndTitleCache<>(targetEntity.getClass(), targetEntity.getUuid(), targetEntity.getId(), targetLabel);
        return result;
    }

    private CdmBase getTarget(DescriptionElementBase element) {
        return getTarget(element.getInDescription());
    }

    private CdmBase getTarget(DescriptionBase db) {
        return db.describedEntity() != null ? (CdmBase)db.describedEntity() : db;
    }

    private String getReferencingObjectDescription(CdmBase entity, String targetString, Language language) {
        return ReferencingObjectFormatter.format(entity, targetString, language);
    }

    @Override
    public List getHqlResult(String hqlQuery){
        return genericDao.getHqlResult(hqlQuery, new Object[0]);
    }

    @Override
    public List getHqlResult(String hqlQuery, Object[] params){
        return genericDao.getHqlResult(hqlQuery, params);
    }

    @Override
    public <T extends IMergable> void merge(T mergeFirst, T mergeSecond, IMergeStrategy mergeStrategy) throws MergeException {
        if (mergeStrategy == null){
            mergeStrategy = DefaultMergeStrategy.NewInstance(((CdmBase)mergeFirst).getClass());
        }
        genericDao.merge((CdmBase)mergeFirst, (CdmBase)mergeSecond, mergeStrategy);
    }

    @Override
    public <T extends IMergable> void merge(T mergeFirst, T mergeSecond, Class<? extends CdmBase> clazz) throws MergeException {
        IMergeStrategy mergeStrategy;
        if (clazz == null){
            mergeStrategy = DefaultMergeStrategy.NewInstance(((CdmBase)mergeFirst).getClass());
        } else {
            mergeStrategy = DefaultMergeStrategy.NewInstance(clazz);
        }
        merge(mergeFirst, mergeSecond, mergeStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    @Deprecated
    public <T extends IMergable> void merge(int mergeFirstId, int mergeSecondId, Class<? extends CdmBase> clazz) throws MergeException {
        IMergeStrategy mergeStrategy;
        T mergeFirst = (T) genericDao.find(clazz, mergeFirstId);
        T mergeSecond = (T) genericDao.find(clazz, mergeSecondId);
        mergeStrategy = DefaultMergeStrategy.NewInstance(clazz);
        merge(mergeFirst, mergeSecond, mergeStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends IMergable> void merge(UUID mergeFirstUuid, UUID mergeSecondUuid, Class<? extends CdmBase> clazz) throws MergeException {
        IMergeStrategy mergeStrategy;
        T mergeFirst = (T) genericDao.find(clazz, mergeFirstUuid);
        T mergeSecond = (T) genericDao.find(clazz, mergeSecondUuid);
        if (mergeFirst == null){
            throw new MergeException("The merge target is not available anymore.");
        }
        if (mergeSecond == null){
            throw new MergeException("The merge candidate is not available anymore.");
        }
        mergeStrategy = DefaultMergeStrategy.NewInstance(clazz);
        merge(mergeFirst, mergeSecond, mergeStrategy);
    }

    @Override
    public <T extends IMergable> void merge(T mergeFirst, T mergeSecond) throws MergeException {
        IMergeStrategy mergeStrategy = DefaultMergeStrategy.NewInstance(((CdmBase)mergeFirst).getClass());
        merge(mergeFirst, mergeSecond, mergeStrategy);
    }

    @Override
    public <T extends IMatchable> List<T> findMatching(T objectToMatch, IMatchStrategy matchStrategy) throws MatchException {
        if (matchStrategy == null){
            matchStrategy = DefaultMatchStrategy.NewInstance(((objectToMatch).getClass()));
        }
        return genericDao.findMatching(objectToMatch, matchStrategy);
    }

    @Override
    public <T extends IMatchable> List<T> findMatching(T objectToMatch, MatchStrategy strategy) throws MatchException {
        return findMatching(objectToMatch, MatchStrategyConfigurator.getMatchStrategy(strategy));
    }

    @Transactional(readOnly = false)
    @Override
    public void saveAllMetaData(Collection<CdmMetaData> metaData) {
        Iterator<CdmMetaData> iterator = metaData.iterator();
        while(iterator.hasNext()){
            CdmMetaData cdmMetaData = iterator.next();
            genericDao.saveMetaData(cdmMetaData);
        }
    }

    @Override
    public Map<CdmMetaDataPropertyName, CdmMetaData> getCdmMetaData() {
        Map<CdmMetaDataPropertyName, CdmMetaData> result = new HashMap<>();
        List<CdmMetaData> metaDataList = genericDao.getMetaData();
        for (CdmMetaData metaData : metaDataList){
            CdmMetaDataPropertyName propertyName = metaData.getPropertyName();
            result.put(propertyName, metaData);
        }
        return result;
    }

    @Override
    public Object initializeCollection(UUID ownerUuid, String fieldName) {
        return genericDao.initializeCollection(ownerUuid, fieldName);
    }

    @Override
    public Object initializeCollection(UUID ownerUuid, String fieldName, List<String> propertyPaths) {
        return genericDao.initializeCollection(ownerUuid, fieldName, propertyPaths);
    }

    @Override
    public boolean isEmpty(UUID ownerUuid, String fieldName) {
        return genericDao.isEmpty(ownerUuid, fieldName);
    }

    @Override
    public int size(UUID ownerUuid, String fieldName) {
        return genericDao.size(ownerUuid, fieldName);
    }

    @Override
    public Object get(UUID ownerUuid, String fieldName, int index) {
        return genericDao.get(ownerUuid, fieldName, index);
    }

    @Override
    public boolean contains(UUID ownerUuid, String fieldName, Object element) {
        return genericDao.contains(ownerUuid, fieldName, element);
    }

    @Override
    public boolean containsKey(UUID ownerUuid, String fieldName, Object key) {
        return genericDao.containsKey(ownerUuid, fieldName, key);
    }

    @Override
    public boolean containsValue(UUID ownerUuid, String fieldName, Object value) {
        return genericDao.containsValue(ownerUuid, fieldName, value);
    }

    @Override
    @Transactional(readOnly = false)
    public void createFullSampleData() {
        genericDao.createFullSampleData();
    }

    @Override
    public <S extends CdmBase> List<S> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths){
        return genericDao.list(type, limit, start, orderHints, propertyPaths);
    }

    @Override
    public <S extends CdmBase> long count(Class<S> type) {
        return genericDao.count(type);
    }

    @Override
    @Transactional(readOnly = false)
    public CdmBase save(CdmBase newInstance) {
        return genericDao.save(newInstance);
    }

    @Override
    @Transactional(readOnly = false)
    public UUID delete(CdmBase instance) {
        return genericDao.delete(instance);
    }

    @Override
    @Transactional(readOnly = false)
    public UUID saveOrUpdate(CdmBase newInstance) {
        return genericDao.saveOrUpdate(newInstance);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends CdmBase> Map<UUID,T> save(Collection<T> newInstances) {
        //this is very ugly, I know, but for now I do not want to copy the saveAll method from CdmEntityDaoBase to genericDao
        //and generally the saveAll method should work for other CdmBase types with generics removed
        return (Map<UUID, T>) originalSourceDao.saveAll((Collection)newInstances);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends CdmBase> Map<UUID,T> saveOrUpdate(Collection<T> newInstances) {
        //this is very ugly, I know, but for now I do not want to copy the saveAll method from CdmEntityDaoBase to genericDao
        //and generally the saveAll method should work for other CdmBase types with generics removed
        return (Map<UUID, T>) originalSourceDao.saveOrUpdateAll((Collection)newInstances);
    }

    @Override
    public <T extends CdmBase> boolean isMergeable(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException {
        return genericDao.isMergeable(cdmBase1, cdmBase2, mergeStrategy);
    }

    @Override
    public List<UUID> listUuid(Class<? extends CdmBase> clazz) {
        return genericDao.listUuid(clazz);
    }

    @Override
    @Transactional(readOnly = true)
    public UUID refresh(CdmBase persistentObject) {
        return genericDao.refresh(persistentObject);
    }

}
