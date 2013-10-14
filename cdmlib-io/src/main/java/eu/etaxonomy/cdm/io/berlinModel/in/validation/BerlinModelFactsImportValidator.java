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
 */
public class BerlinModelFactsImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelFactsImportValidator.class);

	@Override
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
			String strQuery = "SELECT f.factId, f.fact, fc.factCategoryId, fc.FactCategory, pt.StatusFk, n.FullNameCache, s.Status, pt.PTRefFk, r.RefCache  " +
					" FROM Fact f " +
						" INNER JOIN FactCategory fc ON fc.FactCategoryId = f.factCategoryFk " +
						" INNER JOIN PTaxon pt ON f.PTNameFk = pt.PTNameFk AND f.PTRefFk = pt.PTRefFk" +
						" INNER JOIN Name n ON pt.PTNameFk = n.NameId " +
		                " INNER JOIN Status s ON pt.StatusFk = s.StatusId " +
		                " LEFT OUTER JOIN Reference r ON pt.PTRefFk = r.RefId " + 
					" WHERE (pt.StatusFk NOT IN ( 1, 5))  ";
			
			if (StringUtils.isNotBlank(config.getFactFilter())){
				strQuery += String.format(" AND (%s) ", config.getFactFilter()) ; 
			}
			
			ResultSet resulSet = source.getResultSet(strQuery);
			boolean firstRow = true;
			while (resulSet.next()){
				if (firstRow){
					System.out.println("========================================================");
					System.out.println("There are facts for a taxon that is not accepted!");
					System.out.println("========================================================");
				}
				int factId = resulSet.getInt("FactId");
				String fact = resulSet.getString("Fact");
				String factCategory = resulSet.getString("FactCategory");
				int factCategoryId = resulSet.getInt("FactCategoryId");
				String status = resulSet.getString("Status");
				String fullNameCache = resulSet.getString("FullNameCache");
				String ptRefFk = resulSet.getString("PTRefFk");
				String ptRef = resulSet.getString("RefCache");
				
				System.out.println("FactId: " + factId + "\n  Fact: " + fact + 
						"\n  FactCategory: "  + factCategory + "\n  FactCategoryId: " + factCategoryId +
						"\n  Status: " + status + 
						"\n  FullNameCache: " + fullNameCache +  "\n  ptRefFk: " + ptRefFk +
						"\n  sec: " + ptRef );
				
				result = firstRow = false;
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}
}
