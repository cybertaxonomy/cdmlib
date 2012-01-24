// $Id$
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class TableCreator extends SchemaUpdaterStepBase implements ISchemaUpdaterStep {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TableCreator.class);
	
	private static final boolean SORT_INDEX = true;
	
	protected String tableName;
	private List<String> columnNames;
	private List<String> columnTypes;
	private List<Object> defaultValues;
	private List<Boolean> isNotNull;
	private List<String> referencedTables;
	private boolean includeAudTable;
	private boolean includeCdmBaseAttributes;
	private boolean includeIdentifiableEntity;
	protected List<ColumnAdder> columnAdders = new ArrayList<ColumnAdder>();
	protected List<ISchemaUpdaterStep> mnTablesStepList = new ArrayList<ISchemaUpdaterStep>();
	private String primaryKeyParams;
	private String primaryKeyParams_AUD;
	private String uniqueParams;
	private String uniqueParams_AUD;

	
//	public static final TableCreator NewInstance(String stepName, String tableName, List<String> columnNames, List<String> columnTypes, List<Object> defaultValues, List<Boolean> isNull, boolean includeAudTable){
	public static final TableCreator NewInstance(String stepName, String tableName, List<String> columnNames, List<String> columnTypes, boolean includeAudTable, boolean includeCdmBaseAttributes){
		return new TableCreator(stepName, tableName, columnNames, columnTypes, null, null, null, includeAudTable, includeCdmBaseAttributes, false);
	}
	
	public static final TableCreator NewInstance(String stepName, String tableName, String[] columnNames, String[] columnTypes, String[] referencedTables, boolean includeAudTable, boolean includeCdmBaseAttributes){
		return new TableCreator(stepName, tableName, Arrays.asList(columnNames), Arrays.asList(columnTypes), null, null, Arrays.asList(referencedTables), includeAudTable, includeCdmBaseAttributes, false);
	}
	
	public static final TableCreator NewIdentifiableInstance(String stepName, String tableName, String[] columnNames, String[] columnTypes, String[] referencedTables, boolean includeAudTable){
		return new TableCreator(stepName, tableName, Arrays.asList(columnNames), Arrays.asList(columnTypes), null, null, Arrays.asList(referencedTables), includeAudTable, true, true);
	}
	
	protected TableCreator(String stepName, String tableName, List<String> columnNames, List<String> columnTypes, List<Object> defaultValues, List<Boolean> isNotNull, List<String> referencedTables, boolean includeAudTable, boolean includeCdmBaseAttributes, boolean includeIdentifiableEntity) {
		super(stepName);
		this.tableName = tableName;
		this.columnNames = columnNames;
		this.columnTypes = columnTypes;
		this.defaultValues = defaultValues;
		this.isNotNull = isNotNull;
		this.referencedTables = referencedTables;
		this.includeAudTable = includeAudTable;
		this.includeCdmBaseAttributes = includeCdmBaseAttributes;
		this.includeIdentifiableEntity = includeIdentifiableEntity;
		makeColumnAdders();
		makeMnTables();
	}


	private void makeColumnAdders() {
		if (columnNames.size() != columnTypes.size()){
			throw new RuntimeException ("ColumnNames and columnTypes must be of same size. Step: " + getStepName());
		}
			
		for (int i = 0; i < columnNames.size(); i++){
			boolean isNotNull = this.isNotNull == null ? false : this.isNotNull.get(i);
			if ("integer".equals(columnTypes.get(i)) || "int".equals(columnTypes.get(i))){
				String referencedTable = (this.referencedTables == null) ?  null : this.referencedTables.get(i);
				ColumnAdder adder = ColumnAdder.NewIntegerInstance(this.getStepName(), this.tableName, this.columnNames.get(i), includeAudTable, isNotNull, referencedTable);
				this.columnAdders.add(adder);
			}else if ("boolean".equals(columnTypes.get(i)) || "bit".equals(columnTypes.get(i))){
				ColumnAdder adder = ColumnAdder.NewBooleanInstance(getStepName(), this.tableName,  this.columnNames.get(i), includeAudTable, Boolean.valueOf(this.defaultValues.get(i).toString()));
				this.columnAdders.add(adder);
			}else if (columnTypes.get(i).startsWith("string")){
				Integer length = Integer.valueOf(columnTypes.get(i).substring("string_".length()));
				ColumnAdder adder = ColumnAdder.NewStringInstance(this.getStepName(), this.tableName, this.columnNames.get(i), length, includeAudTable);
				this.columnAdders.add(adder);
			}else if ("tinyint".equals(columnTypes.get(i)) ){
				ColumnAdder adder = ColumnAdder.NewTinyIntegerInstance(this.getStepName(), this.tableName, this.columnNames.get(i), includeAudTable, isNotNull);
				this.columnAdders.add(adder);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		try {
			boolean result = true;
			result &= createTable(tableName, false, datasource, monitor);
			if (includeAudTable){
				String aud = "_AUD";
				result &= createTable(tableName + aud, true, datasource, monitor);
			}
//			result &= invokeMns();
			return (result == true )? 0 : null;
		} catch (DatabaseTypeNotSupportedException e) {
			throw new SQLException(e);
		}
	}



	@Override
	public List<ISchemaUpdaterStep> getInnerSteps() {
		return mnTablesStepList;
	}

	private String getColumnsSql(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException {
		String result = "";
		for (ColumnAdder adder : this.columnAdders){
			String singleAdderSQL = adder.getUpdateQueryString(tableName, datasource, monitor) + ", ";
			
			String[] split = singleAdderSQL.split(ColumnAdder.getAddColumnSeperator(datasource));
			result += split[1];
		}
		return result;
	}

	private boolean createTable(String tableName, boolean isAudit, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException {
		String updateQuery = "CREATE TABLE @tableName (";
		if (isAudit){
			updateQuery += " REV integer not null, revtype tinyint, ";
		}
		if (includeCdmBaseAttributes){
				updateQuery += " id integer not null,"
					+ " created datetime, "
					+ " uuid varchar(36),"
					+ " updated datetime, "
					+ " createdby_id integer,"
					+ " updatedby_id integer, ";
				
		}
		if (this.includeIdentifiableEntity){
			updateQuery += "lsid_authority varchar(255), lsid_lsid varchar(255), lsid_namespace varchar(255), lsid_object varchar(255), lsid_revision varchar(255), protectedtitlecache bit not null, titleCache varchar(255),";
		}
		
		updateQuery += 	getColumnsSql(tableName, datasource, monitor);
		
		String primaryKeySql = primaryKey(isAudit)==null ? "" : "primary key (" + primaryKey(isAudit) + "),";
		String uniqueSql = unique(isAudit)== null ? "" : "unique(" + unique(isAudit) + "),";
		updateQuery += primaryKeySql + uniqueSql;
		
		updateQuery = StringUtils.chomp(updateQuery.trim(), ",");
		updateQuery += ")";
		
		updateQuery = updateQuery.replace("@tableName", tableName);
		if (datasource.getDatabaseType().equals(DatabaseTypeEnum.MySQL)){
			updateQuery += " ENGINE=MYISAM DEFAULT CHARSET=utf8 ";
		}
		logger.debug(updateQuery);
		datasource.executeUpdate(updateQuery);
		createForeignKeys(tableName, isAudit, datasource, monitor);
		return true;
	}


	private void makeMnTables() {
		if (this.includeIdentifiableEntity){
			TableCreator tableCreator;
			//annotations
			stepName= "Add @tableName annotations";
			stepName = stepName.replace("@tableName", this.tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, this.tableName, "Annotation", SchemaUpdaterBase.INCLUDE_AUDIT);
			mnTablesStepList.add(tableCreator);

			//credits
			stepName= "Add @tableName credits";
			stepName = stepName.replace("@tableName", this.tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, this.tableName, null, "Credit", null, SchemaUpdaterBase.INCLUDE_AUDIT, SORT_INDEX, false);
			mnTablesStepList.add(tableCreator);
			
			//extensions
			stepName= "Add @tableName extensions";
			stepName = stepName.replace("@tableName", this.tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, this.tableName, "Extension", SchemaUpdaterBase.INCLUDE_AUDIT);
			mnTablesStepList.add(tableCreator);

			//marker
			stepName= "Add @tableName marker";
			stepName = stepName.replace("@tableName", this.tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, this.tableName, "Marker", SchemaUpdaterBase.INCLUDE_AUDIT);
			mnTablesStepList.add(tableCreator);
			
			//OriginalSourceBase
			stepName= "Add @tableName sources";
			stepName = stepName.replace("@tableName", this.tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, this.tableName, null, "OriginalSourceBase", "sources", SchemaUpdaterBase.INCLUDE_AUDIT, false, true);
			mnTablesStepList.add(tableCreator);

			//Rights
			stepName= "Add @tableName rights";
			stepName = stepName.replace("@tableName", this.tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, this.tableName, "Rights", SchemaUpdaterBase.INCLUDE_AUDIT);
			mnTablesStepList.add(tableCreator);

			
		}
	}
	
	private void createForeignKeys(String tableName, boolean isAudit, ICdmDataSource datasource, IProgressMonitor monitor) {
		if (includeCdmBaseAttributes){
			String attribute = "updatedby";
			String referencedTable = "UserAccount";
			makeForeignKey(tableName, datasource, attribute, referencedTable);
			
			attribute = "createdby";
			referencedTable = "UserAccount";
			makeForeignKey(tableName, datasource, attribute, referencedTable);			
		
		}
		if (isAudit){
			String attribute = "REV";
			String referencedTable = "AuditEvent";
			makeForeignKey(tableName, datasource, attribute, referencedTable);
		}
		for (ColumnAdder adder : this.columnAdders){
			if (adder.getReferencedTable() != null){
				makeForeignKey(tableName, datasource, adder.getNewColumnName(), adder.getReferencedTable()); 
			}
		}
	}

	public static void makeForeignKey(String tableName, ICdmDataSource datasource, String attribute, String referencedTable) {
		String index = "FK@tableName_@attribute";
		index = index.replace("@tableName", tableName);
		index = index.replace("@attribute", attribute);
		
		String idSuffix = "_id";
		if ("REV".equalsIgnoreCase(attribute) || attribute.endsWith(idSuffix)){
			idSuffix = "";
		}
		String updateQuery = "ALTER TABLE @tableName ADD INDEX @index (@attribute), ADD CONSTRAINT @index FOREIGN KEY (@attribute) REFERENCES @referencedTable (id)";
		updateQuery = updateQuery.replace("@tableName", tableName);
		updateQuery = updateQuery.replace("@index", index);
		updateQuery = updateQuery.replace("@attribute", attribute + idSuffix);
		updateQuery = updateQuery.replace("@referencedTable", referencedTable);
		
		logger.debug(updateQuery);
		datasource.executeUpdate(updateQuery);
	}


	protected String primaryKey(boolean isAudit){
		String result = null;
		if (! isAudit && this.primaryKeyParams != null){ 
			return this.primaryKeyParams;
		}else if (isAudit && this.primaryKeyParams_AUD != null){ 
			return this.primaryKeyParams_AUD;
		} 

		if (includeCdmBaseAttributes || ! includeCdmBaseAttributes){ //TODO how to handle not CDMBase includes
			if (! isAudit){
				result = "id";
			}else{
				result = "id, REV";
			}
		}
		return result;
	}
	
	protected String unique(boolean isAudit){
		if (! isAudit){
			if (this.uniqueParams != null){
				return this.uniqueParams;
			}
			if (includeCdmBaseAttributes){
				return "uuid"; //TODO how to handle not CDMBase includes
			}
			return null;
		}else{
			if (this.uniqueParams_AUD != null){
				return this.uniqueParams_AUD;
			}
			return null;
		}
	}

	public void setPrimaryKeyParams(String primaryKeyParams, String primaryKeyParams_AUD) {
		this.primaryKeyParams = primaryKeyParams;
		this.primaryKeyParams_AUD = primaryKeyParams_AUD;
	}

	public void setUniqueParams(String uniqueParams, String uniqueParams_AUD) {
		this.uniqueParams = uniqueParams;
		this.uniqueParams_AUD = uniqueParams_AUD;
	}
}
