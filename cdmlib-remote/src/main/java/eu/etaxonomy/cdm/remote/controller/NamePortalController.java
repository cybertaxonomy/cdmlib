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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import io.swagger.annotations.Api;

/**
 * The NamePortalController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * Methods mapped at type level, inherited from super classes ({@link BaseController}):
 * <blockquote>
 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;name&#x002F;{name-uuid}</b>
 *
 * Get the {@link TaxonName} instance identified by the <code>{name-uuid}</code>.
 * The returned TaxonName is initialized by
 * the following strategy: -- NONE --
 * </blockquote>
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */

@Controller
@Api("portal_name")
@RequestMapping(value = {"/portal/name/{uuid}"})
public class NamePortalController extends BaseController<TaxonName, INameService>
{

    private static final List<String> TYPEDESIGNATION_INIT_STRATEGY = Arrays.asList(new String []{
            "typeName.$",
            "typeSpecimen",
            "typeStatus.representations",
            "typifiedNames.nomenclaturalReference.authorship",
            "citation.authorship.$",
            "typeSpecimen.media",
            "registrations.institution"
    });


    private static final List<String> NAMEDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "elements.$",
            "elements.multilanguageText",
            "elements.media",
    });


//	public NamePortalController(){
//		super();
//		setInitializationStrategy(Arrays.asList(new String[]{"$"})); //TODO required???
//	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Autowired
    @Override
    public void setService(INameService service) {
        this.service = service;
    }

    @Autowired
    private IDescriptionService descriptionService;

    /**
     * Get the list of {@link TypeDesignationBase}s of the
     * {@link TaxonName} instance identified by the <code>{name-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;name&#x002F;{name-uuid}&#x002F;typeDesignations</b>
     *
     * @param request
     * @param response
     * @return a List of {@link TypeDesignationBase} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #TYPEDESIGNATION_INIT_STRATEGY}
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(
            value = {"typeDesignations"},
            method = RequestMethod.GET)
    public List<TypeDesignationBase> doGetTypeDesignations(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {
        ModelAndView mv = new ModelAndView();
        TaxonName tnb = getCdmBaseInstance(uuid, response, (List<String>)null);
        Pager<TypeDesignationBase> p = service.getTypeDesignations(tnb,  null, null, null, TYPEDESIGNATION_INIT_STRATEGY);
        return p.getRecords();
    }

    /**
     * Get the list of {@link TaxonNameDescription}s of the Name associated with the
     * {@link TaxonName} instance identified by the <code>{name-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;name&#x002F;{name-uuid}&#x002F;descriptions</b>
     *
     * @param request
     * @param response
     * @return a List of {@link TaxonNameDescription} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #NAMEDESCRIPTION_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"taxonNameDescriptions"},
            method = RequestMethod.GET)
    public List<TaxonNameDescription> doGetNameDescriptions(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {
        logger.info("doGetNameDescriptions()" + request.getRequestURI());
        TaxonName tnb = service.load(uuid, null);
        Pager<TaxonNameDescription> p = descriptionService.getTaxonNameDescriptions(tnb, null, null, NAMEDESCRIPTION_INIT_STRATEGY);
        return p.getRecords();
    }


}
