/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.media;

import java.io.IOException;
import java.io.InputStream;

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
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.media.CdmImageInfo;

/**
 *  TODO make use of timeOut ?
 *
 * @author a.kohlbecker
 * @since May 6, 2021
 */
public class MediaMedadataFileReader {


    private static Logger logger = Logger.getLogger(MediaMedadataFileReader.class);

    private CdmImageInfo cdmImageInfo;

    public static final Integer IMAGE_READ_TIMEOUT = 3000; // ms

    private Integer timeOut = IMAGE_READ_TIMEOUT; // setting default

    public MediaMedadataFileReader(CdmImageInfo cdmImageInfo) {
        this.cdmImageInfo = cdmImageInfo;
    }

    public MediaMedadataFileReader(URI uri) {
        this.cdmImageInfo = new CdmImageInfo(uri);
    }

    /**
     * Reads the image info (width, height, bitPerPixel, metadata, format, mime type)
     */
    public MediaMedadataFileReader readImageInfo() throws IOException, HttpException{

        InputStream inputStream;
        try {
            inputStream = UriUtils.getInputStream(cdmImageInfo.getUri());
            ImageInfo imageInfo = Imaging.getImageInfo(inputStream, null);

            cdmImageInfo.setFormatName(imageInfo.getFormatName());
            cdmImageInfo.setMimeType(imageInfo.getMimeType());
            cdmImageInfo.setWidth(imageInfo.getWidth());
            cdmImageInfo.setHeight(imageInfo.getHeight());
            cdmImageInfo.setBitPerPixel(imageInfo.getBitsPerPixel());
            inputStream.close();

        } catch (ImageReadException e) {
            logger.error("Could not read: " + cdmImageInfo.getUri() + ". " + e.getMessage());
            throw new IOException(e);
        }

        return this;
    }

    public MediaMedadataFileReader readMetaData() throws IOException, HttpException {

        ImageMetadata mediaData = null;
        try {
            InputStream inputStream = UriUtils.getInputStream(cdmImageInfo.getUri());
            mediaData = Imaging.getMetadata(inputStream, null);
        }catch (ImageReadException e) {
            logger.error("Could not read: " + cdmImageInfo.getUri() + ". " + e.getMessage());
            //throw new IOException(e);
        }

        if(mediaData != null) {
            for (ImageMetadataItem imItem : mediaData.getItems()){
                if (imItem instanceof GenericImageMetadataItem){
                    GenericImageMetadataItem item = (GenericImageMetadataItem)imItem;
                    if ("Keywords".equals(item.getKeyword())){
                        String value = text(item);
                        String[] splits = value.split(":");
                        if (splits.length == 2){
                            //convention used e.g. for Flora of cyprus (#9137)
                            cdmImageInfo.getMetaData().put(splits[0].trim(), splits[1].trim());
                        }else{
                            cdmImageInfo.getMetaData().put(
                                    item.getKeyword(),
                                    CdmUtils.concat("; ", cdmImageInfo.getMetaData().get(item.getKeyword()), value)
                                    );
                        }
                    }else if (item.getKeyword().contains("/")){
                        //TODO: not sure where this syntax is used originally
                        String key = item.getKeyword();
                        //key.replace("/", "");
                        int index = key.indexOf("/");
                        key = key.substring(0, index);
                        cdmImageInfo.getMetaData().put(key, text(item));
                    }else{
                        cdmImageInfo.getMetaData().put(item.getKeyword(), text(item));
                    }
                }
            }
        }

        return this;
    }

    /**
     * Reads the size of the image defined by the {@link #imageUri} in bytes
     */
    public MediaMedadataFileReader readImageLength() throws ClientProtocolException, IOException, HttpException{
        try {
            long length = UriUtils.getResourceLength(cdmImageInfo.getUri(), null);
            cdmImageInfo.setLength(length);
        } catch (HttpException e) {
            if (e.getMessage().equals("Could not retrieve Content-Length")){
                InputStream inputStream = UriUtils.getInputStream(cdmImageInfo.getUri());
                int n = 0;
                while(inputStream.read() != -1){
                    n++;
                }
                inputStream.close();
                logger.info("Content-Length not available in http header. Image size computed via input stream size: " + cdmImageInfo.getUri());
                cdmImageInfo.setLength(n);
            }else{
                throw e;
            }
        }
        return this;
    }

    public MediaMedadataFileReader readSuffix(){
        String path = cdmImageInfo.getUri().getPath();
        String suffix = path.substring(StringUtils.lastIndexOf(path, '.') + 1);
        cdmImageInfo.setSuffix(suffix);
        return this;
    }

    public CdmImageInfo getCdmImageInfo() {
        return cdmImageInfo;
    }

    /**
     * Wrapper for the Item.getText() method which applies cleaning of the text representation.
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



}
