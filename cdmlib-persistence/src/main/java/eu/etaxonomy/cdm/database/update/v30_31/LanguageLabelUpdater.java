/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v30_31;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author a.mueller
 \* @since 15.12.2010
 */
public class LanguageLabelUpdater extends SchemaUpdaterStepBase{

	private static final Logger logger = Logger.getLogger(LanguageLabelUpdater.class);

	private static final String stepName = "Update language labels by full language name";

// **************************** STATIC METHODS ********************************/

	public static final LanguageLabelUpdater NewInstance(){
		return new LanguageLabelUpdater(stepName);
	}

	protected LanguageLabelUpdater(String stepName) {
		super(stepName);
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

		try {
			//update representation label
			String sql;
			sql = " UPDATE @@Representation@@ " +
				" SET label = text " +
				" WHERE id IN ( SELECT MN.representations_id " +
					" FROM @@DefinedTermBase@@ lang " +
					" INNER JOIN @@DefinedTermBase_Representation@@ MN ON lang.id = MN.DefinedTermBase_id " +
					" WHERE lang.DTYPE = 'Language' " +
					" )";
			datasource.executeUpdate(caseType.replaceTableNames(sql));

			//update term titleCache
			sql = " UPDATE @@DefinedTermBase@@ dtb " +
				  " SET titleCache = " +
						" ( " +
						" SELECT rep.label  " +
						" FROM @@DefinedTermBase_Representation@@ MN " +
						" INNER JOIN @@Representation@@ rep ON MN.representations_id = rep.id " +
						" WHERE dtb.id = MN.DefinedTermBase_id AND rep.language_id = @langId) " +
					" WHERE dtb.DTYPE = 'Language'";
			String englishId = String.valueOf(getEnglishLanguageId(datasource, monitor, caseType));
			if (englishId == null){
				throw new NullPointerException("English id could not be found");
			}
			sql = sql.replace("@langId", englishId);
			datasource.executeUpdate(caseType.replaceTableNames(sql));

			return;
		} catch (Exception e) {
			String message = e.getMessage();
		    monitor.warning(message, e);
			logger.warn(message);
			result.addException(e, message, this, "invoke");
			return;
		}
	}


}
