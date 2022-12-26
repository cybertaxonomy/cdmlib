/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.function;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.persistence.utils.CdmPersistenceUtils;

/**
 * @author a.mueller
 * @since 20.11.2008
 */
public class TestCdmApplicationUtils {

    private static final Logger logger = LogManager.getLogger();

	private boolean testWritableResourceDirectory() throws IOException{
		CdmPersistenceUtils.getWritableResourceDir();
		return true;
	}

	public static void main(String[] args) {
		TestCdmApplicationUtils me = new TestCdmApplicationUtils();
		try {
			me.testWritableResourceDirectory();
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
}