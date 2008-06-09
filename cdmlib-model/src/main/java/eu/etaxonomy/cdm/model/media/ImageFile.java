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
	
	protected ImageFile(){
		super();
	}
	
	protected ImageFile(String uri, Integer size){
		super(uri, size);
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