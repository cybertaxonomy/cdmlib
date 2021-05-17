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
     * This method only exists due to performance issues for cases when
     * the {@link MediaInfoFileReader} to reduce the overhead imposed by reading
     * the image metadata from the file itself.
     */
    public CdmImageInfo cdmImageInfoWithMetaData(URI imageUri) throws IOException, HttpException;

    public CdmImageInfo cdmImageInfo(URI imageUri) throws IOException, HttpException;

}
