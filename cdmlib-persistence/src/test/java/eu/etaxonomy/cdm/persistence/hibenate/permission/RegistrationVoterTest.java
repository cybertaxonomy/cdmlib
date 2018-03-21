/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibenate.permission;

import java.util.Arrays;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;
import eu.etaxonomy.cdm.persistence.hibernate.permission.voter.RegistrationVoter;

/**
 * Most basic permission votes are tested via the {@link DescriptionBaseVoterTest}. The
 * test in here are mainly focused on testing the {@link RegistrationVoter#furtherVotingDescisions}
 * implementation.
 *
 * @author a.kohlbecker
 * @since 19.10.2017
 *
 */
public class RegistrationVoterTest extends AbstractCdmPermissionVoterTest {

    Registration regPREPARATION;
    Registration regREADY;
    Registration regREJECTED;

    String prep_ready = EnumSet.of(RegistrationStatus.PREPARATION,RegistrationStatus.READY).toString().replaceAll("[\\s\\]\\[]", "");
    String prep = EnumSet.of(RegistrationStatus.PREPARATION,RegistrationStatus.READY).toString().replaceAll("[\\s\\]\\[]", "");

    Authentication auth;

    RegistrationVoter voter = new RegistrationVoter();

    @Before
    public void setup() {

        regPREPARATION = Registration.NewInstance();
        regPREPARATION.setStatus(RegistrationStatus.PREPARATION);

        regREADY = Registration.NewInstance();
        regREADY.setStatus(RegistrationStatus.READY);

        regREJECTED = Registration.NewInstance();
        regREJECTED.setStatus(RegistrationStatus.REJECTED);

        auth = authentication(
                new CdmAuthority(regPREPARATION, prep_ready, EnumSet.of(CRUD.UPDATE)),
                new CdmAuthority(regREADY, prep_ready, EnumSet.of(CRUD.UPDATE)),
                new CdmAuthority(regREJECTED, prep_ready, EnumSet.of(CRUD.UPDATE))
                );

    }

    @Test
    public void test1() {
        int vote = voter.vote(auth,
                regPREPARATION,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.REGISTRATION, null, EnumSet.of(CRUD.UPDATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }


    @Test
    public void test2() {
        int vote = voter.vote(auth,
                regREADY,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.REGISTRATION, null, EnumSet.of(CRUD.UPDATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }


    @Test
    public void test3() {
        int vote = voter.vote(auth,
                regREJECTED,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.REGISTRATION, null, EnumSet.of(CRUD.UPDATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_DENIED, vote);
    }

    /**
     * see https://dev.e-taxonomy.eu/redmine/issues/7323
     */
    @Test
    public void issue7323() {

        Registration regGranted = Registration.NewInstance();
        regGranted.setStatus(RegistrationStatus.PREPARATION);

        Registration regRequired = Registration.NewInstance();
        regRequired.setStatus(RegistrationStatus.PREPARATION);


        Authentication auth = authentication(
                new CdmAuthority(regGranted, prep, EnumSet.of(CRUD.UPDATE))
                );
        int vote = voter.vote(auth,
                regRequired,
                // the attributes to test for
                Arrays.asList(new CdmAuthority(CdmPermissionClass.REGISTRATION, null, EnumSet.of(CRUD.UPDATE), regRequired.getUuid())));
        assertEquals(AccessDecisionVoter.ACCESS_DENIED, vote);

        vote = voter.vote(auth,
                regGranted,
                // the attributes to test for
                Arrays.asList(new CdmAuthority(CdmPermissionClass.REGISTRATION, null, EnumSet.of(CRUD.UPDATE), regGranted.getUuid())));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }

}
