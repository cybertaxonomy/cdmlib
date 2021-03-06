/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.config;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class ConfigFileUtilTest {


	@Test
    public void testGetHomeDirFallback() {
        String userHome = System.getProperty("user.home");
        Assert.assertEquals(userHome+File.separator+".cdmLibrary", ConfigFileUtil.getCdmHomeDirFallback().toString());
    }


}
