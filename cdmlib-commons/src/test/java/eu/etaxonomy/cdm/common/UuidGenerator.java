// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.util.UUID;

import org.apache.log4j.Logger;

/**
 *
 * @author a.mueller
 */
public class UuidGenerator {
	private static final Logger logger = Logger.getLogger(UuidGenerator.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (logger.isDebugEnabled()){logger.debug("create UUIDs");}
		for (int i = 0; i < 100; i++){
			System.out.println(UUID.randomUUID());
		}

	}

}
