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

    protected static final UUID UUID_ACHERONTINII = UUID.fromString("928a0167-98cd-4555-bf72-52116d067625");

    protected static final UUID UUID_ACHERONTIA_STYX = UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331");

    protected static final UUID UUID_LACTUCA = UUID.fromString("b2b007a4-9c8c-43a1-8da4-20ed85464cf2");

    protected static final UUID ACHERONTIA_NODE_UUID = UUID.fromString("20c8f083-5870-4cbd-bf56-c5b2b98ab6a7");

    protected static final UUID ACHERONTIINI_NODE_UUID = UUID.fromString("cecfa77f-f26a-4476-9d87-a8d993cb55d9");

    protected static final UUID ACHERONTIA_LACHESIS_NODE_UUID = UUID.fromString("0b5846e5-b8d2-4ca9-ac51-099286ea4adc");

    protected static final UUID ACHERONTIA_STYX_NODE_UUID = UUID.fromString("61b1dcae-8aa6-478a-bcd6-080cf0eb6ad7");

    protected static final UUID ACHERONTIA_LACHESIS_UUID = UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783");

    protected static final UUID BOOK1_UUID = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");

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
            - TAXONNAME.[CREATE,READ]
            - REFERENCE.[CREATE,READ]

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
            - TAXONNAME.[CREATE,READ]
            - REFERENCE.[CREATE,READ]
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
            - TAXONNAME.[CREATE,READ]
            - REFERENCE.[CREATE,READ]

         */
        tokenForTaxonomist = new UsernamePasswordAuthenticationToken("taxonomist", "test4");
    }


}
