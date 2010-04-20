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
public class BerlinModelCommonNamesImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelCommonNamesImportValidator.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		logger.warn("Checking for common names not yet implemented");
		result &= checkUnreferredNameUsedInSource(state.getConfig());
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	private boolean checkUnreferredNameUsedInSource(BerlinModelImportConfigurator config){
		try {
			boolean result = true;
			Source source = config.getSource();
			String strQueryArticlesWithoutJournal = "SELECT Count(*) as n " +
					" FROM emCommonName " +
					" WHERE (emCommonName.NameInSourceFk NOT IN " + 
							"(SELECT NameId FROM Name AS Name_1)) AND " + 
						"(emCommonName.NameInSourceFk <> - 1)";
			ResultSet rs = source.getResultSet(strQueryArticlesWithoutJournal);
			rs.next();
			int count = rs.getInt("n");
			if (count > 0){
				System.out.println("========================================================");
				logger.warn("There are " + count + " common names that have a name used in source which can not be found in the database.");
				
				System.out.println("========================================================");
			}
			String sql = 
				" SELECT DISTINCT emCommonName.CommonNameId, emCommonName.NameInSourceFk, emCommonName.CommonName, PTaxon.PTNameFk, PTaxon.PTRefFk," + 
					" Name.FullNameCache, PTaxon.RIdentifier " +
				" FROM emCommonName INNER JOIN " +
					" PTaxon ON emCommonName.PTNameFk = PTaxon.PTNameFk AND emCommonName.PTRefFk = PTaxon.PTRefFk INNER JOIN " +
					" Name ON PTaxon.PTNameFk = Name.NameId " +
				" WHERE (emCommonName.NameInSourceFk NOT IN " + 
						"(SELECT NameId FROM Name AS Name_1)) AND " + 
					"(emCommonName.NameInSourceFk <> - 1)";
			
			rs = source.getResultSet(sql);
			int i = 0;
			while (rs.next()){
				i++;
				int commonNameId = rs.getInt("CommonNameId");
				String fullNameCache = rs.getString("FullNameCache");
				String commonName = rs.getString("CommonName");
				int rIdentifier = rs.getInt("RIdentifier");
				int nameFk = rs.getInt("PTNameFk");
				int refFk = rs.getInt("PTRefFk");
				int nameInSourceFk = rs.getInt("NameInSourceFk");
				
				System.out.println("CommonName: " + commonName + "\n  CommonNameId: " + commonNameId + "\n Taxon Name:" + fullNameCache + "\n  TaxonNameFk: " + nameFk + 
						"\n  TaxonRefFk: " + refFk + "\n  TaxonId" + rIdentifier + "\n NameInSourceFk: " + nameInSourceFk + "\n");
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
