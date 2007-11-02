/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:10
 */
@Entity
public class ImageFile extends MediaInstance {
	static Logger logger = Logger.getLogger(ImageFile.class);

	//image height in pixel
	@Description("image height in pixel")
	private int height;
	//image width in pixel
	@Description("image width in pixel")
	private int width;

	public int getHeight(){
		return height;
	}

	/**
	 * 
	 * @param height
	 */
	public void setHeight(int height){
		;
	}

	public int getWidth(){
		return width;
	}

	/**
	 * 
	 * @param width
	 */
	public void setWidth(int width){
		;
	}

}