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

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;
import eu.etaxonomy.cdm.persistence.hibernate.permission.voter.DescriptionElementVoter;

/**
 * Most basic permission votes are tested via the {@link DescriptionBaseVoterTest}. The
 * test in here are mainly focused on testing the {@link DescriptionElementVoter#furtherVotingDescisions}
 * implementation.
 *
 * @author a.kohlbecker
 * @since Feb 2, 2017
 *
 */
public class DescriptionElementVoterTest extends AbstractCdmPermissionVoterTest {

    private DescriptionElementVoter voter = new DescriptionElementVoter();

    private DescriptionElementBase textDataEco = null;
    private Feature ecology = Feature.NewInstance(null, "ecology", null);
    private Feature morphology = Feature.NewInstance(null, "morphology", null);

    @Before
    public void setup() {
        textDataEco = new TextData(ecology);
    }

    @Test
    public void test_U_C_ecology(){

        int vote = voter.vote(
                authentication(
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, ecology.getLabel(), EnumSet.of(CRUD.UPDATE), null),
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, ecology.getLabel(), EnumSet.of(CRUD.CREATE), null)
                        ),
                textDataEco,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, null, EnumSet.of(CRUD.CREATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }

    @Test
    public void test_C_U_ecology(){
        int vote = voter.vote(
                authentication(
                        // reverse order
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, ecology.getLabel(), EnumSet.of(CRUD.CREATE), null),
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, ecology.getLabel(), EnumSet.of(CRUD.UPDATE), null)
                        ),
                textDataEco,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, null, EnumSet.of(CRUD.CREATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }

    @Test
    public void test_CU_ecology(){
        int vote = voter.vote(
                authentication(
                        // combined
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, ecology.getLabel(), EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                textDataEco,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, null, EnumSet.of(CRUD.CREATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }

    @Test
    public void test_UC_ecology(){
        int vote = voter.vote(
                authentication(
                        // combined
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, ecology.getLabel(), EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                textDataEco,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, null, EnumSet.of(CRUD.CREATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }

    @Test
    public void test_UC_morphology_DENIED(){
        int vote = voter.vote(
                authentication(
                        // combined
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, morphology.getLabel(), EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                textDataEco,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, null, EnumSet.of(CRUD.CREATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_DENIED, vote);
    }

    @Test
    public void test_UC_noFeature_DENIED(){
        int vote = voter.vote(
                authentication(
                        // combined
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, ecology.getLabel(), EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                new TextData(),
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, null, EnumSet.of(CRUD.CREATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_DENIED, vote);
    }

    @Test
    public void test_UC_noMatchingClass_DENIED(){
        int vote = voter.vote(
                authentication(
                        // combined
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, ecology.getLabel(), EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                new TextData(),
                Arrays.asList(new CdmAuthority(CdmPermissionClass.TAXONBASE, null, EnumSet.of(CRUD.CREATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_DENIED, vote);
    }

    @Test
    public void test_RU_ecology(){
        int vote = voter.vote(
                authentication(
                        // combined
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, ecology.getLabel(), EnumSet.of(CRUD.READ, CRUD.UPDATE), null)
                        ),
                textDataEco,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, null, EnumSet.of(CRUD.CREATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_DENIED, vote);
    }

}
