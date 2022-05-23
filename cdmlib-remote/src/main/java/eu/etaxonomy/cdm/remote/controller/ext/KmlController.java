/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller.ext;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.remote.controller.BaseController;
import eu.etaxonomy.cdm.remote.controller.OptionsController;
import eu.etaxonomy.cdm.remote.controller.util.ControllerUtils;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;

/**
 * The ExternalGeoController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * @author a.kohlbecker
 * @since 18.06.2009
 *
 */
@CrossOrigin(origins = "*")
@Controller
@Api(value="mapServiceParameters")
@RequestMapping(value = { "kml" })
public class KmlController extends BaseController<TaxonBase, ITaxonService> {

    public static final Logger logger = Logger.getLogger(KmlController.class);

    @Autowired
    private IEditGeoService geoservice;

    @Autowired
    private IOccurrenceService occurrenceService;

    @Autowired
    private INameService nameService;

    @Autowired
    private ITermService termService;

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<MarkerType>(termService));
    }

    @Autowired
    @Override
    public void setService(ITaxonService service) {
        this.service = service;
    }

    /**
     * TODO This controller method replaces the general {@link OptionsController} which has been disabled temporarily.
     * The {@link OptionsController} was causing problems in some situations by blocking POST requests to other controllers.
     * This mainly happened in the taxeditor project where the integration test could not be run due to the cdm remote instance which
     * did not allow POST requests to the /remoting-public/user.service
     */
    @RequestMapping(
            value = "/**",
            method = RequestMethod.OPTIONS
    )
    public ResponseEntity options() {
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Assembles and returns URI parameter Strings for the EDIT Map Service. The distribution areas for the
     * {@link Taxon} instance identified by the <code>{taxon-uuid}</code> are found and are translated into
     * an valid URI parameter String. Higher level distribution areas are expanded in order to include all
     * nested sub-areas.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;geo&#x002F;map&#x002F;distribution&#x002F;{taxon-uuid}</b>
     *
     * @param request
     * @param response
     * @return URI parameter Strings for the EDIT Map Service
     * @throws IOException TODO write controller method documentation
     */
    @RequestMapping(value = { "specimensOrOccurences/{uuid-list}" }, method = RequestMethod.GET)
    public Kml doGetSpecimensOrOccurencesKml(
            @PathVariable("uuid-list") UuidList uuidList,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {


        logger.info("doGetSpecimensOrOccurencesKml() " + requestPathAndQuery(request));

        Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors = null;

        List<SpecimenOrObservationBase> specimensOrObersvations = occurrenceService.load(uuidList, null);

        specimensOrObersvations = specimensOrObersvations.stream().filter(s -> s != null).collect(Collectors.toList());

        Kml kml = geoservice.occurrencesToKML(specimensOrObersvations, specimenOrObservationTypeColors);

        return kml;
    }

    /**
     * Assembles and returns URI parameter Strings for the EDIT Map Service. The distribution areas for the
     * {@link Taxon} instance identified by the <code>{taxon-uuid}</code> are found and are translated into
     * an valid URI parameter String. Higher level distribution areas are expanded in order to include all
     * nested sub-areas.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;geo&#x002F;map&#x002F;distribution&#x002F;{taxon-uuid}</b>
     *
     * @param request
     * @param response
     * @return URI parameter Strings for the EDIT Map Service
     * @throws IOException TODO write controller method documentation
     */
    @RequestMapping(value = { "typeDesignations/{uuid-list}" }, method = RequestMethod.GET)
    public Kml doGetTypeDesignationsKml(
            @PathVariable("uuid-list") UuidList uuidList,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {


        logger.info("doGetTypeDesignationsKml() " + requestPathAndQuery(request));

        Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors = null;

        List<TypeDesignationBase<?>> typeDesignations = nameService.loadTypeDesignations(uuidList, Arrays.asList("typeSpecimen"));

        List<SpecimenOrObservationBase> specimensOrObersvations = typeDesignations.stream()
        		.filter(td -> td != null && td instanceof SpecimenTypeDesignation)
        		.map(SpecimenTypeDesignation.class::cast)
        		.map(SpecimenTypeDesignation::getTypeSpecimen)
        		.filter(s -> s != null)
        		.collect(Collectors.toList());

        Kml kml = geoservice.occurrencesToKML(specimensOrObersvations, specimenOrObservationTypeColors);

        return kml;
    }

    /**
     * Assembles and returns URI parameter Strings for the EDIT Map Service. The distribution areas for the
     * {@link Taxon} instance identified by the <code>{taxon-uuid}</code> are found and are translated into
     * an valid URI parameter String. Higher level distribution areas are expanded in order to include all
     * nested sub-areas.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;geo&#x002F;map&#x002F;distribution&#x002F;{taxon-uuid}</b>
     *
     * @param request
     * @param response
     * @return URI parameter Strings for the EDIT Map Service
     * @throws IOException TODO write controller method documentation
     */
    @RequestMapping(value = { "taxonOccurrencesFor/{uuid}" }, method = RequestMethod.GET)
    public Kml doGetTaxonOccurrenceKml(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
            @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
            @RequestParam(value = "maxDepth", required = false) Integer maxDepth,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {


        logger.info("doGetTaxonOccurrenceKml() " + requestPathAndQuery(request));

        Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors = null;

        List<SpecimenOrObservationBase> specimensOrObersvations = occurencesForTaxon(uuid, relationshipUuids,
				relationshipInversUuids, maxDepth, response);

        Kml kml = geoservice.occurrencesToKML(specimensOrObersvations, specimenOrObservationTypeColors);

        return kml;
    }

	private List<SpecimenOrObservationBase> occurencesForTaxon(UUID taxonUuid, UuidList relationshipUuids,
			UuidList relationshipInversUuids, Integer maxDepth, HttpServletResponse response) throws IOException {
		Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(
                relationshipUuids, relationshipInversUuids, termService);

        Taxon taxon = getCdmBaseInstance(Taxon.class, taxonUuid, response, (List<String>)null);

        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new OrderHint("titleCache", SortOrder.DESCENDING));

        List<SpecimenOrObservationBase> specimensOrObersvations = occurrenceService.listByAssociatedTaxon(
                null, includeRelationships, taxon, maxDepth, null, null, orderHints, null);
		return specimensOrObersvations;
	}


}
