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
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.DistributionTree;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.dto.DistributionInfoDTO;
import eu.etaxonomy.cdm.api.service.dto.DistributionInfoDTO.InfoPart;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.utility.DescriptionUtility;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.NamedAreaLevelPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * IMPORTANT:
 *
 * This controller is mostly a 1:1 copy of the DescriptionController
 * and this provides identical end points which only differ in the depth of the
 * object graphs returned.
 *
 * @author a.kohlbecker
 * @date Jun 25, 2013
 *
 */
@Controller
@Transactional(readOnly=true)
@RequestMapping(value = {
            "/portal/description/{uuid}",
            "/portal/description/{uuid_list}",
            "/portal/descriptionElement/{descriptionelement_uuid}"})
public class DescriptionPortalController extends BaseController<DescriptionBase, IDescriptionService>
{

    protected static final List<String> DESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "elements.$",
            "elements.multilanguageText.*",
            "elements.annotations",
            "elements.sources.citation.authorTeam.$",
            "elements.sources.nameUsedInSource",
            "elements.area.level",
            "elements.modifyingText",
            "elements.stateData.*",
            "elements.statisticalValues.*",
            "elements.unit",
            "elements.media",
            "elements.kindOfUnit"

    });

    protected static final List<String> ORDERED_DISTRIBUTION_INIT_STRATEGY = Arrays.asList(new String []{
            "elements.$",
            "elements.annotations",
            "elements.markers",
            "elements.sources.citation.authorTeam.$",
            "elements.sources.nameUsedInSource",
            "elements.area.level",
    });

    protected static final List<String> DISTRIBUTION_INFO_INIT_STRATEGY = Arrays.asList(new String []{
            "sources.citation.authorTeam.$",
            "sources.nameUsedInSource",
            "annotations"
    });

    @Autowired
    private ITermService termService;

    @Autowired
    private IEditGeoService geoService;


    public DescriptionPortalController() {
        super();
        setInitializationStrategy(DESCRIPTION_INIT_STRATEGY);
    }

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        binder.registerCustomEditor(NamedAreaLevel.class, new NamedAreaLevelPropertyEditor());
        binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<MarkerType>(termService));
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Autowired
    @Override
    public void setService(IDescriptionService service) {
        this.service = service;
    }

    @RequestMapping(value = "/portal/descriptionElement/{descriptionelement_uuid}/annotation", method = RequestMethod.GET)
    public Pager<Annotation> getAnnotations(
            @PathVariable("descriptionelement_uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("getAnnotations() - " + requestPathAndQuery(request) );
        DescriptionElementBase annotatableEntity = service.getDescriptionElementByUuid(uuid);
        Pager<Annotation> annotations = service.getDescriptionElementAnnotations(annotatableEntity, null, null, 0, null, getInitializationStrategy());
        return annotations;
    }

    /**
     * NOTICE: required to have a TreeNodeBeanProcessor configured which suppresses the
     * redundant output of distribution.area
     *
     * @param descriptionUuidList
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean}
     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean}
     * @param hideMarkedAreas
     *            distributions where the area has a {@link Marker} with one of
     *            the specified {@link MarkerType}s will be skipped, see
     *            {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
     * @param omitLevels
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/portal/description/{uuid_list}/DistributionTree", method = RequestMethod.GET)
    public DistributionTree doGetOrderedDistributionsB(
            @PathVariable("uuid_list") UuidList descriptionUuidList,
            @RequestParam(value = "subAreaPreference", required = false) boolean subAreaPreference,
            @RequestParam(value = "statusOrderPreference", required = false) boolean statusOrderPreference,
            @RequestParam(value = "hideMarkedAreas", required = false) DefinedTermBaseList<MarkerType> hideMarkedAreasList,
            @RequestParam(value = "omitLevels", required = false) Set<NamedAreaLevel> omitLevels,
            HttpServletRequest request,
            HttpServletResponse response) {

        logger.info("getOrderedDistributionsB() - " + requestPathAndQuery(request) );

        Set<TaxonDescription> taxonDescriptions = new HashSet<TaxonDescription>();
        TaxonDescription description;
        for (UUID descriptionUuid : descriptionUuidList) {
            logger.debug("  loading description " + descriptionUuid.toString() );
            description = (TaxonDescription) service.load(descriptionUuid, null);
            taxonDescriptions.add(description);
        }

        Set<MarkerType> hideMarkedAreas = null;
        if(hideMarkedAreasList != null){
            hideMarkedAreas = hideMarkedAreasList.asSet();
        }

        logger.debug("  get ordered distributions ");
        DistributionTree distTree = service.getOrderedDistributions(taxonDescriptions, subAreaPreference, statusOrderPreference,
                hideMarkedAreas, omitLevels, ORDERED_DISTRIBUTION_INIT_STRATEGY);
        if (logger.isDebugEnabled()){ logger.debug("done");}
        return distTree;
    }

    /**
     * @param taxonUuid
     * @param parts
     *            possible values: condensedStatusString, tree, mapUriParams,
     *            elements,
     * @param subAreaPreference
     * @param statusOrderPreference
     * @param hideMarkedAreasList
     * @param omitLevels
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/portal/description/distributionInfoFor/{uuid}", method = RequestMethod.GET)
    public ModelAndView doGetDistributionInfo(
            @PathVariable("uuid") UUID taxonUuid,
            @RequestParam("part") Set<InfoPart> partSet,
            @RequestParam(value = "subAreaPreference", required = false) boolean subAreaPreference,
            @RequestParam(value = "statusOrderPreference", required = false) boolean statusOrderPreference,
            @RequestParam(value = "hideMarkedAreas", required = false) DefinedTermBaseList<MarkerType> hideMarkedAreasList,
            @RequestParam(value = "omitLevels", required = false) Set<NamedAreaLevel> omitLevels,
            HttpServletRequest request,
            HttpServletResponse response) {

            logger.debug("doGetDistributionInfo() - " + requestPathAndQuery(request));

            ModelAndView mv = new ModelAndView();

            Set<MarkerType> hideMarkedAreas = null;
            if(hideMarkedAreasList != null){
                hideMarkedAreas = hideMarkedAreasList.asSet();
            }

            EnumSet<InfoPart> parts = EnumSet.copyOf(partSet);

            DistributionInfoDTO dto = geoService.composeDistributionInfoFor(parts, taxonUuid, subAreaPreference, statusOrderPreference,
                    hideMarkedAreas, omitLevels, LocaleContext.getLanguages(), DISTRIBUTION_INFO_INIT_STRATEGY);

            mv.addObject(dto);

            return mv;
    }


}
