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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.media.CdmImageInfo;

/**
 * @author a.kohlbecker
 * @since May 5, 2021
 */
@Component
public class MediaInfoFactory {

    /**
     * TODO needs to be managed in CDM PREFERENCES
     */
    private List<MediaUriTransformation> uriTransformations = new ArrayList<>();


    public MediaInfoFactory() {
        uriTransformations.add(DefaultMediaTransformations.bgbmMediaMetadataService());
    }

    /**
     * This method only exists due to performance issues for cases when
     * the {@link MediaMetadataFileReader} to reduce the overhead imposed by reading
     * the image metadata from the file itself.
     *
     *
     * @param imageUri
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public CdmImageInfo cdmImageInfoWithMetaData(eu.etaxonomy.cdm.common.URI imageUri) throws IOException, HttpException {
        return cdmImageInfoWithMetaData(imageUri.getJavaUri());
    }

    /**
     * This method only exists due to performance issues for cases when
     * the {@link MediaMetadataFileReader} to reduce the overhead imposed by reading
     * the image metadata from the file itself.
     *
     *
     * @param imageUri
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public CdmImageInfo cdmImageInfoWithMetaData(URI imageUri) throws IOException, HttpException {

        // :-) Hooray, we can get the metadata from the web service, this is going to be snappy
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.addAll(uriTransformations);
        processor.applyTo(imageUri);

        // :-( need to use the files reader
        return new MediaMetadataFileReader(imageUri)
               .readBaseInfo()
               .readMetaData()
               .getCdmImageInfo();

    }

    public CdmImageInfo cdmImageInfo(eu.etaxonomy.cdm.common.URI imageUri) throws IOException, HttpException {
        return cdmImageInfo(imageUri.getJavaUri());
    }

    public CdmImageInfo cdmImageInfo(URI imageUri) throws IOException, HttpException {

        // :-) Hooray, we can get the metadata from the web service, this is going to be snappy

        // :-( need to use the files reader
        return new MediaMetadataFileReader(imageUri)
               .readBaseInfo()
               .getCdmImageInfo();
    }





}
