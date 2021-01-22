/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

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

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.dto.SpecimenOrObservationBaseDTO;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
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
            "derivationEvents.derivatives.$",
            "specimenTypeDesignations.*",
            "specimenTypeDesignations.citation.*",
            "specimenTypeDesignations.homotypicalGroup.*",
            "specimenTypeDesignations.typifiedNames",
            "sequences.$",
            "sequences.annotations",
            "markers.markerType",
            "gatheringEvent.$",
            "gatheringEvent.exactLocation.referenceSystem", // TODO implement auto initializer?
            "gatheringEvent.collectingAreas",
            "annotations",
            "descriptions",
            "collection.institute.$"
    });

    /**
     *
     */
    public OccurrencePortalController() {
        super();
        setInitializationStrategy(DEFAULT_INIT_STRATEGY);
    }

    @Autowired
    @Override
    public void setService(IOccurrenceService service) {
        this.service = service;
    }

    @RequestMapping(value = { "asDTO" }, method = RequestMethod.GET)
    public SpecimenOrObservationBaseDTO doGetAsDTO(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) {

        logger.info("doGetAsDTO() " + requestPathAndQuery(request));

        SpecimenOrObservationBase<?> sob = service.load(uuid);
        if(sob.isPublish()) {
            if(sob instanceof FieldUnit){
                return service.assembleFieldUnitDTO((FieldUnit)sob);
            } else {
                return service.assembleDerivedUnitDTO((DerivedUnit)sob);
            }
        }
        // FIXME proper http code
        return null;
    }

    @RequestMapping(value = { "mediaSpecimen" }, method = RequestMethod.GET)
    public Media doGetMediaSpecimen(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response) {

        logger.info("doGetMediaSpecimen() " + requestPathAndQuery(request));


        SpecimenOrObservationBase<?> sob = service.load(uuid, Arrays.asList("mediaSpecimen.sources.citation", "mediaSpecimen.representations.parts"));
        if(sob instanceof MediaSpecimen){
            MediaSpecimen mediaSpecimen = (MediaSpecimen) sob;
            if(mediaSpecimen.isPublish()){
                return mediaSpecimen.getMediaSpecimen();
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