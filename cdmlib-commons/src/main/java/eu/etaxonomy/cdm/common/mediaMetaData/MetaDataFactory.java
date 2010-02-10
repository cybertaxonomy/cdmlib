// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.mediaMetaData;

import java.net.URI;


/**
 * @author k.luther
 * @date 27.11.2009
 *
 */
public final class MetaDataFactory {
	
	private static MetaDataFactory instance;
	
	private MetaDataFactory(){
		
	}
	public synchronized static MetaDataFactory getInstance() {
        if (instance == null) {
            instance = new MetaDataFactory();
        }
        return instance;
    }
	public MediaMetaData readMediaData(URI uri, MimeType mimetype, Integer timeOut){
		//MediaMetaData metaData = MediaMetaData.newInstance();
		//MimeType mimeType = metaData.readMediaInfo(uri);
		switch (mimetype){
			case JPEG:
				JpegImageMetaData jpegMediaMetaData = JpegImageMetaData.newInstance();
				jpegMediaMetaData.readMetaData(uri, timeOut);
				((ImageMetaData)jpegMediaMetaData).readImageInfo(uri, timeOut);
				return jpegMediaMetaData;
			case TIFF:
				TiffImageMetaData tiffMetaData = TiffImageMetaData.newInstance();
				tiffMetaData.readMetaData(uri, timeOut);
				((ImageMetaData)tiffMetaData).readImageInfo(uri, timeOut);
				return tiffMetaData;
			case IMAGE:
				ImageMetaData imageMetaData = ImageMetaData.newInstance();
				imageMetaData.readImageInfo(uri, timeOut);
				if (imageMetaData.mimeType.equals(MimeType.JPEG.getMimeType())){
					JpegImageMetaData jpegMediaMetaData2 = JpegImageMetaData.newInstance();
					jpegMediaMetaData2.readMetaData(uri, timeOut);
					return jpegMediaMetaData2;
				}else if (imageMetaData.mimeType.equals(MimeType.TIFF.getMimeType())){
					TiffImageMetaData tiffMetaData2 = TiffImageMetaData.newInstance();
					tiffMetaData2.readMetaData(uri, timeOut);
					((ImageMetaData)tiffMetaData2).readImageInfo(uri, timeOut);
					return tiffMetaData2;
				} else {
					return imageMetaData;
				}				
			default:
				break;
		}
		return null;
		
			
	}
}

