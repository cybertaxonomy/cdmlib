package eu.etaxonomy.cdm.common.mediaMetaData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata.Item;

import org.apache.sanselan.formats.tiff.TiffImageMetadata;

public class TiffImageMetaData extends ImageMetaData {
	private static Logger logger = Logger.getLogger(TiffImageMetaData.class);

	public void readImageMetaData(URI imageURI){
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
		
		if(metadata instanceof TiffImageMetadata){
			TiffImageMetadata jpegMetadata = (TiffImageMetadata) metadata;
			for (Object object : jpegMetadata.getItems()){
				Item item = (Item) object;
			
				logger.debug("File: " + imageFile.getName() + ". "+ item.getKeyword() +"string is: " + item.getText());
				metaData.put(item.getKeyword(), item.getText());
					
			}
		}else{
			logger.error("The mimetype is not tiff");
		}
		
		
	}

	public static TiffImageMetaData newInstance() {
		
		return new TiffImageMetaData();
	}
	
	
}
