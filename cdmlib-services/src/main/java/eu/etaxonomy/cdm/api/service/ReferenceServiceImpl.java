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
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;


@Service
@Transactional(readOnly = true)
public class ReferenceServiceImpl extends IdentifiableServiceBase<Reference,IReferenceDao> implements IReferenceService {

    static Logger logger = Logger.getLogger(ReferenceServiceImpl.class);

    @Autowired
    private ICdmGenericDao genericDao;
    /**
     * Constructor
     */
    public ReferenceServiceImpl(){
        if (logger.isDebugEnabled()) { logger.debug("Load ReferenceService Bean"); }
    }

    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends Reference> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<Reference> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = Reference.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }


    @Override
    protected void setOtherCachesNull(Reference ref) {
        if (! ref.isProtectedAbbrevTitleCache()){
            ref.setAbbrevTitleCache(null, false);
        }
    }


    @Override
    @Autowired
    protected void setDao(IReferenceDao dao) {
        this.dao = dao;
    }

    @Override
    public List<UuidAndTitleCache<Reference>> getUuidAndTitle() {

        return dao.getUuidAndTitle();
    }

    @Override
    public List<Reference> getAllReferencesForPublishing(){
        return dao.getAllNotNomenclaturalReferencesForPublishing();
    }

    @Override
    public List<Reference> getAllNomenclaturalReferences() {

        return dao.getAllNomenclaturalReferences();
    }

    @Override
    public List<TaxonBase> listCoveredTaxa(Reference reference, boolean includeSubordinateReferences, List<String> propertyPaths) {

        List<TaxonBase> taxonList = dao.listCoveredTaxa(reference, includeSubordinateReferences, null, propertyPaths);

        return taxonList;
    }

    @Override
    public DeleteResult delete(Reference reference) {
        //check whether the reference is used somewhere
        DeleteResult result = isDeletable(reference.getUuid(), null);

        if (result.isOk()){
            dao.delete(reference);
        }

        return result;
    }

    @Override
    @Transactional(readOnly=false)
    public DeleteResult delete(UUID referenceUuid) {
        return delete(dao.load(referenceUuid));
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IReferenceService#getUuidAndAbbrevTitleCache(java.lang.Integer, java.lang.String)
     */
    @Override
    public List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCache(Integer limit, String pattern) {
        return dao.getUuidAndAbbrevTitleCache(limit, pattern, null);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IReferenceService#getUuidAndAbbrevTitleCache(java.lang.Integer, java.lang.String)
     */
    @Override
    public List<UuidAndTitleCache<Reference>> getUuidAndAbbrevTitleCache(Integer limit, String pattern, ReferenceType type) {
        ReferenceType inReferenceType = null;
        inReferenceType = getInReferenceType(type);
        return dao.getUuidAndAbbrevTitleCache(limit, pattern, inReferenceType);
    }

    @Override
    public List<UuidAndTitleCache<Reference>> getUuidAndTitleCache(Integer limit, String pattern, ReferenceType type) {
        ReferenceType inReferenceType = null;
        inReferenceType = getInReferenceType(type);
        return dao.getUuidAndTitleCache(limit, pattern, inReferenceType);
    }


    @Transactional(readOnly = true)
    @Override
    public List<IdentifiedEntityDTO<Reference>> listByIdentifierAbbrev(
            String identifier, DefinedTerm identifierType, MatchMode matchmode,
            Integer limit) {

        Integer numberOfResults = dao.countByIdentifier(Reference.class, identifier, identifierType, matchmode);
        List<Object[]> daoResults = new ArrayList<Object[]>();
        if(numberOfResults > 0) { // no point checking again
            daoResults = dao.findByIdentifierAbbrev( identifier, identifierType,
                    matchmode,  limit);
        }

        List<IdentifiedEntityDTO<Reference>> result = new ArrayList<IdentifiedEntityDTO<Reference>>();
        for (Object[] daoObj : daoResults){
            result.add(new IdentifiedEntityDTO<Reference>((DefinedTerm)daoObj[0], (String)daoObj[1], (UUID)daoObj[2], (String)daoObj[3],(String)daoObj[4]));

        }
        return result;
    }


    private ReferenceType getInReferenceType(ReferenceType type){
        ReferenceType inReferenceType = null;
        if (type.equals(ReferenceType.Article)){
            inReferenceType = ReferenceType.Journal;
        } else if (type.equals(ReferenceType.BookSection)){
            inReferenceType = ReferenceType.Book;
        } else if (type.equals(ReferenceType.InProceedings) ){
            inReferenceType = ReferenceType.Proceedings;
        } else if (type.equals(ReferenceType.Book) || type.equals(ReferenceType.Proceedings)){
            inReferenceType = ReferenceType.PrintSeries;
        } else if (type.equals(ReferenceType.Generic)){
            inReferenceType = ReferenceType.Generic;
        }
        return inReferenceType;
    }

}
