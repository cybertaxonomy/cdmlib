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

	public void  readImageMetaData (URI imageURI){
		InputStream inputStream = null;
		IImageMetadata metadata = null;
		File imageFile = null;
		
		try {
			imageFile = new File(imageURI);
					
			metadata = Sanselan.getMetadata(imageFile);
		} catch (ImageReadException e) {
			logger.error("Error reading image" + " in " + imageFile.getName(), e);
		} catch (IOException e) {
			logger.error("Error reading file"  + " in " + imageFile.getName(), e);
		}
		
		if(metadata instanceof JpegImageMetadata){
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			
			
			int counter = 0;
				for (Object object : jpegMetadata.getItems()){
					Item item = (Item) object;
					System.err.println("File: " + imageFile.getName() + ". "+ item.getKeyword() +"string is: " + item.getText());
					logger.debug("File: " + imageFile.getName() + ". "+ item.getKeyword() +"string is: " + item.getText());
					metaData.put(item.getKeyword(), item.getText());
					
				}
		}else{
			logger.error("The mimetype is not jpeg");
		}
		
		
	}
	
}
