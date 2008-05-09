/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;


import org.apache.log4j.Logger;

import com.sun.activation.registries.MimeTypeFile;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

import java.util.*;

import javax.activation.MimeType;
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
public class MediaInstance extends VersionableEntity {
	private static final Logger logger = Logger.getLogger(MediaInstance.class);
	//http://www.iana.org/assignments/media-types
	private String mimeType;
	//where the media file is stored
	private String uri;
	//in bytes
	private Integer size;
	private Media media;
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static MediaInstance NewInstance(String uri, String mimeType, Integer size){
		MediaInstance result  = new MediaInstance();
		result.setUri(uri);
		result.setMimeType(mimeType);
		result.setSize(size);
		return result;
	}
	
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static MediaInstance NewInstance(){
		return new MediaInstance();
	}
	
	
	
	protected MediaInstance(){
		super();
	}
	
	
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

	public String getUri(){
		return this.uri;
	}

	/**
	 * 
	 * @param uri    uri
	 */
	public void setUri(String uri){
		this.uri = uri;
	}

	
	/**
	 * @return
	 */
	public Integer getSize(){
		return this.size;
	}
	/** 
	 * @param size    size
	 */
	public void setSize(Integer size){
		this.size = size;
	}

	@ManyToOne	
	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

}