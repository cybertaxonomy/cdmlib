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

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.dto.FieldUnitDTO;
import eu.etaxonomy.cdm.api.service.dto.PreservedSpecimenDTO;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */
@Controller
@Api("portal_occurrence")
@RequestMapping(value = {"/portal/occurrence/{uuid}"})
public class OccurrencePortalController extends OccurrenceController
{

    private static Logger logger = Logger.getLogger(OccurrencePortalController.class);

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
            "gatheringEvent.exactLocation.referenceSystem", // TODO implement auto initializer?
            "gatheringEvent.collectingAreas",
            "descriptions"
    });

    @Autowired
    private ITermService termService;



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

    @RequestMapping(value = { "derivateHierarchy" }, method = RequestMethod.GET)
    public FieldUnitDTO doGetDerivateHierarchy(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetDerivateHierarchy() " + requestPathAndQuery(request));

        ModelAndView mv = new ModelAndView();

        SpecimenOrObservationBase sob = service.load(uuid);
        if(sob instanceof FieldUnit){
            FieldUnit fieldUnit = (FieldUnit)sob;
            if(fieldUnit.isPublish()){
                return service.assembleFieldUnitDTO(fieldUnit);

            }
        }
        return null;
    }

    @RequestMapping(value = { "specimenDerivates" }, method = RequestMethod.GET)
    public PreservedSpecimenDTO doGetSpecimenDerivates(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetSpecimenDerivates() " + requestPathAndQuery(request));


        SpecimenOrObservationBase sob = service.load(uuid);
        if(sob instanceof DerivedUnit){
            DerivedUnit derivedUnit = (DerivedUnit) sob;
            if(derivedUnit.isPublish()){
                return service.assemblePreservedSpecimenDTO(derivedUnit);
            }
        }
        return null;
    }

//    @RequestMapping(value = { "specimenDerivatesAndOriginals" }, method = RequestMethod.GET)
//    public ModelAndView doGetSpecimenDerivatesAndOriginals(
//            @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
//            @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
//            @PathVariable("uuid") UUID uuid,
//            HttpServletRequest request,
//            HttpServletResponse response) throws IOException {
//
//        logger.info("doGetSpecimenDerivates() " + request.getRequestURI());
//
//        ModelAndView mv = new ModelAndView();
//        Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(relationshipUuids, relationshipInversUuids, termService);
//        List<FieldUnitDTO> fieldUnitDTOs = service.findFieldUnitDTOByAssociatedTaxon(includeRelationships, uuid);
//        if(fieldUnitDTOs!=null){
//                    mv.addObject(fieldUnitDTOs);
//        }
//
//        return mv;
//    }

}
