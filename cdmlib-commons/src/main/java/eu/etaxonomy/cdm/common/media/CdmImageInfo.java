/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.media;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.GenericImageMetadata.GenericImageMetadataItem;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UriUtils;

/**
 * @author k.luther
 * @author a.mueller
 * @since 27.11.2009
 */
public  class CdmImageInfo extends MediaInfo {
	private static Logger logger = Logger.getLogger(CdmImageInfo.class);

	private int width;
	private int height;
	private int bitPerPixel;
	private final URI imageUri;

	private Map<String, String> metaData;

//********************** Factory Methods ******************************/

	public static CdmImageInfo NewInstance(URI imageUri, Integer timeOut) throws IOException, HttpException {
		CdmImageInfo instance = new CdmImageInfo(imageUri);
		instance.readSuffix();
		instance.readImageLength();
		instance.readImageInfo(timeOut);
		return instance;
	}

	public static CdmImageInfo NewInstanceWithMetaData(URI imageUri, Integer timeOut) throws IOException, HttpException {
		CdmImageInfo instance = NewInstance(imageUri, timeOut);

		instance.readMetaData(timeOut);

		return instance;
	}

//*********************** CONSTRUCTOR **************************************/

	private CdmImageInfo(URI imageUri){
		this.imageUri = imageUri;
	}

//*************************** GETTER /SETTER *******************************/

	public URI getUri() {
		return imageUri;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getBitPerPixel() {
		return bitPerPixel;
	}

	public Map<String, String> getMetaData(){
		return metaData;
	}

//**************************** METHODS *****************************/

	private void readSuffix(){
		String path = imageUri.getPath();

		String suffix = path.substring(StringUtils.lastIndexOf(path, '.') + 1);
		setSuffix(suffix);
	}

	private void readImageLength() throws ClientProtocolException, IOException, HttpException{
		try {
            long length = UriUtils.getResourceLength(imageUri, null);
            setLength(length);
        } catch (HttpException e) {
            if (e.getMessage().equals("Could not retrieve Content-Length")){
                InputStream inputStream = UriUtils.getInputStream(imageUri);
                int n = 0;
                while(inputStream.read() != -1){
                    n++;
                }
                inputStream.close();
                logger.info("Content-Length not available in http header. Image size computed via input stream size: " + imageUri);
                setLength(n);
            }else{
                throw e;
            }
        }
	}

	/**
	 * Reads the image infos (width, height, bitPerPixel, metadata, format, mime type)
	 * @param timeOut
	 * @throws IOException
	 * @throws HttpException
	 */
	private void readImageInfo(Integer timeOut) throws IOException, HttpException{

		InputStream inputStream;
		try {
			inputStream = UriUtils.getInputStream(imageUri);
			ImageInfo imageInfo = Imaging.getImageInfo(inputStream, null);

			setFormatName(imageInfo.getFormatName());
			setMimeType(imageInfo.getMimeType());
			width = imageInfo.getWidth();
			height = imageInfo.getHeight();
			bitPerPixel = imageInfo.getBitsPerPixel();
			inputStream.close();

		} catch (ImageReadException e) {
			logger.error("Could not read: " + imageUri + ". " + e.getMessage());
			throw new IOException(e);
		}
	}


	/**
	 * @param timeOut TODO is not yet used
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 */
	public Map<String, String> readMetaData(Integer timeOut) throws IOException, HttpException {

		try {
			InputStream inputStream = UriUtils.getInputStream(imageUri);

			ImageMetadata mediaData = Imaging.getMetadata(inputStream, null);

			if (mediaData != null){
				metaData = new HashMap<>();
				for (ImageMetadataItem imItem : mediaData.getItems()){
					if (imItem instanceof GenericImageMetadataItem){
					    GenericImageMetadataItem item = (GenericImageMetadataItem)imItem;
					    if ("Keywords".equals(item.getKeyword())){
					        String value = text(item);
					        String[] splits = value.split(":");
	                        if (splits.length == 2){
	                            //convention used e.g. for Flora of cyprus (#9137)
	                            metaData.put(splits[0].trim(), splits[1].trim());
	                        }else{
	                            metaData.put(item.getKeyword(), CdmUtils.concat("; ", metaData.get(item.getKeyword()), value));
	                        }
					    }else if (item.getKeyword().contains("/")){
	                        //TODO: not sure where this syntax is used originally
					        String key = item.getKeyword();
					        //key.replace("/", "");
					        int index = key.indexOf("/");
					        key = key.substring(0, index);
					        metaData.put(key, text(item));
					    }else{
					        metaData.put(item.getKeyword(), text(item));
					    }
					}
				}
			}
		}catch (ImageReadException e) {
			logger.error("Could not read: " + imageUri + ". " + e.getMessage());
			//throw new IOException(e);
		}
		return metaData;
	}

    /**
     * Wrapper for the Item.getText() method which applies cleaning of the text representation.
     *
     * <ol>
     * <li>Strings are surrounded by single quotes, these must be removed</li>
     * </ol>
     * @param item
     */
    private String text(GenericImageMetadataItem item) {
        String  text = item.getText();
        if(text.startsWith("'") && text.endsWith("'")) {
            text = text.substring(1 , text.length() - 1);
        }
        return text;
    }

// ******************* TO STRING **********************************/

	@Override
	public String toString(){
        return getFormatName() + " [" + getMimeType()+ "] w:" + width + " h:" + height + " depth:" + bitPerPixel;
	}
}
