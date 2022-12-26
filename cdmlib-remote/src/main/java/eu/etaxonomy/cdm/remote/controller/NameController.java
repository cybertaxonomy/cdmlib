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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.EntityInitStrategy;
import eu.etaxonomy.cdm.remote.service.RegistrableEntityFilter;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
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

    private static final Logger logger = LogManager.getLogger();

    public static final EntityInitStrategy TYPEDESIGNATION_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "typeStatus.representations",
            "typifiedNames",
            "typeSpecimen",
            "typeName",
            "designationSource.citation",
            "designationSource.citation.authorship.$",
            "registrations", // needed for access control
            "text"
    }));

    public static final EntityInitStrategy FULL_TITLE_CACHE_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "$",
            "relationsFromThisName.$",
            "relationsToThisName.$",
            "status.$",
            "nomenclaturalSource.citation.authorship.$",
            "nomenclaturalSource.citation.inReference.authorship.$",
            "nomenclaturalSource.citation.inReference.inReference.authorship.$",
            "nomenclaturalSource.citation.inReference.inReference.inReference.authorship.$"
    }));

    public static final EntityInitStrategy NAME_RELATIONS_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "$",
            "source.citation",
            "relationsFromThisName.$",
            "relationsFromThisName.toName.registrations",
            "relationsToThisName.$",
            "relationsToThisName.fromName.registrations"
    }));

    public  static final EntityInitStrategy NAME_CACHE_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{

    }));

    public static final EntityInitStrategy NAME_REGISTRATIONS_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "registrations.typeDesignations.$",
            "registrations.institution"
    }));

    public NameController(){
        super();
        setInitializationStrategy(Arrays.asList(new String[]{"$"})); //TODO still needed????
    }

    @Autowired
    @Override
    public void setService(INameService service) {
        this.service = service;
    }


    @Override
    protected <CDM_BASE extends CdmBase> List<String> complementInitStrategy(Class<CDM_BASE> clazz,
            List<String> pathProperties) {

        if(pathProperties == null){
            return pathProperties;
        }

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

        return initStrategy.getPropertyPaths();
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
     */
    @RequestMapping(value = { "typeDesignations" }, method = RequestMethod.GET)
    public List<TypeDesignationBase> doGetTypeDesignations(
            @PathVariable("uuid") UUID uuid, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        if (request != null) {
            logger.info("doGetTypeDesignations()" + requestPathAndQuery(request));
        }
        TaxonName tnb = getCdmBaseInstance(uuid, response, (List<String>)null);
        if(tnb == null){
            return null;
        }
        Pager<TypeDesignationBase> pager = service.getTypeDesignations(tnb, null,
                null, null, TYPEDESIGNATION_INIT_STRATEGY.getPropertyPaths());
        return new ArrayList(RegistrableEntityFilter.newInstance(userHelper).filterPublishedOnly(pager.getRecords()));
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
                null, null, TYPEDESIGNATION_INIT_STRATEGY.getPropertyPaths());
        return new ArrayList(RegistrableEntityFilter.newInstance(userHelper).filterPublishedOnly(result));
    }


    @RequestMapping(
            value = {"nameCache"},
            method = RequestMethod.GET)
    public List<String> doGetNameCache(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {

        logger.info("doGetNameCache()" + requestPathAndQuery(request));
        TaxonName tnb = getCdmBaseInstance(uuid, response, NAME_CACHE_INIT_STRATEGY.getPropertyPaths());
        if(tnb == null){
            return null;
        }
        String nameCacheString = tnb.getNameCache();
        List<String> result = new ArrayList<>();
        result.add(nameCacheString);
        return result;

    }

    @RequestMapping(value = "taggedName", method = RequestMethod.GET)
    public List<TaggedText> doGetTaggedName(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetDescriptionElementsByType() - " + requestPathAndQuery(request));
        return service.getTaggedName(uuid);
    }

    @RequestMapping(value = "taggedFullTitle", method = RequestMethod.GET)
    public List<TaggedText> doGetTaggedFullTitle(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetTaggedFullTitle() - " + requestPathAndQuery(request));

        TaxonName name = service.load(uuid, FULL_TITLE_CACHE_INIT_STRATEGY.getPropertyPaths());
        return name.getTaggedFullTitle();
    }

    @RequestMapping(
            value = "registrations",
            method = RequestMethod.GET)
    public Set<Registration> doGetRegistrations(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {

        logger.info("doGetRegistrations" + requestPathAndQuery(request));
        TaxonName tnb = getCdmBaseInstance(uuid, response, NAME_REGISTRATIONS_INIT_STRATEGY.getPropertyPaths());
        Set<Registration> regs = tnb.getRegistrations();
        if(regs != null && regs.size() > 0){
            Set<Registration> regsFiltered = new HashSet<>(regs.size());
            for(Registration reg : regs){
                if(userHelper.userIsAutheticated() && reg.getStatus().equals(RegistrationStatus.PUBLISHED)) {
                    regsFiltered.add(reg);
                } else {
                    logger.debug("skipping unpublished registration");
                }
            }
            return regsFiltered;
        }
        return null;
    }

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
        TaxonName tnb = getCdmBaseInstance(uuid, response, NAME_RELATIONS_INIT_STRATEGY.getPropertyPaths());

        Set<NameRelationship> nameRelations = tnb.getNameRelations();

        if(nameRelations != null && nameRelations.size() > 0){
            Set<NameRelationship> nameRelationsFiltered = RegistrableEntityFilter.
                newInstance(userHelper).filterPublishedOnly(tnb, nameRelations);
            return pageFromCollection(nameRelationsFiltered, pageIndex, pageSize, start, limit, response);
        }
        return null;
    }

    /**
     * Provides the  „Protologue / original publication“ of the names nomenclatural reference.
     *
     */
    @RequestMapping(value = "protologueLinks", method = RequestMethod.GET)
    public Set<ExternalLink> doGetProtologueLinks(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetProtologueLinks() - " + requestPathAndQuery(request));
        TaxonName name = service.load(uuid, Arrays.asList("nomenclaturalSource.links"));
        if(name.getNomenclaturalSource() != null) {
            return name.getNomenclaturalSource().getLinks();
        }
        return null;
    }

}
