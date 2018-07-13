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
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IRegistrationService;
import eu.etaxonomy.cdm.api.service.dto.RegistrationDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.registration.IRegistrationWorkingSetService;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
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
@RequestMapping(value = {"/registrationDTO"})
public class RegistrationDTOController extends AbstractController<Registration, IRegistrationService>
{

    public static final Logger logger = Logger.getLogger(RegistrationDTOController.class);

    public RegistrationDTOController(){
        setInitializationStrategy(Arrays.asList(new String[]{
                "$",
                "name.$",
                "typeDesignations.$"
             }));
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
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
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
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
    @RequestMapping(value="identifier/**", method = RequestMethod.GET)
    public RegistrationDTO doGetByIdentifier(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetByIdentifier() " + requestPathAndQuery(request));

        String identifier = readPathParameter(request, "/registrationDTO/identifier/");

        Pager<RegistrationDTO> regPager = registrationWorkingSetService.pageDTOs(identifier, 0, 2);

        if(regPager.getCount() > 0){
            return regPager.getRecords().get(0);
        } else {
            return null;
        }
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "identifier", value = "The persitent identifier of the Registration.", required = true, dataType = "string", paramType = "path"),
    })
    @ApiOperation(value = "Finds Registrations by persitent identifier."
    )
    @RequestMapping(value="identifier/**", method = RequestMethod.GET, params={"validateUniqueness"})
    public Pager<RegistrationDTO> doPageByIdentifier(
            @RequestParam(value = "pageNumber", required=true) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doPageByIdentifier() " + requestPathAndQuery(request));

        String identifier = readPathParameter(request, "/registrationDTO/identifier/");

        Pager<RegistrationDTO> regDTOPager = registrationWorkingSetService.pageDTOs(identifier, pageIndex, pageSize);

        return regDTOPager;
    }

    @RequestMapping(value="find", method = RequestMethod.GET)
    public Pager<RegistrationDTO> doFind(
            @RequestParam(value = "submitterUuid", required=false) UUID submitterUuid,
            @RequestParam(value = "status", required=false) RegistrationStatusList status,
            @RequestParam(value = "typeDesignationStatusUuids", required=false) UuidList typeDesignationStatusUuids,
            @RequestParam(value = "identifierFilterPattern", required=false) String identifierFilterPattern,
            @RequestParam(value = "taxonNameFilterPattern", required=false) String taxonNameFilterPattern,
            @RequestParam(value = "pageNumber", required=false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doFind() " + requestPathAndQuery(request));

        Collection<RegistrationStatus> statusSet = null;
        if(status != null){
            statusSet = status.asSet();
        }
        Pager<RegistrationDTO> pager = registrationWorkingSetService.pageDTOs(submitterUuid, statusSet,
                identifierFilterPattern, taxonNameFilterPattern, typeDesignationStatusUuids,
                pageSize, pageIndex, null);
        return pager;
    }

}
