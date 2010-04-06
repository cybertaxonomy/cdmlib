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
public class BerlinModelOccurrenceImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelOccurrenceImportValidator.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		result &= checkTaxonIsAccepted(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		logger.warn("Checking for Occurrence not yet fully implemented");
		return result;
	}
	
	
	//******************************** CHECK *************************************************
		
		private static boolean checkTaxonIsAccepted(BerlinModelImportConfigurator bmiConfig){
			try {
				boolean result = true;
				Source source = bmiConfig.getSource();
				String strQuery = "SELECT emOccurrence.OccurrenceId, PTaxon.StatusFk, Name.FullNameCache, Status.Status, PTaxon.PTRefFk, Reference.RefCache " + 
							" FROM emOccurrence INNER JOIN " +
								" PTaxon ON emOccurrence.PTNameFk = PTaxon.PTNameFk AND emOccurrence.PTRefFk = PTaxon.PTRefFk INNER JOIN " + 
				                " Name ON PTaxon.PTNameFk = Name.NameId INNER JOIN " +
				                " Status ON PTaxon.StatusFk = Status.StatusId LEFT OUTER JOIN " +
				                " Reference ON PTaxon.PTRefFk = Reference.RefId " + 
							" WHERE (PTaxon.StatusFk <> 1)  ";
				
				ResultSet resulSet = source.getResultSet(strQuery);
				boolean firstRow = true;
				while (resulSet.next()){
					if (firstRow){
						System.out.println("========================================================");
						logger.warn("There are Occurrences for a taxon that is not accepted!");
						System.out.println("========================================================");
					}
					int occurrenceId = resulSet.getInt("OccurrenceId");
					int statusFk = resulSet.getInt("StatusFk");
					String status = resulSet.getString("Status");
					String fullNameCache = resulSet.getString("FullNameCache");
					String ptRefFk = resulSet.getString("PTRefFk");
					String ptRef = resulSet.getString("RefCache");
					
					System.out.println("OccurrenceId:" + occurrenceId + "\n  Status: " + status + 
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
