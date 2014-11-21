// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v33_34;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;

/**
 * @author a.mueller
 * @created Jan 14, 2014
 */
public class SchemaUpdater_34_341 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_34_341.class);
	private static final String endSchemaVersion = "3.4.1.0.201411210000";
	private static final String startSchemaVersion = "3.4.0.0.201407010000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_34_341 NewInstance() {
		return new SchemaUpdater_34_341();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_34_341() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		String columnName;
		String newColumnName;
		String oldColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();
		
		//DnaMarker in Primer
		//TODO H2 / PostGreSQL / SQL Server
		stepName = "Add foreign key for Primer.dnaMarker";
		tableName = "Primer";
		newColumnName = "dnaMarker_id";
		boolean notNull = false;
		String referencedTable = "DefinedTermBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);
		
		//Institution for DerivationEvent
		//TODO H2 / PostGreSQL / SQL Server
		stepName = "Add foreign key for DerivationEvent.institution";
		tableName = "DerivationEvent";
		newColumnName = "institution_id";
		referencedTable = "AgentBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);

		//Institution for Amplication
		//TODO H2 / PostGreSQL / SQL Server
		stepName = "Add foreign key for Amplification.institution";
		tableName = "Amplification";
		newColumnName = "institution_id";
		referencedTable = "AgentBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);
		
		
		return stepList;
		
	}



	@Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_331_34.NewInstance();
	}

}
