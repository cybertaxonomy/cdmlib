/**
* Copyright (C) 2009 EDIT
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

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since 22.07.2010
 *
 */
@Controller
@Api("term")
@RequestMapping(value = {"/term/{uuid}"})
public class TermController
        extends AbstractIdentifiableController<DefinedTermBase, ITermService> {

    private static final List<String> TERM_COMPARE_INIT_STRATEGY = Arrays.asList(new String []{
            "vocabulary"
    });

    @Autowired
    @Override
    public void setService(ITermService service) {
        this.service = service;
    }

    /**
     * TODO write controller method documentation
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,	value = "compareTo/{uuidThat}")
    public ModelAndView doCompare(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("uuidThat") UUID uuidThat,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        DefinedTermBase<?> thisTerm = service.load(uuid, TERM_COMPARE_INIT_STRATEGY);
        DefinedTermBase<?> thatTerm = service.load(uuidThat, TERM_COMPARE_INIT_STRATEGY);
        if(thisTerm.getVocabulary().equals(thatTerm.getVocabulary())){
            if(OrderedTermBase.class.isAssignableFrom(thisTerm.getClass())){
                Integer result = ((OrderedTermBase)thisTerm).compareTo((OrderedTermBase)thatTerm);
                mv.addObject(result);
                return mv;
            }else{
                response.sendError(400, "Only ordered term types can be compared");
                return mv;
            }
        }else{
            response.sendError(400, "Terms of different vocabuaries can not be compared");
            return mv;
        }
    }
}
