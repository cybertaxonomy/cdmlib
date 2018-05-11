/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * Updates the base type of a class.
 * E.g. if a class is VersionableEntity it may be upgraded to AnnotatableEntity
 * @since 2015-03-20
 * @author a.mueller
 */
public class ClassBaseTypeUpdater extends AuditedSchemaUpdaterStepBase {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TableCreator.class);

	private final boolean includeIdentifiableEntity;
	private final boolean includeAnnotatableEntity;
	protected List<ISchemaUpdaterStep> mnTablesStepList = new ArrayList<>();
	protected List<ISchemaUpdaterStep> columnAdderStepList = new ArrayList<>();


	public static final ClassBaseTypeUpdater NewVersionableToAnnotatableInstance(String stepName, String tableName, boolean includeAudTable){
		return new ClassBaseTypeUpdater(stepName, tableName, includeAudTable, true, false);
	}
	public static final ClassBaseTypeUpdater NewAnnotatableToIdentifiableInstance(String stepName, String tableName, boolean includeAudTable){
		return new ClassBaseTypeUpdater(stepName, tableName, includeAudTable, false, true);
	}
	public static final ClassBaseTypeUpdater NewVersionableToIdentifiableInstance(String stepName, String tableName, boolean includeAudTable){
		return new ClassBaseTypeUpdater(stepName, tableName, includeAudTable, true, true);
	}

	protected ClassBaseTypeUpdater(String stepName, String tableName, boolean includeAudit, boolean includeAnnotatable, boolean includeIdentifiable) {
		super(stepName, tableName, includeAudit);
		this.includeAnnotatableEntity = includeAnnotatable;
		this.includeIdentifiableEntity = includeIdentifiable;
		TableCreator.makeMnTables(mnTablesStepList, tableName, includeAnnotatable, includeIdentifiable);
		makeColumns();
	}


	private void makeColumns() {
		String innerStepName;
		String newColumnName;
		ColumnAdder adder;
		if (this.includeIdentifiableEntity){

			//lsid authority
			innerStepName = "-add lsid_authority";
			newColumnName = "lsid_authority";
			adder = ColumnAdder.NewStringInstance(stepName + innerStepName, tableName,
					newColumnName, SchemaUpdaterBase.INCLUDE_AUDIT);
			this.columnAdderStepList.add(adder);

			//lsid lsid
			innerStepName = "-add lsid_lsid";
			newColumnName = "lsid_lsid";
			adder = ColumnAdder.NewStringInstance(stepName + innerStepName, tableName,
					newColumnName, SchemaUpdaterBase.INCLUDE_AUDIT);
			this.columnAdderStepList.add(adder);

			//lsid namespace
			innerStepName = "-add lsid_namespace";
			newColumnName = "lsid_namespace";
			adder = ColumnAdder.NewStringInstance(stepName + innerStepName, tableName,
					newColumnName, SchemaUpdaterBase.INCLUDE_AUDIT);
			this.columnAdderStepList.add(adder);

			//lsid object
			innerStepName = "-add lsid_object";
			newColumnName = "lsid_object";
			adder = ColumnAdder.NewStringInstance(stepName + innerStepName, tableName,
					newColumnName, SchemaUpdaterBase.INCLUDE_AUDIT);
			this.columnAdderStepList.add(adder);

			//lsid revision
			innerStepName = "-add lsid_revision";
			newColumnName = "lsid_revision";
			adder = ColumnAdder.NewStringInstance(stepName + innerStepName, tableName,
					newColumnName, SchemaUpdaterBase.INCLUDE_AUDIT);
			this.columnAdderStepList.add(adder);

			//protected title cache
			innerStepName = "-add protected title cache";
			newColumnName = "protectedTitleCache";
			adder = ColumnAdder.NewBooleanInstance(innerStepName, tableName, newColumnName,
					SchemaUpdaterBase.INCLUDE_AUDIT, false);
			this.columnAdderStepList.add(adder);

			//title cache
			innerStepName = "-add titleCache";
			newColumnName = "titleCache";
			adder = ColumnAdder.NewStringInstance(stepName + innerStepName, tableName,
					newColumnName, SchemaUpdaterBase.INCLUDE_AUDIT);
			this.columnAdderStepList.add(adder);
		}
	}

	@Override
	protected void invokeOnTable(String tableName, ICdmDataSource datasource,
	        IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result)  {
		//we only do have inner steps here
		return;
	}


	@Override
	public List<ISchemaUpdaterStep> getInnerSteps() {
		List<ISchemaUpdaterStep> result = new ArrayList<>( mnTablesStepList);
		result.addAll(columnAdderStepList);
		return result;
	}

}
