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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.mueller
 * @created 17.02.2010
 * @version 1.0
 */
public class BerlinModelFactsImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelFactsImportValidator.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		logger.warn("Checking for Facts not yet fully implemented");
		result &= checkDesignationRefsExist(bmiConfig);
		return result;
	}
	
	
	private boolean checkDesignationRefsExist(BerlinModelImportConfigurator config){
		try {
			boolean result = true;
			Source source = config.getSource();
			String strQueryArticlesWithoutJournal = "SELECT Count(*) as n " +
					" FROM Fact " +
					" WHERE (NOT (PTDesignationRefFk IS NULL) ) OR " +
                      " (NOT (PTDesignationRefDetailFk IS NULL) )";
			ResultSet rs = source.getResultSet(strQueryArticlesWithoutJournal);
			rs.next();
			int count = rs.getInt("n");
			if (count > 0){
				System.out.println("========================================================");
				logger.warn("There are "+count+" Facts with not empty designation references. Designation references are not imported.");
				
				System.out.println("========================================================");
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}
}
