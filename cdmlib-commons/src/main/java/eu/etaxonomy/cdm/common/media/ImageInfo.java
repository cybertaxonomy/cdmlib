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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata.Item;

import eu.etaxonomy.cdm.common.UriUtils;


/**
 * @author k.luther
 \* @since 27.11.2009
 */
public  class ImageInfo extends MediaInfo {
	private static Logger logger = Logger.getLogger(ImageInfo.class);

	private int width;
	private int height;
	private int bitPerPixel;
	private final URI imageUri;

	private Map<String, String> metaData;

//********************** Factory Methods ******************************/

	public static ImageInfo NewInstance(URI imageUri, Integer timeOut) throws IOException, HttpException {
		ImageInfo instance = new ImageInfo(imageUri);
		instance.readSuffix();
		instance.readImageLength();
		instance.readImageInfo(timeOut);
		return instance;
	}

	public static ImageInfo NewInstanceWithMetaData(URI imageUri, Integer timeOut) throws IOException, HttpException {
		ImageInfo instance = NewInstance(imageUri, timeOut);
		instance.readMetaData(timeOut);
		return instance;
	}

//*********************** CONSTRUCTOR **************************************/

	private ImageInfo(URI imageUri){
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
		long length = UriUtils.getResourceLength(imageUri, null);
		setLength(length);
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
			org.apache.sanselan.ImageInfo imageInfo = Sanselan.getImageInfo(inputStream, null);

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


	public Map<String, String> readMetaData(Integer timeOut) throws IOException, HttpException {

		try {
			InputStream inputStream = UriUtils.getInputStream(imageUri);

			 IImageMetadata mediaData = Sanselan.getMetadata(inputStream, null);

			if (mediaData != null){
				metaData = new HashMap<>();
				for (Object object : mediaData.getItems()){
					Item item = (Item) object;
					if (item.getKeyword().contains("/")){
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
		} catch (ImageReadException e) {
			logger.error("Could not read: " + imageUri + ". " + e.getMessage());
			throw new IOException(e);
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
     * @return
     */
    private String text(Item item) {
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
