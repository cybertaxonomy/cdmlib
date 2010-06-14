// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.erms.validation;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;


import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.erms.ErmsImportConfigurator;
import eu.etaxonomy.cdm.io.erms.ErmsImportState;

/**
 * @author a.mueller
 * @created 17.02.2010
 * @version 1.0
 */
public class ErmsReferenceImportValidator implements IOValidator<ErmsImportState>{
	private static final Logger logger = Logger.getLogger(ErmsReferenceImportValidator.class);

	public boolean validate(ErmsImportState state){
		boolean result = true;
		ErmsImportConfigurator config = state.getConfig();
		logger.warn("Checking for references not yet fully implemented");
//		result &= checkTaxonStatus(config);
//		result &= checkInactivated(config);
		return result;
	}
	
	private boolean checkTaxonStatus(ErmsImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strSQL = " SELECT RelPTaxon.RelQualifierFk, RelPTaxon.relPTaxonId, PTaxon.PTNameFk, PTaxon.PTRefFk, PTaxon_1.PTNameFk AS Expr1, PTaxon.RIdentifier, PTaxon_1.RIdentifier AS Expr3, Name.FullNameCache "  +
				" FROM RelPTaxon " + 
					" INNER JOIN PTaxon ON RelPTaxon.PTNameFk1 = PTaxon.PTNameFk AND RelPTaxon.PTRefFk1 = PTaxon.PTRefFk " + 
					" INNER JOIN PTaxon AS PTaxon_1 ON RelPTaxon.PTNameFk2 = PTaxon_1.PTNameFk AND RelPTaxon.PTRefFk2 = PTaxon_1.PTRefFk  " + 
					" INNER JOIN Name ON PTaxon.PTNameFk = Name.NameId " +
				" WHERE (dbo.PTaxon.StatusFk = 1) AND ((RelPTaxon.RelQualifierFk = 7) OR (RelPTaxon.RelQualifierFk = 6) OR (RelPTaxon.RelQualifierFk = 2)) ";
			ResultSet rs = source.getResultSet(strSQL);
			boolean firstRow = true;
			int i = 0;
			while (rs.next()){
				i++;
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are taxa that have a 'is synonym of' - relationship but having taxon status 'accepted'!");
					System.out.println("========================================================");
				}
				int rIdentifier = rs.getInt("RIdentifier");
				int nameFk = rs.getInt("PTNameFk");
				int refFk = rs.getInt("PTRefFk");
				int relPTaxonId = rs.getInt("relPTaxonId");
				String taxonName = rs.getString("FullNameCache");
				
				System.out.println("RIdentifier:" + rIdentifier + "\n  name: " + nameFk + 
						"\n  taxonName: " + taxonName + "\n  refId: " + refFk + "\n  RelPTaxonId: " + relPTaxonId );
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



}
