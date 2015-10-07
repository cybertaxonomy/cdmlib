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

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.wordnik.swagger.annotations.Api;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.dto.DistributionInfoDTO;
import eu.etaxonomy.cdm.api.service.dto.DistributionInfoDTO.InfoPart;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.ext.geo.CondensedDistributionRecipe;
import eu.etaxonomy.cdm.ext.geo.EditGeoServiceUtilities;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.NamedAreaLevelPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@Api("description")
@RequestMapping(value = {"/description/{uuid}", "/description/{uuid_list}"})
public class DescriptionController extends BaseController<DescriptionBase, IDescriptionService>
{
    @Autowired
    private IFeatureTreeService featureTreeService;

    @Autowired
    private ITermService termService;


    @Autowired
    private IEditGeoService geoService;

    protected static final List<String> TAXONDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "elements.$",
            "elements.sources.citation.authorship",
            "elements.sources.nameUsedInSource",
            "elements.multilanguageText",
            "elements.media",
    });

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

    @RequestMapping(value = "hasStructuredData", method = RequestMethod.GET)
    public ModelAndView doHasStructuredData(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doHasStructuredData() - " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        DescriptionBase description = service.load(uuid);

        if(!(description instanceof TaxonDescription)){
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
            // will terminate thread
        }

        boolean hasStructuredData = service.hasStructuredData(description);

        mv.addObject(hasStructuredData);
        return mv;
    }

    @RequestMapping(value = "//descriptionElement/{descriptionelement_uuid}", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
    public ModelAndView doGetDescriptionElement(
            @PathVariable("descriptionelement_uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        ModelAndView mv = new ModelAndView();
        logger.info("doGetDescriptionElement() - " + request.getRequestURI());
        DescriptionElementBase element = service.getDescriptionElementByUuid(uuid);
        if(element == null) {
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }
        mv.addObject(element);
        return mv;
    }

    @RequestMapping(value = "//descriptionElement/{descriptionelement_uuid}/annotations", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
    public Pager<Annotation> doGetDescriptionElementAnnotations(
            @PathVariable("descriptionelement_uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetDescriptionElementAnnotations() - " + request.getRequestURI());
        DescriptionElementBase annotatableEntity = service.getDescriptionElementByUuid(uuid);
        if(annotatableEntity == null){
            HttpStatusMessage.UUID_INVALID.send(response);
            // method will exit here
            return null;
        }

        Pager<Annotation> annotations = service.getDescriptionElementAnnotations(annotatableEntity, null, null, 0, null, getInitializationStrategy());
        return annotations;
    }

    @RequestMapping(value = "//descriptionElement/{descriptionelement_uuid}/states", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
    public ModelAndView doGetDescriptionElementStates(
            @PathVariable("descriptionelement_uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetDescriptionElementStates() - " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        DescriptionElementBase descriptionElement = service.loadDescriptionElement(uuid,
                Arrays.asList( new String[]{
                        "states.state.representations",
                        "modifiers",
                        "modifyingText"
                        } ));
        if(descriptionElement == null){
            HttpStatusMessage.UUID_INVALID.send(response);
            // method will exit here
            return null;
        }

        if(descriptionElement instanceof CategoricalData){

        }
        List<StateData> states = ((CategoricalData)descriptionElement).getStateData();
        mv.addObject(states);
        return mv;
    }

    /*
    @RequestMapping(value = "{uuid_list}/namedAreaTree", method = RequestMethod.GET)
    public NamedAreaTree doGetOrderedDistributions(
            @PathVariable("uuid_list") UuidList descriptionUuidList,
            @RequestParam(value = "omitLevels", required = false) Set<NamedAreaLevel> levels,
            //@ModelAttribute("omitLevels") HashSet<NamedAreaLevel> levels,
            HttpServletRequest request, HttpServletResponse response) {
        logger.info("getOrderedDistributions(" + ObjectUtils.toString(levels) + ") - " + request.getRequestURI());
        Set<TaxonDescription> taxonDescriptions = new HashSet<TaxonDescription>();
        TaxonDescription description;
        for (UUID descriptionUuid : descriptionUuidList) {
            description = (TaxonDescription) service.load(descriptionUuid);
            taxonDescriptions.add(description);
        }
        NamedAreaTree areaTree = service.getOrderedDistributions(taxonDescriptions, levels);
        return areaTree;
    }
    */

    @RequestMapping(value = "//description/{uuid}/naturalLanguageDescription/{featuretree_uuid}", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
    public ModelAndView doGenerateNaturalLanguageDescription(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("featuretree_uuid") UUID featureTreeUuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGenerateNaturalLanguageDescription() - " + request.getRequestURI());

        DescriptionBase description = service.load(uuid);

        ModelAndView mv = new ModelAndView();

        List<Language> languages = LocaleContext.getLanguages();

        if(!(description instanceof TaxonDescription)){
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
            // will terminate thread
        }

        FeatureTree featureTree = featureTreeService.load(featureTreeUuid, null);
        if(featureTree == null){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
            // will terminate thread
        }

        String naturalLanguageDescription = service.generateNaturalLanguageDescription(
                featureTree,
                (TaxonDescription)description,
                languages,
                ", ");
        TextData textData = TextData.NewInstance(Feature.DESCRIPTION());
        textData.putText(Language.DEFAULT(), naturalLanguageDescription);
        mv.addObject(textData);
        return mv;
    }

    /**
     * @param taxonUuid
     * @param parts
     *            possible values: condensedStatus, tree, mapUriParams,
     *            elements,
     * @param subAreaPreference
     * @param statusOrderPreference
     * @param hideMarkedAreasList
     * @param omitLevels
     * @param request
     * @param response
     * @param recipe
     *  The recipe for creating the condensed distribution status
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @RequestMapping(value = "//description/distributionInfoFor/{uuid}", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
    public ModelAndView doGetDistributionInfo(
            @PathVariable("uuid") UUID taxonUuid,
            @RequestParam("part") Set<InfoPart> partSet,
            @RequestParam(value = "subAreaPreference", required = false) boolean subAreaPreference,
            @RequestParam(value = "statusOrderPreference", required = false) boolean statusOrderPreference,
            @RequestParam(value = "hiddenAreaMarkerType", required = false) DefinedTermBaseList<MarkerType> hideMarkedAreasList,
            @RequestParam(value = "omitLevels", required = false) Set<NamedAreaLevel> omitLevels,
            @RequestParam(value = "statusColors", required = false) String statusColorsString,
            @RequestParam(value = "recipe", required = false, defaultValue="EuroPlusMed") CondensedDistributionRecipe recipe,
            HttpServletRequest request,
            HttpServletResponse response) throws JsonParseException, JsonMappingException, IOException {

            logger.info("doGetDistributionInfo() - " + requestPathAndQuery(request));

            ModelAndView mv = new ModelAndView();

            Set<MarkerType> hideMarkedAreas = null;
            if(hideMarkedAreasList != null){
                hideMarkedAreas = hideMarkedAreasList.asSet();
            }


            EnumSet<InfoPart> parts = EnumSet.copyOf(partSet);

            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors = EditGeoServiceUtilities.buildStatusColorMap(statusColorsString, termService);

            DistributionInfoDTO dto = geoService.composeDistributionInfoFor(parts, taxonUuid, subAreaPreference, statusOrderPreference,
                    hideMarkedAreas, omitLevels, presenceAbsenceTermColors, LocaleContext.getLanguages(), getInitializationStrategy(), recipe);

            mv.addObject(dto);

            return mv;
    }


}