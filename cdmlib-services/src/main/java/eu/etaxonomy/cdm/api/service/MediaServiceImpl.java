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
import eu.etaxonomy.cdm.model.description.DescriptionBase;
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
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
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
	 private IDescriptionService descriptionService;

	 @Autowired
     private IOccurrenceService specimenService;
	 @Autowired
     private ITaxonService taxonService;
	 @Autowired
     private INameService nameService;

	@Override
    public Pager<MediaKey> getMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countMediaKeys(taxonomicScope, geoScopes);

		List<MediaKey> results = new ArrayList<MediaKey>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getMediaKeys(taxonomicScope, geoScopes, pageSize, pageNumber, propertyPaths);
		}

		return new DefaultPagerImpl<MediaKey>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
    public Pager<Rights> getRights(Media t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countRights(t);

		List<Rights> results = new ArrayList<Rights>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getRights(t, pageSize, pageNumber,propertyPaths);
		}

		return new DefaultPagerImpl<Rights>(pageNumber, numberOfResults, pageSize, results);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
	 */
	@Override
	@Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends Media> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<Media> cacheStrategy, IProgressMonitor monitor) {
		//IIdentifiableEntityCacheStrategy<Media> cacheStrategy = MediaDefaultCacheStrategy();
		if (clazz == null){
			clazz = Media.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IMediaService#delete(java.util.UUID, eu.etaxonomy.cdm.api.service.config.MediaDeletionConfigurator)
     */
    @Override
    @Transactional(readOnly=false)
    public DeleteResult delete(UUID mediaUuid, MediaDeletionConfigurator config) {
        DeleteResult result = new DeleteResult();
        Media media = this.load(mediaUuid);

        result = isDeletable(mediaUuid, config);
        CdmBase updatedObject = null;
        if (result.isOk()){
            Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(media);
            String message = null;
            for (CdmBase ref: references){

               if (ref instanceof TextData){

                    TextData textData = HibernateProxyHelper.deproxy(ref, TextData.class);
                    DescriptionBase description = HibernateProxyHelper.deproxy(textData.getInDescription(), DescriptionBase.class);

                    if (description.isImageGallery()){
                        if (description instanceof TaxonDescription){
                            TaxonDescription desc = HibernateProxyHelper.deproxy(description, TaxonDescription.class);
                            if (desc.getTaxon() == null ){
                                continue;
                            } else if ( (config.isDeleteFromDescription() && config.getDeleteFrom() instanceof Taxon  && config.getDeleteFrom().getId() == desc.getTaxon().getId())|| config.isDeleteFromEveryWhere()){
                                Taxon taxon = desc.getTaxon();
                                updatedObject = taxon;

                                while(textData.getMedia().contains(media)){
                                    textData.removeMedia(media);
                                }
                                if (textData.getMedia().isEmpty()){
                                    desc.removeElement(textData);
                                }
                                if (desc.getElements().isEmpty()){
                                    taxon.removeDescription(desc);
                                }

                            } else {
                                // this should not be happen, because it is not deletable. see isDeletable
                                result.setAbort();


                            }
                        } else if (description instanceof SpecimenDescription){
                            SpecimenDescription desc = HibernateProxyHelper.deproxy(description, SpecimenDescription.class);
                            if (desc.getDescribedSpecimenOrObservation() == null ){
                                continue;
                            } else if ((config.isDeleteFromDescription() && config.getDeleteFrom() instanceof SpecimenOrObservationBase  && config.getDeleteFrom().getId() == desc.getDescribedSpecimenOrObservation().getId())  || config.isDeleteFromEveryWhere()){
                                SpecimenOrObservationBase<?> specimen = desc.getDescribedSpecimenOrObservation();
                                updatedObject = specimen;
                                desc.removeElement(textData);
                                textData.removeMedia(media);
                                if (desc.getElements().isEmpty()){
                                    specimen.removeDescription(desc);
                                }
                            } else {
                                result.setAbort();

                            }
                        }else if (description instanceof TaxonNameDescription){
                            TaxonNameDescription desc = HibernateProxyHelper.deproxy(description, TaxonNameDescription.class);

                            if (desc.getTaxonName() == null ){
                                continue;
                            } else if ((config.isDeleteFromDescription() && config.getDeleteFrom() instanceof TaxonName  && config.getDeleteFrom().getId() == desc.getTaxonName().getId())   || config.isDeleteFromEveryWhere()){
                                TaxonName name= desc.getTaxonName();
                                updatedObject = name;
                                desc.removeElement(textData);
                                textData.removeMedia(media);
                                if (desc.getElements().isEmpty()){
                                    name.removeDescription(desc);
                                }
                            } else {

                                result.setAbort();


                            }
                        }
                    }
                } else if ((ref instanceof MediaSpecimen && config.getDeleteFrom().getId() == ref.getId() && config.getDeleteFrom() instanceof MediaSpecimen)
                        || (ref instanceof MediaSpecimen && config.isDeleteFromEveryWhere())){
                    MediaSpecimen mediaSpecimen = HibernateProxyHelper.deproxy(ref, MediaSpecimen.class);

                        mediaSpecimen.setMediaSpecimen(null);
                        updatedObject = mediaSpecimen;

                }else if (ref instanceof MediaRepresentation){
                    continue;

                }else {

                    result.setAbort();


                }

            }
            if (result.isOk()){
                dao.delete(media);
            } else{
                if (updatedObject instanceof
                         TaxonBase){
                    taxonService.update((TaxonBase)updatedObject);
                    result.addUpdatedObject(updatedObject);
                }
                if (updatedObject instanceof
                        TaxonName){
                   nameService.update((TaxonName)updatedObject);
               }
                if (updatedObject instanceof
                        SpecimenOrObservationBase){
                   specimenService.update((SpecimenOrObservationBase)updatedObject);
               }
            }
        }
        return result;
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
                        message = "The media can't be deleted from the database because it is referenced by another taxon. ("+desc.getTaxon().getTitleCache()+")";
                        result.setAbort();
                    }

                } else if (description instanceof SpecimenDescription){
                    SpecimenDescription desc = HibernateProxyHelper.deproxy(description, SpecimenDescription.class);
                    if (desc.getDescribedSpecimenOrObservation() == null || (mediaConfig.isDeleteFromDescription() && (deleteFrom instanceof SpecimenOrObservationBase && ((SpecimenOrObservationBase)deleteFrom).getId() == desc.getDescribedSpecimenOrObservation().getId()))){
                        continue;
                    } else{
                        message = "The media can't be deleted from the database because it is referenced by another specimen or observation. ("+desc.getDescribedSpecimenOrObservation().getTitleCache()+")";
                        result.setAbort();
                    }
                } else if (description instanceof TaxonNameDescription){
                    TaxonNameDescription desc = HibernateProxyHelper.deproxy(description, TaxonNameDescription.class);
                    if (desc.getTaxonName() == null || (mediaConfig.isDeleteFromDescription() && (deleteFrom instanceof TaxonName && ((TaxonName)deleteFrom).getId() == desc.getTaxonName().getId()))){
                        continue;
                    } else{
                        message = "The media can't be deleted from the database because it is referenced by another specimen or observation. ("+desc.getTaxonName().getTitleCache()+")";
                        result.setAbort();
                    }
                }

            } else {
                message = "The media can't be completely deleted because it is referenced by another " + ref.getUserFriendlyTypeName() ;
            }
            if (message != null){
                result.addException(new ReferencedObjectUndeletableException(message));
                result.addRelatedObject(ref);

            }

        }


        return result;

    }
}
