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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.NamedAreaLevelPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;

/**
 *
 * @author a.kohlbecker
 * @since Jun 25, 2013
 *
 */
@Controller
@Api("portal_description")
@Transactional(readOnly=true)
@RequestMapping(value = {
            "/portal/description/{uuid}",
            "/portal/description/{uuid_list}"})
public class DescriptionPortalController extends BaseController<DescriptionBase, IDescriptionService> {

    private static final Logger logger = Logger.getLogger(DescriptionPortalController.class);

    protected static final List<String> DESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "elements.$",
            "elements.multilanguageText.*",
            "elements.annotations",
            "elements.sources.citation.authorship.$",
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
            "elements.sources.citation.authorship.$",
            "elements.sources.nameUsedInSource",
            "elements.area.level",
    });

    protected static final List<String> DISTRIBUTION_INFO_INIT_STRATEGY = Arrays.asList(new String []{
            "sources.citation.authorship.$",
            "sources.nameUsedInSource",
            "annotations"
    });

    @Autowired
    private ITermService termService;

    @Autowired
    private IVocabularyService vocabularyService ;


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

    @RequestMapping(value = "//portal/descriptionElement/{descriptionelement_uuid}/annotation", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
    public Pager<Annotation> getAnnotations(
            @PathVariable("descriptionelement_uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("getAnnotations() - " + requestPathAndQuery(request) );
        DescriptionElementBase annotatableEntity = service.getDescriptionElementByUuid(uuid);
        Pager<Annotation> annotations = service.getDescriptionElementAnnotations(annotatableEntity, null, null, 0, null, getInitializationStrategy());
        return annotations;
    }

}
