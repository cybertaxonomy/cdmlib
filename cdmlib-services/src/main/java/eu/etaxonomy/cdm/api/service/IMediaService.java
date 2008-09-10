package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;

public interface IMediaService<T extends VersionableEntity> extends IService<T> {
//public interface IAgentService<T extends Agent> extends IIdentifiableEntityService<T> {
//	public abstract Map<UUID, T> saveAgentAll(Collection<T> agentCollection);
//	public abstract List<? extends Agent> getAllAgents(int limit, int start);

	public abstract Map<UUID, T> saveMediaAll(Collection<T> mediaCollection);
	
	public abstract List<Media> getAllMedia(int limit, int start);

	public abstract List<MediaRepresentation> getAllMediaRepresentations(int limit, int start);

	public abstract List<MediaRepresentationPart> getAllMediaRepresentationParts(int limit, int start);
}
