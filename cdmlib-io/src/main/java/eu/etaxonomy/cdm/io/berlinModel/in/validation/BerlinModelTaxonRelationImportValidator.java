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
		result &= checkInActivatedStatus(state);
		result &= checkSynonymRelationsWithAcceptedTaxa(state);
		result &= checkRelPTaxonWithNotes(state);
		return result;
	}
	
	
	private boolean checkInActivatedStatus(BerlinModelImportState state){
		try {
			boolean result = true;
			Source source = state.getConfig().getSource();
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
				" WHERE (Notes IS NOT NULL) AND (RTRIM(LTRIM(Notes)) <> '') ";
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
	
	/**
	 * @param state
	 * @return
	 */
	private boolean checkSynonymRelationsWithAcceptedTaxa(BerlinModelImportState state) {
		boolean success = true;
		try {
			
			Source source = state.getConfig().getSource();
			String strQuery = 
				"SELECT RelPTaxon.RelPTaxonId, RelPTQualifier, PTaxon.RIdentifier, Name.FullNameCache fromName, PTaxon.PTRefFk, Name.NameId as fromNameId, AcceptedName.FullNameCache acceptedName" +
				" FROM RelPTaxon INNER JOIN PTaxon ON RelPTaxon.PTNameFk1 = PTaxon.PTNameFk AND RelPTaxon.PTRefFk1 = PTaxon.PTRefFk " +
					" INNER JOIN RelPTQualifier ON RelPTaxon.RelQualifierFk = RelPTQualifier.RelPTQualifierId " +
					" LEFT OUTER JOIN Name ON PTaxon.PTNameFk = Name.NameId " +
					" LEFT OUTER JOIN Name AS AcceptedName ON RelPTaxon.PTNameFk2 = AcceptedName.NameId " +
				" WHERE (PTaxon.StatusFk = 1) AND (RelPTaxon.RelQualifierFk IN (2, 4, 5, 6, 7)) ";
			
			ResultSet rs = source.getResultSet(strQuery);
			boolean firstRow = true;
			int i = 0;
			while (rs.next()){
				i++;
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are accepted taxa being synonyms in a synonym relationship!");
					System.out.println("========================================================");
				}

				int relPTaxonId = rs.getInt("RelPTaxonId");
				String relType = rs.getString("RelPTQualifier");
				int fromIdentifier = rs.getInt("RIdentifier");
				String fromName = rs.getString("fromName");
				int fromRefFk = rs.getInt("PTRefFk");
				int fromNameId = rs.getInt("fromNameId");
				String toName = rs.getString("acceptedName");
				
				System.out.println("RelPTaxonId:" + relPTaxonId + 
						"\n TaxonRIdentifier: " + fromIdentifier + "\n name: " + fromName + "\n nameId: " + fromNameId + "\n RefFk: " + fromRefFk + "\n RelType: " + relType  
						+ "\n acceptedName: " + toName //+ "\n  ToNameId: " + toNameId + "\n  ToStatus: " + toStatus + "\n  ToDoubtful: " + doubtfulTo )
						);
				success = (firstRow = false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}


}
