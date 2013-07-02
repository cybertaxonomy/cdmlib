// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.algaterra.validation;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.mueller
 * @created 26.02.2013
 */
public class AlgaTerraDnaImportValidator implements IOValidator<BerlinModelImportState> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AlgaTerraDnaImportValidator.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		result &= checkDnaFactWithoutFact(state);
		result &= checkDnaFactsWithMultipleFacts(state);
		return result;
	}
	
	
	//******************************** CHECK *************************************************
		
		private static boolean checkDnaFactWithoutFact(BerlinModelImportState state){
			try {
				boolean result = true;
				Source source = state.getConfig().getSource();
				String strQuery = " SELECT count(*) as n FROM DNAFact " +
							" WHERE (DNAFactID NOT IN " +
		                      "  (SELECT   ExtensionFk " +
		                      "      FROM   dbo.Fact AS f " +
		                      "     WHERE      (FactCategoryFk = 203))) " ; 
	
				ResultSet rs = source.getResultSet(strQuery);
				
				rs.next();
				int n = rs.getInt("n");
				if (n > 0){
					System.out.println("========================================================");
					System.out.println("There " + n + " are DNAFacts with no facts referencing them!");
					System.out.println("   SQL: " + strQuery);
					System.out.println("========================================================");
					result = false;
				}
				
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		
		private static boolean checkDnaFactsWithMultipleFacts(BerlinModelImportState state){
			try {
				boolean result = true;
				Source source = state.getConfig().getSource();
				String strQuery = " SELECT count(*) as n FROM (" + 
										" SELECT ExtensionFk, COUNT(*) AS n " +
										" FROM Fact AS f  " +
										" WHERE  (FactCategoryFk = 203) " +
										" GROUP BY ExtensionFk " + 
										" HAVING (COUNT(*) > 1) " +
									") as tmp ";
				
				ResultSet rs = source.getResultSet(strQuery);
				
				rs.next();
				int n = rs.getInt("n");
				if (n > 0){
					System.out.println("========================================================");
					System.out.println("There " + n + " are DNAFacts with more then 1 fact referencing them!");
					System.out.println("   SQL: " + strQuery);
					System.out.println("========================================================");
					result = false;
				}
				
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}


}
