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
public class ErmsSourceUsesImportValidator implements IOValidator<ErmsImportState>{
	private static final Logger logger = Logger.getLogger(ErmsSourceUsesImportValidator.class);

	public boolean validate(ErmsImportState state){
		boolean result = true;
		ErmsImportConfigurator config = state.getConfig();
		logger.warn("Checking for source uses not yet fully implemented");
//		result &= checkTaxonStatus(config);
//		result &= checkInactivated(config);
		return result;
	}
	


	private boolean checkTaxonStatus(ErmsImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strSQL = " SELECT tu_sources.sourceuse_id, sourceuses.sourceuse_name, tu.tu_acctaxon, tu.tu_parent, tu.id, tu.tu_name, " +
						" tu.tu_authority, tu.tu_displayname, status.status_name "  +
				" FROM  tu_sources " + 
					" INNER JOIN sourceuses ON tu_sources.sourceuse_id = sourceuses.sourceuse_id " + 
					" INNER JOIN tu ON tu_sources.tu_id = tu.id  " + 
					" INNER JOIN status ON dbo.tu.tu_status = dbo.status.status_id " +
				" WHERE (tu_sources.sourceuse_id = 4) ";
			ResultSet rs = source.getResultSet(strSQL);
			boolean firstRow = true;
			int i = 0;
			while (rs.next()){
				i++;
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are source uses of typ 'source of synonymy' having equal 'id' and 'tu_acctaxon'");
					System.out.println("========================================================");
				}
				int id = rs.getInt("id");
				String tu_displayname = rs.getString("tu_displayname");
				String status_name = rs.getString("status_name");
				
				System.out.println("id:" + id + "\n  name: " + tu_displayname + 
						"\n  status name: " + status_name );
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
