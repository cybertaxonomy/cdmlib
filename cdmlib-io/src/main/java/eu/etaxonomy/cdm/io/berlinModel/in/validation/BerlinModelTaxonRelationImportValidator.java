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
public class BerlinModelTaxonRelationImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonRelationImportValidator.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		logger.warn("Checking for TaxonRelations not yet fully implemented");
		result &= checkInActivatedStatus(bmiConfig);
		
		return result;
	}
	
	
	private boolean checkInActivatedStatus(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strSQL = 
				" SELECT RelPTaxon.RelPTaxonId, RelPTaxon.RelQualifierFk, FromName.FullNameCache AS FromName, RelPTaxon.PTNameFk1 AS FromNameID, "  +
		    			" Status.Status AS FromStatus, ToName.FullNameCache AS ToName, RelPTaxon.PTNameFk2 AS ToNameId, ToStatus.Status AS ToStatus, FromTaxon.DoubtfulFlag AS doubtfulFrom, ToTaxon.DoubtfulFlag AS doubtfulTo" + 
    			" FROM PTaxon AS FromTaxon " + 
    				" INNER JOIN RelPTaxon ON FromTaxon.PTNameFk = RelPTaxon.PTNameFk1 AND FromTaxon.PTRefFk = RelPTaxon.PTRefFk1 " + 
    				" INNER JOIN PTaxon AS ToTaxon ON RelPTaxon.PTNameFk2 = ToTaxon.PTNameFk AND RelPTaxon.PTRefFk2 = ToTaxon.PTRefFk " + 
    				" INNER JOIN Name AS ToName ON ToTaxon.PTNameFk = ToName.NameId " + 
    				" INNER JOIN Name AS FromName ON FromTaxon.PTNameFk = FromName.NameId " + 
    				" INNER JOIN Status ON FromTaxon.StatusFk = Status.StatusId AND FromTaxon.StatusFk = Status.StatusId " + 
    				" INNER JOIN Status AS ToStatus ON ToTaxon.StatusFk = ToStatus.StatusId AND ToTaxon.StatusFk = ToStatus.StatusId " +
				" WHERE (RelPTaxon.RelQualifierFk = - 99)";
			
			ResultSet rs = source.getResultSet(strSQL);
			boolean firstRow = true;
			int i = 0;
			while (rs.next()){
				i++;
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are TaxonRelationships with status 'inactivated'(-99)!");
					System.out.println("========================================================");
				}
				
				int relPTaxonId = rs.getInt("RelPTaxonId");
				String fromName = rs.getString("FromName");
				int fromNameID = rs.getInt("FromNameID");
				String fromStatus = rs.getString("FromStatus");
				
				String toName = rs.getString("ToName");
				int toNameId = rs.getInt("ToNameId");
				String toStatus = rs.getString("ToStatus");
				String doubtfulFrom = String.valueOf(rs.getObject("doubtfulFrom"));
				String doubtfulTo = String.valueOf(rs.getObject("doubtfulTo"));
				
				
				System.out.println("RelPTaxonId:" + relPTaxonId + 
						"\n  FromName: " + fromName + "\n  FromNameID: " + fromNameID + "\n  FromStatus: " + fromStatus + "\n  FromDoubtful: " + doubtfulFrom + 
						"\n  ToName: " + toName + "\n  ToNameId: " + toNameId + "\n  ToStatus: " + toStatus + "\n  ToDoubtful: " + doubtfulTo );
				result = firstRow = false;
			}
			if (i > 0){
				System.out.println(" ");
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * @param state
	 * @return
	 */
	private boolean checkRelPTaxonWithNotes(BerlinModelImportState state) {
		boolean success = true;
		try {
			
			Source source = state.getConfig().getSource();
			String strQuery = 
				"SELECT count(*) AS n FROM RelPTaxon " + 
				" WHERE (Notes IS NOT NULL) AND (RTRIM(LTRIM(Notes)) <> '')";
			ResultSet rs = source.getResultSet(strQuery);
			rs.next();
			int n;
			n = rs.getInt("n");
			if (n > 0){
				System.out.println("========================================================");
				logger.warn("There are " + n + " RelPTaxa with a note. Notes for RelPTaxa are not imported!");
				System.out.println("========================================================");
				success = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}


}
