/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaRepresentationSTO extends BaseSTO {
	/**
	 * The files MIME type.
	 */
	private String mimeType;
	
	/**
	 * the file suffix (e.g. jpg, tif, mov)
	 */
	private String suffix;
		
	private List<MediaRepresenationPartSTO> representationParts = new ArrayList<MediaRepresenationPartSTO>();;
	
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public void addRepresenationPart(String uuid, String uri, Integer heigth, Integer width, Integer duration){
		MediaRepresenationPartSTO mi = new MediaRepresenationPartSTO();
		mi.setUuid(uuid);
		mi.setUri(uri);
		mi.setHeigth(heigth);
		mi.setWidth(width);
		mi.setDuration(duration);
		this.representationParts.add(mi);
	}
	
	public void addRepresenationPart(MediaRepresenationPartSTO part){
		this.representationParts.add(part);
	}
	public List<MediaRepresenationPartSTO> getRepresentationParts() {
		return representationParts;
	}
	public void setRepresentationParts(
			List<MediaRepresenationPartSTO> representationParts) {
		this.representationParts = representationParts;
	}
	
	
	
	
}
