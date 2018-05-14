/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v50_51;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.v47_50.SchemaUpdater_47_50;

/**
/**
 * @author a.mueller
 * @date 09.06.2017
 *
 */
public class SchemaUpdater_50_51 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_50_51.class);
	private static final String startSchemaVersion = "5.0.0.0.20180514";
	private static final String endSchemaVersion = "5.1.0.0.20180614";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_50_51 NewInstance() {
		return new SchemaUpdater_50_51();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_50_51() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		String newColumnName;
		String query;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();


		//#6699 delete term version
		//just in case not fixed before yet
		stepName = "Delete term version";
		query = "DELETE FROM @@CdmMetaData@@ WHERE propertyName = 'TERM_VERSION'";
		step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
        stepList.add(step);

        //#7414 remove mediaCreatedOld column
        stepName = "remove mediaCreatedOld column";
        tableName = "Media";
        String oldColumnName = "mediaCreatedOld";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        return stepList;

	}


    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_47_50.NewInstance();
	}

}
