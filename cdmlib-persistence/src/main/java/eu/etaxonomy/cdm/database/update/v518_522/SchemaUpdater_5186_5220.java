/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v518_522;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.v515_518.SchemaUpdater_5185_5186;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 02.09.2020
 */
public class SchemaUpdater_5186_5220 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5186_5220.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_18_06;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_22_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5186_5220 NewInstance() {
		return new SchemaUpdater_5186_5220();
	}

	protected SchemaUpdater_5186_5220() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//9327
        stepName = "Add sourcedTaxon column to SecundumSource";
        tableName = "OriginalSourceBase";
        String newColumnName = "sourcedTaxon_id";
        String referencedTable = "TaxonBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

	    //9327
        //move nomenclatural reference to nomenclatural source
        stepName = "move secundum reference to secundum source";
        tableName = "TaxonBase";
        String referenceColumnName = "sec_id";
        String microReferenceColumnName = "secMicroReference";
        String sourceColumnName = "sourcedTaxon_id";
        String sourceType = "SEC";
        String dtype = "SecundumSource";
        SecReference2SourceMover.NewInstance(stepList, stepName, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName, dtype, sourceType);


        return stepList;
    }


    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5185_5186.NewInstance();
    }
}