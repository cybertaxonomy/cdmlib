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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata.Item;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;

/**
 * @author k.luther
 * @date 27.11.2009
 *
 */
public class JpegImageMetaData extends ImageMetaData {
	private static Logger logger = Logger.getLogger(JpegImageMetaData.class);
	
	
	public static JpegImageMetaData newInstance(){
		return new JpegImageMetaData();
	}

	/*public void  readImageMetaData (URI imageURI, Integer){
		
		IImageMetadata mediaData = null;
		File imageFile = null;
		readImageInfo(imageURI);
		try {
			InputStream inputStream;
			URL imageUrl = imageURI.toURL();    
			    
			URLConnection connection = imageUrl.openConnection();
			inputStream = connection.getInputStream();
			mediaData = Sanselan.getMetadata(inputStream, null);
		    
			
		} catch (ImageReadException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		
		if(mediaData instanceof JpegImageMetadata){
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) mediaData;
			
			
			int counter = 0;
				for (Object object : jpegMetadata.getItems()){
					Item item = (Item) object;
					
					//logger.debug("File: " + imageFile.getName() + ". "+ item.getKeyword() +" string is: " + item.getText());
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
		}else{
			logger.error("The mimetype is not jpeg");
		}
		
		
	}*/
	
}
