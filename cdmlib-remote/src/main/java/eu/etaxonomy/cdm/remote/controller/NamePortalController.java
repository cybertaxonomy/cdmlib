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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.EntityInitStrategy;
import eu.etaxonomy.cdm.remote.service.RegistrableEntityFilter;
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

    private static final Logger logger = LogManager.getLogger();

    private static final EntityInitStrategy DEFAULT_INIT_STRATEGY =  new EntityInitStrategy(
            "$",
            "nomenclaturalSource.citation.inReference.inReference"
            );

    private static final List<String> TYPEDESIGNATION_INIT_STRATEGY = TypeDesignationPortalController.DEFAULT_INIT_STRATEGY;


    private static final List<String> NAMEDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "elements.$",
            "elements.multilanguageText",
            "elements.media",
            "elements.sources.citation.authorship",
            "elements.sources.nameUsedInSource",
    });

    private static final EntityInitStrategy NOMREF_INIT_STRATEGY = new EntityInitStrategy(
            "nomenclaturalSource.citation.authorship.$",
            "nomenclaturalSource.citation.inReference.authorship.$",
            "nomenclaturalSource.citation.inReference.inReference.authorship.$",
            "nomenclaturalSource.citation.inReference.inReference.inReference.authorship.$"
            );

    private static EntityInitStrategy nameRelationsInitStrategy = null;

    public static EntityInitStrategy getNameRelationsInitStrategy() {
        if(nameRelationsInitStrategy == null){
            nameRelationsInitStrategy = extendNameRelationsInitStrategies(NameController.NAME_RELATIONS_INIT_STRATEGY.getPropertyPaths(), true);
        }
        return nameRelationsInitStrategy;
    }

    public NamePortalController() {
        setInitializationStrategy(DEFAULT_INIT_STRATEGY.getPropertyPaths());
    }

    @Override
    protected <CDM_BASE extends CdmBase> List<String> complementInitStrategy(Class<CDM_BASE> clazz,
            List<String> pathProperties) {

        if(pathProperties == null){
            return pathProperties;
        }
        EntityInitStrategy initStrategy = extendNameRelationsInitStrategies(pathProperties, false);
        return initStrategy.getPropertyPaths();
    }

    static EntityInitStrategy extendNameRelationsInitStrategies(List<String> pathProperties, boolean addNomrefInitStrategy) {

        EntityInitStrategy initStrategy = new EntityInitStrategy(pathProperties);

        EntityInitStrategy nameRelInitExtendet = TaxonPortalController.NAMERELATIONSHIP_INIT_STRATEGY;

        if(addNomrefInitStrategy){
            nameRelInitExtendet.extend("toName", NOMREF_INIT_STRATEGY.getPropertyPaths(), false);
            nameRelInitExtendet.extend("fromName", NOMREF_INIT_STRATEGY.getPropertyPaths(), false);
        }

        List<String> transtientNameRelationsInitstrategies = initStrategy.getPropertyPaths()
                .stream()
                .filter(s -> s.startsWith("nameRelations"))
                .collect(Collectors.toList());
        if(!transtientNameRelationsInitstrategies.isEmpty()){
            // nameRelations is a transient property! replace it by relationsFromThisName and relationsToThisName
            for(String remove : transtientNameRelationsInitstrategies) {
                initStrategy.getPropertyPaths().remove(remove);
            }
            initStrategy.extend("relationsFromThisName", nameRelInitExtendet.getPropertyPaths(), true);
            initStrategy.extend("relationsToThisName", nameRelInitExtendet.getPropertyPaths(), true);
        } else {
            if(pathProperties.stream().anyMatch(s -> s.startsWith("relationsFromThisName"))){
                initStrategy.getPropertyPaths().remove("relationsFromThisName"); // remove the very simple one
                initStrategy.extend("relationsFromThisName", nameRelInitExtendet.getPropertyPaths(), true);
            }
            if(pathProperties.stream().anyMatch(s -> s.startsWith("relationsToThisName"))){
                initStrategy.getPropertyPaths().remove("relationsToThisName"); // remove the very simple one
                initStrategy.extend("relationsToThisName", nameRelInitExtendet.getPropertyPaths(), true);
            }
        }
        return initStrategy;
    }

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
        return new ArrayList(RegistrableEntityFilter.newInstance(userHelper).filterPublishedOnly(p.getRecords()));
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
        return new ArrayList<>(RegistrableEntityFilter.newInstance(userHelper).filterPublishedOnly(result));
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
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
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
            Set<NameRelationship> nameRelationsFiltered = RegistrableEntityFilter.
                    newInstance(userHelper).filterPublishedOnly(tnb, nameRelations);
            return pageFromCollection(nameRelationsFiltered, pageIndex, pageSize, start, limit, response);
        }
        return null;
    }



}
