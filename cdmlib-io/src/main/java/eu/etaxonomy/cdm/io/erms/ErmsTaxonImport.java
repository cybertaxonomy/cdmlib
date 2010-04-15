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
import eu.etaxonomy.cdm.io.common.mapping.DbIgnoreMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportExtensionCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportExtensionTypeCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMarkerCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbNotYetImplementedMapper;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.erms.validation.ErmsTaxonImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
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
public class ErmsTaxonImport  extends ErmsImportBase<TaxonBase> implements IMappingImport<TaxonBase, ErmsImportState>{
	private static final Logger logger = Logger.getLogger(ErmsTaxonImport.class);
	
	public static final UUID TNS_EXT_UUID = UUID.fromString("41cb0450-ac84-4d73-905e-9c7773c23b05");
	
	private DbImportMapping mapping;
	
	//second path is not used anymore, there is now an ErmsTaxonRelationImport class instead
	private boolean isSecondPath = false;
	
	private int modCount = 10000;
	private static final String pluralString = "taxa";
	private static final String dbTableName = "tu";
	private static final Class cdmTargetClass = TaxonBase.class;

	public ErmsTaxonImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}
	
	

//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getIdQuery()
//	 */
//	@Override
//	protected String getIdQuery() {
//		String strQuery = " SELECT id FROM tu WHERE id < 300000 " ;
//		return strQuery;
//	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getMapping()
	 */
	protected DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "id", TAXON_NAMESPACE)); //id + tu_status
			UUID tsnUuid = ErmsTransformer.uuidTsn;
			mapping.addMapper(DbImportExtensionMapper.NewInstance("tsn", tsnUuid, "TSN", "TSN", "TSN"));
//			mapping.addMapper(DbImportStringMapper.NewInstance("tu_name", "(NonViralName)name.nameCache"));
			
			UUID displayNameUuid = ErmsTransformer.uuidDisplayName;
			mapping.addMapper(DbImportExtensionMapper.NewInstance("tu_displayname", displayNameUuid, "display name", "display name", "display name"));
			UUID fuzzyNameUuid = ErmsTransformer.uuidFuzzyName;
			mapping.addMapper(DbImportExtensionMapper.NewInstance("tu_fuzzyname", fuzzyNameUuid, "fuzzy name", "fuzzy name", "fuzzy name"));
			mapping.addMapper(DbImportStringMapper.NewInstance("tu_authority", "(NonViralName)name.authorshipCache"));
			
			UUID fossilStatusUuid = ErmsTransformer.uuidFossilStatus;
			mapping.addMapper(DbImportExtensionMapper.NewInstance("fossil_name", fossilStatusUuid, "fossil status", "fossil status", "fos. stat."));
//			mapping.addMapper(DbImportExtensionTypeCreationMapper.NewInstance("fossil_name", EXTENSION_TYPE_NAMESPACE, "fossil_name", "fossil_name", "fossil_name"));
			
			UUID credibilityUuid = ErmsTransformer.uuidCredibility;
			mapping.addMapper(DbImportExtensionMapper.NewInstance("tu_credibility", credibilityUuid, "credibility", "credibility", "credibility")); //Werte: null, unknown, marked for deletion
			
			UUID completenessUuid = ErmsTransformer.uuidCompleteness;
			mapping.addMapper(DbImportExtensionMapper.NewInstance("tu_completeness", completenessUuid, "completeness", "completeness", "completeness")); //null, unknown, tmpflag, tmp2, tmp3, complete
			
			UUID unacceptUuid = ErmsTransformer.uuidUnacceptReason;
			mapping.addMapper(DbImportExtensionMapper.NewInstance("tu_unacceptreason", unacceptUuid, "unaccept reason", "unaccept reason", "reason"));
			
			UUID qualityUuid = ErmsTransformer.uuidQualityStatus;
			mapping.addMapper(DbImportExtensionMapper.NewInstance("qualitystatus_name", qualityUuid, "quality status", "quality status", "quality status")); //checked by Tax Editor ERMS1.1, Added by db management team (2x), checked by Tax Editor
			
//			UUID hiddenUuid = ErmsTransformer.uuidHidden;
//			mapping.addMapper(DbImportMarkerCreationMapper.Mapper.NewInstance("qualitystatus_name", qualityUuid, "quality status", "quality status", "quality status")); //checked by Tax Editor ERMS1.1, Added by db management team (2x), checked by Tax Editor
			
			
			//ignore
			mapping.addMapper(DbIgnoreMapper.NewInstance("tu_marine", "marine flag not implemented in PESI"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("tu_brackish", "brackish flag not implemented in PESI"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("tu_fresh", "freshwater flag not implemented in PESI"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("tu_terrestrial", "terrestrial flag not implemented in PESI"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("tu_fossil", "tu_fossil implemented as foreign key"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("cache_citation", "citation cache not needed in PESI"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_sp", "included in rank/object creation")); 
			
			
			//not yet implemented or ignore
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_hidden", "Needs DbImportMarkerMapper implemented"));
			
//			//second path / implemented in ErmsTaxonRelationImport
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
		String strSelect = " SELECT tu.*, parent1.tu_name AS parent1name, parent2.tu_name AS parent2name, parent3.tu_name AS parent3name, " 
			+ " parent1.tu_rank AS parent1rank, parent2.tu_rank AS parent2rank, parent3.tu_rank AS parent3rank, " + 
			" status.status_id as status_id,  fossil.fossil_name, qualitystatus.qualitystatus_name";
		String strFrom = " FROM tu  LEFT OUTER JOIN  tu AS parent1 ON parent1.id = tu.tu_parent " + 
				" LEFT OUTER JOIN   tu AS parent2  ON parent2.id = parent1.tu_parent " + 
				" LEFT OUTER JOIN tu AS parent3 ON parent2.tu_parent = parent3.id " + 
				" LEFT OUTER JOIN status ON tu.tu_status = status.status_id " + 
				" LEFT OUTER JOIN fossil ON tu.tu_fossil = fossil.fossil_id " +
				" LEFT OUTER JOIN qualitystatus ON tu.tu_qualitystatus = qualitystatus.id ";
		String strWhere = " WHERE ( tu.id IN (" + ID_LIST_TOKEN + ") )";
		String strRecordQuery = strSelect + strFrom + strWhere;
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
		String displayName = rs.getString("tu_displayname");
		
		String parent1Name = rs.getString("parent1name");
		Integer parent1Rank = rs.getInt("parent1rank");
		
		String parent2Name = rs.getString("parent2name");
		Integer parent2Rank = rs.getInt("parent2rank");
		
		String parent3Name = rs.getString("parent3name");
		Integer parent3Rank = rs.getInt("parent3rank");
		
		
		NonViralName taxonName = getTaxonName(rs, state);
		//set epithets
		if (taxonName.isGenus() || taxonName.isSupraGeneric()){
			taxonName.setGenusOrUninomial(tuName);
		}else if (taxonName.isInfraGeneric()){
			taxonName.setInfraGenericEpithet(tuName);
			taxonName.setGenusOrUninomial(parent1Name);
		}else if (taxonName.isSpecies()){
			taxonName.setSpecificEpithet(tuName);
			getGenusAndInfraGenus(parent1Name, parent2Name, parent1Rank, taxonName);
		}else if (taxonName.isInfraSpecific()){
			if (parent1Rank < 220){
				handleException(parent1Rank, taxonName, displayName);
			}
			taxonName.setInfraSpecificEpithet(tuName);
			taxonName.setSpecificEpithet(parent1Name);
			getGenusAndInfraGenus(parent2Name, parent3Name, parent2Rank, taxonName);
		}else if (taxonName.getRank()== null){
			logger.warn("rank super domain still needs to be implemented. Used domain instead.");
			if ("Biota".equalsIgnoreCase(tuName)){
				Rank rank = Rank.DOMAIN();  //should be Superdomain
				taxonName.setRank(rank);
				taxonName.setGenusOrUninomial(tuName);
			}else{
				String warning = "TaxonName has no rank. Use namecache.";
				logger.warn(warning);
				taxonName.setNameCache(tuName);
			}
			
		}
		//e.g. Leucon [Platyhelminthes] ornatus
		if (containsBrackets(displayName)){
			taxonName.setNameCache(displayName);
			logger.warn("Set name cache: " +  displayName);
		}
		
		//add original source for taxon name (taxon original source is added in mapper
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
	 * @param parent1Rank
	 * @param displayName 
	 * @param taxonName 
	 */
	private void handleException(Integer parent1Rank, NonViralName taxonName, String displayName) {
		logger.warn("Parent of infra specific taxon is higher than species. Used nameCache: " + displayName) ;
		taxonName.setNameCache(displayName);
	}



	/**
	 * @param displayName
	 * @return
	 */
	private boolean containsBrackets(String displayName) {
		int index = displayName.indexOf("[");
		return (index > -1);
	}



	/**
	 * @param parent1Name
	 * @param parent2Name
	 * @param parent1Rank
	 * @param taxonName
	 */
	private void getGenusAndInfraGenus(String parentName, String grandParentName, Integer parent1Rank, NonViralName taxonName) {
		if (parent1Rank <220 && parent1Rank > 180){
			//parent is infrageneric
			taxonName.setInfraGenericEpithet(parentName);
			taxonName.setGenusOrUninomial(grandParentName);
		}else{
			taxonName.setGenusOrUninomial(parentName);
		}
	}

	/**
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private NonViralName getTaxonName(ResultSet rs, ErmsImportState state) throws SQLException {
		NonViralName result;
		Integer kingdomId = parseKingdomId(rs);
		Integer intRank = rs.getInt("tu_rank");
		
		NomenclaturalCode nc = ErmsTransformer.kingdomId2NomCode(kingdomId);
		Rank rank = null;
		if (kingdomId != null){
			rank = state.getRank(intRank, kingdomId);
		}
		if (nc != null){
			result = (NonViralName)nc.getNewTaxonNameInstance(rank);
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
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(ErmsImportState state){
		return ! state.getConfig().isDoTaxa();
	}



}
