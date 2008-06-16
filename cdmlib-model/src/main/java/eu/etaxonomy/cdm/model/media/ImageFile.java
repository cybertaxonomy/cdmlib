/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;


import java.util.Calendar;

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.agent.Agent;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:28
 */
@Entity
public class ImageFile extends MediaRepresentationPart {
	static Logger logger = Logger.getLogger(ImageFile.class);
	//image height in pixel
	private int height;
	//image width in pixel
	private int width;

	public static ImageFile NewInstance(String uri, Integer size){
		return new ImageFile(uri, size);
	}
	
	public static ImageFile NewInstance(String uri, Integer size, Integer height, Integer width){
		return new ImageFile(uri, size, height, width);
	}
	
	/**
	 * Generated an instance of Media that contains one MediaRepresentation consisting of one ImageFile
	 * @param mimeType the MimeType
	 * @param suffix the file suffix (e.g. jpg, png, ...
	 * @param mediaCreated creation date of the media
	 * @param artist artist that created this media 
	 * @param uri the uri of the image file
	 * @param size the size of the image file
	 * @param height the height of the image file
	 * @param width the width of the image file
	 * @return
	 */
	public static Media NewMediaInstance(Calendar mediaCreated, Agent artist, String uri, String mimeType, String suffix, Integer size, Integer height, Integer width){
		Media media = Media.NewInstance();
		media.setMediaCreated(mediaCreated);
		media.setArtist(artist);
		MediaRepresentation mediaRepresentation = MediaRepresentation.NewInstance(mimeType, suffix);
		media.addRepresentation(mediaRepresentation);
		ImageFile image = ImageFile.NewInstance(uri, size, height, size);
		mediaRepresentation.addRepresentationPart(image);
		return media;
	}
	
	
	
	
	protected ImageFile(){
		super();
	}
	
	protected ImageFile(String uri, Integer size){
		super(uri, size);
	}
	
	protected ImageFile(String uri, Integer size, Integer height, Integer width){
		super(uri, size);
		this.setHeight(height);
		this.setWidth(width);
	}
	
	public int getHeight(){
		return this.height;
	}

	/**
	 * 
	 * @param height    height
	 */
	public void setHeight(int height){
		this.height = height;
	}

	public int getWidth(){
		return this.width;
	}

	/**
	 * 
	 * @param width    width
	 */
	public void setWidth(int width){
		this.width = width;
	}

}