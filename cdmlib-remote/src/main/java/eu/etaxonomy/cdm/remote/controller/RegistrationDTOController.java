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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IRegistrationService;
import eu.etaxonomy.cdm.api.service.dto.RegistrationDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.registration.IRegistrationWorkingSetService;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import io.swagger.annotations.Api;

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

    /**
     * @param identifier
     * @param validateUniqueness
     * @param response
     * @return
     * @throws IOException
     */
    protected Pager<Registration> pageByIdentifier(String identifier, boolean validateUniqueness,
            HttpServletResponse response) throws IOException {
        List<Restriction<?>> restrictions = new ArrayList<>();
        if( !userHelper.userIsAutheticated() || userHelper.userIsAnnonymous() ) {
            restrictions.add(new Restriction<>("status", null, RegistrationStatus.PUBLISHED));
        }

        Pager<Registration> regPager = service.pageByRestrictions(Registration.class, "identifier", identifier, MatchMode.EXACT, restrictions, 2, 0, null, getInitializationStrategy());

        if(validateUniqueness && regPager.getCount() > 1){
            HttpStatusMessage.create("The identifier " + identifier + " refrences multiple registrations", HttpServletResponse.SC_PRECONDITION_FAILED).send(response);
        }
        return regPager;
    }

}
