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
public class BerlinModelTaxonNameImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameImportValidator.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		result &= checkNamesWithJournalReferences(state);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	
	private boolean checkNamesWithJournalReferences(BerlinModelImportState state){
		try {
			boolean result = true;
			Source source = state.getConfig().getSource();
			String strQuery = " SELECT n.NameId, n.FullNameCache, n.Created_Who, cat.RefCategoryAbbrev, r.NomRefCache, r.RefCache, r.Title, r.Edition, r.Volume, r.Series, r.RefYear " +
						" FROM RefCategory AS cat INNER JOIN Reference AS r ON cat.RefCategoryId = r.RefCategoryFk " + 
						" INNER JOIN Name AS n ON r.RefId = n.NomRefFk " +
						" WHERE (r.RefCategoryFk = 9) ";
			if (StringUtils.isNotBlank(state.getConfig().getNameIdTable())){
				strQuery += String.format(" AND (n.NameId IN " +
                        " (SELECT NameId FROM %s ))" , state.getConfig().getNameIdTable()) ; 
			}
						
			ResultSet rs = source.getResultSet(strQuery);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					System.out.println("There are names with journals as nomencl. reference !");
					System.out.println("========================================================");
				}
				int nameId = rs.getInt("nameId");
				String cat = rs.getString("RefCategoryAbbrev");
				String fullNameCache = rs.getString("fullNameCache");
				String createdWho = rs.getString("Created_Who");
				
				String nomRefCache = rs.getString("nomRefCache");
				String refCache = rs.getString("RefCache");
				String title = rs.getString("title");
				String edition = rs.getString("Edition");
				String volume = rs.getString("Volume");
				String series = rs.getString("Series");
					
				System.out.println("NameID:" + nameId + "\n  cat: " + cat + 
						"\n  fullNameCache: " + fullNameCache + "\n  created_who: " + createdWho + 
						"\n  nomRefCache: " + nomRefCache + "\n  refCache: " + refCache + "\n  title: " + title + 
						"\n  edition: " + edition + "\n  volume: " + volume + 
						"\n  series: " + series );
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
}
