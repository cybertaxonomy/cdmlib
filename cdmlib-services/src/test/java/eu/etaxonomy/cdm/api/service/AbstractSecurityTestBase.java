// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.UUID;

import org.junit.Before;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import eu.etaxonomy.cdm.config.Configuration;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTestWithSecurity;

/**
 * @author a.kohlbecker
 * @date Feb 4, 2014
 *
 */
public abstract class AbstractSecurityTestBase extends CdmTransactionalIntegrationTestWithSecurity {

    protected static final UUID PART_EDITOR_UUID = UUID.fromString("38a251bd-0ba4-426f-8fcb-5c09560749a7");

    protected static final UUID TAXON_EDITOR_UUID = UUID.fromString("56eac992-67ba-40be-896c-4e992ca2afc0");

    protected static final UUID GROUP_SPECIAL_EDITOR_UUID = UUID.fromString("8a423129-6d88-41cc-a1da-96bca45f5838");

    protected static final String PASSWORD_TAXON_EDITOR = "test2";

    protected static final String PASSWORD_ADMIN = "sPePhAz6";

    protected UsernamePasswordAuthenticationToken tokenForAdmin;

    protected UsernamePasswordAuthenticationToken tokenForTaxonEditor;

    protected UsernamePasswordAuthenticationToken tokenForDescriptionEditor;

    protected UsernamePasswordAuthenticationToken tokenForPartEditor;

    protected UsernamePasswordAuthenticationToken tokenForTaxonomist;

    protected UsernamePasswordAuthenticationToken tokenForUserManager;


    @Before
    public void setUp(){
        /* User 'admin':
            - ROLE_ADMIN
            - TAXONBASE.[READ]
            - TAXONBASE.[CREATE]
            - TAXONBASE.[DELETE]
            - TAXONBASE.[UPDATE]
        */
        tokenForAdmin = new UsernamePasswordAuthenticationToken(Configuration.adminLogin, PASSWORD_ADMIN);

        /* User 'userManager':
            - ROLE_ADMIN
            - TAXONBASE.[READ]
            - TAXONBASE.[CREATE]
            - TAXONBASE.[DELETE]
            - TAXONBASE.[UPDATE]
        */
        tokenForUserManager = new UsernamePasswordAuthenticationToken("userManager", PASSWORD_ADMIN);

        /* User 'taxonEditor':
            - TAXONBASE.[CREATE]
            - TAXONBASE.[UPDATE]

        */
        tokenForTaxonEditor = new UsernamePasswordAuthenticationToken("taxonEditor", PASSWORD_TAXON_EDITOR);

        /*  User 'descriptionEditor':
            - DESCRIPTIONBASE.[CREATE]
            - DESCRIPTIONBASE.[UPDATE]
            - DESCRIPTIONELEMENT(Ecology).[CREATE]
            - DESCRIPTIONELEMENT(Ecology).[UPDATE]
            - Groups :
               - "SpecialEditors"
         */
        tokenForDescriptionEditor = new UsernamePasswordAuthenticationToken("descriptionEditor", "test");

        /* User 'partEditor':
            - TAXONBASE.[ADMIN]
            - TAXONNODE.[UPDATE,CREATE,DELETE,READ,UPDATE]{20c8f083-5870-4cbd-bf56-c5b2b98ab6a7}
            - DESCRIPTIONELEMENTBASE.[CREATE,DELETE,READ,UPDATE]
            - DESCRIPTIONBASE.[CREATE,DELETE,READ,UPDATE]
         */
        tokenForPartEditor = new UsernamePasswordAuthenticationToken("partEditor", "test4");

        /* User 'taxonomist':
            - TAXONBASE.[READ]
            - TAXONBASE.[CREATE]
            - TAXONBASE.[DELETE]
            - TAXONBASE.[UPDATE]
            - DESCRIPTIONELEMENTBASE.[CREATE,DELETE,READ,UPDATE]
            - DESCRIPTIONBASE.[CREATE,DELETE,READ,UPDATE]
            - ROLE_PUBLISH

         */
        tokenForTaxonomist = new UsernamePasswordAuthenticationToken("taxonomist", "test4");
    }


}
