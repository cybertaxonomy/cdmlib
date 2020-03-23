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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.EntityInitStrategy;
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
public class NamePortalController extends BaseController<TaxonName, INameService> {

    private static final Logger logger = Logger.getLogger(NamePortalController.class);

    private static final List<String> TYPEDESIGNATION_INIT_STRATEGY = TypeDesignationPortalController.DEFAULT_INIT_STRATEGY;


    private static final List<String> NAMEDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "elements.$",
            "elements.multilanguageText",
            "elements.media",
    });

    private static EntityInitStrategy nameRelationsInitStrategy = null;

    /**
     * @return the nameRelationsInitStrategy
     */
    public static EntityInitStrategy getNameRelationsInitStrategy() {
        if(nameRelationsInitStrategy == null){
            nameRelationsInitStrategy = extendNameRelationsInitStrategies(NameController.NAME_RELATIONS_INIT_STRATEGY.getPropertyPaths());
        }
        return nameRelationsInitStrategy;
    }


    @Override
    protected <CDM_BASE extends CdmBase> List<String> complementInitStrategy(Class<CDM_BASE> clazz,
            List<String> pathProperties) {

        if(pathProperties == null){
            return pathProperties;
        }

        EntityInitStrategy initStrategy = extendNameRelationsInitStrategies(pathProperties);

        return initStrategy.getPropertyPaths();
    }

    /**
     * @param pathProperties
     * @return
     */
    static EntityInitStrategy extendNameRelationsInitStrategies(List<String> pathProperties) {
        EntityInitStrategy initStrategy = new EntityInitStrategy(pathProperties);

        if(pathProperties.contains("nameRelations")){
            // nameRelations is a transient property!
            initStrategy.getPropertyPaths().remove("nameRelations");
            initStrategy.extend("relationsFromThisName", TaxonPortalController.NAMERELATIONSHIP_INIT_STRATEGY.getPropertyPaths(), true);
            initStrategy.extend("relationsToThisName", TaxonPortalController.NAMERELATIONSHIP_INIT_STRATEGY.getPropertyPaths(), true);
        } else {
            if(pathProperties.contains("relationsFromThisName")){
                initStrategy.getPropertyPaths().remove("relationsFromThisName");
                initStrategy.extend("relationsFromThisName", TaxonPortalController.NAMERELATIONSHIP_INIT_STRATEGY.getPropertyPaths(), true);
            }
            if(pathProperties.contains("relationsToThisName")){
                initStrategy.getPropertyPaths().remove("relationsToThisName");
                initStrategy.extend("relationsToThisName", TaxonPortalController.NAMERELATIONSHIP_INIT_STRATEGY.getPropertyPaths(), true);
            }
        }
        return initStrategy;
    }

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
    @RequestMapping(
            value = {"typeDesignations"},
            method = RequestMethod.GET)
    public List<TypeDesignationBase> doGetTypeDesignations(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {
        TaxonName tnb = getCdmBaseInstance(uuid, response, (List<String>)null);
        if(tnb == null){
            return null;
        }
        Pager<TypeDesignationBase> p = service.getTypeDesignations(tnb,  null, null, null, TYPEDESIGNATION_INIT_STRATEGY);
        return p.getRecords();
    }

    /**
     * Get the list of {@link TypeDesignationBase}s associated to any name in the same homotypical group to which
     * the {@link TaxonName} identified by the <code>{name-uuid}</code> belongs.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name&#x002F;{name-uuid}&#x002F;typeDesignations</b>
     *
     * @param request
     * @param response
     * @return a List of {@link TypeDesignationBase} entities which are initialized
     *         using the {@link #TYPEDESIGNATION_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = { "typeDesignationsInHomotypicalGroup" }, method = RequestMethod.GET)
    public List<TypeDesignationBase> doGetTypeDesignationsInHomotypicalGroup(
            @PathVariable("uuid") UUID uuid, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        if (request != null) {
            logger.info("doGetTypeDesignationsInHomotypicalGroup()" + requestPathAndQuery(request));
        }
        List<TypeDesignationBase> result = service.getTypeDesignationsInHomotypicalGroup(uuid,
                null, null, TYPEDESIGNATION_INIT_STRATEGY);
        return result;
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

        TaxonName tnb = getCdmBaseInstance(uuid, response, (List<String>)null);
        if(tnb == null){
            return null;
        }
        Pager<TaxonNameDescription> p = descriptionService.getTaxonNameDescriptions(tnb, null, null, NAMEDESCRIPTION_INIT_STRATEGY);
        return p.getRecords();

    }

    // TODO this is a copy of the same method in NameController --> this class should extend  NameController !!!
    @RequestMapping(
            value = "nameRelations",
            method = RequestMethod.GET)
    public Object doGetNameRelations(
            @PathVariable("uuid") UUID uuid,
            // doPage request parameters
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            // doList request parameters
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetNameRelations" + requestPathAndQuery(request));
        TaxonName tnb = getCdmBaseInstance(uuid, response, getNameRelationsInitStrategy().getPropertyPaths());

        Set<NameRelationship> nameRelations = tnb.getNameRelations();

        if(nameRelations != null && nameRelations.size() > 0){
        Set<NameRelationship> nameRelationsFiltered = new HashSet<>(nameRelations.size());

        if(userHelper.userIsAnnonymous()){
            // need to filter out unpublished related names in this case
                for(NameRelationship rel : nameRelations){
                    // check if the name has been published ba any registration
                    Set<Registration> regsToCheck = new HashSet<>();
                    if(rel.getToName().equals(tnb) && rel.getFromName().getRegistrations() != null) {
                        regsToCheck.addAll(rel.getFromName().getRegistrations());
                    }
                    if(rel.getFromName().equals(tnb) && rel.getToName().getRegistrations() != null) {
                        regsToCheck.addAll(rel.getToName().getRegistrations());
                    }
                    // if there is no registration for this name we assume that it is published
                    boolean nameIsPublished = regsToCheck.size() == 0;
                    nameIsPublished |= regsToCheck.stream().anyMatch(reg -> reg.getStatus().equals(RegistrationStatus.PUBLISHED));
                    if(nameIsPublished){
                        nameRelationsFiltered.add(rel);
                    } else {
                        logger.debug("Hiding NameRelationship " + rel);
                    }
                }
            }  else {
                // no filtering needed
                nameRelationsFiltered = nameRelations;
            }
            return pageFromCollection(nameRelationsFiltered, pageNumber, pageSize, start, limit, response);
        }
        return null;
    }


}
