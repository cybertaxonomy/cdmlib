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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.CdmTypePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.MatchModePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.NamedAreaPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@RequestMapping(value = {"/occurrence"})
public class OccurrenceListController extends IdentifiableListController<SpecimenOrObservationBase, IOccurrenceService> {


    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private ITermService termService;

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(IOccurrenceService service) {
        this.service = service;
    }

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
    }

    /**
     * @param taxonUuid
     * @param relationshipUuids e.g. CongruentTo;  "60974c98-64ab-4574-bb5c-c110f6db634d"
     * @param relationshipInversUuids
     * @param maxDepth null for unlimited
     * @param pageNumber
     * @param pageSize
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(
            value = {"byAssociatedTaxon"},
            method = RequestMethod.GET)
    public Pager<SpecimenOrObservationBase> doListByAssociatedTaxon(
                @RequestParam(value = "taxonUuid", required = true) UUID taxonUuid,
                @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
                @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
                @RequestParam(value = "maxDepth", required = false) Integer maxDepth,
                @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                @RequestParam(value = "pageSize", required = false) Integer pageSize,
                HttpServletRequest request,
                HttpServletResponse response) throws IOException {

        logger.info("doListByAssociatedTaxon()" + request.getServletPath() + "?" + request.getQueryString());

        Set<TaxonRelationshipEdge> includeRelationships = null;
        if(relationshipUuids != null || relationshipInversUuids != null){
            includeRelationships = new HashSet<TaxonRelationshipEdge>();
            if(relationshipUuids != null) {
                for (UUID uuid : relationshipUuids) {
                    if(relationshipInversUuids != null && relationshipInversUuids.contains(uuid)){
                        includeRelationships.add(new TaxonRelationshipEdge((TaxonRelationshipType) termService.find(uuid), Direction.relatedTo, Direction.relatedFrom));
                        relationshipInversUuids.remove(uuid);
                    } else {
                        includeRelationships.add(new TaxonRelationshipEdge((TaxonRelationshipType) termService.find(uuid), Direction.relatedTo));
                    }
                }
            }
            if(relationshipInversUuids != null) {
                for (UUID uuid : relationshipInversUuids) {
                    includeRelationships.add(new TaxonRelationshipEdge((TaxonRelationshipType) termService.find(uuid), Direction.relatedFrom));
                }
            }
        }

        Taxon associatedTaxon = (Taxon) taxonService.find(taxonUuid);
        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        List<OrderHint> orderHints = null;

        return service.pageByAssociatedTaxon(null, includeRelationships, associatedTaxon,
                maxDepth, pagerParams.getPageSize(), pagerParams.getPageIndex(),
                orderHints, DEFAULT_INIT_STRATEGY);

    }
}