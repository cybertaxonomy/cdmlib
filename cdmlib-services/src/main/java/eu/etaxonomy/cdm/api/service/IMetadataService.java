/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.Map;

import eu.etaxonomy.cdm.config.CdmSourceException;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;

/**
 * @author cmathew
 \* @since 20 Nov 2015
 *
 */
public interface IMetadataService {


    /**
     * Returns the CDM model schema version number
     *
     * @return the CDM model schema version number
     * @throws CdmSourceException , incase of an underlying SQL error
     */
    public String getDbSchemaVersion() throws CdmSourceException;

    /**
     * Returns a boolean flag to indicate whether the database is empty
     *
     * @return boolean flag to indicate whether the database is empty
     * @throws CdmSourceException , incase of an underlying SQL error
     */
    public boolean isDbEmpty() throws CdmSourceException;

    /**
     * Returns metadata corresponding to the underlying data source
     *
     * @return
     * @throws CdmSourceException
     */
    public Map<CdmMetaDataPropertyName, String> getCdmMetadataMap() throws CdmSourceException;

}
