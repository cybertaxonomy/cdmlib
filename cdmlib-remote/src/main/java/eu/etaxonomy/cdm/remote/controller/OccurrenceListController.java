// $Id$
/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@RequestMapping(value = {"/occurrence"})
public class OccurrenceListController extends IdentifiableListController<SpecimenOrObservationBase, IOccurrenceService> {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(IOccurrenceService service) {
        this.service = service;
    }

    @Autowired
    private ITaxonService taxonService;

     //listByAnyAssociation
    @RequestMapping(
            value = {"byAssociatedTaxon"},
            method = RequestMethod.GET)
    public List<SpecimenOrObservationBase> doListByAssociatedTaxon(
                @RequestParam(value = "taxonUuid", required = true) UUID taxonUuid,
                @RequestParam(value = "maxDepth", required = false) Integer maxDepth,
                @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                @RequestParam(value = "pageSize", required = false) Integer pageSize,
                HttpServletRequest request,
                HttpServletResponse response) throws IOException {

        Set<TaxonRelationshipEdge> includeRelationships = null;
        Taxon associatedTaxon = (Taxon) taxonService.find(taxonUuid);
        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        List<OrderHint> orderHints = null;
        return service.listByAssociatedTaxon(null, includeRelationships, associatedTaxon, maxDepth, pagerParams.getPageSize(), pagerParams.getPageIndex(), orderHints, DEFAULT_INIT_STRATEGY);

    }
}