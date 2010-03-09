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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.DbIgnoreMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportNameTypeDesignationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportSynonymMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportTaxIncludedInMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbNotYetImplementedMapper;
import eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper;
import eu.etaxonomy.cdm.io.common.mapping.IDbImportTransformed;
import eu.etaxonomy.cdm.io.common.mapping.IDbImportTransformer;
import eu.etaxonomy.cdm.io.erms.validation.ErmsTaxonImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
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

	public static final String TAXON_NAMESPACE = "Taxon";
	public static final String NAME_NAMESPACE = "TaxonName";
	
	
	public static final UUID TNS_EXT_UUID = UUID.fromString("41cb0450-ac84-4d73-905e-9c7773c23b05");
	
	private DbImportMapping mapping;
	
	//TODO store in state or somehow else
	private boolean isSecondPath = false;
	
	private int modCount = 10000;
	private static final String pluralString = "taxa";
	private String dbTableName = "tu";
	private Class cdmTargetClass = TaxonBase.class;

	public ErmsTaxonImport(){
		super();
	}
	
	

//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getIdQuery()
//	 */
//	@Override
//	protected String getIdQuery() {
//		String strQuery = " SELECT id FROM tu WHERE id < 300000 " ;
//		return strQuery;
//	}



	/**
	 * @return
	 */
	private DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			//TODO create original source
			mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "id", TAXON_NAMESPACE)); //id + tu_status
			//FIXME extension type
			mapping.addMapper(DbImportExtensionMapper.NewInstance("tsn", ExtensionType.ABBREVIATION()));
			mapping.addMapper(DbImportStringMapper.NewInstance("tu_name", "(NonViralName)name.nameCache"));
			//FIXME extension type
			mapping.addMapper(DbImportExtensionMapper.NewInstance("tu_displayname", ExtensionType.ABBREVIATION()));
			mapping.addMapper(DbImportExtensionMapper.NewInstance("tu_fuzzyname", ExtensionType.ABBREVIATION()));
			mapping.addMapper(DbImportStringMapper.NewInstance("tu_authority", "(NonViralName)name.authorshipCache"));
			
			//ignore
			mapping.addMapper(DbIgnoreMapper.NewInstance("tu_marine"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("tu_brackish"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("tu_fresh"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("tu_terrestrial"));
			
			//not yet implemented or ignore
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_unacceptreason"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_credibility")); //Werte: null, unknown, marked for deletion
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_completeness")); //null, unknown, tmpflag, tmp2, tmp3, complete
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_qualitystatus")); //checked by Tax Editor ERMS1.1, Added by db management team (2x), checked by Tax Editor
			
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_fossil"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_hidden"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_sp"));  //included in object creation
			mapping.addMapper(DbIgnoreMapper.NewInstance("cache_citation"));
			
//			//second path
//			DbImportMapping secondPathMapping = new DbImportMapping();
//			secondPathMapping.addMapper(DbImportTaxIncludedInMapper.NewInstance("id", "tu_parent", TAXON_NAMESPACE, null)); //there is only one tree
//			secondPathMapping.addMapper(DbImportSynonymMapper.NewInstance("id", "tu_acctaxon", TAXON_NAMESPACE, null)); 			
//			secondPathMapping.addMapper(DbImportNameTypeDesignationMapper.NewInstance("id", "tu_typetaxon", NAME_NAMESPACE, "tu_typedesignationstatus"));
//			secondPathMapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_acctaxon"));
//			mapping.setSecondPathMapping(secondPathMapping);
			
		}
		return mapping;
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
	

//	/**
//	 * @param config
//	 * @return
//	 */
//	private String getSecondPathRecordQuery(ErmsImportConfigurator config) {
//		//TODO get automatic by second path mappers
//		String selectAttributes = "id, tu_parent, tu_typetaxon, tu_typetaxon, tu_typedesignation, tu_acctaxon, tu_status"; 
//		String strRecordQuery = 
//			" SELECT  " + selectAttributes + 
//			" FROM tu " +
//			" WHERE ( tu.id IN (" + ID_LIST_TOKEN + ") )";
//		return strRecordQuery;
//	}


//	private String getSecondPathIdQuery(){
//		return getIdQuery();
//	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#doInvoke(eu.etaxonomy.cdm.io.erms.ErmsImportState)
	 */
	@Override
	protected boolean doInvoke(ErmsImportState state) {
		//first path
		boolean success = super.doInvoke(state);
		
//		//second path
//		isSecondPath = true;
//		ErmsImportConfigurator config = state.getConfig();
//		Source source = config.getSource();
//			
//		String strIdQuery = getSecondPathIdQuery();
//		String strRecordQuery = getSecondPathRecordQuery(config);
//
//		int recordsPerTransaction = config.getRecordsPerTransaction();
//		try{
//			ResultSetPartitioner partitioner = ResultSetPartitioner.NewInstance(source, strIdQuery, strRecordQuery, recordsPerTransaction);
//			while (partitioner.nextPartition()){
//				partitioner.doPartition(this, state);
//			}
//		} catch (SQLException e) {
//			logger.error("SQLException:" +  e);
//			return false;
//		}
//		
//		isSecondPath = false;
//
//		logger.info("end make " + getPluralString() + " ... " + getSuccessString(success));
		return success;

	}



	
	
	public boolean doPartition(ResultSetPartitioner partitioner, ErmsImportState state) {
		//TODO make more generic all import classes 
		state.setCurrentImport(this);
		
		boolean success = true ;
		Set taxaToSave = new HashSet<TaxonBase>();
		
		
 		DbImportMapping<?, ?> mapping = getMapping();
		mapping.initialize(state, cdmTargetClass);
		ResultSet rs = partitioner.getResultSet();
		try{
			while (rs.next()){
				success &= mapping.invoke(rs,taxaToSave, isSecondPath);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	
		partitioner.startDoSave();
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
	public TaxonBase createObject(ResultSet rs, ErmsImportState state) throws SQLException {
		int statusId = rs.getInt("status_id");
		String tuName = rs.getString("tu_name");
		TaxonNameBase taxonName = getTaxonName(rs, state);
		//add original source
		ReferenceBase citation = state.getConfig().getSourceReference();
		addOriginalSource(rs, taxonName, "id", NAME_NAMESPACE, citation);
		
//		taxonName.setNameCache("Test");
		
		ErmsImportConfigurator config = state.getConfig();
		ReferenceBase sec = config.getSourceReference();
		if (statusId == 1){
			return Taxon.NewInstance(taxonName, sec);
		}else{
			return Synonym.NewInstance(taxonName, sec);
		}
	}

	/**
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private TaxonNameBase getTaxonName(ResultSet rs, ErmsImportState state) throws SQLException {
		TaxonNameBase result;
		Integer kingdomId = parseKingdomId(rs);
		Integer intRank = rs.getInt("tu_rank");
		
		NomenclaturalCode nc = ErmsTransformer.kingdomId2NomCode(kingdomId);
		Rank rank = null;
		if (kingdomId != null){
			rank = state.getRank(intRank, kingdomId);
		}
		if (nc != null){
			result = nc.getNewTaxonNameInstance(rank);
		}else{
			result = NonViralName.NewInstance(rank);
		}
		
		return result;
	}

	/**
	 * Returns the kingdom id by extracting it from the second character in the <code>tu_sp</code> 
	 * attribute. If the attribute can not be parsed to a valid id <code>null</code>
	 * is returned. If the attribute is <code>null</code> the id of the record is returned.
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private int parseKingdomId(ResultSet rs) throws SQLException {
		Integer result = null;
		String treeString = rs.getString("tu_sp");
		if (treeString != null){
			if (CdmUtils.isNotEmpty(treeString) && treeString.length() > 1){
				String strKingdom = treeString.substring(1,2);
				
				if (! treeString.substring(0, 1).equals("#") && ! treeString.substring(2, 3).equals("#") ){
					logger.warn("Tree string " + treeString + " has no recognized format");
				}else{
					try {
						result = Integer.valueOf(strKingdom);
					} catch (NumberFormatException e) {
						logger.warn("Kingdom string " + strKingdom + "could not be recognized as a valid number");
					}
				}
			}
		}else{
			Integer tu_id = rs.getInt("id");
			result = tu_id;
		}
		return result;
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
