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

import org.junit.Test;
import org.springframework.security.access.AccessDecisionVoter;

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;
import eu.etaxonomy.cdm.persistence.hibernate.permission.voter.DescriptionBaseVoter;

/**
 * @author a.kohlbecker
 * @since Feb 2, 2017
 *
 */
public class DescriptionBaseVoterTest extends AbstractCdmPermissionVoterTest {

    private DescriptionBaseVoter voter = new DescriptionBaseVoter();

    @Test
    public void test_U_C(){

        int vote = voter.vote(
                authentication(
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null),
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.CREATE), null)
                        ),
                TaxonDescription.NewInstance(),
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }

    @Test
    public void test_C_U(){
        int vote = voter.vote(
                authentication(
                        // reverse order
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.CREATE), null),
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null)
                        ),
                TaxonDescription.NewInstance(),
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);

    }

    @Test
    public void test_CU(){
        int vote = voter.vote(
                authentication(
                        // combined
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                TaxonDescription.NewInstance(),
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);

    }

    @Test
    public void test_UC(){
        int vote = voter.vote(
                authentication(
                        // combined reverse
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE, CRUD.CREATE), null)
                        ),
                TaxonDescription.NewInstance(),
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);

    }

    /**
     * For a not orphan TaxonDescription the voter must evaluate the CRUD properties
     */
    @Test
    public void test_CU_DENIED(){

        int vote = voter.vote(
                authentication(
                        // insufficient grants
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                // an not orphan TaxonDescription since it is associated with a taxon
                TaxonDescription.NewInstance(Taxon.NewInstance(null, null)),
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.DELETE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_DENIED, vote);
    }

    /**
     * Deletion of orphan objects is always allowed and insufficient CRUD operation will not
     * influence the result.
     */
    @Test
    public void test_CU_ALLOW_orphaned(){
        int vote = voter.vote(
                authentication(
                        // insufficient grants
                        new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                // an orphan TaxonDescription which has no taxon
                TaxonDescription.NewInstance(),
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.DELETE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }

    /**
     * If the classes do not match the voter will return the fallthrough vote which is ACCESS_DENIED.
     */
    @Test
    public void test_CU_DENIED_nonMatchingClass(){
        int vote = voter.vote(
                authentication(
                        // insufficient grants
                        new CdmAuthority(CdmPermissionClass.TAXONBASE, null, EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                TaxonDescription.NewInstance(),
                Arrays.asList(new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.DELETE), null)));
        assertEquals(AccessDecisionVoter.ACCESS_DENIED, vote);
    }


}
