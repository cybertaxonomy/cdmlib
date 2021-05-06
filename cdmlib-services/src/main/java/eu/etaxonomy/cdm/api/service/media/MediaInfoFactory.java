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

    /**
     * TODO needs to be managed in CDM PREFERENCES
     *
     * Transformation from
     * https://pictures.bgbm.org/digilib/Scaler?fn=Cyprus/Sisymbrium_aegyptiacum_C1.jpg&mo=file
     *  to
     * https://image.bgbm.org/metadata/info?file=Cyprus/Sisymbrium_aegyptiacum_C1.jpg
     */
    private MediaUriTransformation bgbmMediaMetadataService() {
        MediaUriTransformation mut = new MediaUriTransformation();
        mut.setHost(new SearchReplace("pictures.bgbm.org", "image.bgbm.org"));
        mut.setPathQueryFragment(new SearchReplace("(digilib\\/Scaler\\?fn=)([^&]+)(&mo=file)", "file=$2"));
        return mut;
    }

    public MediaInfoFactory() {
        uriTransformations.add(bgbmMediaMetadataService());
    }

    public CdmImageInfo cdmImageInfoWithMetaData(URI imageUri) throws IOException, HttpException {
        return new MediaMedadataFileReader(imageUri)
               .readSuffix()
               .readImageLength()
               .readImageInfo()
               .readMetaData()
               .getCdmImageInfo();

    }

    public CdmImageInfo cdmImageInfo(URI imageUri) throws IOException, HttpException {
        return new MediaMedadataFileReader(imageUri)
                .readSuffix()
                .readImageLength()
                .readImageInfo()
                .getCdmImageInfo();

    }





}
