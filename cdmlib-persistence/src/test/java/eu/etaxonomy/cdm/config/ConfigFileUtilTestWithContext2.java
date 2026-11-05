/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.config;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("configFileUtil-app-context.xml")
public class ConfigFileUtilTestWithContext2 {

    @Autowired
    ConfigFileUtil configFileUtil;

	@Test
    public void testGetHomeDir() {
        String userHome = System.getProperty("user.home");
        assertEquals(userHome + File.separator + ".cdmLibrary", configFileUtil.getCdmHomeDir().getAbsolutePath());
    }
}