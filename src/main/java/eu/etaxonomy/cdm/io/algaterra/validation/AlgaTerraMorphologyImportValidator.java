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

import eu.etaxonomy.cdm.io.algaterra.AlgaTerraImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.mueller
 * @created 17.02.2010
 * @version 1.0
 */
public class AlgaTerraMorphologyImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(AlgaTerraMorphologyImportValidator.class);


	@Override
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		AlgaTerraImportConfigurator config = (AlgaTerraImportConfigurator)state.getConfig();
		result &= checkMissingCultureStrains(config);
		//result &= checkPartOfJournal(bmiConfig);
		System.out.println("Checking for Morphology not yet implemented");
		return result;
	}
	
	
	//******************************** CHECK *************************************************
		
		private static boolean checkMissingCultureStrains(AlgaTerraImportConfigurator config){
			try {
				boolean result = true;
				Source source = config.getSource();
				String strQuery = "SELECT mf.morphoFactId, mf.CultureStrainNo, eco.ecoFactId " +
						" FROM MorphoFact mf LEFT JOIN EcoFact eco ON eco.CultureStrain  = mf.CultureStrainNo " +
						" WHERE CultureStrainNo IS NOT NULL AND ecoFactId IS NULL " +
						" ORDER BY MorphoFactId ";

				ResultSet resulSet = source.getResultSet(strQuery);
				boolean firstRow = true;
				while (resulSet.next()){
					if (firstRow){
						System.out.println("========================================================");
						System.out.println("There are MorphoFacts with no matching EcoFacts!");
						System.out.println("========================================================");
					}
					int morphoFactId = resulSet.getInt("morphoFactId");
					String cultureStrain = resulSet.getString("CultureStrainNo");
					
					System.out.println("MorphoFactId:" + morphoFactId + "\n  cultureStrainNo: " + cultureStrain);
					result = firstRow = false;
				}
				
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}

}
