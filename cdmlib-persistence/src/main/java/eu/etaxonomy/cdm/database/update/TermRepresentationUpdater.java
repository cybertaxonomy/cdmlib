/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * Class for updating term representations.
 * @author a.mueller
 * @date 27.09.2011
 *
 */
public class TermRepresentationUpdater
            extends SchemaUpdaterStepBase
            implements ITermUpdaterStep{

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermRepresentationUpdater.class);

	public static final TermRepresentationUpdater NewInstance(String stepName, UUID uuidTerm, String description,  String label, String abbrev, UUID uuidLanguage){
		return new TermRepresentationUpdater(stepName, uuidTerm, description, label, abbrev, uuidLanguage, false);
	}

	public static final TermRepresentationUpdater NewReverseInstance(String stepName, UUID uuidTerm, String description,  String label, String abbrev, UUID uuidLanguage){
		return new TermRepresentationUpdater(stepName, uuidTerm, description, label, abbrev, uuidLanguage, true);
	}

	private UUID uuidTerm ;
	private String description;
	private String label;
	private String abbrev;
	private UUID uuidLanguage;
	private boolean isReverse = false;

	private TermRepresentationUpdater(String stepName, UUID uuidTerm, String description, String label, String abbrev, UUID uuidLanguage, boolean isReverse) {
		super(stepName);
		this.abbrev = abbrev;
		this.description = description;
		this.label = label;
		this.uuidTerm = uuidTerm;
		this.uuidLanguage = uuidLanguage;
		this.isReverse = isReverse;
	}



	@Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException{

		String sqlCheckTermExists = " SELECT count(*) as n FROM @@DefinedTermBase@@ WHERE uuid = '" + uuidTerm + "'";

		Long n = (Long)datasource.getSingleValue(caseType.replaceTableNames(sqlCheckTermExists));
		if (n == 0){
			String name = label != null ? label : abbrev != null ? abbrev : description;
			String message = "Term for representations update does not exist. Term not updated: " + CdmUtils.Nz(name) + "(" + uuidTerm + ")";
			monitor.warning(message);
			result.addError(message, this, "invoke");
			return;
		}

		//language id
		Integer langId = null;
		if (uuidLanguage != null){
			langId = getLanguageId(uuidLanguage, datasource, monitor, caseType);
			if (langId == null){
				String message = "Language for language uuid (%s) could not be found. Term representations not updated.";
				message = String.format(message, uuidLanguage.toString());
				monitor.warning(message);
	            result.addError(message, this, "invoke");
	            return;
			}
		}

		Integer repId = getRepresentationId(datasource, monitor, langId, caseType);
		if (repId == null){
			String message = "repId is null";
		    result.addError(message, this, "invoke");
			return;
		}

		//standard representation
		String sqlUpdateRepresentationFormat = " UPDATE @@Representation@@ r SET %s = '%s' WHERE r.id = %d ";
		sqlUpdateRepresentationFormat = caseType.replaceTableNames(sqlUpdateRepresentationFormat);
		if (description != null){
			String sqlUpdateRepresentation = String.format(sqlUpdateRepresentationFormat, "text", description, repId);
			datasource.executeUpdate(sqlUpdateRepresentation);
		}
		if (label != null){
			String sqlUpdateRepresentation = String.format(sqlUpdateRepresentationFormat, "label", label, repId);
			datasource.executeUpdate(sqlUpdateRepresentation);
		}
		if (abbrev != null){
			String sqlUpdateRepresentation = String.format(sqlUpdateRepresentationFormat, "abbreviatedLabel", abbrev, repId);
			datasource.executeUpdate(sqlUpdateRepresentation);
		}

		return;
	}

	/**
	 * @param datasource
	 * @param monitor
	 * @param langId
	 * @param caseType
	 * @return
	 * @throws SQLException
	 */
	private Integer getRepresentationId(ICdmDataSource datasource,
			IProgressMonitor monitor, Integer langId, CaseType caseType) throws SQLException {
		//representation

		String tableName = isReverse ? "RelationshipTermBase_inverseRepresentation" : "DefinedTermBase_Representation" ;
		String repIdFkCol = isReverse ? "inverserepresentations_id" : "representations_id";
		String sqlId = " SELECT rep.id " +
			" FROM @@Representation@@ rep INNER JOIN %s MN ON MN.%s = rep.id " +
			" INNER JOIN @@DefinedTermBase@@ dtb ON MN.DefinedTermBase_id = dtb.id " +
			" WHERE dtb.uuid = '%s' ";
		tableName = caseType.transformTo(tableName);
		sqlId = String.format(sqlId, tableName, repIdFkCol, uuidTerm.toString());
		sqlId = caseType.replaceTableNames(sqlId);
		if (uuidLanguage != null){
			sqlId += " AND rep.language_id = " + langId;
		}
		ResultSet rs = datasource.executeQuery(sqlId);
		Integer repId;
		if (rs.next()){
			repId = rs.getInt("id");
		}else{
			String warning = "No representations do exist yet. Can't update term representation for term '%s'!";
			warning = String.format(warning, uuidTerm.toString());
			monitor.warning(warning);
			repId = null;
		}
		return repId;
	}

}
