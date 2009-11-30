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
	public MediaMetaData readMediaData(URI uri, MimeType mimetype){
		//MediaMetaData metaData = MediaMetaData.newInstance();
		//MimeType mimeType = metaData.readMediaInfo(uri);
		switch (mimetype){
			case JPEG:
				JpegImageMetaData jpegMediaMetaData = JpegImageMetaData.newInstance();
				jpegMediaMetaData.readImageMetaData(uri);
				((ImageMetaData)jpegMediaMetaData).readImageInfo(uri);
				return jpegMediaMetaData;
			case TIFF:
				TiffImageMetaData tiffMetaData = TiffImageMetaData.newInstance();
				tiffMetaData.readImageMetaData(uri);
				((ImageMetaData)tiffMetaData).readImageInfo(uri);
				return tiffMetaData;
			case IMAGE:
				ImageMetaData imageMetaData = ImageMetaData.newInstance();
				imageMetaData.readImageInfo(uri);
				if (imageMetaData.mimeType.equals(MimeType.JPEG.getMimeType())){
					JpegImageMetaData jpegMediaMetaData2 = JpegImageMetaData.newInstance();
					jpegMediaMetaData2.readImageMetaData(uri);
					return jpegMediaMetaData2;
				}else if (imageMetaData.mimeType.equals(MimeType.TIFF.getMimeType())){
					TiffImageMetaData tiffMetaData2 = TiffImageMetaData.newInstance();
					tiffMetaData2.readImageMetaData(uri);
					((ImageMetaData)tiffMetaData2).readImageInfo(uri);
					return tiffMetaData2;
				}
			default:
				break;
		}
		return null;
		
			
	}
}

