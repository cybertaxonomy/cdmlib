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
 * metadata for an external file such as images, phylogenetic trees, or audio
 * recordings available through the location attribute!
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:16
 */
@Entity
public class MediaInstance extends VersionableEntity {
	static Logger logger = Logger.getLogger(MediaInstance.class);

	//http://www.iana.org/assignments/media-types/
	@Description("http://www.iana.org/assignments/media-types/")
	private String mimeType;
	//filename suffix defining the media-type as an alternative to the proper MIME type
	@Description("filename suffix defining the media-type as an alternative to the proper MIME type")
	private String suffix;
	//where the media file is stored
	@Description("where the media file is stored")
	private String uri;
	//in bytes
	@Description("in bytes")
	private int size;

	public String getMimeType(){
		return mimeType;
	}

	/**
	 * 
	 * @param mimeType
	 */
	public void setMimeType(String mimeType){
		;
	}

	public String getSuffix(){
		return suffix;
	}

	/**
	 * 
	 * @param suffix
	 */
	public void setSuffix(String suffix){
		;
	}

	public String getUri(){
		return uri;
	}

	/**
	 * 
	 * @param uri
	 */
	public void setUri(String uri){
		;
	}

	public int getSize(){
		return size;
	}

	/**
	 * 
	 * @param size
	 */
	public void setSize(int size){
		;
	}

}