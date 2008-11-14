
package eu.etaxonomy.cdm.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;

/**
 * 
 * @author n.hoffmann
 * @created 13.11.2008
 * @version 1.0
 */
public class MediaMetaData {
	private static Logger logger = Logger.getLogger(MediaMetaData.class);
	
	/**
	 * 
	 * 
	 * @author n.hoffmann
	 * @created 13.11.2008
	 * @version 1.0
	 */
	public static class ImageMetaData{
		
		public ImageMetaData(){};
		
		private int width, height, bitPerPixel;
		private String formatName, mimeType;
		
		
		
		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public int getBitPerPixel() {
			return bitPerPixel;
		}

		public void setBitPerPixel(int bitPerPixel) {
			this.bitPerPixel = bitPerPixel;
		}

		public String getFormatName() {
			return formatName;
		}

		public void setFormatName(String formatName) {
			this.formatName = formatName;
		}

		public String getMimeType() {
			return mimeType;
		}

		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}

		@Override
		public String toString(){
            return formatName + " [" + mimeType+ "] w:" + width + " h:" + height + " depth:" + bitPerPixel;
		}
		
		public boolean readFrom(File imageFile){
			return readImageMetaData(imageFile, this) != null;
		}
		
		public boolean readFrom(URL imageUrl){
			return readImageMetaData(imageUrl, this) != null;
		}
	}
	
	private static ImageMetaData readImageInfo(ImageInfo imageInfo, ImageMetaData imageMetaData) {
		
		//ImageFormat imageFormat = imageInfo.getFormat();
		
		imageMetaData.width = imageInfo.getWidth();
		imageMetaData.height = imageInfo.getHeight();
		imageMetaData.mimeType = imageInfo.getMimeType();
		imageMetaData.formatName = imageInfo.getFormatName();
		imageMetaData.bitPerPixel = imageInfo.getBitsPerPixel();
		
		return imageMetaData;
	}
	
	public static ImageMetaData readImageMetaData(File imageFile, ImageMetaData imageMetaData){
		try{
			ImageInfo imageInfo = Sanselan.getImageInfo(imageFile);
			return readImageInfo(imageInfo, imageMetaData);
		} catch (ImageReadException e) {
			logger.error("Could not read image information for image file: " + imageFile + ". " + e.getMessage());
		} catch (IOException e) {
			logger.error("Could not open file: " + imageFile + ". " + e.getMessage());
		}
		
		return null;
	}
	

	public static ImageMetaData readImageMetaData(URL imageUrl, ImageMetaData imageMetaData){
		InputStream inputStream = null;
		try {
			
			URLConnection connection = imageUrl.openConnection();
			
			inputStream = connection.getInputStream();
			
			ImageInfo imageInfo = Sanselan.getImageInfo(inputStream, null);
			
			return readImageInfo(imageInfo, imageMetaData);
			
		} catch (IOException e) {
			try {if(inputStream != null) inputStream.close();} catch (IOException e1) {/* IGNORE */}
			logger.warn("Could not read: "+ imageUrl.getPath() + "; reason:"+e.getMessage());
		} catch (ImageReadException e) {
			logger.error("Could not open url: " + imageUrl + ". " + e.getMessage());
		}
		
		return null;		
	}
}
