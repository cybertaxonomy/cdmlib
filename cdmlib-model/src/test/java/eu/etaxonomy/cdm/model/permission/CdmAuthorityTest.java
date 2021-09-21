/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.permission;

import java.util.EnumSet;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;


/**
 * Testclass for {@link CdmAuthority}.
 *
 * @author a.mueller
 * @since 21.09.2021
 */
public class CdmAuthorityTest {

    @Test
    public void testToString(){
        CdmAuthority authority = CdmAuthority.NewInstance(PermissionClass.COLLECTION, "prop",
                EnumSet.allOf(CRUD.class), UUID.fromString("fcc80ea2-6185-4fa1-a2a0-8dedcce6f839"));

        Assert.assertEquals("COLLECTION.prop[CREATE, READ, UPDATE, DELETE]{fcc80ea2-6185-4fa1-a2a0-8dedcce6f839}", authority.toString());

        authority = CdmAuthority.NewInstance(null, null,
                null, null);
        Assert.assertEquals("-NO CLASS-[]", authority.toString());
    }

    @Test
    public void testIsEquals(){
        CdmAuthority authority = CdmAuthority.NewInstance(PermissionClass.COLLECTION, "prop",
                EnumSet.allOf(CRUD.class), UUID.fromString("fcc80ea2-6185-4fa1-a2a0-8dedcce6f839"));
        CdmAuthority authority2 = CdmAuthority.NewInstance(PermissionClass.COLLECTION, "xxx",
                EnumSet.allOf(CRUD.class), UUID.fromString("fcc80ea2-6185-4fa1-a2a0-8dedcce6f839"));
        Assert.assertFalse(authority.isEqual(authority2));
        authority2 = CdmAuthority.NewInstance(PermissionClass.COLLECTION, "prop",
                EnumSet.allOf(CRUD.class), UUID.fromString("fcc80ea2-6185-4fa1-a2a0-8dedcce6f839"));
        Assert.assertTrue(authority.isEqual(authority2));

        authority = CdmAuthority.NewInstance(null, "", null, null);
        authority2 = CdmAuthority.NewInstance(null, null, null, null);
        Assert.assertTrue(authority.isEqual(authority2));
        authority2 = CdmAuthority.NewInstance(null, null, EnumSet.noneOf(CRUD.class), null);
        Assert.assertTrue(authority.isEqual(authority2));
        authority2 = CdmAuthority.NewInstance(null, null, EnumSet.of(CRUD.CREATE), null);
        Assert.assertFalse(authority.isEqual(authority2));
    }

}