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
import org.springframework.test.context.TestPropertySource;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("minimum-test-app-context.xml")
@TestPropertySource(properties={"user.home=/home/oldshatterhand"})
public class ConfigFileUtilTestWithContext1 {

    @Autowired
    ConfigFileUtil configFileUtil;

	@Test
    public void testGetHomeDir() {
        assertEquals("/home/oldshatterhand" + File.separator + ".cdmLibrary", configFileUtil.getCdmHomeDir().getAbsolutePath());
    }
}