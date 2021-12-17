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
import eu.etaxonomy.cdm.common.media.CdmImageInfo;

/**
 * @author a.kohlbecker
 * @since May 6, 2021
 */
public interface IMediaInfoFactory {

    /**
     * Reads the image metadata by first trying to read it first from a metadata service.
     * If this is not possible it reads it by downloading the file.
     *
     * @param imageUri the image uri
     * @param forceMetaData if true reading metadata additional metadata reading is forced even if not performant
     */
    public CdmImageInfo cdmImageInfo(URI imageUri, boolean forceMetaData) throws IOException, HttpException;

}
