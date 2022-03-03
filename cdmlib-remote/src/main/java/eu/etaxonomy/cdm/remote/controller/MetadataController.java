/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.etaxonomy.cdm.api.service.IMetadataService;
import eu.etaxonomy.cdm.config.CdmSourceException;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @date May 19, 2020
 */
@RestController
@Api("metadata")
@RequestMapping(value = {"/metadata"})
public class MetadataController {

    @Autowired
    private IMetadataService service;


    @RequestMapping(method = RequestMethod.GET)
    public Map<CdmMetaDataPropertyName, String> getMetadata() throws CdmSourceException {
        return service.getCdmMetadataMap();
    }

}
