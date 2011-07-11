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

import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.mueller
 * @created 17.02.2010
 * @version 1.0
 */
public class BerlinModelRefDetailImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelRefDetailImportValidator.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		result &= checkRefDetailsWithSecondarySource(state);
		result &= checkRefDetailsWithIdInSource(state);
		result &= checkRefDetailsWithNotes(state);
		
		return result;
	}

	/**
	 * @param state
	 * @return
	 */
	private boolean checkRefDetailsWithSecondarySource(BerlinModelImportState state) {
		boolean success = true;
		try {
			
			Source source = state.getConfig().getSource();
			String strQuery = 
				"SELECT count(*) AS n FROM RefDetail " + 
				" WHERE (SecondarySources IS NOT NULL) AND (RTRIM(LTRIM(SecondarySources)) <> '')";
			ResultSet rs = source.getResultSet(strQuery);
			rs.next();
			int n;
			n = rs.getInt("n");
			if (n > 0){
				System.out.println("========================================================");
				logger.warn("There are " + n + " RefDetails with a secondary source. Secondary sources are not supported yet");
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
	private boolean checkRefDetailsWithIdInSource(BerlinModelImportState state) {
		boolean success = true;
		try {
			
			Source source = state.getConfig().getSource();
			String strQuery = 
				"SELECT count(*) AS n FROM RefDetail " + 
				" WHERE (IdInSource IS NOT NULL) AND (RTRIM(LTRIM(IdInSource)) <> '')";
			ResultSet rs = source.getResultSet(strQuery);
			rs.next();
			int n;
			n = rs.getInt("n");
			if (n > 0){
				System.out.println("========================================================");
				logger.warn("There are " + n + " RefDetails with an idInSource. IdInSources are not supported yet");
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
	private boolean checkRefDetailsWithNotes(BerlinModelImportState state) {
		boolean success = true;
		try {
			
			Source source = state.getConfig().getSource();
			String strQuery = 
				"SELECT count(*) AS n FROM RefDetail " + 
				" WHERE (Notes IS NOT NULL) AND (RTRIM(LTRIM(Notes)) <> '')";
			ResultSet rs = source.getResultSet(strQuery);
			rs.next();
			int n;
			n = rs.getInt("n");
			if (n > 0){
				System.out.println("========================================================");
				logger.warn("There are " + n + " RefDetails with a note. Notes for RefDetails are not imported!");
				System.out.println("========================================================");
				success = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}

}
