/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;


import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import javax.persistence.*;

/**
 * metadata for an external file such as images, phylogenetic trees, or audio
 * recordings available through the location attribute!
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:34
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class MediaRepresentation extends VersionableEntity {
	private static final Logger logger = Logger.getLogger(MediaRepresentation.class);
	//http://www.iana.org/assignments/media-types
	private String mimeType;
	//the file suffix (e.g. jpg, tif, mov)
	private String suffix;
	private Media media;
	private List<MediaRepresentationPart> mediaRepresentationParts = new ArrayList<MediaRepresentationPart>();
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static MediaRepresentation NewInstance(String mimeType, String suffix){
		MediaRepresentation result  = new MediaRepresentation();
		result.setMimeType(mimeType);
		result.setSuffix(suffix);
		return result;
	}
	
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static MediaRepresentation NewInstance(){
		return new MediaRepresentation();
	}
	
	
	
	protected MediaRepresentation(){
		super();
	}
	
/***************  getter /setter *************************************/
	
	
	public String getMimeType(){
		return this.mimeType;
	}

	/**
	 * 
	 * @param mimeType    mimeType
	 */
	public void setMimeType(String mimeType){
		this.mimeType = mimeType;
	}

	
	public String getSuffix(){
		return this.suffix;
	}

	/**
	 * 
	 * @param mimeType    mimeType
	 */
	public void setSuffix(String suffix){
		this.suffix = suffix;
	}
	
	@ManyToOne	
	public Media getMedia() {
		return media;
	}

	@Deprecated //use only for bidirectional and hibernate
	protected void setMedia(Media media) {
		this.media = media;
	}
	
	
	@OneToMany(mappedBy="mediaRepresentation")
	@org.hibernate.annotations.IndexColumn(name="sortIndex")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public List<MediaRepresentationPart> getParts(){
		return this.mediaRepresentationParts;
	}
	protected void setParts(List<MediaRepresentationPart> mediaRepresentationParts){
		this.mediaRepresentationParts = mediaRepresentationParts;
	}
	public void addRepresentationPart(MediaRepresentationPart mediaRepresentationPart){
		if (mediaRepresentationPart != null){
			this.getParts().add(mediaRepresentationPart);
			mediaRepresentationPart.setMediaRepresentation(this);
		}
	}
	public void removeRepresentationPart(MediaRepresentationPart representationPart){
		this.getParts().remove(representationPart);
		if (representationPart != null){
			representationPart.setMediaRepresentation(null);
		}
	}
	
	
	

}