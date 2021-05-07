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

import org.apache.http.HttpException;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;

/**
 * Reads media metadata and file information from a
 * <a href="https://github.com/cybertaxonomy/MediaInfoService">MediaInfoService</a>
 *
 * @author a.kohlbecker
 * @since May 7, 2021
 */
public class MediaInfoServiceReader extends AbstactMediaMetadataReader {


    protected MediaInfoServiceReader(URI imageUri, URI metadataUri) {
        super(imageUri, metadataUri);
    }

    @Override
    public MediaMetadataFileReader read() throws IOException, HttpException {
        try {
            UriUtils.getInputStream(metadataUri);

        } catch(IOException e) {
            //FIMXE
        }
        return null;
    }

}
