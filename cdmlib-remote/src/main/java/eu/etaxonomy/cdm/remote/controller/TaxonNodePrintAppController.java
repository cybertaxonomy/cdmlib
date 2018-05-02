/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * This class once was a controller but is no longer needed as such.
 * The cdmlib-print project however still relies on this class being present to
 * access its methods directly, but is not using it as controller, though
 *
 *
 * @author n.hoffmann
 * @since Apr 8, 2010
 *
 * @deprecated only used by cdmlib-print in an unorthodox way to get cdm entities as xml
 */
@Deprecated
@Component
public class TaxonNodePrintAppController extends AbstractController<TaxonNode, ITaxonNodeService> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonNodePrintAppController.class);


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
     *
     * @deprecated use the TaxonNodeController.doPageChildNodes() instead,
     *  has no request mapping to avoid conflicts with that method
     */
    @Deprecated
    public List<TaxonNode> getChildNodes(
           UUID taxonNodeUuid,
            HttpServletResponse response
            ) throws IOException {

        TaxonNode taxonNode = service.find(taxonNodeUuid);

        return service.loadChildNodesOfTaxonNode(taxonNode, NODE_INIT_STRATEGY, false, null);
    }

    public TaxonNode doGet(UUID taxonNodeUuid, HttpServletRequest request, HttpServletResponse response
            ) throws IOException {
        return service.load(taxonNodeUuid, getInitializationStrategy());
    }

    public TaxonBase doGetTaxon(UUID taxonNodeUuid) throws IOException {
        TaxonNode taxonNode = service.load(taxonNodeUuid, getInitializationStrategy());
        return taxonNode.getTaxon();
    }


}
