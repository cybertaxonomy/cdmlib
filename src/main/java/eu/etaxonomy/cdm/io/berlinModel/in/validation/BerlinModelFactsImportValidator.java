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

import org.apache.commons.lang.StringUtils;
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
		result &= checkDesignationRefsExist(state);
		result &= checkFactsForSynonyms(state);
		return result;
	}
	
	
	private boolean checkDesignationRefsExist(BerlinModelImportState state){
		try {
			boolean result = true;
			BerlinModelImportConfigurator config = state.getConfig();
			Source source = state.getConfig().getSource();
			String strQuery = "SELECT Count(*) as n " +
					" FROM Fact " +
					" WHERE (NOT (PTDesignationRefFk IS NULL) ) OR " +
                      " (NOT (PTDesignationRefDetailFk IS NULL) )";
			
			if (StringUtils.isNotBlank(config.getFactFilter())){
				strQuery += String.format(" AND (%s) ", config.getFactFilter()) ; 
			}
			
			ResultSet rs = source.getResultSet(strQuery);
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
	
	private boolean checkFactsForSynonyms(BerlinModelImportState state){
		try {
			boolean result = true;
			BerlinModelImportConfigurator config = state.getConfig();
			Source source = config.getSource();
			String strQuery = "SELECT Count(*) as n " +
					" FROM Fact " +
						"INNER JOIN PTaxon ON Fact.PTNameFk = PTaxon.PTNameFk AND Fact.PTRefFk = PTaxon.PTRefFk" +
					" WHERE PTaxon.StatusFk IN (2, 3, 4) ";
			
			if (StringUtils.isNotBlank(config.getFactFilter())){
				strQuery += String.format(" AND (%s) ", config.getFactFilter()) ; 
			}
			
			ResultSet rs = source.getResultSet(strQuery);
			rs.next();
			int count = rs.getInt("n");
			if (count > 0){
				System.out.println("========================================================");
				logger.warn("There are "+count+" Facts with attached synonyms.");
				
				System.out.println("========================================================");
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}
}
