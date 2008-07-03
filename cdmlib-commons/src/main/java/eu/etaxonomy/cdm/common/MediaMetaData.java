
package eu.etaxonomy.cdm.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
                ii = null;
            	BufferedImage bim = ImageIO.read(in);
            	if(bim == null)
            		throw new IOException("Cannot read "+imageFile);
            	imageMetaData.width = bim.getWidth();
            	imageMetaData.height = bim.getHeight();
            	imageMetaData.bitPerPixel = bim.getColorModel().getPixelSize();
            	imageMetaData.mimeType = "?";
            	imageMetaData.formatName = String.valueOf(bim.getType());
            	
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
            logger.error("Could not read "+ imageFile.getPath() + "; reason:"+e.getMessage());
        }
        return null;
	}
}
