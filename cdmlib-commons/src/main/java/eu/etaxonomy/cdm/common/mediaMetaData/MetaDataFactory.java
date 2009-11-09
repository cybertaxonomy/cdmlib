package eu.etaxonomy.cdm.common.mediaMetaData;

import java.net.URI;



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

