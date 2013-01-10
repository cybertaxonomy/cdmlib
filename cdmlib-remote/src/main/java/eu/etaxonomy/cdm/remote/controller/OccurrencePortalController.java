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
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@RequestMapping(value = {"/portal/occurrence/{uuid}"})
public class OccurrencePortalController extends BaseController<SpecimenOrObservationBase, IOccurrenceService>
{

    private static final List<String> DEFAULT_INIT_STRATEGY =  Arrays.asList(new String []{
            "$",
            "determinations.*",
            "sources.$",
            "derivedFrom.type",
            "derivedFrom.originals.*",
            "derivedFrom.originals.determinations.taxon",
            "derivedFrom.gatheringEvent.exactLocation.$"
    });


    /**
     *
     */
    public OccurrencePortalController() {
        super();
        setInitializationStrategy(DEFAULT_INIT_STRATEGY);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Autowired
    @Override
    public void setService(IOccurrenceService service) {
        this.service = service;
    }

    @RequestMapping(value = { "derivedFrom" }, method = RequestMethod.GET)
    public ModelAndView doGetDerivedFrom(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetDerivedFrom() " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        List<String> initStrategy = DEFAULT_INIT_STRATEGY;

        SpecimenOrObservationBase sob = getCdmBaseInstance(uuid, response, initStrategy);
        if(sob instanceof DerivedUnitBase){
            DerivationEvent derivationEvent = ((DerivedUnitBase)sob).getDerivedFrom();
            mv.addObject(derivationEvent);
        }
        return mv;
    }



}
