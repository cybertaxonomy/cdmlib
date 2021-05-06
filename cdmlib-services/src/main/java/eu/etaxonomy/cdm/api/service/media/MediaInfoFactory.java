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

    public CdmImageInfo cdmImageInfoWithMetaData(URI imageUri) throws IOException, HttpException {
        return new MediaMedadataFileReader(imageUri)
               .readBaseInfo()
               .readMetaData()
               .getCdmImageInfo();

    }

    public CdmImageInfo cdmImageInfo(URI imageUri) throws IOException, HttpException {
        return new MediaMedadataFileReader(imageUri)
               .readBaseInfo()
               .getCdmImageInfo();
    }





}
