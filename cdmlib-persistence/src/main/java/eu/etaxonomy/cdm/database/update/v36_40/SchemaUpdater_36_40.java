// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v36_40;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.v35_36.SchemaUpdater_35_36;

/**
 * @author a.mueller
 * @created 16.04.2016
 */
public class SchemaUpdater_36_40 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_36_40.class);
	private static final String endSchemaVersion = "4.0.0.0.201604200000";
	private static final String startSchemaVersion = "3.6.0.0.201527040000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_36_40 NewInstance() {
		return new SchemaUpdater_36_40();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_36_40() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
//		String columnName;
		String newColumnName;
		String oldColumnName;
		String columnNames[];
		String referencedTables[];
		String columnTypes[];
//		boolean includeCdmBaseAttributes = false;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();

        //#5606
        //Add preferred stable URI to SpecimenOrObservation
        stepName = "Add preferred stable URI to SpecimenOrObservation";
        tableName = "SpecimenOrObservationBase";
        newColumnName = "preferredStableUri";
        step = ColumnAdder.NewClobInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#5717
        //Add sec micro reference
        stepName = "Add secMicroReference to TaxonBase";
        tableName = "TaxonBase";
        newColumnName = "secMicroReference";
        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        return stepList;
	}


	@Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_35_36.NewInstance();
	}

}
