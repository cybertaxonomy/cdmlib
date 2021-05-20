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

import eu.etaxonomy.cdm.common.UriUtils;

/**
 * TODO make use of timeOut ?
 *
 * Most code  was extracted from CdmImageInfo.
 *
 */
public class MediaInfoFileReader extends AbstactMediaMetadataReader {

    private static Logger logger = Logger.getLogger(MediaInfoFileReader.class);

    public static final Integer IMAGE_READ_TIMEOUT = 3000; // ms

    private Integer timeOut = IMAGE_READ_TIMEOUT; // setting default

    /**
     * The <code>MediaMetadataFileReader</code> should not be used directly this method
     * only exists for not to break legacy code.
     * <p>
     * Instead the {@link IMediaInfoFactory} should always be used for to allow
     * all application parts to benefit from the potential speed up through the
     * MediaMetadataService or other fast source of metadata.
     */
    @Deprecated
    public static MediaInfoFileReader legacyFactoryMethod(eu.etaxonomy.cdm.common.URI uri) {
        return new MediaInfoFileReader(uri);
    }

    protected MediaInfoFileReader(eu.etaxonomy.cdm.common.URI uri) {
        super(uri, uri);
    }

    @Override
    public AbstactMediaMetadataReader read() throws IOException, HttpException {
        return readBaseInfo().readMetaData();
    }

    /**
     * Combines the calls to:
     *
     * <ul>
     * <li>{@link #readImageInfo()}</li>
     * <li>{@link #readImageLength()}</li>
     * <li>{@link #readSuffix()}</li>
     * </ul>
     * to read the commonly needed base information.
     *
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public MediaInfoFileReader readBaseInfo() throws IOException, HttpException{
        readImageInfo();
        readImageLength();
        readSuffix();
        return this;
    }

    /**
     * Reads the image info (width, height, bitPerPixel, metadata, format, mime type)
     */
    public AbstactMediaMetadataReader readImageInfo() throws IOException, HttpException{

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

    public AbstactMediaMetadataReader readMetaData() throws IOException, HttpException {

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
                    processPutMetadataEntry(item.getKeyword(), item.getText());
                }
            }
        }

        return this;
    }

    /**
     * Reads the size of the image defined by the {@link #imageUri} in bytes
     */
    public AbstactMediaMetadataReader readImageLength() throws ClientProtocolException, IOException, HttpException{
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

    public AbstactMediaMetadataReader readSuffix(){
        String path = cdmImageInfo.getUri().getPath();
        String suffix = path.substring(StringUtils.lastIndexOf(path, '.') + 1);
        cdmImageInfo.setSuffix(suffix);
        return this;
    }
}