/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * Swaps a relationship. The related_from object becomes the related_to object
 * and vice versa. Also the relationship type label and inverse label are swapped.
 *
 * @author a.mueller
 * @since 07.11.2020
 */
public class RelationSwapper extends AuditedSchemaUpdaterStepBase {

    private static final Logger logger = Logger.getLogger(RelationSwapper.class);

    private static final int PLACEHOLDER = -987;

    private String relTypeColumnName;
    private UUID uuidRelationType;
    private String newTitleCache;
    private String newIdInVocabulary;

	/**
	 * @param tableName the relations table name
	 * @param uuidRelationType
	 * @param newIdInVocabulary if not <code>null</code> the newIdInVocabulary is updated
	 * @return
	 */
	public static final RelationSwapper NewInstance(List<ISchemaUpdaterStep> stepList, String stepName,
	        String tableName, UUID uuidRelationType, String relTypeColumnName,
	        String newTitleCache, String newIdInVocabulary, boolean includeAudit){
		return new RelationSwapper(stepList, stepName, tableName, uuidRelationType,
		        relTypeColumnName, newTitleCache, newIdInVocabulary, includeAudit);
	}

	private RelationSwapper(List<ISchemaUpdaterStep> stepList, String stepName,
	        String tableName, UUID uuidRelationType, String relTypeColumnName,
	        String newTitleCache, String newIdInVocabulary, boolean includeAudit) {
		super(stepList, stepName, tableName, includeAudit);
		this.uuidRelationType = uuidRelationType;
		this.relTypeColumnName = relTypeColumnName;
		this.newTitleCache = newTitleCache;
		this.newIdInVocabulary = newIdInVocabulary;
	}

    @Override
    protected void invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) {

        try {
            String defTermTable = this.isAuditing ? "DefinedTermBase_AUD": "DefinedTermBase ";
            String mnTable = this.isAuditing ? "DefinedTermBase_Representation_AUD": "DefinedTermBase_Representation ";
            String inverseMnTable = this.isAuditing ? "DefinedTermBase_InverseRepresentation_AUD": "DefinedTermBase_InverseRepresentation ";
            String audParams = this.isAuditing ? ", REV, REVTYPE ": "";

            //get relType id
            String sql = " SELECT id FROM %s WHERE uuid = '%s'";
            Integer termId = (Integer)datasource.getSingleValue(String.format(sql,
            		caseType.transformTo(defTermTable) , this.uuidRelationType));

            if (termId == null || termId == 0){
            	String message = "RelationshipType term ("+uuidRelationType+") does not exist. Can't swap terms";
            	monitor.warning(message);
            	logger.warn(message);
            	result.addError(message, this, "invoke");
            	return;
            }

            //swap relatedFrom and relatedTo
            sql = " UPDATE %s "
                + " SET relatedFrom_id = relatedTo_id, relatedTo_id = relatedFrom_id "
                + " WHERE %s = %d ";
            sql = String.format(sql, caseType.transformTo(tableName), this.relTypeColumnName, termId);
            datasource.executeUpdate(sql);

            //  insert inverse into not-inverse with placeholder term id
            sql = " INSERT INTO %s (definedTermBase_id, representations_id %s) "
                + " SELECT %d, inverseRepresentations_id %s"
                + " FROM %s "
                + " WHERE DefinedTermBase_id = %d ";
            sql = String.format(sql, caseType.transformTo(mnTable),
                    audParams, PLACEHOLDER, audParams, caseType.transformTo(inverseMnTable), termId);
            datasource.executeUpdate(sql);

            //  delete inverse terms
            sql = " DELETE FROM %s WHERE definedTermBase_id = %d ";
            sql = String.format(sql, caseType.transformTo(inverseMnTable), termId);
            datasource.executeUpdate(sql);

            //  insert not-inverse into inverse with placeholder term id
            sql = " INSERT INTO %s (definedTermBase_id, inverseRepresentations_id %s) "
                + " SELECT definedTermBase_id, representations_id %s "
                + " FROM %s "
                + " WHERE DefinedTermBase_id = %d ";
            sql = String.format(sql, caseType.transformTo(inverseMnTable),
                    audParams, audParams, caseType.transformTo(mnTable), termId);
            datasource.executeUpdate(sql);

            //  delete inverse terms
            sql = " DELETE FROM %s WHERE definedTermBase_id = %d ";
            sql = String.format(sql, caseType.transformTo(mnTable), termId);
            datasource.executeUpdate(sql);

            //  replace placeholder in former inverse terms
            sql = " UPDATE %s SET definedTermBase_id = %d WHERE definedTermBase_id = %d ";
            sql = String.format(sql, caseType.transformTo(mnTable), termId, PLACEHOLDER);
            datasource.executeUpdate(sql);

            //new titleCache
            //NOTE: of course better the titleCache would be computed then set manually, but in most
            //cases this should be ok for now
            if (this.newTitleCache != null){
                sql = " UPDATE %s SET titleCache = '%s' WHERE id = %d ";
                sql = String.format(sql, caseType.transformTo(defTermTable), newTitleCache , termId);
                datasource.executeUpdate(sql);
            }

            //new idInVoc
            if (this.newIdInVocabulary != null){
                sql = " UPDATE %s SET idInVocabulary = '%d' WHERE id = %d ";
                sql = String.format(sql, caseType.transformTo(defTermTable), newIdInVocabulary , termId);
                datasource.executeUpdate(sql);
            }

            //symbols
            //not yet implemented

        } catch (SQLException e) {
            String message = e.getMessage();
            monitor.warning(message, e);
            logger.error(e);
            result.addException(e, message, getStepName() + ", RelationSwapper.invokeOnTable");
            return;
        }

		return;
	}

}