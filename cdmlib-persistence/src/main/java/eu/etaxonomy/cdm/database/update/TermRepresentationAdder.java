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
 * Class for adding a term representations.
 *
 * Untested!!
 *
 * @author a.mueller
 * @since 11.05.2018
 *
 */
public class TermRepresentationAdder
            extends SchemaUpdaterStepBase {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermRepresentationAdder.class);

	public static final TermRepresentationAdder NewInstance(String stepName, UUID uuidTerm, String description,  String label, String abbrev, UUID uuidLanguage){
		return new TermRepresentationAdder(stepName, uuidTerm, description, label, abbrev, uuidLanguage, false);
	}

	public static final TermRepresentationAdder NewReverseInstance(String stepName, UUID uuidTerm, String description,  String label, String abbrev, UUID uuidLanguage){
		return new TermRepresentationAdder(stepName, uuidTerm, description, label, abbrev, uuidLanguage, true);
	}

	private UUID uuidTerm ;
	private String description;
	private String label;
	private String abbrev;
	private UUID uuidLanguage;
	private boolean isReverse = false;

	private TermRepresentationAdder(String stepName, UUID uuidTerm, String description, String label, String abbrev, UUID uuidLanguage, boolean isReverse) {
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

	    Integer termId;
	    String sqlCheckTermExists = " SELECT id FROM @@DefinedTermBase@@ WHERE uuid = '" + uuidTerm + "'";
		ResultSet rs = datasource.executeQuery(caseType.replaceTableNames(sqlCheckTermExists));
		if (rs.next() == false){
			String name = label != null ? label : abbrev != null ? abbrev : description;
			String message = "Term for representations update does not exist. Term not updated: " + CdmUtils.Nz(name) + "(" + uuidTerm + ")";
			monitor.warning(message);
			result.addError(message, this, "invoke");
			return;
		}else{
		    termId = rs.getInt("id");
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
		if (repId != null){
            String message = "Representation for language uuid (%s) already exists. Did not add term representation.";
            message = String.format(message, uuidLanguage.toString());
            monitor.warning(message);
            result.addError(message, this, "invoke");
            return;
		}

		String query = " SELECT max(id) id FROM @@Representation@@ ";
		query = caseType.replaceTableNames(query);
        repId = (Integer)datasource.getSingleValue(query) + 1;

        query = " INSERT INTO @@Representation@@(id, uuid, created, label, text, abbreviatedLabel, language_id) "
                + " VALUES (%d, '%s', '%s', '%s', '%s', '%s', %d ) ";
        //TODO created
        query = String.format(query, repId, UUID.randomUUID(), getNowString(), label, description, abbrev, langId);
        query = caseType.replaceTableNames(query);
        datasource.executeUpdate(query);

        String tableName = isReverse ? "TermBase_inverseRepresentation" : "DefinedTermBase_Representation" ;
        String repIdCol = isReverse ? "inverserepresentations_id" : "representations_id";
        String termCol = isReverse ? "term_id" : "DefinedTermBase_id";
        query = " INSERT INTO %s (%s, %s) "
                + " VALUES (%d, %d) ";
        tableName = caseType.transformTo(tableName);
        query = String.format(query, tableName, termCol, repIdCol, termId, repId);
        datasource.executeQuery(query);

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
			repId = null;
		}
		return repId;
	}

}
