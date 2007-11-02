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

/**
 * metadata for an external file such as images, phylogenetic trees, or audio
 * recordings available through the location attribute!
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:28
 */
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
	 * @param newVal
	 */
	public void setMimeType(String newVal){
		mimeType = newVal;
	}

	public String getSuffix(){
		return suffix;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSuffix(String newVal){
		suffix = newVal;
	}

	public String getUri(){
		return uri;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUri(String newVal){
		uri = newVal;
	}

	public int getSize(){
		return size;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSize(int newVal){
		size = newVal;
	}

}