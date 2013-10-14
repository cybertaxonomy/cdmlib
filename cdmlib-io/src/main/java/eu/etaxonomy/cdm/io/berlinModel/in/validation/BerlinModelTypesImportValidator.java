// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in.validation;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.IOValidator;

/**
 * @author a.mueller
 * @created 17.02.2010
 */
public class BerlinModelTypesImportValidator implements IOValidator<BerlinModelImportState> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BerlinModelTypesImportValidator.class);

	@Override
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		System.out.println("Checking for Types not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
}
