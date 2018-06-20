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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */

@Controller
@Api("name")
@RequestMapping(value = {"/name/{uuid}"})
public class NameController extends AbstractIdentifiableController<TaxonName, INameService>{

    private static Logger logger = Logger.getLogger(NameController.class);

    private static final List<String> TYPEDESIGNATION_INIT_STRATEGY = Arrays.asList(new String []{
            "typeStatus.representations",
            "typifiedNames",
            "typeSpecimen",
            "typeName",
            "citation",
            "citation.authorship.$",
    });

    private static final List<String> NAME_CACHE_INIT_STRATEGY = Arrays.asList(new String []{

    });

    private static final List<String> NAME_REGISTRATIONS_INIT_STRATEGY = Arrays.asList(new String []{
            "registrations.typeDesignations.$",
            "registrations.institution"
    });

    public NameController(){
        super();
        setInitializationStrategy(Arrays.asList(new String[]{"$"})); //TODO still needed????
    }

    @Autowired
    @Override
    public void setService(INameService service) {
        this.service = service;
    }


    /**
     * Get the list of {@link TypeDesignationBase}s of the
     * {@link TaxonName} instance identified by the <code>{name-uuid}</code>.
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
            logger.info("doGetTypeDesignations()" + requestPathAndQuery(request));
        }
        TaxonName tnb = getCdmBaseInstance(uuid, response, (List<String>) null);
        Pager<TypeDesignationBase> pager = service.getTypeDesignations(tnb, null,
                null, null, TYPEDESIGNATION_INIT_STRATEGY);
        return pager.getRecords();
    }

    @RequestMapping(
            value = {"nameCache"},
            method = RequestMethod.GET)
    public List<String> doGetNameCache(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {

        logger.info("doGetNameCache()" + requestPathAndQuery(request));
        TaxonName tnb = getCdmBaseInstance(uuid, response, NAME_CACHE_INIT_STRATEGY);
        String nameCacheString = tnb.getNameCache();
        List<String> result = new ArrayList<>();
        result.add(nameCacheString);
        return result;

    }

    @RequestMapping(value = "taggedName", method = RequestMethod.GET)
    public ModelAndView doGetTaggedName(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetDescriptionElementsByType() - " + requestPathAndQuery(request));

        ModelAndView mv = new ModelAndView();
        mv.addObject(service.getTaggedName(uuid));
        return mv;
    }

    @RequestMapping(
            value = {"registrations"},
            method = RequestMethod.GET)
    public Set<Registration> doGetRegistrations(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {

        logger.info("doGetRegistrations" + requestPathAndQuery(request));
        TaxonName tnb = getCdmBaseInstance(uuid, response, NAME_REGISTRATIONS_INIT_STRATEGY);
        Set<Registration> regs = tnb.getRegistrations();
        if(regs != null && regs.size() > 0){
            Set<Registration> regsFiltered = new HashSet<>(regs.size());
            for(Registration reg : regs){
                if(userIsAutheticated() && userIsAnnonymous() && reg.getStatus().equals(RegistrationStatus.PUBLISHED)) {
                    regsFiltered.add(reg);
                } else {
                    logger.debug("skipping unpublished registration");
                }
            }
            return regsFiltered;
        }
        return null;
    }


}
