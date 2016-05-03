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
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
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
	 private IDescriptionService descriptionService;

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
    public DeleteResult delete(UUID mediaUuid, MediaDeletionConfigurator config) {
        DeleteResult result = new DeleteResult();
        Media media = this.load(mediaUuid);

        result = isDeletable(media, config);

        if (result.isOk()){
            dao.delete(media);
        }
        return result;
    }

    @Override
    public DeleteResult isDeletable(Media media, DeleteConfiguratorBase config){
        DeleteResult result = new DeleteResult();
        Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(media);
        String message = null;
        for (CdmBase ref: references){
            if (ref instanceof TextData){
                TextData textData = HibernateProxyHelper.deproxy(ref, TextData.class);
                DescriptionBase description = textData.getInDescription();
                if (description.isImageGallery()){
                    if (description instanceof TaxonDescription){
                        TaxonDescription desc = HibernateProxyHelper.deproxy(description, TaxonDescription.class);
                        if (desc.getTaxon() == null){
                            continue;
                        } else{
                            message = "The media can't be deleted because it is referenced by a taxon. ("+desc.getTaxon().getTitleCache()+")";
                        }

                    } else if (description instanceof SpecimenDescription){
                        SpecimenDescription desc = HibernateProxyHelper.deproxy(description, SpecimenDescription.class);
                        if (desc.getDescribedSpecimenOrObservation() == null){
                            continue;
                        } else{
                            message = "The media can't be deleted because it is referenced by a specimen or observation. ("+desc.getDescribedSpecimenOrObservation().getTitleCache()+")";
                        }
                    }
                }
            }
            if (message != null){
                result.addException(new ReferencedObjectUndeletableException(message));
                result.addRelatedObject(ref);
                result.setAbort();
            }

        }


        return result;

    }
}
