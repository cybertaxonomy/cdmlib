/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller.ext;

import io.swagger.annotations.Api;

import java.awt.Color;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.api.utility.DescriptionUtility;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.ext.geo.OccurrenceServiceRequestParameterDto;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.remote.controller.BaseController;
import eu.etaxonomy.cdm.remote.controller.util.ControllerUtils;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

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
@Controller
@Api(value="mapServiceParameters")
@RequestMapping(value = { "ext/edit/mapServiceParameters/" })
public class ExternalGeoController extends BaseController<TaxonBase, ITaxonService> {

    public static final Logger logger = Logger.getLogger(ExternalGeoController.class);

    @Autowired
    private IEditGeoService geoservice;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private IOccurrenceService occurrenceService;

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
     * Assembles and returns URI parameter Strings for the EDIT Map Service. The distribution areas for the
     * {@link Taxon} instance identified by the <code>{taxon-uuid}</code> are found and are translated into
     * an valid URI parameter String. Higher level distribution areas are expanded in order to include all
     * nested sub-areas.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;geo&#x002F;map&#x002F;distribution&#x002F;{taxon-uuid}</b>
     *
     *
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean}
     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean}
     * @param hideMarkedAreas
     *            comma separated list of {@link MarkerType} uuids,
     *            distributions where the area has a {@link Marker} with one of
     *            the specified {@link MarkerType}s will be skipped, see
     *            {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
     * @param request
     * @param response
     * @return URI parameter Strings for the EDIT Map Service
     * @throws IOException
     */
    @RequestMapping(value = { "taxonDistributionFor/{uuid}" }, method = RequestMethod.GET)
    public ModelAndView doGetDistributionMapUriParams(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "subAreaPreference", required = false) boolean subAreaPreference,
            @RequestParam(value = "statusOrderPreference", required = false) boolean statusOrderPreference,
            @RequestParam(value = "hideMarkedAreas", required = false) DefinedTermBaseList<MarkerType> hideMarkedAreasList,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        logger.info("doGetDistributionMapUriParams() " + request.getRequestURI());
        ModelAndView mv = new ModelAndView();

        // get the descriptions for the taxon
        Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);

        Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors = null;
        //languages
        List<Language> langs = LocaleContext.getLanguages();

        Set<MarkerType> hideMarkedAreas = null;
        if(hideMarkedAreasList != null){
            hideMarkedAreas = hideMarkedAreasList.asSet();
        }

        Set<DefinedTerm> scopes = null;
        Set<NamedArea> geographicalScope = null;
        Integer pageSize = null;
        Integer pageNumber = null;
        List<String> propertyPaths = null;
        Pager<TaxonDescription> page = descriptionService.pageTaxonDescriptions(taxon, scopes, geographicalScope, pageSize, pageNumber, propertyPaths);

        List<TaxonDescription> taxonDescriptions = page.getRecords();
        String uriParams = geoservice.getDistributionServiceRequestParameterString(taxonDescriptions,
                subAreaPreference, statusOrderPreference,
                hideMarkedAreas, presenceAbsenceTermColors, langs);
        mv.addObject(uriParams);

        return mv;
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
    public ModelAndView doGetOccurrenceMapUriParams(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
            @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
            @RequestParam(value = "maxDepth", required = false) Integer maxDepth,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors = null;

        logger.info("doGetOccurrenceMapUriParams() " + requestPathAndQuery(request));
        ModelAndView mv = new ModelAndView();

        Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(
                relationshipUuids, relationshipInversUuids, termService);

        Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);

        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("titleCache", SortOrder.DESCENDING));

        List<SpecimenOrObservationBase> specimensOrObersvations = occurrenceService.listByAssociatedTaxon(
                null, includeRelationships, taxon, maxDepth, null, null, orderHints, null);

        OccurrenceServiceRequestParameterDto dto = geoservice.getOccurrenceServiceRequestParameterString(specimensOrObersvations,
                specimenOrObservationTypeColors );
        mv.addObject(dto);
        return mv;
    }

    /**
     * EXPERIMENTAL !!!!!
     * DO NOT USE   !!!!!
     *
     * @param vocabUuid
     * @param request
     * @param response
     * @return
     * @throws IOException
     *
     * @author a.kohlbecker
     */
    @RequestMapping(value = { "mapShapeFileToNamedAreas" }, method = RequestMethod.GET)
    public ModelAndView doMapShapeFileToNamedAreas(
            @RequestParam(required=false, value="vocabularyUuid") UUID vocabUuid,
            @RequestParam(required=false, value="namedAreaUuids") UuidList namedAreaUuids,
            @RequestParam(required=true, value="localFile") String localFile,
            @RequestParam(required=true, value="idSearchField") List<String> idSearchFields,
            @RequestParam(required=true, value="wmsLayerName") String wmsLayerName,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        logger.info("doMapShapeFileToNamedAreas() " + requestPathAndQuery(request));
        ModelAndView mv = new ModelAndView();

        FileReader reader = new FileReader(localFile);

        Set<UUID> areaUuidSet = null;
        if(namedAreaUuids != null) {
            areaUuidSet = namedAreaUuids.asSet();
        }
        Map<NamedArea, String> resultMap = geoservice.mapShapeFileToNamedAreas(
                reader, idSearchFields , wmsLayerName , vocabUuid, areaUuidSet);
        Map<String, String> flatResultMap = new HashMap<String, String>(resultMap.size());
        for(NamedArea area : resultMap.keySet()){
            flatResultMap.put(area.getTitleCache() + " [" + area.getUuid() + "]", resultMap.get(area));
        }
        mv.addObject(flatResultMap);
        return mv;

    }


}
