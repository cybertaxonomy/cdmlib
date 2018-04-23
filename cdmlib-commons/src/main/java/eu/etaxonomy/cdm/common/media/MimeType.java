/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.media;



/**
 * @author a.kohlbecker
 \* @since 27.11.2009
 *
 */
public enum MimeType {
	TIFF ("image/tiff"),
	JPEG ("image/jpeg"),
	IMAGE ("image"),
	UNKNOWN ("unknown"), 
	PNG ("image/png");
		
	private final String mimeType;
	
	MimeType(String mimeType){
		this.mimeType = mimeType;
	}
	public String getMimeType(){
		return mimeType;
	}
}
