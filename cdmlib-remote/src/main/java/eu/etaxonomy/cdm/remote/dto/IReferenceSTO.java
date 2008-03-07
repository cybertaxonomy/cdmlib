package eu.etaxonomy.cdm.remote.dto;

import java.util.Set;
import java.util.UUID;

public interface IReferenceSTO extends IBaseSTO{

	public abstract String getAuthorship();

	public abstract void setAuthorship(String authorship);

	public abstract String getFullCitation();

	public abstract void setFullCitation(String fullCitation);

	public abstract Set<IdentifiedString> getMediaUri();

	public abstract void addMediaUri(String mediaUri, UUID mediaUUID);

	public abstract void addMediaUri(String mediaUri, String mediaUUID);

}