/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v47_50;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.database.update.SingleTermUpdater;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;


/**
 * Updates the pro parte and partial synonyms. Makes them real concept relationships
 * #7334
 *
 * @author a.mueller
 * @since 11.05.2018
 */
public class ProParteSynonymUpdater extends SchemaUpdaterStepBase {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ProParteSynonymUpdater.class);

	private static final String stepName = "Make pro parte synonyms concept relationships";


// **************************** STATIC METHODS ********************************/

	public static final ProParteSynonymUpdater NewInstance(List<ISchemaUpdaterStep> stepList){
		ProParteSynonymUpdater result = new ProParteSynonymUpdater();
		stepList.add(result);

		return result;
	}

	private ProParteSynonymUpdater() {
		super(stepName);
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

        int proParteId = createProParteRelType(datasource, monitor, caseType, result);
        int partialId = createPartialRelType(datasource, monitor, caseType, result);

        try{
            invokeSingle(false, proParteId, datasource, monitor, caseType, result);
            invokeSingle(true, partialId, datasource, monitor, caseType, result);

        } catch (Exception e) {
            String message = e.getMessage();
            monitor.warning(message, e);
            result.addException(e, message, this, "invoke");
        }

	    return;
	}

    private int createProParteRelType(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        UUID uuidTerm = TaxonRelationshipType.uuidProParteSynonymFor;
        String symbol = "p.p.";
        String description = "Pro parte synonym for";
        UUID uuidAfterTerm = UUID.fromString("605b1d01-f2b1-4544-b2e0-6f08def3d6ed");
        String reverseDescription = "Has pro parte synonym";
        String reverseAbbrev = "⊃p.p.";

        return createRelType(datasource, monitor, caseType, result,
                uuidTerm, symbol, description, uuidAfterTerm,
                reverseDescription, reverseAbbrev);
    }

    private int createPartialRelType(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        UUID uuidTerm = TaxonRelationshipType.uuidPartialSynonymFor;
        String symbol = "partial";
        String description = "Partial synonym for";
        UUID uuidAfterTerm = UUID.fromString("8a896603-0fa3-44c6-9cd7-df2d8792e577");
        String reverseDescription = "Has partial synonym";
        String reverseAbbrev = "⊃partim";

        return createRelType(datasource, monitor, caseType, result,
                uuidTerm, symbol, description, uuidAfterTerm,
                reverseDescription, reverseAbbrev);

    }

    private int createRelType(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result, UUID uuidTerm, String symbol,
            String description, UUID uuidAfterTerm,
            String reverseDescription, String reverseAbbrev) throws SQLException {

        String inverseSymbol = "⊃" + symbol;
        String query = "SELECT count(*) n FROM @@DefinedTermBase@@ dtb WHERE dtb.uuid = '%s'";
        query = String.format(query, uuidTerm);
        query = caseType.replaceTableNames(query);
        Long n = (Long)datasource.getSingleValue(query);
        if (n == 0){

            //create type term
            String idInVocabulary = symbol;
            String label = description;
            String abbrev = idInVocabulary;
            String reverseLabel = reverseDescription;
            String dtype = "TaxonRelationshipType";
            UUID uuidVocabulary = UUID.fromString("15db0cf7-7afc-4a86-a7d4-221c73b0c9ac");
            UUID uuidLanguage = Language.uuidEnglish;
            boolean isOrdered = true;
            ISchemaUpdaterStep step = SingleTermUpdater.NewReverseInstance(stepName, TermType.TaxonRelationshipType,
                    uuidTerm, idInVocabulary, symbol,
                    description, label, abbrev, reverseDescription, reverseLabel, reverseAbbrev,
                    dtype, uuidVocabulary, uuidLanguage, isOrdered, uuidAfterTerm);
            step.invoke(datasource, monitor, caseType, result);

//            //reverse  representation
//            step = TermRepresentationAdder.NewReverseInstance(dtype, uuidTerm, reverseDescription, reverseLabel, reverseAbbrev, uuidLanguage);
//            step.invoke(datasource, monitor, caseType, result);
        }

        //update some fields
        query = " UPDATE @@DefinedTermBase@@ dtb " +
                " SET symbol = '%s', inverseSymbol = '%s', transitive = @FALSE@ " +
                " WHERE uuid = '%s'";
        query = String.format(query, symbol, inverseSymbol, uuidTerm.toString());
        query = doReplacements(query, caseType, datasource);
        datasource.executeUpdate(query);

        //select id
        query = "SELECT id n FROM @@DefinedTermBase@@ dtb WHERE dtb.uuid = '%s'";
        query = String.format(query, uuidTerm);
        query = doReplacements(query, caseType, datasource);
        Integer id = (Integer)datasource.getSingleValue(query);

        return id;
    }


    private void invokeSingle(boolean isPartial, int typeId, ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

        //get maxId from taxonRelationship
        Integer maxId = 0;
        String query = "SELECT max(id) id FROM @@TaxonRelationship@@";
        query = doReplacements(query, caseType, datasource);
        ResultSet rs = datasource.executeQuery(query);
        if (rs.next()){
            maxId = rs.getInt("id");
        }

        //
        String attributeName = isPartial? "partial" : "proParte";
        query = " SELECT tb.id synId, titleCache " +
                " FROM @@TaxonBase@@ tb  " +
                " WHERE tb.%s = @TRUE@ ";
        query = String.format(query, attributeName);
        query = doReplacements(query, caseType, datasource);
        rs = datasource.executeQuery(query);
        while (rs.next()){
            maxId++;
            Integer synId = rs.getInt("synId");
            String insert = "INSERT INTO @@TaxonRelationship@@ (id, uuid, relatedFrom_id, relatedTo_id, "
                    +       " doubtful, citation_id, citationMicroReference, type_id, "
                    +       " created, updated, createdBy_id, updatedBy_id )"
                    + " SELECT %d, '%s', syn.id, acceptedTaxon_id,  "
                    +       " syn.doubtful, syn.sec_id secId, syn.secMicroReference, %d, "
                    +       " syn.created, syn.updated, syn.createdBy_id, syn.updatedBy_id "
                    + " FROM TaxonBase syn "
                    + " WHERE syn.id = %d ";
            insert = String.format(insert, maxId, UUID.randomUUID(), typeId, synId );
            insert = doReplacements(insert, caseType, datasource);
            datasource.executeUpdate(insert);

            String titleCache = rs.getString("titleCache");
            titleCache = normalizeTitleCache(titleCache);
            String update = "UPDATE @@TaxonBase@@ "
                    + " SET DTYPE = 'Taxon', sec_id = null, secMicroReference = null,"
                    + "     %s = null, titleCache = '%s', taxonStatusUnknown = @FALSE@,"
                    + "     doubtful = @FALSE@, acceptedTaxon_id = null, type_id = null "
                    + " WHERE id = %d ";
            update = String.format(update, attributeName, titleCache, synId);
            update = doReplacements(update, caseType, datasource);
            datasource.executeUpdate(update);
        }

    }


    /**
     * @param titleCache
     * @return
     */
    private String normalizeTitleCache(String titleCache) {
        if(titleCache == null){
            return "";
        }
        int index = titleCache.indexOf("syn. sec.");
        if (index < 0){
            return titleCache;
        }else{
            titleCache = titleCache.substring(0, index) + "sec. ???";
            return titleCache;
        }
    }

    private String doReplacements(String query, CaseType caseType, ICdmDataSource datasource) {
        query = caseType.replaceTableNames(query);
        query = query.replaceAll("@FALSE@", getBoolean(false, datasource));
        query = query.replaceAll("@TRUE@", getBoolean(true, datasource));
        return query;
    }

}
