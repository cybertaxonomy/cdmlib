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
public class BerlinModelTaxonNameRelationImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameRelationImportValidator.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		result &= checkUnrelatedHomotypicSynonyms(bmiConfig);
		
		return result;
	}
	
	
	
	private boolean checkUnrelatedHomotypicSynonyms(BerlinModelImportConfigurator bmiConfig){
	
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strSQL = " SELECT Name.NameId AS NameId1, Name2.NameId AS NameId2, Name.FullNameCache AS NameCache1, Name2.FullNameCache AS NameCache2, dbo.NomStatus.NomStatus AS fromNomStatus, NomStatus_1.NomStatus AS toNomStatus " +
			" FROM RelPTaxon INNER JOIN " + 
                      " Name ON RelPTaxon.PTNameFk1 = Name.NameId INNER JOIN " + 
                      " Name AS Name2 ON RelPTaxon.PTNameFk2 = Name2.NameId LEFT OUTER JOIN " + 
                      " NomStatus AS NomStatus_1 INNER JOIN " + 
                      " NomStatusRel AS NomStatusRel_1 ON NomStatus_1.NomStatusId = NomStatusRel_1.NomStatusFk ON  " + 
                      " Name2.NameId = NomStatusRel_1.NameFk LEFT OUTER JOIN " + 
					  " NomStatus INNER JOIN " + 
                      " NomStatusRel ON NomStatus.NomStatusId = NomStatusRel.NomStatusFk ON Name.NameId = NomStatusRel.NameFk " + 
				" WHERE  RelPTaxon.RelQualifierFk = 7 AND " +
					" NomStatus.NomStatus <> 'nom. illeg.' AND NomStatus.NomStatus <> 'nom. inval.' AND " + 
					  " NomStatus.NomStatus <> 'nom. altern.' AND NomStatus.NomStatus <> 'nom. nud.' AND " +
					  " NomStatus.NomStatus <> 'nom. rej.' AND NomStatus.NomStatus <> 'nom. rej. prop.' AND " +
					" RelPTaxon.PTNameFk1 NOT IN " + 
                         " (SELECT     NameFk1 " + 
                         " FROM RelName " +
                         "   WHERE  RelNameQualifierFk = 1 OR RelNameQualifierFk = 3 " +
                       "  UNION " + 
                         "  SELECT NameFk2 " +
                         "  FROM  RelName AS RelName2 " + 
                         "  WHERE  RelNameQualifierFk = 1 OR RelNameQualifierFk = 3)";

			ResultSet rs = source.getResultSet(strSQL);
			boolean firstRow = true;
			int i = 0;
			while (rs.next()){
				i++;
				if (firstRow){
					System.out.println("==================================================================================================");
					logger.warn("There are names that have a homotypic relationship as taxa but no 'is basionym' or 'is replaced synonym' relationship or a status not requiring a basionym/repl.syn.");
					System.out.println("===================================================================================================");
				}
				
				int nameId1 = rs.getInt("NameId1");
				String nameCache1 = rs.getString("NameCache1");
				int nameId2 = rs.getInt("NameId2");
				String nameCache2 = rs.getString("NameCache2");
				String status1 = rs.getString("fromNomStatus");
				String status2 = rs.getString("toNomStatus");
				
				System.out.println(
						"NameId1:" + nameId1 + "\n  NameCache1: " + nameCache1 + "\n  status1: " + status1 +  
						"\n  NameId2: " + nameId2 + "\n  NameCache2: " + nameCache2 + "\n  status2: " + status2) ;
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
