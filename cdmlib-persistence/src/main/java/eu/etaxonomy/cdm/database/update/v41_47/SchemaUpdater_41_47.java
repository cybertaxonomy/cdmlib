/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v41_47;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.IndexAdder;
import eu.etaxonomy.cdm.database.update.LanguageStringTableCreator;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.v40_41.NomenclaturalCodeUpdater;
import eu.etaxonomy.cdm.database.update.v40_41.SchemaUpdater_40_41;

/**
 * @author a.mueller
 * @created 16.04.2016
 */
public class SchemaUpdater_41_47 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_41_47.class);
	private static final String endSchemaVersion = "4.1.0.0.201607300000";
	private static final String startSchemaVersion = "4.0.0.0.201604200000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_41_47 NewInstance() {
		return new SchemaUpdater_41_47();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_41_47() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		String query;
		String newColumnName;
		String oldColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();


        //#3658 update nomenclatural code
        NomenclaturalCodeUpdater.NewInstance(stepList);

        //#5970
        //Implement allowOverride in CdmPreference
        stepName = "Add allowOverride in CdmPreference";
        tableName = "CdmPreference";
        newColumnName = "allowOverride";
        step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, ! INCLUDE_AUDIT, false);
        stepList.add(step);

        //#5875
        //Implement isDefault to DescriptionBase
        stepName = "Add isDefault in DescriptionBase";
        tableName = "DescriptionBase";
        newColumnName = "isDefault";
        step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
        stepList.add(step);


        //index
        stepName = "Add identityCache index";
        tableName = "SpecimenOrObservationBase";
        newColumnName = "identityCache";
        step = IndexAdder.NewInstance(stepName, tableName, newColumnName, null);
        stepList.add(step);

        stepName = "Add protectedIdentityCache";
        tableName = "SpecimenOrObservationBase";
        newColumnName = "protectedIdentityCache";
        step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
        stepList.add(step);

        //#5634 Add excluded note
        stepName = "Add excluded note";
        tableName = "TaxonNode";
        String attributeName = "excludedNote";
        step = LanguageStringTableCreator.NewLanguageStringInstance(stepName, tableName, attributeName, INCLUDE_AUDIT);
        stepList.add(step);

        return stepList;
    }


    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_40_41.NewInstance();
	}

}
