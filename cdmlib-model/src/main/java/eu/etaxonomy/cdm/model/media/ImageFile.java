/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;


import java.net.URI;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.model.agent.AgentBase;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:28
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ImageFile", propOrder = {
    "height",
    "width"
})
@XmlRootElement(name = "ImageFile")
@Entity
@Audited
public class ImageFile extends MediaRepresentationPart {
	private static final long serialVersionUID = 5451418445009559953L;
	private static final Logger logger = Logger.getLogger(ImageFile.class);

	//image height in pixel
	@XmlElement(name = "Height")
	private int height;

	//image width in pixel
	@XmlElement(name = "Width")
	private int width;

	public static ImageFile NewInstance(URI uri, Integer size){
		logger.debug("NewInstance");
		return new ImageFile(uri, size);
	}

	public static ImageFile NewInstance(URI uri, Integer size, Integer height, Integer width){
		return new ImageFile(uri, size, height, width);
	}

	public static ImageFile NewInstance(URI uri, Integer size, ImageInfo imageInfo){
		ImageFile imageFile = NewInstance(uri, size);

		if(imageInfo != null){
			imageFile.setHeight(imageInfo.getHeight());
			imageFile.setWidth(imageInfo.getWidth());
		}

		return imageFile;
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
	public static Media NewMediaInstance(DateTime mediaCreated, AgentBase artist, URI uri, String mimeType, String suffix, Integer size, Integer height, Integer width){
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

	protected ImageFile(URI uri, Integer size){
		super(uri, size);
	}

	protected ImageFile(URI uri, Integer size, Integer height, Integer width){
		super(uri, size);
		if (height != null){
			this.setHeight(height);
		}
		if (width != null){
			this.setWidth(width);
		}
	}

	public Integer getHeight(){
		return this.height;
	}

	/**
	 *
	 * @param height    height
	 */
	public void setHeight(Integer height){
		this.height = height;
	}

	public Integer getWidth(){
		return this.width;
	}

	/**
	 *
	 * @param width    width
	 */
	public void setWidth(Integer width){
		this.width = width;
	}

}