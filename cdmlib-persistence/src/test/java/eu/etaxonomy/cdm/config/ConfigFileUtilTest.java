/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.config;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.common.CdmUtils;


public class ConfigFileUtilTest {

	@Test
	public void testGetReadableResourceStream() {
		String resourceFileName = ConfigFileUtil.MUST_EXIST_FILE;
		try {
			InputStream inputStream = CdmUtils.getReadableResourceStream(resourceFileName);
			assertNotNull(inputStream);
		} catch (IOException e) {
			Assert.fail("IOException");
		}
	}

	@Test
	public void testGetFolderSeperator() {
		Assert.assertEquals(File.separator, ConfigFileUtil.getFolderSeperator());
	}

	@Test
	@Ignore
	public void testGetHomeDir() {
		Assert.assertEquals("", ConfigFileUtil.getCdmHomeDir());
	}


}
