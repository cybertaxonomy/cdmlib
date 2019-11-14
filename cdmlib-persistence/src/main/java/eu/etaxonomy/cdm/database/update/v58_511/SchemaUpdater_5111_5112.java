/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v58_511;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableNameChanger;

/**
 * @author a.mueller
 * @date 01.11.2019
 */
public class SchemaUpdater_5111_5112 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5111_5112.class);

	private static final String startSchemaVersion = "5.11.1.0.20191108";
	private static final String endSchemaVersion = "5.11.2.0.20191109";

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5111_5112 NewInstance() {
		return new SchemaUpdater_5111_5112();
	}

	protected SchemaUpdater_5111_5112() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //##8673 add DTYPE to IntextReference
        stepName = "add DTYPE to IntextReference";
        tableName = "IntextReference";
        ColumnAdder.NewDTYPEInstance(stepList, stepName, tableName, "IntextReference", INCLUDE_AUDIT) ;

        //##8673 add description_id column to IntextReference
        stepName = "add description_id column to IntextReference";
        tableName = "IntextReference";
        newColumnName ="description_id";
        String referencedTable = "DescriptionBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //##8673 rename table IntextReference to CdmLink
        stepName = "rename table IntextReference to CdmLink";
        String oldTableName = "IntextReference";
        String newTableName = "CdmLink";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);

        //##8673 add cdmSource_id to OriginalSourceBase
        stepName = "add cdmSource_id to OriginalSourceBase";
        tableName = "OriginalSourceBase";
        newColumnName ="cdmSource_id";
        referencedTable = "CdmLink";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);


        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_511_5111.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

}
