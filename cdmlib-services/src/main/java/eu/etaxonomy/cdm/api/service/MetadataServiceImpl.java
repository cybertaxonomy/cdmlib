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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.config.CdmSourceException;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;

/**
 * Provides metadata information corresponding ti the underlying CDM Datasource.
 *
 * @author cmathew
 \* @since 20 Nov 2015
 *
 */
@Service
@Transactional(readOnly = true)
public class MetadataServiceImpl implements IMetadataService {


    private IDatabaseService databaseService;

    @Autowired
    public void setDatabaseService(IDatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public  String getDbSchemaVersion() throws CdmSourceException  {
        return databaseService.getDbSchemaVersion();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDbEmpty() throws CdmSourceException {
        return databaseService.isDbEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<CdmMetaDataPropertyName, String> getCdmMetadataMap() throws CdmSourceException {
        return databaseService.getCdmMetadataMap();
    }

}
