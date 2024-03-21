/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.etaxonomy.cdm.api.service.INameMatchingService;
import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.NameMatchingResult;
import io.swagger.annotations.Api;

/**
 * @author andreabee90
 * @since 05.03.2024
 */

@RestController
@Api("name_matching")
@RequestMapping(value = {"/namecache/{namecache}" })

public class NameMatchingController {

    @Autowired
    private INameMatchingService nameMatchingservice;


    @RequestMapping(
            value = {"match"},
            method = RequestMethod.GET)
    public NameMatchingResult doGetNameMatching(
            @PathVariable("namecache") String nameCache) throws IOException {
        if (nameCache!= null & !nameCache.isEmpty()) {
            nameCache= nameCache.substring(0,1).toUpperCase() + nameCache.substring(1).toLowerCase();
        }
        NameMatchingResult result = nameMatchingservice.findMatchingNames(nameCache, null, false);
        return result;
    }
}