package eu.etaxonomy.cdm.model.media;

import java.util.Set;

import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

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
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public abstract Set<Media> getMedia();

	//also to be implemented by the implementing class
	//protected void setMedia(Set<Media> media); 
	
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