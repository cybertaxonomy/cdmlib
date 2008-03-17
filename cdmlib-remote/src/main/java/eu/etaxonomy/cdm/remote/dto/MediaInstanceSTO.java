/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

public class MediaInstanceSTO extends BaseSTO {
	/**
	 * The files MIME type.
	 */
	private String mimeType;
	
	/**
	 * The files URI.
	 */
	private String uri;

	/**
	 * The width of the media in case it is an image or video
	 */
	private Integer width;
	/**
	 * The heigth of the media in case it is an image or video
	 */
	private Integer heigth;
	
	
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public int getHeigth() {
		return heigth;
	}
	public void setHeigth(Integer heigth) {
		this.heigth = heigth;
	}
}
