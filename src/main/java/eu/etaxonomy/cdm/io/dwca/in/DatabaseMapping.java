/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * TODO inwork, currently it works with H2
 * @author a.mueller
 * @created 22.03.2012
 *
 */
public class DatabaseMapping implements IImportMapping {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DatabaseMapping.class);

	private static final String DATABASE_INTERNAL_IMPORT_MAPPING = "_internalImportMapping";
	protected static final String TABLE_IMPORT_MAPPING  = "importmapping";

	private static final String COL_TASK_ID = "task_id";

	private static final String COL_SOURCE_NS = "source_namespace";

	private static final String COL_SOURCE_ID = "source_id";

	private static final String COL_DEST_NS = "destination_namespace";

	private static final String COL_DEST_ID = "destination_id"; 
	
	private ICdmDataSource datasource;
	private String mappingId;
	private Map<String, Class> shortCuts = new HashMap<String, Class>();
	private Map<Class, String> reverseShortCuts = new HashMap<Class, String>();
	
	
	@Override
	public void putMapping(String namespace, Integer sourceKey, IdentifiableEntity destinationObject){
		putMapping(namespace, String.valueOf(sourceKey), destinationObject);
	}
		

	/**
	 * @param database
	 */
	public DatabaseMapping(String mappingId) {
		super();
		initDatasource();
		this.mappingId = mappingId;
	}

	@Override
	public void putMapping(String namespace, String sourceKey, IdentifiableEntity destinationObject){
		CdmKey<IdentifiableEntity<?>> cdmKey = new CdmKey(destinationObject);
		putMapping(namespace, sourceKey, cdmKey);
	}
	
	public void putMapping(String namespace, String sourceKey, CdmKey<IdentifiableEntity<?>> cdmKey) {
		try {
			deleteExistingMapping(namespace, sourceKey);
			persistNotExistingMapping(namespace, sourceKey, cdmKey);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private int persistNotExistingMapping(String sourceNamespace, String sourceId, CdmKey<IdentifiableEntity<?>> cdmKey) throws SQLException {
		
		//cdm namespace
		String clazz = getCdmClassStr(cdmKey.clazz);
		//insert
		String insertMappingSql = " INSERT INTO %s (%s, %s, %s, %s, %s)" +
			" VALUES ('%s','%s','%s','%s','%s')";
		insertMappingSql = String.format(insertMappingSql,
				TABLE_IMPORT_MAPPING, COL_TASK_ID, COL_SOURCE_NS, COL_SOURCE_ID, COL_DEST_NS, COL_DEST_ID,
				this.mappingId, sourceNamespace, sourceId, clazz, cdmKey.id);
		return this.datasource.executeUpdate(insertMappingSql);
	}


	/**
	 * @param cdmKey
	 * @return
	 */
	private String getCdmClassStr(Class cdmClass) {
		String clazz = reverseShortCuts.get(cdmClass);
		if (clazz == null){
			clazz = cdmClass.getCanonicalName();
		}
		return clazz;
	}


	private int deleteExistingMapping(String sourceNamespace, String sourceId) throws SQLException {
		String deleteMappingSql = " DELETE FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = '%s'";
		deleteMappingSql = String.format(deleteMappingSql,TABLE_IMPORT_MAPPING, COL_TASK_ID, this.mappingId, COL_SOURCE_NS, sourceNamespace, COL_SOURCE_ID, sourceId);
		return this.datasource.executeUpdate(deleteMappingSql);
	}


	private int deleteAll() throws SQLException {
		String deleteMappingSql = " DELETE FROM %s WHERE %s = '%s' ";
		deleteMappingSql = String.format(deleteMappingSql,TABLE_IMPORT_MAPPING, COL_TASK_ID, this.mappingId);
		return this.datasource.executeUpdate(deleteMappingSql);
	}
	
	public int size() throws SQLException {
		String sql = " SELECT count(*) as n FROM %s WHERE %s = '%s' ";
		sql = String.format(sql,TABLE_IMPORT_MAPPING, COL_TASK_ID, this.mappingId);
		ResultSet rs = this.datasource.executeQuery(sql);
		rs.next();
		return rs.getInt("n");
		
	}

	@Override
	public Set<CdmKey> get(String sourceNamespace, String sourceId) {
		Set<CdmKey> result = new HashSet<CdmKey>();
		String selectMappingSql = " SELECT %s, %s FROM %s" +
				" WHERE %s = '%s' AND %s = '%s' AND %s = '%s' ";
		selectMappingSql = String.format(selectMappingSql,
				COL_DEST_NS, COL_DEST_ID, TABLE_IMPORT_MAPPING, 
				COL_TASK_ID, this.mappingId, COL_SOURCE_NS, sourceNamespace, 
				COL_SOURCE_ID , sourceId);
		try {
			ResultSet rs = this.datasource.executeQuery(selectMappingSql);
			while (rs.next()){
				String clazzStr = rs.getString(COL_DEST_NS);
				Object id = rs.getObject(COL_DEST_ID);
				if (id == null){
					throw new RuntimeException("Destination id for import mapping is 'null'");
				}
				
				Class clazz = getCdmClass(clazzStr);
				
				CdmKey<?> key = new CdmKey(clazz, Integer.valueOf(String.valueOf(id)));
				result.add(key);
			}
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}

	public boolean exists(String sourceNamespace, String sourceId, Class<?> destinationClass){
		String selectMappingSql = " SELECT count(*) as n FROM %s" +
			" WHERE %s = '%s' AND %s = '%s' AND %s = '%s' AND %s = '%s' ";
		
		String cdmClass = getCdmClassStr(destinationClass);
		
		selectMappingSql = String.format(selectMappingSql,
			TABLE_IMPORT_MAPPING, COL_TASK_ID, this.mappingId, 
			COL_SOURCE_NS, sourceNamespace, COL_SOURCE_ID , sourceId, COL_DEST_NS, cdmClass);
		try {
			ResultSet rs = this.datasource.executeQuery(selectMappingSql);
			rs.next();
			int n = rs.getInt("n");
			
			return n > 0;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public InMemoryMapping getPartialMapping( Map<String, Set<String>> namespacedSourceKeys) {
		InMemoryMapping partialMapping = new InMemoryMapping();
		for (Entry<String,Set<String>> entry  : namespacedSourceKeys.entrySet()){
			String namespace = entry.getKey();
			for (String sourceKey : entry.getValue() ){
				Set<CdmKey> destObjects = this.get(namespace, sourceKey);
				for (CdmKey cdmKey : destObjects){
					partialMapping.putMapping(namespace, sourceKey, cdmKey);
				}
			}
		}
		return partialMapping;
	}


	@Override
	public void finish() {
		try {
			int count = size();
			deleteAll();
			System.out.println("Finish" +  count +  ":" + size());
		} catch (SQLException e) {
			throw new RuntimeException();
		}
	}

	
	/**
	 * @param clazzStr
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class getCdmClass(String clazzStr) throws ClassNotFoundException {
		Class clazz = shortCuts.get(clazzStr);
		if (clazz == null){
			clazz = Class.forName(clazzStr);
		}
		return clazz;
	}
	
	
	private void initDatasource() {
		getDatabase();
		shortCuts.put("BotanicalName", BotanicalName.class);
		shortCuts.put("ZoologicalName", ZoologicalName.class);
		shortCuts.put("Taxon", Taxon.class);
		shortCuts.put("Synonym", Synonym.class);
		shortCuts.put("Reference", Reference.class);
		shortCuts.put("Team", Team.class);
		shortCuts.put("Person", Person.class);
		//reverse
		for (String key :shortCuts.keySet()){
			reverseShortCuts.put(shortCuts.get(key), key);
		}
	}

	
	public ICdmDataSource getDatabase(){
		try {
			try {
				datasource = CdmPersistentDataSource.NewInstance(DATABASE_INTERNAL_IMPORT_MAPPING);
			} catch (DataSourceNotFoundException e) {
				datasource = CdmDataSource.NewH2EmbeddedInstance("_tmpMapping", "a", "b");
				CdmPersistentDataSource.save(DATABASE_INTERNAL_IMPORT_MAPPING, datasource);
			}
			datasource.executeQuery("SELECT * FROM " + TABLE_IMPORT_MAPPING);
		} catch (SQLException e) {
			try {
				String strCreateTable = "CREATE TABLE IF NOT EXISTS %s (";
				strCreateTable += "%s nvarchar(36) NOT NULL,";
				strCreateTable += "%s nvarchar(100) NOT NULL,";
				strCreateTable += "%s nvarchar(100) NOT NULL,";
				strCreateTable += "%s nvarchar(100) NOT NULL,";
				strCreateTable += "destination_id nvarchar(50) NOT NULL,";
				strCreateTable += "PRIMARY KEY (task_id, source_namespace, source_id)";
				strCreateTable += ") ";
				strCreateTable = String.format(strCreateTable, TABLE_IMPORT_MAPPING, COL_TASK_ID, COL_SOURCE_NS, COL_SOURCE_ID, COL_DEST_NS, COL_DEST_ID);
				datasource.executeUpdate(strCreateTable);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return datasource;
	}

	
}
