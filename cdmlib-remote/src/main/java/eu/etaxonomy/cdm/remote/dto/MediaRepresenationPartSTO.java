/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

/**
 * @author a.kohlbecker
 * @created 10.06.2008
 * @version 1.0
 */
public class MediaRepresenationPartSTO extends BaseSTO {
	
	/**
	 * in bytes
	 */
	private Integer size;
	
	/**
	 * The files URI.
	 */
	private String uri;

	/**
	 * The width of the media in case it is an image or video
	 */
	private Integer width;
	/**
	 * The height of the media in case it is an image or video
	 */
	private Integer heigth;

	/**
	 * duration of an audio or movie file, this value is of course not valid for images.
	 */
	private Integer duration;
	
	
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
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
