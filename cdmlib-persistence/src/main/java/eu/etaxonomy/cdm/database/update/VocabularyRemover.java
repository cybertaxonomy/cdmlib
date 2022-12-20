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
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * Removes a given vocabulary if it is not in use and if all terms have been removed.
 *
 * TODO does not yet check all DefinedTermBase_XXX tables except for representations.
 * Does also not handle AUD tables => should probably be handled as DeleteEvent, not simply delete the record;
 * There is now a factory method for handling AUDs.
 *
 * @author a.mueller
 * @since 2022-12-20
  */
public class VocabularyRemover
        extends SchemaUpdaterStepBase{

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

// **************************** FACTORY METHODS ********************************/

	public static final VocabularyRemover NewMultipleCheckInstance(List<ISchemaUpdaterStep> stepList, String stepName,
	        String uuidVocabulary, List<String> checkUsedQueries){
		return new VocabularyRemover(stepList, stepName, uuidVocabulary, checkUsedQueries, false);
	}

	/**
	 * @param firstCheckUsedQuery The first query to check if this vocabulary is used. Must return a single int value > 0
	 * if this vocabulary is used at the given place.
	 */
	public static final VocabularyRemover NewInstance(List<ISchemaUpdaterStep> stepList, String stepName,
	        String uuidVocabulary, String firstCheckUsedQuery){
		List<String> checkUsedQueries = new ArrayList<>();
        if (firstCheckUsedQuery != null) {
            checkUsedQueries.add(firstCheckUsedQuery);
        }
		return new VocabularyRemover(stepList, stepName, uuidVocabulary, checkUsedQueries, false);
	}

	/**
     * @param firstCheckUsedQuery The first query to check if this vocabulary is used.
     * Must return a single int value > 0 if this vocablary is used at the given place.
     */
    public static final VocabularyRemover NewAudInstance(List<ISchemaUpdaterStep> stepList, String stepName,
            String uuidVocabulary, String firstCheckUsedQuery){
        List<String> checkUsedQueries = new ArrayList<>();
        if (firstCheckUsedQuery != null) {
            checkUsedQueries.add(firstCheckUsedQuery);
        }
        return new VocabularyRemover(stepList, stepName, uuidVocabulary, checkUsedQueries, true);
    }

// *************************** VARIABLES *****************************************/

    private String uuidVocabulary ;
    private List<String> checkUsedQueries = new ArrayList<>();
    private boolean isAudit = false;

// ***************************** CONSTRUCTOR ***************************************/

	private VocabularyRemover(List<ISchemaUpdaterStep> stepList, String stepName, String uuidTerm,
	        List<String> checkUsedQueries, boolean isAudit) {
		super(stepList, stepName);
		this.uuidVocabulary = uuidTerm;
		this.checkUsedQueries = checkUsedQueries;
		this.isAudit = isAudit;
	}

// ******************************* METHODS *************************************************/

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

        //get term id
		String sql = " SELECT id FROM %s WHERE uuid = '%s'";
		String tableName = isAudit ? "TermCollection_AUD" : "TermCollection";
		Integer id = (Integer)datasource.getSingleValue(String.format(sql,
				caseType.transformTo(tableName), this.uuidVocabulary));
		if (id == null || id == 0){
			return;
		}

		//check if in use
		if (checkTermInUse(datasource, monitor, id, caseType)){
			return;
		}

		//if not ... remove
		removeTerm(datasource, monitor, id, caseType, result);

		return;
	}

	private void removeTerm(ICdmDataSource datasource, IProgressMonitor monitor, int id,
	        CaseType caseType, SchemaUpdateResult result) {

		try {
            //get representation ids
            List<Integer> repIDs = new ArrayList<>();
            String tableName = isAudit ? "TermCollection_Representation_AUD" : "TermCollection_Representation";
            getRepIds(datasource, id, repIDs, "representations_id", tableName, caseType );

            //remove from MN table
            String sql = " DELETE FROM %s WHERE TermCollection_id = %d";
            sql = String.format(sql, caseType.transformTo(tableName), id);
            datasource.executeUpdate(sql);

            //remove representations
            tableName = isAudit ? "Representation_AUD" : "Representation";
            for (Integer repId : repIDs){
            	sql = " DELETE FROM %s WHERE id = %d ";
            	sql = String.format(sql,
            			caseType.transformTo(tableName),
            			repId);
            	datasource.executeUpdate(sql);
            }

            //remove term
            tableName = isAudit ? "TermCollection_AUD" : "TermCollection";
            sql = " DELETE FROM %s WHERE id = %d";
            sql = String.format(sql,
            		caseType.transformTo(tableName),
            		id);
            datasource.executeUpdate(sql);
        } catch (SQLException e) {
            String message = e.getMessage();
            monitor.warning(message, e);
            result.addException(e, message, this, "removeTerm");
        }
	}

	private void getRepIds(ICdmDataSource datasource, int id,
			List<Integer> repIDs, String mnRepresentationIdAttr, String mnTableName, CaseType caseType) throws SQLException {
		String sql = " SELECT DISTINCT %s as repId FROM %s WHERE TermCollection_id = %d";
		sql = String.format(sql, mnRepresentationIdAttr, caseType.transformTo(mnTableName), id);
		ResultSet rs = datasource.executeQuery(sql);
		while (rs.next()){
			int repId = rs.getInt("repId");  //TODO nullSafe, but should not happen
			repIDs.add(repId);
		}
	}

	private boolean checkTermInUse(ICdmDataSource datasource,
	        @SuppressWarnings("unused") IProgressMonitor monitor, int id, CaseType caseType) throws SQLException {
		for (String query : checkUsedQueries){
			query = String.format(caseType.replaceTableNames(query), id);
			Number i = (Number)datasource.getSingleValue(query);
			if (i != null && (Long)i > 0.0){
				return true;
			}
		}
		return false;
	}

	public VocabularyRemover addCheckUsedQuery(String query){
		this.checkUsedQueries.add(query);
		return this;
	}
}