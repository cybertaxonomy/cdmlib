/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.erms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.berlinModel.CdmStringMapper;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.IoStateBase;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.DbImportExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportStringMapper;
import eu.etaxonomy.cdm.io.erms.validation.ErmsTaxonImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class ErmsTaxonImport  extends ErmsImportBase<TaxonBase> {
	private static final Logger logger = Logger.getLogger(ErmsTaxonImport.class);

	private static final String NAMESPACE = "Taxon";
	
	public static final UUID TNS_EXT_UUID = UUID.fromString("41cb0450-ac84-4d73-905e-9c7773c23b05");
	
	
	private int modCount = 10000;
	private static final String pluralString = "taxa";
	private String dbTableName = "tu";

	public ErmsTaxonImport(){
		super();
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(ErmsImportConfigurator config) {
		String strRecordQuery = 
			" SELECT * " + 
			" FROM tu INNER JOIN status ON tu.tu_status = status.status_id " +
			" WHERE ( tu.id IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}


	/**
	 * @return
	 */
	private DbImportMapping getMapping() {
		String tableName = dbTableName;
		DbImportMapping mapping = new DbImportMapping(tableName);
		mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this));
		//FIXME extension type
		mapping.addMapper(DbImportExtensionMapper.NewInstance(ExtensionType.ABBREVIATION(),"tsn"));
		mapping.addMapper(DbImportStringMapper.NewInstance("tu_name", "name.namecache", null));
		//FIXME extension type
		mapping.addMapper(DbImportExtensionMapper.NewInstance(ExtensionType.ABBREVIATION(), "tu_displayname"));
		mapping.addMapper(DbImportExtensionMapper.NewInstance(ExtensionType.ABBREVIATION(), "tu_fuzzyname"));
		
		
//		mapping.addMapper(IdMapper.NewInstance("NameId"));
//		mapping.addMapper(MethodMapper.NewInstance("RankFk", this));
//		mapping.addMapper(MethodMapper.NewInstance("SupraGenericName", this));
//		mapping.addMapper(MethodMapper.NewInstance("Genus", this));
		return mapping;
	}
	
	
	public boolean doPartition(ResultSetPartitioner partitioner, ErmsImportState state) {
		boolean success = true ;
		ErmsImportConfigurator config = state.getConfig();
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		Map<String, TaxonNameBase> taxonNameMap = (Map<String, TaxonNameBase>) partitioner.getObjectMap(TaxonNameBase.class);
		
		DbImportMapping mapping = getMapping();
		mapping.initialize();
		
		ResultSet rs = partitioner.getResultSet();
		try{
			while (rs.next()){
				success &= mapping.invoke(rs,taxaToSave);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	
			
		getTaxonService().save(taxaToSave);
		return success;
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> nameIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			while (rs.next()){
//				handleForeignKey(rs, nameIdSet, "PTNameFk");
//				handleForeignKey(rs, referenceIdSet, "PTRefFk");
			}
			
//			//name map
//			nameSpace = "Name";
//			cdmClass = TaxonNameBase.class;
//			idSet = nameIdSet;
//			Map<String, Person> objectMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
//			result.put(cdmClass, objectMap);

			//reference map
//			nameSpace = "Reference";
//			cdmClass = ReferenceBase.class;
//			Map<String, Person> referenceMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(Person.class, teamIdSet, nameSpace);
//			result.put(ReferenceBase.class, referenceMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet)
	 */
	public TaxonBase createObject(ResultSet rs) throws SQLException {
		int statusId = rs.getInt("status_id");
		String tuName = rs.getString("tu_name");
		//FIXME name type
		TaxonNameBase taxonName = BotanicalName.NewInstance(null);
		//FIXME pass config from somewhere
		ErmsImportConfigurator config = ErmsImportConfigurator.NewInstance(null, null);
		ReferenceBase sec = config.getSourceReference();
		if (statusId == 1){
			return Taxon.NewInstance(taxonName, sec);
		}else{
			return Synonym.NewInstance(taxonName, sec);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state){
		IOValidator<ErmsImportState> validator = new ErmsTaxonImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getTableName()
	 */
	@Override
	protected String getTableName() {
		return dbTableName;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getPluralString()
	 */
	@Override
	public String getPluralString() {
		return pluralString;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(ErmsImportState state){
		return ! state.getConfig().isDoTaxa();
	}



}
