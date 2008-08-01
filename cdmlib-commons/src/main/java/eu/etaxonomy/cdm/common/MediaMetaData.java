
package eu.etaxonomy.cdm.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.devlib.schmidt.imageinfo.ImageInfo;

public class MediaMetaData {
	private static Logger logger = Logger.getLogger(MediaMetaData.class);
	
	public static class ImageMetaData{
		
		public ImageMetaData(){};
		
		int width, height, bitPerPixel;
		String formatName, mimeType;
		
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
	}
	
	public static ImageMetaData readImageMetaData(File imageFile, ImageMetaData imageMetaData){
    	FileInputStream in = null;
        try {
            ImageInfo ii = new ImageInfo();
            in = new FileInputStream(imageFile);
            ii.setInput(in);
            if (!ii.check()) {
                // ImageInfo can not handle TIFF, ...
                // use Java AWT/ImageIO installation of the
//            	
//            	FIXME
//            	the above comment got truncated so i am posting 
//            	what I found out. Tiff-Images can only be read if an 
//            	extension to ImageIO is installed. The Extension comes as 
//            	JAI-IMAGEIO, which is part the JavaAdvancedImage-API and also 
//            	platform dependant. Maybe include the in our own maven repo
//            	https://jai-imageio.dev.java.net/binary-builds.html
//            	linux version comes with jar files which are platform independant 
//            	but you loose native acceleration
//            	
            	
                ii = null;
    			BufferedImage bImage = ImageIO.read(imageFile);
            	if(bImage == null){
            		in.close();
            		throw new IOException(" No ImageReader for image type. Is jai-imageio installed?");
            	}
            	imageMetaData.width = bImage.getWidth();
            	imageMetaData.height = bImage.getHeight();
            	imageMetaData.bitPerPixel = bImage.getColorModel().getPixelSize();
            	imageMetaData.mimeType = "?";
            	imageMetaData.formatName = String.valueOf(bImage.getType());
            	
            } else {
                // ImageInfo can analyze:
                // JPEG, PNG, GIF, BMP, PCX, IFF, RAS, PBM, PGM,
                // PPM
                // and PSD
            	imageMetaData.formatName = ii.getFormatName();
            	
            	
            	imageMetaData.mimeType = ii.getMimeType();
            	imageMetaData.width = ii.getWidth();
            	imageMetaData.height = ii.getHeight();
            	imageMetaData.bitPerPixel = ii.getBitsPerPixel();
            }
            in.close();
            logger.debug("File: " + imageFile.getPath() + " \n"+imageMetaData);
            return imageMetaData;

        } catch (IOException e) {
        	try {if(in != null) in.close();} catch (IOException e1) {/* IGNORE */}
            logger.warn("Could not read "+ imageFile.getPath() + "; reason:"+e.getMessage());
        }
        return null;
	}
}
