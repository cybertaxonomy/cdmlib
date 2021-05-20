/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.media;

import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.common.URI;

/**
 * @author k.luther
 * @author a.mueller
 * @since 27.11.2009
 */
public  class CdmImageInfo extends MediaInfo {

	private int width;
	private int height;
	private int bitPerPixel;
	private final URI imageUri;

	private Map<String, String> metaData = new HashMap<>();


//*********************** CONSTRUCTOR **************************************/

	public CdmImageInfo(URI imageUri){
		this.imageUri = imageUri;
	}

//*************************** GETTER /SETTER *******************************/

	public URI getUri() {
		return imageUri;
	}

	public int getWidth() {
		return width;
	}

    public void setWidth(int width) {
        this.width = width;
    }

	public void setHeight(int height) {
        this.height = height;
    }

    public void setBitPerPixel(int bitPerPixel) {
        this.bitPerPixel = bitPerPixel;
    }

    public int getHeight() {
		return height;
	}

	public int getBitPerPixel() {
		return bitPerPixel;
	}

	public Map<String, String> getMetaData(){
		return metaData;
	}

// ******************* TO STRING **********************************/

	@Override
	public String toString(){
        return getFormatName() + " [" + getMimeType()+ "] w:" + width + " h:" + height + " depth:" + bitPerPixel;
	}
}
