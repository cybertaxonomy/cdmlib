package eu.etaxonomy.cdm.common.mediaMetaData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata.Item;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;





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
	
	
	

	public void readImageInfo(URI imageUri){
		
		File image = null;
		try {
			
			image = new File(imageUri);
			
			ImageInfo imageInfo = Sanselan.getImageInfo(image);
			
			readImageInfo(imageInfo);
			
		} catch (IOException e) {
			
			logger.warn("Could not read: "+ image.getName() + "; reason:"+e.getMessage());
		} catch (ImageReadException e) {
			logger.error("Could not open url: " + image.getPath() + ". " + e.getMessage());
		}
				
	}

	@Override
	public void readMetaData(URI mediaUri) {
		readImageInfo(mediaUri);
		try {
			IImageMetadata mediaData = Sanselan.getMetadata(new File(mediaUri));
			for (Object object : mediaData.getItems()){
				Item item = (Item) object;
				System.err.println("File: " + mediaUri.getPath() + ". "+ item.getKeyword() +"string is: " + item.getText());
				logger.debug("File: " + mediaUri.getPath() + ". "+ item.getKeyword() +"string is: " + item.getText());
				metaData.put(item.getKeyword(), item.getText());
				
			}
		} catch (ImageReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static ImageMetaData newInstance() {
		
		return new ImageMetaData();
	}

	
	
	

	
	
	
}
