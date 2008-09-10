package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaRepresentationDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaRepresentationPartDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;

@Service
@Transactional
public class MediaServiceImpl<T extends VersionableEntity>
extends ServiceBase<T> implements IMediaService<T> {
	
	private IMediaDao mediaDao;
	@Autowired
	private IMediaRepresentationDao mediaRepresentationDao;
	@Autowired
	private IMediaRepresentationPartDao mediaRepresentationPartDao;
	
	@Autowired
	protected void setDao(IMediaDao dao) {
		this.dao = (ICdmEntityDao)dao;
		this.mediaDao = dao;
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, T> saveMediaAll(Collection<T> mediaCollection){
		return saveCdmObjectAll(mediaCollection);
	    //TODO: Fix saveAll() types
//		return mediaDao.saveAll(mediaCollection);
	}

	public List<Media> getAllMedia(int limit, int start){
		return mediaDao.list(limit, start);
	}

	public List<MediaRepresentation> getAllMediaRepresentations(int limit, int start){
		return mediaRepresentationDao.list(limit, start);
	}

	public List<MediaRepresentationPart> getAllMediaRepresentationParts(int limit, int start){
		return mediaRepresentationPartDao.list(limit, start);
	}
}
