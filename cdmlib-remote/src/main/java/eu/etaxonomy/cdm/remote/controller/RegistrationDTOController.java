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
import java.util.Collection;
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
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IRegistrationService;
import eu.etaxonomy.cdm.api.service.dto.RegistrationDTO;
import eu.etaxonomy.cdm.api.service.dto.RegistrationWorkingSet;
import eu.etaxonomy.cdm.api.service.exception.RegistrationValidationException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.registration.IRegistrationWorkingSetService;
import eu.etaxonomy.cdm.database.PermissionDeniedException;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import eu.etaxonomy.cdm.remote.editor.term.RegistrationStatusList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */

@Controller
@Api("registration")
public class RegistrationDTOController
            extends AbstractController<Registration, IRegistrationService>{

    private static final List<OrderHint> ORDER_BY_DATE_AND_ID = Arrays.asList(
            new OrderHint("registrationDate", SortOrder.DESCENDING),
            new OrderHint("specificIdentifier", SortOrder.DESCENDING)
            );

    private static final List<OrderHint> ORDER_BY_SUMMARY = Arrays.asList(new OrderHint("summary", SortOrder.ASCENDING));

    public static final Logger logger = Logger.getLogger(RegistrationDTOController.class);

    public RegistrationDTOController(){
        setInitializationStrategy(Arrays.asList(new String[]{
                "$",
                "name.$",
                "typeDesignations.$"
             }));
    }

    @Autowired
    @Override
    public void setService(IRegistrationService service) {
        this.service = service;
    }

    @Autowired
    private IRegistrationWorkingSetService registrationWorkingSetService;

    @Autowired
    RegistrationController registrationController;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor("NULL"));
        binder.registerCustomEditor(RegistrationStatusList.class, new RegistrationStatusList().propertyEditor());
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "identifier", value = "The persitent identifier of the Registration.", required = true, dataType = "string", paramType = "path"),
    })
    @ApiOperation(value = "Finds Registration by persitent identifier.",
        notes = "The identifier passed as paramter must be unique in the database otherwise the server will responde with the HTTP error code: " + HttpServletResponse.SC_PRECONDITION_FAILED,
        response = Registration.class
    )
    @RequestMapping(value="/registrationDTO", method = RequestMethod.GET, params={"identifier"})
    public RegistrationDTO doGetByIdentifier(
            @RequestParam(value = "identifier", required = true) String identifier,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetByIdentifier() " + requestPathAndQuery(request));
        Pager<RegistrationDTO> registrationDTOsPager = pageRegistrationDTOs(identifier, true, 0, 2, response);
        if(registrationDTOsPager == null) {
            HttpStatusMessage.create("No registration found for " + identifier + " ", HttpServletResponse.SC_NOT_FOUND).send(response);
            return null;
        } else {
            return registrationDTOsPager.getRecords().get(0);
        }
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "identifier", value = "The persitent identifier of the Registration.", required = true, dataType = "string", paramType = "path"),
    })
    @ApiOperation(value = "Finds Registrations by persistent identifier."
    )
    @RequestMapping(value="/registrationDTO", method = RequestMethod.GET, params={"identifier", "validateUniqueness"})
    public Pager<RegistrationDTO> doPageByIdentifier(
            @RequestParam(value = "identifier", required = true) String identifier,
            @RequestParam(value = "validateUniqueness") boolean validateUniqueness,
            @RequestParam(value = "pageNumber", required=true) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doPageByIdentifier() " + requestPathAndQuery(request));

        return pageRegistrationDTOs(identifier, validateUniqueness, pageIndex, pageSize, response);

    }

    protected Pager<RegistrationDTO> pageRegistrationDTOs(String identifier, boolean validateUniqueness, Integer pageIndex, Integer pageSize, HttpServletResponse response) throws IOException {

        Pager<RegistrationDTO> regPager = registrationWorkingSetService.pageDTOs(identifier, pageIndex, pageSize);

        if(regPager.getCount() == 1){
            return regPager;
        } else if(regPager.getCount() > 1){
            if(validateUniqueness) {
                HttpStatusMessage.create("The identifier " + identifier + " refrences multiple registrations", HttpServletResponse.SC_PRECONDITION_FAILED).send(response);
                return null; // never reached, due to previous send()
            } else {
                return regPager;
            }
        } else {
            return null;
        }
    }

    @RequestMapping(value="/registrationDTO/find", method = RequestMethod.GET)
    public Pager<RegistrationDTO> doFind(
            @RequestParam(value = "submitterUuid", required=false) UUID submitterUuid,
            @RequestParam(value = "status", required=false) RegistrationStatusList status,
            @RequestParam(value = "typeDesignationStatusUuids", required=false) UuidList typeDesignationStatusUuids,
            @RequestParam(value = "identifierFilterPattern", required=false) String identifierFilterPattern,
            @RequestParam(value = "taxonNameFilterPattern", required=false) String taxonNameFilterPattern,
            @RequestParam(value = "referenceFilterPattern", required=false) String referenceFilterPattern,
            @RequestParam(value = "pageNumber", required=false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response) {

        logger.info("doFind() " + requestPathAndQuery(request));

        Collection<RegistrationStatus> statusSet = null;
        if(status != null){
            statusSet = status.asSet();
        }
        Pager<RegistrationDTO> pager = registrationWorkingSetService.pageDTOs(submitterUuid, statusSet,
                identifierFilterPattern, taxonNameFilterPattern, referenceFilterPattern,
                typeDesignationStatusUuids, pageSize, pageIndex, ORDER_BY_DATE_AND_ID);
        return pager;
    }


    @RequestMapping(value="/registrationWorkingSetDTO/{reference_uuid}", method = RequestMethod.GET)
    public RegistrationWorkingSet doGetRegistrationWorkingSet(
            @PathVariable("reference_uuid") UUID referenceUuid,
            HttpServletRequest request,
            HttpServletResponse response) throws RegistrationValidationException {

        logger.info("doGetRegistrationWorkingSet() " + requestPathAndQuery(request));

        RegistrationWorkingSet workingset = registrationWorkingSetService.loadWorkingSetByReferenceUuid(referenceUuid, true);

        return workingset;
    }


    @RequestMapping(value="/registrationDTO/findInTaxonGraph", method = RequestMethod.GET)
    public Pager<RegistrationDTO> doPageByTaxomicInclusion(
            @RequestParam(value = "taxonNameFilter", required = true) String taxonNameFilterPattern,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            @RequestParam(value = "pageNumber", required = false, defaultValue="0") Integer pageIndex,
            @RequestParam(value = "pageSize", required = false, defaultValue="30" /*AbstractController.DEFAULT_PAGE_SIZE_VALUE*/ ) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) {

        logger.info("findInTaxonGraph() " + requestPathAndQuery(request));

        Collection<RegistrationStatus> includedStatus = Arrays.asList(RegistrationStatus.PUBLISHED);

        Pager<RegistrationDTO> regPager = registrationWorkingSetService.findInTaxonGraph(null, includedStatus,
                taxonNameFilterPattern, matchMode,
                pageSize, pageIndex, ORDER_BY_DATE_AND_ID);

        return regPager;
    }

    @RequestMapping(value="/registrationDTO", method = RequestMethod.GET, params="nameUuid")
    public Pager<RegistrationDTO> doGetByNameUUID(
            @RequestParam(value = "submitterUuid", required=false) UUID submitterUuid,
            @RequestParam(value = "status", required=false) RegistrationStatusList status,
            @RequestParam(value = "nameUuid", required=true) Collection<UUID> nameUuids,
            @RequestParam(value = "pageNumber", required=false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws PermissionDeniedException, RegistrationValidationException {

        logger.info("doGetByNameUUID() " + requestPathAndQuery(request));

        Collection<RegistrationStatus> statusSet = null;
        if(status != null){
            statusSet = status.asSet();
        }
        Pager<RegistrationDTO> pager = registrationWorkingSetService.pageWorkingSetsByNameUUID(
                nameUuids, pageSize, pageIndex, ORDER_BY_DATE_AND_ID);
        return pager;
    }
}