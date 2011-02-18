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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.mediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.media.MediaDefaultCacheStrategy;

@Service
@Transactional(readOnly=true, propagation = Propagation.SUPPORTS)
public class MediaServiceImpl extends IdentifiableServiceBase<Media,IMediaDao> implements IMediaService {

	@Autowired
	protected void setDao(IMediaDao dao) {
		this.dao = dao;
	}

	public Pager<MediaKey> getMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countMediaKeys(taxonomicScope, geoScopes);
		
		List<MediaKey> results = new ArrayList<MediaKey>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getMediaKeys(taxonomicScope, geoScopes, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<MediaKey>(pageNumber, numberOfResults, pageSize, results);
	}
	
	public Pager<Rights> getRights(Media t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countRights(t);
		
		List<Rights> results = new ArrayList<Rights>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getRights(t, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<Rights>(pageNumber, numberOfResults, pageSize, results);
	}


	public ImageMetaData getImageInfo(URI imageUri, Integer timeOut) throws IOException, HttpException{
		ImageMetaData imageMetaData = ImageMetaData.newInstance();
		imageMetaData.readImageInfo(imageUri, timeOut);
		imageMetaData.readMetaData(imageUri, timeOut);
		
		return imageMetaData;
	}
	
	public Map<String,String> getImageMetaData(URI imageUri, Integer timeOut) throws IOException, HttpException{
		
		ImageMetaData imageMetaData = ImageMetaData.newInstance();
		imageMetaData.readMetaData(imageUri, timeOut);
		
		
		return imageMetaData.getMetaData();
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IMediaService#getImageSize(java.net.URI, java.lang.Integer)
	 */
	public Integer getImageSize(URI imageUri, Integer timeOut) {
		try {
			URL url = imageUri.toURL();
			HttpURLConnection.setFollowRedirects(false);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			return connection.getContentLength();
			
		} catch (MalformedURLException e) {
			logger.trace("MalformedURLException when trying to get filesize for: " +imageUri);
		} catch (IOException e) {
			logger.trace("IOException when trying to get filesize for: " +imageUri);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache()
	 */
	@Override
	public void updateTitleCache() {
		Class<Media> clazz = Media.class;
		//IIdentifiableEntityCacheStrategy<Media> cacheStrategy = MediaDefaultCacheStrategy();
		super.updateTitleCache(clazz, null, null);
	}
}
