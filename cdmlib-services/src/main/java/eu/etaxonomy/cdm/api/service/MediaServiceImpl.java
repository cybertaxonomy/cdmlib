package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.description.IdentificationKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaRepresentationDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaRepresentationPartDao;

@Service
@Transactional
public class MediaServiceImpl extends AnnotatableServiceBase<Media,IMediaDao> implements IMediaService {
	
	@Autowired
	private IMediaRepresentationDao mediaRepresentationDao;
	
	@Autowired
	private IMediaRepresentationPartDao mediaRepresentationPartDao;	
	
	@Transactional(readOnly = false)
	public Map<UUID, Media> saveMediaAll(Collection<Media> mediaCollection){
		return saveCdmObjectAll(mediaCollection);
	    //TODO: Fix saveAll() types
//		return mediaDao.saveAll(mediaCollection);
	}

	public List<Media> getAllMedia(int limit, int start){
		return dao.list(limit, start);
	}

	public List<MediaRepresentation> getAllMediaRepresentations(int limit, int start){
		return mediaRepresentationDao.list(limit, start);
	}

	public List<MediaRepresentationPart> getAllMediaRepresentationParts(int limit, int start){
		return mediaRepresentationPartDao.list(limit, start);
	}

	@Autowired
	protected void setDao(IMediaDao dao) {
		this.dao = dao;
	}

	public Pager<IdentificationKey> getIdentificationKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes,	Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countIdentificationKeys(taxonomicScope, geoScopes);
		
		List<IdentificationKey> results = new ArrayList<IdentificationKey>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getIdentificationKeys(taxonomicScope, geoScopes, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<IdentificationKey>(pageNumber, numberOfResults, pageSize, results);
	}
}
