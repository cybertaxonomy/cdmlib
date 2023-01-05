/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v30_40;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;

/**
 * @author a.mueller
 * @since 15.12.2013
 */
public class RankClassUpdater extends SchemaUpdaterStepBase {

    private static final Logger logger = LogManager.getLogger();

	private static final String stepName = "Update rank class values";

// **************************** STATIC METHODS ********************************/

	public static final RankClassUpdater NewInstance(List<ISchemaUpdaterStep> stepList){
		return new RankClassUpdater(stepList, stepName);
	}

	protected RankClassUpdater(List<ISchemaUpdaterStep> stepList, String stepName) {
		super(stepList, stepName);
	}

	@Override
	public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
	        CaseType caseType, SchemaUpdateResult result) throws SQLException {

		try {
			//update representation label
			String sql;
			sql = caseType.replaceTableNames(" SELECT orderindex FROM @@DefinedTermBase@@ WHERE uuid = '%s'");
			Integer genusOrderIndex = (Integer)datasource.getSingleValue(String.format(sql, Rank.uuidGenus));
			Integer infraGenericOrderIndex = (Integer)datasource.getSingleValue(String.format(sql, Rank.uuidInfragenericTaxon));
			Integer speciesOrderIndex = (Integer)datasource.getSingleValue(String.format(sql, Rank.uuidSpecies));

			//suprageneric
			sql = caseType.replaceTableNames(
					" UPDATE @@DefinedTermBase@@ " +
					" SET rankClass = '%s' " +
					" WHERE DTYPE = 'Rank' AND orderindex < %d");
			datasource.executeUpdate(String.format(sql, RankClass.Suprageneric.getKey(), genusOrderIndex));

			//genus
			sql = caseType.replaceTableNames(
					" UPDATE @@DefinedTermBase@@ " +
					" SET rankClass = '%s' " +
					" WHERE DTYPE = 'Rank' AND orderindex = %d");
			datasource.executeUpdate(String.format(sql, RankClass.Genus.getKey(), genusOrderIndex));

			//infrageneric
			sql = caseType.replaceTableNames(
					" UPDATE @@DefinedTermBase@@ " +
					" SET rankClass = '%s' " +
					" WHERE DTYPE = 'Rank' AND orderindex > %d AND orderindex <= %d");
			datasource.executeUpdate(String.format(sql, RankClass.Infrageneric.getKey(), genusOrderIndex, infraGenericOrderIndex));

			//species group
			sql = caseType.replaceTableNames(
					" UPDATE @@DefinedTermBase@@ " +
					" SET rankClass = '%s' " +
					" WHERE DTYPE = 'Rank' AND orderindex > %d AND orderindex < %d");
			datasource.executeUpdate(String.format(sql, RankClass.SpeciesGroup.getKey(), infraGenericOrderIndex, speciesOrderIndex));

			//species
			sql = caseType.replaceTableNames(
					" UPDATE @@DefinedTermBase@@ " +
					" SET rankClass = '%s' " +
					" WHERE DTYPE = 'Rank' AND orderindex = %d");
			datasource.executeUpdate(String.format(sql, RankClass.Species.getKey(), speciesOrderIndex));

			//infraspecific
			sql = caseType.replaceTableNames(
					" UPDATE @@DefinedTermBase@@ " +
					" SET rankClass = '%s' " +
					" WHERE DTYPE = 'Rank' AND orderindex > %d");
			datasource.executeUpdate(String.format(sql, RankClass.Infraspecific.getKey(), speciesOrderIndex));

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
