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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @since 16.09.2010
 *
 */
public class TableCreator extends AuditedSchemaUpdaterStepBase {
	private static final Logger logger = Logger.getLogger(TableCreator.class);

	private static final boolean IS_LIST = true;
	private static final boolean IS_1_TO_M = true;
	private static final boolean IS_M_TO_M = false;


	private final List<String> columnNames;
	private final List<String> columnTypes;
	private final List<Object> defaultValues;
	private final List<Boolean> isNotNull;
	private final List<String> referencedTables;
	private final boolean includeCdmBaseAttributes;
	private final boolean includeIdentifiableEntity;
	private final boolean includeAnnotatableEntity;
	private boolean includeEventBase;
	private final boolean excludeVersionableAttributes;
	protected List<ColumnAdder> columnAdders = new ArrayList<>();
	protected List<ISchemaUpdaterStep> mnTablesStepList = new ArrayList<>();
	private String primaryKeyParams;
	private String primaryKeyParams_AUD;
	private String uniqueParams;
	private String uniqueParams_AUD;


	public static final TableCreator NewInstance(String stepName, String tableName, List<String> columnNames, List<String> columnTypes, boolean includeAudTable, boolean includeCdmBaseAttributes){
		return new TableCreator(stepName, tableName, columnNames, columnTypes, null, null, null, includeAudTable, includeCdmBaseAttributes, false, false, false);
	}

	public static final TableCreator NewInstance(String stepName, String tableName, String[] columnNames, String[] columnTypes, String[] referencedTables, boolean includeAudTable, boolean includeCdmBaseAttributes){
		return new TableCreator(stepName, tableName, Arrays.asList(columnNames), Arrays.asList(columnTypes), null, null, Arrays.asList(referencedTables), includeAudTable, includeCdmBaseAttributes, false, false, false);
	}

	public static final TableCreator NewNonVersionableInstance(String stepName, String tableName, String[] columnNames, String[] columnTypes, String[] referencedTables){
		return new TableCreator(stepName, tableName, Arrays.asList(columnNames), Arrays.asList(columnTypes), null, null, Arrays.asList(referencedTables), false, true, false, false, true);
	}

	public static final TableCreator NewVersionableInstance(String stepName, String tableName, String[] columnNames, String[] columnTypes, String[] referencedTables, boolean includeAudTable){
		return new TableCreator(stepName, tableName, Arrays.asList(columnNames), Arrays.asList(columnTypes), null, null, Arrays.asList(referencedTables), includeAudTable, true, false, false, false);
	}

	public static final TableCreator NewAnnotatableInstance(String stepName, String tableName, String[] columnNames, String[] columnTypes, String[] referencedTables, boolean includeAudTable){
		return new TableCreator(stepName, tableName, Arrays.asList(columnNames), Arrays.asList(columnTypes), null, null, Arrays.asList(referencedTables), includeAudTable, true, true, false, false);
	}

	public static final TableCreator NewEventInstance(String stepName, String tableName, String[] columnNames, String[] columnTypes, String[] referencedTables, boolean includeAudTable){
		TableCreator result = new TableCreator(stepName, tableName, Arrays.asList(columnNames), Arrays.asList(columnTypes), null, null, Arrays.asList(referencedTables), includeAudTable, true, true, false, false);
		result.includeEventBase = true;
		return result;
	}

	public static final TableCreator NewIdentifiableInstance(String stepName, String tableName, String[] columnNames, String[] columnTypes, String[] referencedTables, boolean includeAudTable){
		return new TableCreator(stepName, tableName, Arrays.asList(columnNames), Arrays.asList(columnTypes), null, null, Arrays.asList(referencedTables), includeAudTable, true, true, true, false);
	}

	protected TableCreator(String stepName, String tableName, List<String> columnNames, List<String> columnTypes, List<Object> defaultValues, List<Boolean> isNotNull, List<String> referencedTables,
			boolean includeAudTable, boolean includeCdmBaseAttributes, boolean includeAnnotatableEntity, boolean includeIdentifiableEntity, boolean excludeVersionableAttributes) {
		super(stepName, tableName, includeAudTable);
		this.columnNames = columnNames;
		this.columnTypes = columnTypes;
		this.defaultValues = defaultValues;
		this.isNotNull = isNotNull;
		this.referencedTables = referencedTables;
		this.includeCdmBaseAttributes = includeCdmBaseAttributes;
		this.includeAnnotatableEntity = includeAnnotatableEntity;
		this.includeIdentifiableEntity = includeIdentifiableEntity;
		this.excludeVersionableAttributes = excludeVersionableAttributes;
		makeColumnAdders();
		makeMnTables(mnTablesStepList, this.tableName, this.includeAnnotatableEntity, this.includeIdentifiableEntity);
	}


	@Override
	public List<ISchemaUpdaterStep> getInnerSteps() {
		return mnTablesStepList;
	}

	/**
	 * Fills the {@link #columnAdders} list.
	 */
	private void makeColumnAdders() {
		if (columnNames.size() != columnTypes.size()){
			throw new RuntimeException ("ColumnNames and columnTypes must be of same size. Step: " + getStepName());
		}

		try {
			for (int i = 0; i < columnNames.size(); i++){
				boolean isNotNull = this.isNotNull == null ? false : this.isNotNull.get(i);
				if ("integer".equals(columnTypes.get(i)) || "int".equals(columnTypes.get(i))){
					String referencedTable = (this.referencedTables == null) ?  null : this.referencedTables.get(i);
					ColumnAdder adder = ColumnAdder.NewIntegerInstance(this.getStepName(), this.tableName, this.columnNames.get(i), includeAudTable, isNotNull, referencedTable);
					this.columnAdders.add(adder);
				}else if ("boolean".equals(columnTypes.get(i)) || "bit".equals(columnTypes.get(i))){
					String defaultValue = this.defaultValues == null ? null : this.defaultValues.get(i).toString();
					ColumnAdder adder = ColumnAdder.NewBooleanInstance(getStepName(), this.tableName,  this.columnNames.get(i), includeAudTable, Boolean.valueOf(defaultValue));
					this.columnAdders.add(adder);
				}else if (columnTypes.get(i).startsWith("string")){
					Integer length = Integer.valueOf(columnTypes.get(i).substring("string_".length()));
					ColumnAdder adder = ColumnAdder.NewStringInstance(this.getStepName(), this.tableName, this.columnNames.get(i), length, includeAudTable);
					this.columnAdders.add(adder);
				}else if (columnTypes.get(i).startsWith("clob")){
					ColumnAdder adder = ColumnAdder.NewClobInstance(this.getStepName(), this.tableName, this.columnNames.get(i), includeAudTable);
					this.columnAdders.add(adder);
				}else if ("tinyint".equals(columnTypes.get(i)) ){
					ColumnAdder adder = ColumnAdder.NewTinyIntegerInstance(this.getStepName(), this.tableName, this.columnNames.get(i), includeAudTable, isNotNull);
					this.columnAdders.add(adder);
				}else if ("datetime".equals(columnTypes.get(i)) ){
					ColumnAdder adder = ColumnAdder.NewDateTimeInstance(this.getStepName(), this.tableName, this.columnNames.get(i), includeAudTable, isNotNull);
					this.columnAdders.add(adder);
				}else if ("double".equals(columnTypes.get(i)) ){
					ColumnAdder adder = ColumnAdder.NewDoubleInstance(this.getStepName(), this.tableName, this.columnNames.get(i), includeAudTable, isNotNull);
					this.columnAdders.add(adder);
				}else{
					throw new RuntimeException("Column type " + columnTypes.get(i) + " not yet supported");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * fills the mnTablesStepList
	 * @param mnTablesStepList, String tableName
	 */
	public static void makeMnTables(List<ISchemaUpdaterStep> mnTablesStepList, String tableName, boolean includeAnnotatable, boolean includeIdentifiable) {
		TableCreator tableCreator;
		String stepName;

		if (includeAnnotatable){
			//annotations
			stepName= "Add @tableName annotations";
			stepName = stepName.replace("@tableName", tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, tableName, "Annotation", SchemaUpdaterBase.INCLUDE_AUDIT, !IS_LIST, IS_1_TO_M);
			mnTablesStepList.add(tableCreator);

			//marker
			stepName= "Add @tableName marker";
			stepName = stepName.replace("@tableName", tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, tableName, "Marker", SchemaUpdaterBase.INCLUDE_AUDIT, !IS_LIST, IS_1_TO_M);
			mnTablesStepList.add(tableCreator);
		}

		if (includeIdentifiable){

			//credits
			stepName= "Add @tableName credits";
			stepName = stepName.replace("@tableName", tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, tableName, null, "Credit", null, null, SchemaUpdaterBase.INCLUDE_AUDIT, IS_LIST, IS_1_TO_M);
			mnTablesStepList.add(tableCreator);

			//identifier
			stepName= "Add @tableName identifiers";
			stepName = stepName.replace("@tableName", tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, tableName, null, "Identifier", null, null, SchemaUpdaterBase.INCLUDE_AUDIT, IS_LIST, IS_1_TO_M);
			mnTablesStepList.add(tableCreator);

			//extensions
			stepName= "Add @tableName extensions";
			stepName = stepName.replace("@tableName", tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, tableName, "Extension", SchemaUpdaterBase.INCLUDE_AUDIT, !IS_LIST, IS_1_TO_M);
			mnTablesStepList.add(tableCreator);

			//OriginalSourceBase
			stepName= "Add @tableName sources";
			stepName = stepName.replace("@tableName", tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, tableName, null, "OriginalSourceBase", null, "sources", SchemaUpdaterBase.INCLUDE_AUDIT, ! IS_LIST, IS_1_TO_M);
			mnTablesStepList.add(tableCreator);

			//Rights
			stepName= "Add @tableName rights";
			stepName = stepName.replace("@tableName", tableName);
			tableCreator = MnTableCreator.NewMnInstance(stepName, tableName, null, "Rights", "RightsInfo", "rights", SchemaUpdaterBase.INCLUDE_AUDIT, !IS_LIST, IS_M_TO_M);
            mnTablesStepList.add(tableCreator);
		}
	}


	@Override
	protected void invokeOnTable(String tableName, ICdmDataSource datasource,
	        IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result)  {
		try {
			//CREATE
			String updateQuery = "CREATE TABLE @tableName (";
			//AUDIT
			if (isAuditing){
				updateQuery += " REV integer not null, revtype " + ColumnAdder.getDatabaseColumnType(datasource, "tinyint") + ", ";
			}
			//CdmBase
			if (includeCdmBaseAttributes){
					updateQuery += " id integer NOT NULL,"
						+ " created " + ColumnAdder.getDatabaseColumnType(datasource, "datetime") + ", "
						+ " uuid varchar(36) NOT NULL,"
						+ (excludeVersionableAttributes? "" : " updated " + ColumnAdder.getDatabaseColumnType(datasource, "datetime") + ", ")
						+ " createdby_id integer,"
						+ (excludeVersionableAttributes ? "" : " updatedby_id integer, ");
			}
			//EventBase
			if (this.includeEventBase){
				updateQuery += "timeperiod_start varchar(255), timeperiod_end varchar(255), timeperiod_freetext varchar(255), actor_id int, description varchar(255),";
			}
			//Identifiable
			if (this.includeIdentifiableEntity){
				updateQuery += "lsid_authority varchar(255), lsid_lsid varchar(255), lsid_namespace varchar(255), lsid_object varchar(255), lsid_revision varchar(255), protectedtitlecache bit not null, titleCache varchar(255),";
			}
			//specific columns
			updateQuery += 	getColumnsSql(tableName, datasource, monitor);

			//primary and unique keys
			String primaryKeySql = primaryKey(isAuditing)==null ? "" : "primary key (" + primaryKey(isAuditing) + "),";
			String uniqueSql = unique(isAuditing)== null ? "" : "unique(" + unique(isAuditing) + "),";
			updateQuery += primaryKeySql + uniqueSql;

			//finalize
			updateQuery = StringUtils.chomp(updateQuery.trim(), ",") + ")";

			//replace
			updateQuery = updateQuery.replace("@tableName", tableName);

			//append datasource specific string
			updateQuery += datasource.getDatabaseType().getHibernateDialect().getTableTypeString();
			logger.debug("UPDATE Query: " + updateQuery);

			//execute
			datasource.executeUpdate(updateQuery);

			//Foreign Keys
			createForeignKeys(tableName, isAuditing, datasource, monitor, caseType, result);

			return;
		} catch (Exception e) {
			monitor.warning(e.getMessage(), e);
			logger.error(e);
			result.addException(e, e.getMessage(), "TableCreator.invokeOnTable");
			return;
		}
	}


	/**
	 * Returns the sql part for the {@link #columnAdders} columns.
	 * This is done by reusing the same method in the ColumnAdder class and removing all the prefixes like 'ADD COLUMN'
	 */
	private String getColumnsSql(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException {
		String result = "";
		for (ColumnAdder adder : this.columnAdders){
			String singleAdderSQL = adder.getUpdateQueryString(tableName, datasource, monitor) + ", ";

			String[] split = singleAdderSQL.split(ColumnAdder.getAddColumnSeperator(datasource));
			result += split[1];
		}
		return result;
	}


	private void createForeignKeys(String tableName, boolean isAudit, ICdmDataSource datasource,
	        IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) {
		if (includeCdmBaseAttributes){
			//updatedBy
		    if (! this.excludeVersionableAttributes){
				String attribute = "updatedby";
				String referencedTable = "UserAccount";
				makeForeignKey(tableName, datasource, monitor, attribute,
				        referencedTable, caseType, result);
			}

		    //createdBy
			String attribute = "createdby";
			String referencedTable = "UserAccount";
			makeForeignKey(tableName, datasource, monitor, attribute,
			        referencedTable, caseType, result);

		}
		if (isAudit){
		    //REV
			String attribute = "REV";
			String referencedTable = "AuditEvent";
			makeForeignKey(tableName, datasource, monitor, attribute,
			        referencedTable, caseType, result);
		}
		if (this.includeEventBase){
			//actor
		    String attribute = "actor_id";
			String referencedTable = "AgentBase";
			makeForeignKey(tableName, datasource, monitor, attribute,
			        referencedTable, caseType, result);
		}
		for (ColumnAdder adder : this.columnAdders){
			if (adder.getReferencedTable() != null){
				makeForeignKey(tableName, datasource, monitor, adder.getNewColumnName(),
				        adder.getReferencedTable(), caseType, result);
			}
		}
		return;
	}


    public static void makeForeignKey(String tableName, ICdmDataSource datasource,
            IProgressMonitor monitor, String attribute, String referencedTable, CaseType caseType,
            SchemaUpdateResult result) {

		referencedTable = caseType.transformTo(referencedTable);

        String idSuffix = "_id";
        if (isRevAttribute(attribute) || attribute.endsWith(idSuffix)){
            idSuffix = "";
        }
        String columnName =  attribute + idSuffix;

		if (supportsForeignKeys(datasource, monitor, tableName, referencedTable)){
			String index = "FK@tableName_@attribute";
			index = index.replace("@tableName", tableName);
			index = index.replace("@attribute", attribute);


			//OLD - don't remember why we used ADD INDEX here
//			String updateQuery = "ALTER TABLE @tableName ADD INDEX @index (@attribute), ADD FOREIGN KEY (@attribute) REFERENCES @referencedTable (@id)";
			String updateQuery = "ALTER TABLE @tableName ADD @constraintName FOREIGN KEY (@attribute) REFERENCES @referencedTable (@id)";
			updateQuery = updateQuery.replace("@tableName", tableName);
//			updateQuery = updateQuery.replace("@index", index);
			updateQuery = updateQuery.replace("@attribute", columnName);
			updateQuery = updateQuery.replace("@referencedTable", referencedTable);
			if (datasource.getDatabaseType().equals(DatabaseTypeEnum.MySQL)){
				updateQuery = updateQuery.replace("@constraintName", "CONSTRAINT " + index);
			}else{
				updateQuery = updateQuery.replace("@constraintName", "");  //H2 does not support "CONSTRAINT", didn't check for others
			}

			if (isRevAttribute(attribute)){
				updateQuery = updateQuery.replace("@id", "revisionnumber");
			}else{
				updateQuery = updateQuery.replace("@id", "id");
			}
			logger.debug(updateQuery);
			try {
				datasource.executeUpdate(updateQuery);
			} catch (Exception e) {
				String message = "Problem when creating Foreign Key for " + tableName +"." + attribute +": " + e.getMessage();
				monitor.warning(message);
				logger.warn(message, e);
				result.addWarning(message);
				return;   //we do not interrupt update if only foreign key generation did not work
			}
			return;
		}else{
		    //create only index
			IndexAdder indexAdder = IndexAdder.NewIntegerInstance("Add index instead of Foreign Key", tableName, columnName);
			try {
                indexAdder.invoke(datasource, monitor, caseType, result);
            } catch (Exception e) {
                String message = "Problem when creating index instead of Foreign Key for " + tableName +"." + columnName +": " + e.getMessage();
                monitor.warning(message);
                logger.warn(message, e);
                result.addWarning(message);
                return;   //we do not interrupt update if only index generation did not work
            }
		    return;
		}
	}

	/**
	 * Determines if the tables and the database support foreign keys. If determination is not possible true is returned as default.
	 * @param datasource
	 * @param monitor
	 * @param tableName
	 * @param referencedTable
	 * @return
	 */
	private static boolean supportsForeignKeys(ICdmDataSource datasource, IProgressMonitor monitor, String tableName, String referencedTable) {
		boolean result = true;
		if (! datasource.getDatabaseType().equals(DatabaseTypeEnum.MySQL)){
			return true;
		}else{
			try {
				String myIsamTables = "";
				String format = "SELECT ENGINE FROM information_schema.TABLES where TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'";
				String sql = String.format(format, datasource.getDatabase(), tableName);
				String engine = (String)datasource.getSingleValue(sql);
				if (engine.equals("MyISAM")){
					result = false;
					myIsamTables = CdmUtils.concat(",", myIsamTables, tableName);
				}
				sql = String.format(format,  datasource.getDatabase(), referencedTable);
				engine = (String)datasource.getSingleValue(sql);
				if (engine.equals("MyISAM")){
					result = false;
					myIsamTables = CdmUtils.concat(",", myIsamTables, referencedTable);
				}
				if (result == false){
					String message = "Tables (%s) use MyISAM engine. MyISAM does not support foreign keys.";
					message = String.format(message, myIsamTables);
					monitor.warning(message);
				}
				return result;
			} catch (Exception e) {
				String message = "Problems to determine table engine for MySQL.";
				monitor.warning(message);
				return true;  //default
			}
		}
	}

	private static boolean isRevAttribute(String attribute) {
		return "REV".equalsIgnoreCase(attribute);
	}


	/**
	 * Constructs the primary key creation string
	 * @param isAudit
	 * @return
	 */
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

	/**
	 * Constructs the unique key creation string
	 * @param isAudit
	 * @return
	 */
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
