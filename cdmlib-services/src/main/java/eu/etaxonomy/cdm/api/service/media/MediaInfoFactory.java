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
public class MediaInfoFactory implements IMediaInfoFactory {

    /**
     * TODO needs to be managed in CDM PREFERENCES per service and version.
     *
     * ./MediaInfoService/1.0/ --> MediaUriTransformation
     */
    private List<MediaUriTransformation> mediaInfoService_1_0_Transformations = new ArrayList<>();

    public MediaInfoFactory() {
        mediaInfoService_1_0_Transformations.addAll(DefaultMediaTransformations.bgbmMediaMetadataService());
    }

    /**
     * This method only exists due to performance issues for cases when
     * the {@link MediaInfoFileReader} to reduce the overhead imposed by reading
     * the image metadata from the file itself.
     */
    @Override
    public CdmImageInfo cdmImageInfo(URI imageUri, boolean forceMetaData) throws IOException, HttpException {

        List<URI> metadataServiceURIs = applyURITransformations(imageUri);
        if(!metadataServiceURIs.isEmpty()) {
            // :-) Hooray, we can get the metadata from the web service, this is going to be snappy
            return new MediaInfoServiceReader(imageUri, metadataServiceURIs.get(0))
                    .read()
                    .getCdmImageInfo();
        } else {
            // :-( need to use the files reader
            MediaInfoFileReader mediaReader = new MediaInfoFileReader(imageUri)
                   .readBaseInfo();
            AbstactMediaMetadataReader reader = forceMetaData ? mediaReader.readMetaData() : mediaReader;
            return reader.getCdmImageInfo();
        }
    }

    protected List<URI> applyURITransformations(URI imageUri) {
        MediaUriTransformationProcessor processor = new MediaUriTransformationProcessor();
        processor.addAll(mediaInfoService_1_0_Transformations);
        List<URI> metadataServiceURIs = processor.applyTo(imageUri);
        return metadataServiceURIs;
    }
}