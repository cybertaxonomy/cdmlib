package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@MappedSuperclass
public abstract class IdentifyableMediaEntity extends IdentifiableEntity implements IMediaDocumented{
	static Logger logger = Logger.getLogger(IdentifyableMediaEntity.class);

	private Set<Media> media = new HashSet();
	
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Media> getMedia() {
		return media;
	}
	protected void setMedia(Set<Media> media) {
		this.media = media;
	}
	public void addMedia(Media media) {
		this.media.add(media);
	}
	public void removeMedia(Media media) {
		this.media.remove(media);
	}

}
