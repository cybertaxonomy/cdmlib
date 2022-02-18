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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IDescriptionElementService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.NamedAreaLevelPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */
@Controller
@Api("descriptionElement")
@RequestMapping(value = {"/descriptionElement/{uuid}", "/descriptionElement/{uuid_list}"})
public class DescriptionElementController
        extends BaseController<DescriptionElementBase, IDescriptionElementService>{

    private static final List<String> STATE_INIT_STRATEGY = Arrays.asList( new String[]{
            "states.state.representations",
            "modifiers",
            "modifyingText"
            } );

    public static final Logger logger = Logger.getLogger(DescriptionElementController.class);

    @Autowired
    private ITermService termService;

    private IDescriptionElementService service;

    @Override
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        binder.registerCustomEditor(NamedAreaLevel.class, new NamedAreaLevelPropertyEditor());
        binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<MarkerType>(termService));
    }

    @Override
    @Autowired
    public void setService(IDescriptionElementService service) {
        this.service = service;
    }

//    @RequestMapping(method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
//    public ModelAndView doGetDescriptionElement(
//            @PathVariable("uuid") UUID uuid,
//            HttpServletRequest request,
//            HttpServletResponse response) throws IOException {
//
//        ModelAndView mv = new ModelAndView();
//        logger.info("doGetDescriptionElement() - " + request.getRequestURI());
//        DescriptionElementBase element = service.loadDescriptionElement(uuid, getInitializationStrategy());
//        if(element == null) {
//            HttpStatusMessage.UUID_NOT_FOUND.send(response);
//        }
//        mv.addObject(element);
//        return mv;
//    }

    @RequestMapping(value = "annotations", method = RequestMethod.GET)
    public Pager<Annotation> doGetDescriptionElementAnnotations(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetDescriptionElementAnnotations() - " + request.getRequestURI());
        DescriptionElementBase annotatableEntity = service.load(uuid);
        if(annotatableEntity == null){
            HttpStatusMessage.UUID_INVALID.send(response);
            // method will exit here
            return null;
        }

        Pager<Annotation> annotations = service.getAnnotations(annotatableEntity, null, null, 0, null, getInitializationStrategy());
        return annotations;
    }

    @RequestMapping(value = "states", method = RequestMethod.GET)
    public ModelAndView doGetDescriptionElementStates(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetDescriptionElementStates() - " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        DescriptionElementBase descriptionElement = service.load(uuid, STATE_INIT_STRATEGY);
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


}
