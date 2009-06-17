package eu.etaxonomy.cdm.model.media;

import java.util.List;
import java.util.Set;

/**
 * If a class is implementing this interface a set of <code>media</code> may be added
 * to an instance of this class. The class should also implement the protected method:
 * <code>protected void setMedia(Set<Media> media)</code> that maybe used by the persistence
 *  framework (e.g. hibernate) 
 *  
 * @author a.mueller
 * @created 07.07.2008
 * @version 1.0
 */
public interface IMediaEntity extends IMediaDocumented{

	/**
	 * Gets all media belonging to this object
	 * @return
	 */
	
	public abstract Set<Media> getMedia();
	
	/**
	 * Adds a media to this object
	 * @param media
	 */
	public abstract void addMedia(Media media);

	/**
	 * Removes a media from this object
	 * @param media
	 */
	public abstract void removeMedia(Media media);

}