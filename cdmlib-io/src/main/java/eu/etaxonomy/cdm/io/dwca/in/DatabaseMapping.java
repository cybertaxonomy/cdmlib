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
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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
    private static final Logger logger = Logger.getLogger(DatabaseMapping.class);

	private static final String DATABASE_INTERNAL_IMPORT_MAPPING = "_internalImportMapping";
	protected static final String TABLE_IMPORT_MAPPING  = "importmapping";

	private static final String COL_TASK_ID = "task_id";

	private static final String COL_SOURCE_NS = "source_namespace";

	private static final String COL_SOURCE_ID = "source_id";

	private static final String COL_DEST_NS = "destination_namespace";

	private static final String COL_DEST_ID = "destination_id";

    private static final int SOURCE_KEY_LENGTH = 255;


	private ICdmDataSource datasource;
	private final String mappingId;
	private final Map<String, Class> shortCuts = new HashMap<String, Class>();
	private final Map<Class, String> reverseShortCuts = new HashMap<Class, String>();


	@Override
	public void putMapping(String namespace, Integer sourceKey, IdentifiableEntity destinationObject){
		putMapping(namespace, String.valueOf(sourceKey), destinationObject);
	}

    public DatabaseMapping(String mappingId) {
        this(mappingId, null);
    }

	/**
	 * @param database
	 */
	public DatabaseMapping(String mappingId, String file) {
		super();
		initDatasource(file);
		this.mappingId = mappingId;
	}

	@Override
	public void putMapping(String namespace, String sourceKey, IdentifiableEntity destinationObject){
		CdmKey<IdentifiableEntity<?>> cdmKey = new CdmKey(destinationObject);
		putMapping(namespace, sourceKey, cdmKey);
	}

	public void putMapping(String namespace, String sourceKey, CdmKey<IdentifiableEntity<?>> cdmKey) {
		try {
		    String normalizedKey = normalizeKey(sourceKey);

			deleteExistingMapping(namespace, sourceKey);
			persistNotExistingMapping(namespace, normalizedKey, cdmKey);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private int persistNotExistingMapping(String sourceNamespace, String normalizedKey, CdmKey<IdentifiableEntity<?>> cdmKey) throws SQLException {

		//cdm namespace
		String clazz = getCdmClassStr(cdmKey.clazz);

		//insert
		String insertMappingSql = " INSERT INTO %s (%s, %s, %s, %s, %s)" +
			" VALUES ('%s','%s','%s','%s','%s')";
		insertMappingSql = String.format(insertMappingSql,
				TABLE_IMPORT_MAPPING, COL_TASK_ID, COL_SOURCE_NS, COL_SOURCE_ID, COL_DEST_NS, COL_DEST_ID,
				this.mappingId, sourceNamespace, normalizedKey, clazz, cdmKey.id);
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
	    String normalizedKey = normalizeKey(sourceId);
		String deleteMappingSql = " DELETE FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = '%s'";
		deleteMappingSql = String.format(deleteMappingSql,TABLE_IMPORT_MAPPING, COL_TASK_ID, this.mappingId, COL_SOURCE_NS, sourceNamespace, COL_SOURCE_ID, normalizedKey);
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
		String normalizedKey = normalizeKey(sourceId);
		String selectMappingSql = " SELECT %s, %s FROM %s" +
				" WHERE %s = '%s' AND %s = '%s' AND %s = '%s' ";
		selectMappingSql = String.format(selectMappingSql,
				COL_DEST_NS, COL_DEST_ID, TABLE_IMPORT_MAPPING,
				COL_TASK_ID, this.mappingId, COL_SOURCE_NS, sourceNamespace,
				COL_SOURCE_ID , normalizedKey);
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

	@Override
    public boolean exists(String sourceNamespace, String sourceId, Class<?> destinationClass){
		String selectMappingSql = " SELECT count(*) as n FROM %s" +
			" WHERE %s = '%s' AND %s = '%s' AND %s = '%s' AND %s = '%s' ";


		String cdmClass = getCdmClassStr(destinationClass);
		String normalizedKey = normalizeKey(sourceId);
		selectMappingSql = String.format(selectMappingSql,
			TABLE_IMPORT_MAPPING, COL_TASK_ID, this.mappingId,
			COL_SOURCE_NS, sourceNamespace, COL_SOURCE_ID , normalizedKey, COL_DEST_NS, cdmClass);
		try {
			ResultSet rs = this.datasource.executeQuery(selectMappingSql);
			rs.next();
			int n = rs.getInt("n");

			return n > 0;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

    /**
     * Normalizes the key coming from the DwCA File.
     * This includes handling ' and keys with length > 255
     * @param sourceKey
     * @return
     */
    private String normalizeKey(String key) {
        if (key == null){
            return null;
        }
        String result = key.replace("'", "''");
        if (result.length() > SOURCE_KEY_LENGTH){
            //TODO better use MD5 hash or similar
            logger.info("Source key was trunkated: " + key);
            result = result.substring(0, SOURCE_KEY_LENGTH);
        }
        return result;
    }

	@Override
	public InMemoryMapping getPartialMapping( Map<String, Set<String>> namespacedSourceKeys) {
		InMemoryMapping partialMapping = new InMemoryMapping();
		for (Entry<String,Set<String>> entry  : namespacedSourceKeys.entrySet()){
			String namespace = entry.getKey();
			for (String sourceKey : entry.getValue() ){
			    String normalizedKey = normalizeKey(sourceKey);
				Set<CdmKey> destObjects = this.get(namespace, normalizedKey);
				for (CdmKey cdmKey : destObjects){
					partialMapping.putMapping(namespace, normalizedKey, cdmKey);
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
			logger.info("Finalize database mapping " +  count +  ": " + size());
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


	private void initDatasource(String file) {
		getDatabase(file);
		shortCuts.put("TaxonNameBase", TaxonNameBase.class);
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
	    return getDatabase(null);
	}

	public ICdmDataSource getDatabase(String path){
		try {
			try {
			    if (path == null){
			        datasource = CdmPersistentDataSource.NewInstance(DATABASE_INTERNAL_IMPORT_MAPPING);
			    }else{
			        makeDatasource(path);
			    }
			} catch (DataSourceNotFoundException e) {
				makeDatasource(path);
				CdmPersistentDataSource.save(DATABASE_INTERNAL_IMPORT_MAPPING, datasource);
			}
			datasource.executeQuery("SELECT * FROM " + TABLE_IMPORT_MAPPING);
		} catch (SQLException e) {
			//create database structure
		    try {
				String strCreateTable = "CREATE TABLE IF NOT EXISTS %s (";
				strCreateTable += "%s nvarchar(36) NOT NULL,";
				strCreateTable += "%s nvarchar(100) NOT NULL,";
				strCreateTable += "%s nvarchar(" + SOURCE_KEY_LENGTH + ") NOT NULL,";
				strCreateTable += "%s nvarchar(100) NOT NULL,";
				strCreateTable += "destination_id nvarchar(50) NOT NULL,";
				strCreateTable += "PRIMARY KEY (task_id, source_namespace, source_id)";
				strCreateTable += ") ";
				strCreateTable = String.format(strCreateTable, TABLE_IMPORT_MAPPING, COL_TASK_ID, COL_SOURCE_NS, COL_SOURCE_ID, COL_DEST_NS, COL_DEST_ID);
				datasource.executeUpdate(strCreateTable);
				logger.warn("Mapping database structure created");
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
		}
		return datasource;
	}

    /**
     * @param path
     */
    private void makeDatasource(String path) {
        datasource = CdmDataSource.NewH2EmbeddedInstance("_tmpMapping", "a", "b", path);
    }


}
