/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v31_33;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;

/**
 * @author a.mueller
 * @date 15.12.2013
 */
public class TermVocabularyRepresentationUpdater
            extends SchemaUpdaterStepBase{

    private static final Logger logger = Logger.getLogger(TermVocabularyRepresentationUpdater.class);

    private static final String stepName = "Update term vocabulary representations";

// **************************** STATIC METHODS ********************************/

	public static final TermVocabularyRepresentationUpdater NewInstance(){
		return new TermVocabularyRepresentationUpdater(stepName);
	}

	protected TermVocabularyRepresentationUpdater(String stepName) {
		super(stepName);
	}

	@Override
	public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
	        CaseType caseType, SchemaUpdateResult result) throws SQLException {

		try {
			String sql = String.format(
					" SELECT id " +
					" FROM %s dtb " +
					" WHERE dtb.uuid = '%s'",
					 caseType.transformTo("DefinedTermBase"),
					 Language.uuidEnglish);
			String languageId = String.valueOf(datasource.getSingleValue(sql));

			//for each vocabulary
			for(VocabularyEnum vocabularyEnum : VocabularyEnum.values()) {
				//read vocabulary from terms files
				String filename = vocabularyEnum.name()+".csv";
				CSVReader reader = new CSVReader(CdmUtils.getUtf8ResourceReader("terms" + CdmUtils.getFolderSeperator() + filename));
				String [] nextLine = reader.readNext();
				TermVocabulary<?> voc = TermVocabulary.NewInstance(TermType.Unknown);
				voc.readCsvLine(arrayedLine(nextLine));

				//get uuid, label and description for the vocabulary
				UUID uuid = voc.getUuid();
				Representation repEN = voc.getRepresentations().iterator().next();
				String label = repEN.getLabel();
				String description = repEN.getText();

				//find representation in database
				sql = caseType.replaceTableNames(
						" SELECT rep.uuid " +
						" FROM @@TermVocabulary@@ voc " +
							" INNER JOIN @@TermVocabulary_Representation@@ MN ON MN.TermVocabulary_id = voc.id " +
							" INNER JOIN @@Representation@@ rep ON rep.id = MN.representations_id " +
						" WHERE voc.uuid = '%s' AND rep.language_id = %s");
				sql = String.format(sql, uuid.toString(), languageId);
				String repUuid = (String)datasource.getSingleValue(sql);

				//update with correct label and representation
				sql = " UPDATE %s SET label = '%s', text = '%s' WHERE uuid = '%s'";
				sql = String.format(sql, caseType.transformTo("Representation"), label, description, repUuid);

				//update vocabulary titleCache
				sql = " UPDATE %s SET titleCache = '%s' WHERE uuid = '%s'";
				sql = String.format(sql, caseType.transformTo("TermVocabulary"), label, uuid);

				datasource.executeUpdate(sql);
			}

			return;
		} catch (Exception e) {
		    String message = e.getMessage();
            monitor.warning(message, e);
            logger.warn(message);
            result.addException(e, message, this, "invoke");
            return;
		}
	}

	private List<String> arrayedLine(String [] nextLine){
		ArrayList<String> csvTermAttributeList = new ArrayList<String>(8);
		for (String col : nextLine){
			csvTermAttributeList.add(col);
		}
		while (csvTermAttributeList.size()<8){
			csvTermAttributeList.add("");
		}
		return csvTermAttributeList;
	}


}
