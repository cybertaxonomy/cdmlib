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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.wordnik.swagger.annotations.Api;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.dto.DerivateDTO;
import eu.etaxonomy.cdm.api.service.dto.PreservedSpecimenDTO;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@Api("portal_occurrence")
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
            "derivedFrom.originals.gatheringEvent.exactLocation.$",
            "derivedFrom.gatheringEvent.exactLocation.$",
            "specimenTypeDesignations.*",
            "specimenTypeDesignations.citation.*",
            "specimenTypeDesignations.homotypicalGroup.*",
            "sequences.$",
            "sequences.annotations",
            "markers.markerType",
            "gatheringEvent.$",
            "descriptions"
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

        SpecimenOrObservationBase sob = getCdmBaseInstance(uuid, response, getInitializationStrategy());
        if(sob instanceof DerivedUnit){
            DerivationEvent derivationEvent = ((DerivedUnit)sob).getDerivedFrom();
            mv.addObject(derivationEvent);
        }
        return mv;
    }

    @RequestMapping(value = { "derivateHierarchy" }, method = RequestMethod.GET)
    public ModelAndView doGetDerivateHierarchy(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "taxonUuid", required = true) UUID taxonUuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetDerivateHierarchy() " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        SpecimenOrObservationBase sob = service.load(uuid);
        if(sob instanceof FieldUnit){
            final DerivateDTO fieldUnitDTO = service.assembleFieldUnitDTO((FieldUnit)sob, taxonUuid);
            if(fieldUnitDTO!=null){
                mv.addObject(fieldUnitDTO);
            }
        }
        return mv;
    }

    @RequestMapping(value = { "specimenDerivates" }, method = RequestMethod.GET)
    public ModelAndView doGetSpecimenDerivates(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetSpecimenDerivates() " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        SpecimenOrObservationBase sob = service.load(uuid);
        if(sob instanceof DerivedUnit){
            PreservedSpecimenDTO specimenDTO = service.assemblePreservedSpecimenDTO((DerivedUnit) sob);
            if(specimenDTO!=null){
                mv.addObject(specimenDTO);
            }
        }
        return mv;
    }



}
