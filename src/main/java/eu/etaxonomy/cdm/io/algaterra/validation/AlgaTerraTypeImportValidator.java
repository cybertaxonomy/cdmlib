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
public class AlgaTerraTypeImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(AlgaTerraTypeImportValidator.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		result &= checkMultipleEntriesInTypeSpecimenDesignation(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		System.out.println("Checking for Specimen not yet fully implemented");
		return result;
	}
	
	
	//******************************** CHECK *************************************************
		
		private static boolean checkMultipleEntriesInTypeSpecimenDesignation(BerlinModelImportConfigurator config){
			try {
				boolean result = true;
				Source source = config.getSource();
				String strQuery = " SELECT COUNT(*) AS n, TypeSpecimenFk " +
							" FROM TypeSpecimenDesignation " +
							" GROUP BY TypeSpecimenFk " +
							" HAVING (COUNT(*) > 1)  ";

				ResultSet resulSet = source.getResultSet(strQuery);
				if (resulSet.next()){
					System.out.println("========================================================");
					System.out.println("There are multiple TypeSpecimenDesignations sharing the same TypeSpecimen!");
					System.out.println("This is not supported by the current import and will lead to duplicates!");
					System.out.println("========================================================");
					result = false;
				}

				strQuery = " SELECT COUNT(*) AS n, TypeDesignationFk " +
						" FROM TypeSpecimenDesignation " +
						" GROUP BY TypeDesignationFk " +
						" HAVING (COUNT(*) > 1)  ";

				resulSet = source.getResultSet(strQuery);
				if (resulSet.next()){
					System.out.println("========================================================");
					System.out.println("There are multiple TypeSpecimenDesignations sharing the same TypeDesignation!");
					System.out.println("This is not supported by the current import and will lead to duplicates!");
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
