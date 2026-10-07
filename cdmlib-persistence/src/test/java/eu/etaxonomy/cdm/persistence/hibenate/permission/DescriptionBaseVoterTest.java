/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibenate.permission;

import java.util.EnumSet;

import org.junit.Test;

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.TargetEntityStates;
import eu.etaxonomy.cdm.persistence.permission.voter.DescriptionBaseVoter;
import eu.etaxonomy.cdm.persistence.permission.voter.CdmVote;

/**
 * @author a.kohlbecker
 * @since Feb 2, 2017
 */
public class DescriptionBaseVoterTest extends AbstractCdmPermissionVoterTest {

    private DescriptionBaseVoter voter = new DescriptionBaseVoter();

    @Test
    public void test_U_C(){

        CdmVote vote = voter.vote(
                authentication(
                        new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null),
                        new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.CREATE), null)
                        ),
                new TargetEntityStates(TaxonDescription.NewInstance()),
                new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null));
        assertEquals(CdmVote.GRANTED, vote);
    }

    @Test
    public void test_C_U(){
        CdmVote vote = voter.vote(
                authentication(
                        // reverse order
                        new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.CREATE), null),
                        new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null)
                        ),
                new TargetEntityStates(TaxonDescription.NewInstance()),
                new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null));
        assertEquals(CdmVote.GRANTED, vote);
    }

    @Test
    public void test_CU(){
        CdmVote vote = voter.vote(
                authentication(
                        // combined
                        new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                new TargetEntityStates(TaxonDescription.NewInstance()),
                new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null));
        assertEquals(CdmVote.GRANTED, vote);
    }

    @Test
    public void test_UC(){
        CdmVote vote = voter.vote(
                authentication(
                        // combined reverse
                        new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE, CRUD.CREATE), null)
                        ),
                new TargetEntityStates(TaxonDescription.NewInstance()),
                new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.UPDATE), null));
        assertEquals(CdmVote.GRANTED, vote);
    }

    /**
     * For a not orphan TaxonDescription the voter must evaluate the CRUD properties
     */
    @Test
    public void test_CU_DENIED(){

        CdmVote vote = voter.vote(
                authentication(
                        // insufficient grants
                        new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                // an not orphan TaxonDescription since it is associated with a taxon
                new TargetEntityStates(TaxonDescription.NewInstance(Taxon.NewInstance(null, null))),
                new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.DELETE), null));
        assertEquals(CdmVote.DENIED, vote);
    }

    /**
     * Deletion of orphan objects is always allowed and insufficient CRUD operation will not
     * influence the result.
     */
    @Test
    public void test_CU_ALLOW_orphaned(){
        CdmVote vote = voter.vote(
                authentication(
                        // insufficient grants
                        new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                // an orphan TaxonDescription which has no taxon
                new TargetEntityStates(TaxonDescription.NewInstance()),
                new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.DELETE), null));
        assertEquals(CdmVote.GRANTED, vote);
    }

    /**
     * If the classes do not match the voter will return the fallthrough vote which is ACCESS_DENIED.
     */
    @Test
    public void test_CU_DENIED_nonMatchingClass(){
        CdmVote vote = voter.vote(
                authentication(
                        // insufficient grants
                        new CdmAuthority(PermissionClass.TAXONBASE, null, EnumSet.of(CRUD.CREATE, CRUD.UPDATE), null)
                        ),
                new TargetEntityStates(TaxonDescription.NewInstance()),
                new CdmAuthority(PermissionClass.DESCRIPTIONBASE, null, EnumSet.of(CRUD.DELETE), null));
        assertEquals(CdmVote.DENIED, vote);
    }
}
