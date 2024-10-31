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
import java.util.ArrayList;
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

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.format.reference.NomenclaturalSourceFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */
@Controller
@Api("reference")
@RequestMapping(value = {"/reference/{uuid}"})
public class ReferenceController extends AbstractIdentifiableController<Reference, IReferenceService>{

    private static final List<String> NOMENCLATURAL_CITATION_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "authorship",
            "inReference.inReference",
            "inReference.authorship"
    });

    private static final List<String> CITATION_WITH_AUTHORSHIP_INIT_STRATEGY = Arrays.asList(new String []{
            "authorship.$"
    });

    public ReferenceController(){
        setInitializationStrategy(Arrays.asList(new String[]{
                "$",
                "authorship.$",
                "annotationType.$",
                "annotationType.includes.$",
                "annotations.$",
                "annotations.annotationType.$",
                "annotations.annotationType.includes.$",
             }));
    }

    @Autowired
    @Override
    public void setService(IReferenceService service) {
        this.service = service;
    }

    /**
     * TODO write controller documentation
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(
        value = {"nomenclaturalCitation"},
        method = RequestMethod.GET)
    public ModelAndView doGetNomenclaturalCitation(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "microReference", required = false) String microReference)throws IOException {
        ModelAndView mv = new ModelAndView();
        Reference ref = service.load(uuid, NOMENCLATURAL_CITATION_INIT_STRATEGY);
        String nomRefCit = NomenclaturalSourceFormatter.INSTANCE().format(ref, microReference);
        mv.addObject(nomRefCit);
        return mv;
    }

    @RequestMapping(
            value = {"authorship"},
            method = RequestMethod.GET)
        public ModelAndView doGetAuthorship(
                @PathVariable("uuid") UUID uuid,
                @SuppressWarnings("unused") HttpServletRequest request,
                @SuppressWarnings("unused") HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        Reference rb = service.load(uuid, CITATION_WITH_AUTHORSHIP_INIT_STRATEGY);
        if(rb.getAuthorship() != null){
            mv.addObject(rb.getAuthorship());
        }
        return mv;
    }

    @Override
    protected  <CDM_BASE extends CdmBase> List<String> complementInitStrategy(@SuppressWarnings("unused") Class<CDM_BASE> clazz, List<String> pathProperties) {

        if(pathProperties.stream().anyMatch(s -> s.startsWith("annotations"))) {
            List<String> complemented = new ArrayList<>(pathProperties);
            complemented.add("annotations.annotationType.*");
            complemented.add("annotations.annotationType.includes");
            return complemented;
        }

        return pathProperties;

    }
}