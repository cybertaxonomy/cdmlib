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
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/name/{uuid}"})
public class NameController extends BaseController<TaxonNameBase, INameService>
{

    private static final List<String> TYPEDESIGNATION_INIT_STRATEGY = Arrays.asList(new String []{
            "typeStatus.representations",
            "typifiedNames",
            "typeSpecimen",
            "typeName",
            "citation",
            "citation.authorTeam.$",
    });

    private static final List<String> NAME_CACHE_INIT_STRATEGY = Arrays.asList(new String []{

    });

    public NameController(){
        super();
        setInitializationStrategy(Arrays.asList(new String[]{"$"})); //TODO still needed????
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Autowired
    @Override
    public void setService(INameService service) {
        this.service = service;
    }


    /**
     * Get the list of {@link TypeDesignationBase}s of the
     * {@link TaxonNameBase} instance identified by the <code>{name-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name&#x002F;{name-uuid}&#x002F;typeDesignations</b>
     *
     * @param request
     * @param response
     * @return a List of {@link TypeDesignationBase} entities which are initialized
     *         using the {@link #TYPEDESIGNATION_INIT_STRATEGY}
     * @throws IOException
     *
     * TODO obsolete method?
     */
    @RequestMapping(value = { "typeDesignations" }, method = RequestMethod.GET)
    public List<TypeDesignationBase> doGetNameTypeDesignations(
            @PathVariable("uuid") UUID uuid, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        if (request != null) {
        	logger.info("doGetTypeDesignations()" + request.getRequestURI());
        }
        TaxonNameBase tnb = getCdmBaseInstance(uuid, response,
                (List<String>) null);
        Pager<TypeDesignationBase> p = service.getTypeDesignations(tnb, null,
                null, null, TYPEDESIGNATION_INIT_STRATEGY);
        return p.getRecords();
    }

    @RequestMapping(
            value = {"nameCache"},
            method = RequestMethod.GET)
    public List<String> doGetNameCache(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {

        logger.info("doGetNameCache()" + request.getRequestURI());
        TaxonNameBase tnb = getCdmBaseInstance(uuid, response, NAME_CACHE_INIT_STRATEGY);
        NonViralName nvn = (NonViralName) tnb;
        String nameCacheString = nvn.getNameCache();
        List result = new ArrayList<String>();
        result.add(nameCacheString);
        return result;

    }

    @RequestMapping(value = "taggedName", method = RequestMethod.GET)
    public ModelAndView doGetTaggedName(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetDescriptionElementsByType() - " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();
        mv.addObject(service.getTaggedName(uuid));
        return mv;
    }


}
