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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata.Item;


/**
 * @author k.luther
 * @date 27.11.2009
 *
 */
public  class ImageMetaData extends MediaMetaData {
	private static Logger logger = Logger.getLogger(ImageMetaData.class);
		protected ImageMetaData(){
			this.metaData = new HashMap<String, String>();
		}
		
		protected int width, height, bitPerPixel;
		
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
		
		
		
	
	
	private  void readImageInfo(ImageInfo imageInfo) {
		this.formatName = imageInfo.getFormatName();
		this.mimeType = imageInfo.getMimeType();
		this.width = imageInfo.getWidth();
		this.height = imageInfo.getHeight();
		this.bitPerPixel = imageInfo.getBitsPerPixel();
		
	}
	
	
	

	public void readImageInfo(URI imageUri, Integer timeOut) throws IOException{
		
		File image = null;
		InputStream inputStream;
		try {
			
		
			URL imageUrl = imageUri.toURL();    
		    
			URLConnection connection = imageUrl.openConnection();
			connection.setConnectTimeout(timeOut);
			
			inputStream = connection.getInputStream();

			ImageInfo imageInfo = Sanselan.getImageInfo(inputStream, null);
			
			
			readImageInfo(imageInfo);
		    
		} catch (IOException e) {
			logger.warn("Could not read: "+ imageUri.toString() + "; reason:"+e.getMessage());
			throw e;
		} catch (ImageReadException e) {
			logger.error("Could not open url: " + imageUri + ". " + e.getMessage());
		}
				
	}

	
	public void readMetaData(URI mediaUri, Integer timeOut) throws IOException {
		readImageInfo(mediaUri, timeOut);
		try {
			InputStream inputStream;
			URL imageUrl = mediaUri.toURL();    
			    
			URLConnection connection = imageUrl.openConnection();
			connection.setConnectTimeout(timeOut);
			inputStream = connection.getInputStream();
			
			IImageMetadata mediaData = Sanselan.getMetadata(inputStream, null);
			
			if (mediaData != null){
				for (Object object : mediaData.getItems()){
					Item item = (Item) object;
					if (item.getKeyword().contains("/")){
						String key = item.getKeyword();
						//key.replace("/", "");
						int index = key.indexOf("/");
						key = key.substring(0, index);
						metaData.put(key, item.getText());
						
					}else{
						metaData.put(item.getKeyword(), item.getText());
						
					}
					
				}
			}
		} catch (ImageReadException e) {
			logger.warn(e.getLocalizedMessage());
		} catch (IOException e) {
			logger.warn("The image server is not available!");
			throw e;
		}
		
	}

	public static ImageMetaData newInstance() {
		
		return new ImageMetaData();
	}

}
