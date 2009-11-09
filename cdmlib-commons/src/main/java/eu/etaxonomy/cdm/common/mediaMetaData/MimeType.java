package eu.etaxonomy.cdm.common.mediaMetaData;

import javax.xml.bind.annotation.XmlEnum;


public enum MimeType {
TIFF ("image/tiff"),
JPEG ("image/jpeg"),
IMAGE ("image"),
UNKNOWN ("unknown");
	
private final String mimeType;

MimeType(String mimeType){
	this.mimeType = mimeType;
}
public String getMimeType(){
	return mimeType;
}

}
