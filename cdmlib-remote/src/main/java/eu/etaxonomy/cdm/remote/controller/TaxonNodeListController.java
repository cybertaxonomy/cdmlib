// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author n.hoffmann
 * @created Apr 8, 2010
 */
@Controller
@Api("taxonNode")
public class TaxonNodeListController extends AbstractController<TaxonNode, ITaxonNodeService> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonNodeListController.class);


    private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
            "taxon.sec",
            "taxon.name"
    });


    private ITaxonNodeService service;

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.AbstractListController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(ITaxonNodeService service) {
        this.service = service;
    }

    /**
     * @param treeUuid
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(
            value = {"//taxonNode/{taxonNodeUuid}/childNodes"},
            method = RequestMethod.GET)
    public List<TaxonNode> getChildNodes(
            @PathVariable("taxonNodeUuid") UUID taxonNodeUuid,
            HttpServletResponse response
            ) throws IOException {

        TaxonNode taxonNode = service.find(taxonNodeUuid);

        return service.loadChildNodesOfTaxonNode(taxonNode, NODE_INIT_STRATEGY, false, null);
    }


}
