// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print.out.pdf;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;


/**
 * @author n.hoffmann
 * @created Jan 11, 2011
 * @version 1.0
 */
public class PdfOutputModuleTest {

	
	/**
	 * 
	 */
	@Test
	public void testGetXslt() {
		PdfOutputModule outputModule = new PdfOutputModule();
		
		assertNotNull(outputModule.getXslt());
	}
	
}
