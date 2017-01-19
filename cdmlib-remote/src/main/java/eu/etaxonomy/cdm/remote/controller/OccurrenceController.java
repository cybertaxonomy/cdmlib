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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@Api("occurrence")
@RequestMapping(value = {"/occurrence/{uuid}"})
public class OccurrenceController extends AbstractIdentifiableController<SpecimenOrObservationBase, IOccurrenceService>{

    protected static final List<String> DEFAULT_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "sequences.$",
    });

    private static final List<String> DERIVED_UNIT_INIT_STRATEGY =  Arrays.asList(new String []{
            "derivedFrom.derivatives",
            "derivedFrom.originals",
    });

    private static final List<String> EXTENSIONS_INIT_STRATEGY =  Arrays.asList(new String []{
            "extensions.type",
    });



    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Autowired
    @Override
    public void setService(IOccurrenceService service) {
        this.service = service;
    }

    @RequestMapping(value = { "derivedFrom" }, method = RequestMethod.GET)
    public DerivationEvent doGetDerivedFrom(
            @PathVariable("uuid") UUID uuid, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetDerivedFrom()" + requestPathAndQuery(request));

        SpecimenOrObservationBase<?> sob = getCdmBaseInstance(uuid, response, DERIVED_UNIT_INIT_STRATEGY);
        if(sob instanceof DerivedUnit){
            DerivationEvent derivationEvent = ((DerivedUnit)sob).getDerivedFrom();
            if (derivationEvent != null) {
                return derivationEvent;
            }
        }
        return null;
    }

    /**
     *
     * @param uuid
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = { "extensions" }, method = RequestMethod.GET)
    public Object doGetExtensions(
            @PathVariable("uuid") UUID uuid, HttpServletRequest request,
            // doPage request parametes
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            // doList request parametes
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletResponse response) throws IOException {

        logger.info("doGetExtensions()" + requestPathAndQuery(request));
        SpecimenOrObservationBase<?> sob = getCdmBaseInstance(uuid, response, EXTENSIONS_INIT_STRATEGY);

        return pageFromCollection(sob.getExtensions(), pageNumber, pageSize, start, limit, response) ;
    }
}
