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
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.MediaDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(readOnly=true)
public class MediaServiceImpl extends IdentifiableServiceBase<Media,IMediaDao> implements IMediaService {

    @Override
    @Autowired
	protected void setDao(IMediaDao dao) {
		this.dao = dao;
	}

	@Autowired
    private IOccurrenceService specimenService;
	@Autowired
    private ITaxonService taxonService;
	@Autowired
    private INameService nameService;


	@Override
    public Pager<MediaKey> getMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countMediaKeys(taxonomicScope, geoScopes);

		List<MediaKey> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getMediaKeys(taxonomicScope, geoScopes, pageSize, pageNumber, propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
    public Pager<Rights> getRights(Media t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countRights(t);

		List<Rights> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getRights(t, pageSize, pageNumber,propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	@Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends Media> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<Media> cacheStrategy, IProgressMonitor monitor) {
		//IIdentifiableEntityCacheStrategy<Media> cacheStrategy = MediaDefaultCacheStrategy();
		if (clazz == null){
			clazz = Media.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
	}

    @Override
    @Transactional(readOnly=false)
    public DeleteResult delete(UUID mediaUuid, MediaDeletionConfigurator config) {
        DeleteResult result = new DeleteResult();
        Media media = this.load(mediaUuid);

        result = isDeletable(mediaUuid, config);
        if (result.isOk()){
            Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(media);
            for (CdmBase ref: references){

                IDescribable<?> updatedObject = null;
                IService<ICdmBase> service = null;
                if (ref instanceof TextData){

                    TextData textData = HibernateProxyHelper.deproxy(ref, TextData.class);
                    DescriptionBase<?> description = HibernateProxyHelper.deproxy(textData.getInDescription(), DescriptionBase.class);

                    IDescribable<?> objectToUpdate = null;
                    boolean deleteIsMatchingInstance = false;
                    if (description instanceof TaxonDescription){
                        objectToUpdate = ((TaxonDescription)description).getTaxon();
                        deleteIsMatchingInstance = config.getDeleteFrom() instanceof Taxon;
                        service = (IService)taxonService;
                    }else if (description instanceof SpecimenDescription){
                        objectToUpdate = ((SpecimenDescription)description).getDescribedSpecimenOrObservation();
                        deleteIsMatchingInstance = config.getDeleteFrom() instanceof SpecimenOrObservationBase;
                        service = (IService)specimenService;
                    }else if (description instanceof TaxonNameDescription){
                        objectToUpdate = ((TaxonNameDescription)description).getTaxonName();
                        deleteIsMatchingInstance = config.getDeleteFrom() instanceof TaxonName;
                        service = (IService)nameService;
                    }else{
                        throw new RuntimeException("Unsupported DescriptionBase class");
                    }

                    if (objectToUpdate == null ){
                        continue;
                    } else if ( (config.isDeleteFromDescription() && deleteIsMatchingInstance  &&
                                   config.getDeleteFrom().getId() == objectToUpdate.getId())
                                || config.isDeleteFromEveryWhere()){
                        updatedObject = handleDeleteMedia(media, textData, description,
                                (IDescribable)objectToUpdate);
                    } else {
                        // this should not be happen, because it is not deletable. see isDeletable
                        result.setAbort();
                    }

//                } else if (ref instanceof MediaSpecimen && config.getDeleteFrom().getId() == ref.getId() && config.getDeleteFrom() instanceof MediaSpecimen){
//                        MediaSpecimen mediaSpecimen = HibernateProxyHelper.deproxy(ref, MediaSpecimen.class);
//                        mediaSpecimen.setMediaSpecimen(null);
//                        updatedObject = mediaSpecimen;
//                        service = (IService)specimenService;
                }else if (ref instanceof MediaRepresentation){
                    continue;
                }else {
                    result.setAbort();
                }

                if (updatedObject != null){
                    service.update(updatedObject); //service should always be != null if updatedObject != null
                    result.addUpdatedObject((CdmBase)updatedObject);
                }
            }
            if (result.isOk()){
                dao.delete(media);
            }

        }
        return result;
    }

    /**
     * @param media
     * @param textData
     * @param desc
     * @param taxon
     */
    private IDescribable<DescriptionBase<?>> handleDeleteMedia(Media media, TextData textData,
            DescriptionBase<?> desc, IDescribable<DescriptionBase<?>> describable) {
        while(textData.getMedia().contains(media)){
            textData.removeMedia(media);
        }
        //if the textData contains text it should not be deleted
        if (textData.getMedia().isEmpty() && textData.getMultilanguageText().isEmpty()){
            desc.removeElement(textData);
        }
        if (desc.getElements().isEmpty()){
            describable.removeDescription(desc);
        }
        return describable;
    }


    @Override
    public DeleteResult isDeletable(UUID mediaUuid, DeleteConfiguratorBase config){
        DeleteResult result = new DeleteResult();
        Media media = this.load(mediaUuid);
        Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(media);
        MediaDeletionConfigurator mediaConfig = (MediaDeletionConfigurator)config;
        CdmBase deleteFrom = mediaConfig.getDeleteFrom();

        if (mediaConfig.isDeleteFromEveryWhere()){
           return result;
        }
        for (CdmBase ref: references){
            String message = null;
            if (ref instanceof MediaRepresentation){
                continue;
            }
            if (ref instanceof TextData){
                TextData textData = HibernateProxyHelper.deproxy(ref, TextData.class);
                DescriptionBase description = HibernateProxyHelper.deproxy(textData.getInDescription(), DescriptionBase.class);

                if (description instanceof TaxonDescription){
                    TaxonDescription desc = HibernateProxyHelper.deproxy(description, TaxonDescription.class);
                    if (desc.getTaxon() == null || (mediaConfig.isDeleteFromDescription() && (deleteFrom instanceof Taxon && ((Taxon)deleteFrom).getId() == desc.getTaxon().getId()))){
                        continue;
                    } else{
                        message = "The media can't be deleted from the database because it is referenced by a taxon. ("+desc.getTaxon().getTitleCache()+")";
                        result.setAbort();
                    }

                } else if (description instanceof SpecimenDescription){
                    SpecimenDescription desc = HibernateProxyHelper.deproxy(description, SpecimenDescription.class);
                    if (desc.getDescribedSpecimenOrObservation() == null || (mediaConfig.isDeleteFromDescription() && (deleteFrom instanceof SpecimenOrObservationBase && ((SpecimenOrObservationBase)deleteFrom).getId() == desc.getDescribedSpecimenOrObservation().getId()))){
                        continue;
                    } else{
                        message = "The media can't be deleted from the database because it is referenced by a specimen or observation. ("+desc.getDescribedSpecimenOrObservation().getTitleCache()+")";
                        result.setAbort();
                    }
                } else if (description instanceof TaxonNameDescription){
                    TaxonNameDescription desc = HibernateProxyHelper.deproxy(description, TaxonNameDescription.class);
                    if (desc.getTaxonName() == null || (mediaConfig.isDeleteFromDescription() && (deleteFrom instanceof TaxonName && ((TaxonName)deleteFrom).getId() == desc.getTaxonName().getId()))){
                        continue;
                    } else{
                        message = "The media can't be deleted from the database because it is referenced by a scientific name. ("+desc.getTaxonName().getTitleCache()+")";
                        result.setAbort();
                    }
                }

            }if (ref instanceof MediaSpecimen){
               message = "The media can't be deleted from the database because it is referenced by a mediaspecimen. ("+((MediaSpecimen)ref).getTitleCache()+")";
               result.setAbort();
            }else {
                message = "The media can't be completely deleted because it is referenced by another " + ref.getUserFriendlyTypeName() ;
                result.setAbort();
            }
            if (message != null){
                result.addException(new ReferencedObjectUndeletableException(message));
                result.addRelatedObject(ref);

            }

        }

        return result;
    }
}
