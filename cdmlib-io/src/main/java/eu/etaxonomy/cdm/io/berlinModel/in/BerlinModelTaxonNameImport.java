/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelTaxonNameImportValidator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTaxonNameImport extends BerlinModelImportBase {
	private static final boolean BLANK_TO_NULL = true;

	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameImport.class);

	public static final String NAMESPACE = "TaxonName";
	
	public static final UUID SOURCE_ACC_UUID = UUID.fromString("c3959b4f-d876-4b7a-a739-9260f4cafd1c");
	
	private static int modCount = 5000;
	private static final String pluralString = "TaxonNames";
	private static final String dbTableName = "Name";


	public BerlinModelTaxonNameImport(){
		super(dbTableName, pluralString);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		if (state.getConfig().getNameIdTable()==null ){
			return super.getIdQuery(state);
		}else{
			return "SELECT nameId FROM " + state.getConfig().getNameIdTable() + ""; 
		}
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery()
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		Source source = config.getSource();
		
			String facultativCols = "";
			String strFacTable = "RefDetail";
			String strFacColumn = "IdInSource";
			String strColAlias = null;
			if (checkSqlServerColumnExists(source, strFacTable, strFacColumn)){
				facultativCols +=  ", " + strFacTable + "." + strFacColumn ;
				if (! CdmUtils.Nz(strColAlias).equals("") ){
					facultativCols += " AS " + strColAlias;
				}
			}

		String strRecordQuery = 
					"SELECT Name.* , RefDetail.RefDetailId, RefDetail.RefFk, " +
                      		" RefDetail.FullRefCache, RefDetail.FullNomRefCache, RefDetail.PreliminaryFlag AS RefDetailPrelim, RefDetail.Details, " + 
                      		" RefDetail.SecondarySources, Rank.RankAbbrev, Rank.Rank " +
                      		facultativCols +
                    " FROM Name LEFT OUTER JOIN RefDetail ON Name.NomRefDetailFk = RefDetail.RefDetailId AND  " +
                    	" Name.NomRefFk = RefDetail.RefFk " +
                    	" LEFT OUTER JOIN Rank ON Name.RankFk = Rank.rankID " + 
                " WHERE name.nameId IN ("+ID_LIST_TOKEN+") ";
					//strQuery += " AND RefDetail.PreliminaryFlag = 1 ";
					//strQuery += " AND Name.Created_When > '03.03.2004' ";
		return strRecordQuery +  "";
	}



	@Override
	protected void doInvoke(BerlinModelImportState state) {
		//update rank labels if necessary
		String strAbbrev = state.getConfig().getInfrGenericRankAbbrev();
		Rank rank = Rank.INFRAGENERICTAXON();
		testRankAbbrev(strAbbrev, rank);
		
		strAbbrev = state.getConfig().getInfrSpecificRankAbbrev();
		rank = Rank.INFRASPECIFICTAXON();
		testRankAbbrev(strAbbrev, rank);
		
		super.doInvoke(state);
	}

	private void testRankAbbrev(String strAbbrev, Rank rank) {
		if (strAbbrev != null){
			Representation rep = rank.getRepresentation(Language.ENGLISH());
			rep.setAbbreviatedLabel(strAbbrev);
			getTermService().saveOrUpdate(rank);
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		String dbAttrName;
		String cdmAttrName;
		boolean success = true ;
		BerlinModelImportConfigurator config = state.getConfig();
		Set<TaxonNameBase> namesToSave = new HashSet<TaxonNameBase>();
		Map<String, Team> teamMap = (Map<String, Team>) partitioner.getObjectMap(BerlinModelAuthorTeamImport.NAMESPACE);
			
		ResultSet rs = partitioner.getResultSet();
			
		try {
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i != 1 ){ logger.info("Names handled: " + (i-1));}
				
				//create TaxonName element
				int nameId = rs.getInt("nameId");
				Object authorFk = rs.getObject("AuthorTeamFk");
				Object exAuthorFk = rs.getObject("ExAuthorTeamFk");
				Object basAuthorFk = rs.getObject("BasAuthorTeamFk");
				Object exBasAuthorFk = rs.getObject("ExBasAuthorTeamFk");
				String strCultivarGroupName = rs.getString("CultivarGroupName");
				String strCultivarName = rs.getString("CultivarName");
				String nameCache = rs.getString("NameCache");
				String fullNameCache = rs.getString("FullNameCache");
				
				try {
					
					//define rank
					boolean useUnknownRank = true;
					Rank rank = BerlinModelTransformer.rankId2Rank(rs, useUnknownRank, config.isSwitchSpeciesGroup());
					
					boolean allowInfraSpecTaxonRank = state.getConfig().isAllowInfraSpecTaxonRank() ;
					if (rank == null || rank.equals(Rank.UNKNOWN_RANK()) || (rank.equals(Rank.INFRASPECIFICTAXON()) && ! allowInfraSpecTaxonRank)){
						rank = handleProlesAndRaceSublusus(state, rs, rank);
					}
					
					if (rank.getId() == 0){
						getTermService().save(rank);
						logger.warn("Rank did not yet exist: " +  rank.getTitleCache());
					}
					
					//create TaxonNameBase
					TaxonNameBase taxonNameBase;
					if (config.getNomenclaturalCode() != null){
						taxonNameBase = config.getNomenclaturalCode().getNewTaxonNameInstance(rank);
						//check cultivar
						if (taxonNameBase instanceof BotanicalName){
							if (CdmUtils.isNotEmpty(strCultivarGroupName) && CdmUtils.isNotEmpty(strCultivarName)){
								taxonNameBase = CultivarPlantName.NewInstance(rank);
							}
						}
					}else{
						taxonNameBase = NonViralName.NewInstance(rank);
					}
					
					if (rank == null){
						//TODO rank should never be null or a more sophisticated algorithm has to be implemented for genus/supraGenericName
						logger.warn("Rank is null. Genus epethiton was imported. May be wrong");
						success = false;
					}
					
					//epithets
					if (rank != null && rank.isSupraGeneric()){
						dbAttrName = "supraGenericName";
					}else{
						dbAttrName = "genus";
					}
					cdmAttrName = "genusOrUninomial";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName, BLANK_TO_NULL);
					
					dbAttrName = "genusSubdivisionEpi";
					cdmAttrName = "infraGenericEpithet";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName, BLANK_TO_NULL);
					
					dbAttrName = "speciesEpi";
					cdmAttrName = "specificEpithet";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName, BLANK_TO_NULL);
					
	
					dbAttrName = "infraSpeciesEpi";
					cdmAttrName = "infraSpecificEpithet";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName, BLANK_TO_NULL);
					
					dbAttrName = "unnamedNamePhrase";
					cdmAttrName = "appendedPhrase";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName, BLANK_TO_NULL);
					
					//Details
					dbAttrName = "details";
					cdmAttrName = "nomenclaturalMicroReference";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName, BLANK_TO_NULL);
	
					//nomRef
					success &= makeNomenclaturalReference(config, taxonNameBase, nameId, rs, partitioner);
	
					//Source_Acc
					boolean colExists = true;
					try {
						colExists = state.getConfig().getSource().checkColumnExists("Name", "Source_Acc");
					} catch (NoSuchMethodException e) {
						logger.debug("Source does not support 'checkColumnExists'");
					}
					if (colExists){
						String sourceAcc = rs.getString("Source_Acc");
						if (StringUtils.isNotBlank(sourceAcc)){
							ExtensionType sourceAccExtensionType = getExtensionType(state, SOURCE_ACC_UUID, "Source_Acc","Source_Acc","Source_Acc");
							Extension datesExtension = Extension.NewInstance(taxonNameBase, sourceAcc, sourceAccExtensionType);
						}
					}
					
					//created, notes
					boolean excludeUpdated = true;
					success &= doIdCreatedUpdatedNotes(state, taxonNameBase, rs, nameId, NAMESPACE, excludeUpdated);
	
					//NonViralName
					if (taxonNameBase instanceof NonViralName){
						NonViralName<?> nonViralName = (NonViralName<?>)taxonNameBase;
						
						//authorTeams
						if (teamMap != null ){
							nonViralName.setCombinationAuthorTeam(getAuthorTeam(teamMap, authorFk, nameId, config));
							nonViralName.setExCombinationAuthorTeam(getAuthorTeam(teamMap, exAuthorFk, nameId, config));
							nonViralName.setBasionymAuthorTeam(getAuthorTeam(teamMap, basAuthorFk, nameId, config));
							nonViralName.setExBasionymAuthorTeam(getAuthorTeam(teamMap, exBasAuthorFk, nameId, config));
						}else{
							logger.warn("TeamMap is null");
							success = false;
						}
					}//nonviralName
					
	
					
					//zoologicalName
					if (taxonNameBase instanceof ZoologicalName){
						ZoologicalName zooName = (ZoologicalName)taxonNameBase;
						makeZoologialName(rs, zooName, nameId);
					}
					//botanicalName  
					else if (taxonNameBase instanceof BotanicalName){
						BotanicalName botName = (BotanicalName)taxonNameBase;
						success &= makeBotanicalNamePart(rs, botName) ;
						
					}
					
	//				dbAttrName = "preliminaryFlag";
					Boolean preliminaryFlag = rs.getBoolean("PreliminaryFlag");
					Boolean hybridFormulaFlag = rs.getBoolean("HybridFormulaFlag");  //hybrid flag does not lead to cache update in Berlin Model
					if (preliminaryFlag == true || hybridFormulaFlag == true){
						//Computes all caches and sets 
						taxonNameBase.setTitleCache(fullNameCache, true);
						taxonNameBase.setFullTitleCache(taxonNameBase.getFullTitleCache(), true);
						if (taxonNameBase instanceof NonViralName){
							NonViralName<?> nvn = (NonViralName<?>)taxonNameBase;
							nvn.setNameCache(nameCache, true);
							nvn.setAuthorshipCache(nvn.getAuthorshipCache(), true);
						}
					}
					namesToSave.add(taxonNameBase);
					
				}
				catch (UnknownCdmTypeException e) {
					logger.warn("Name with id " + nameId + " has unknown rankId " + " and could not be saved.");
					success = false; 
				}
				
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

			
//		logger.info( i + " names handled");
		getNameService().save(namesToSave);
		return success;
	}


	private Rank handleProlesAndRaceSublusus(BerlinModelImportState state, ResultSet rs, Rank rank) throws SQLException {
		Rank result;
		String rankAbbrev = rs.getString("RankAbbrev");
		String rankStr = rs.getString("Rank");
		if (CdmUtils.nullSafeEqual(rankAbbrev, "prol.") ){
			result = getRank(state, BerlinModelTransformer.uuidRankProles, rankStr, "Rank Proles", rankAbbrev, CdmBase.deproxy(Rank.SPECIES().getVocabulary(), OrderedTermVocabulary.class), Rank.CONVAR(), RankClass.Infraspecific);
		}else if(CdmUtils.nullSafeEqual(rankAbbrev, "race")){
			result = getRank(state, BerlinModelTransformer.uuidRankRace, rankStr, "Rank Race", rankAbbrev, CdmBase.deproxy(Rank.SPECIES().getVocabulary(), OrderedTermVocabulary.class), Rank.CONVAR(), RankClass.Infraspecific);
		}else if(CdmUtils.nullSafeEqual(rankAbbrev, "sublusus")){
			result = getRank(state, BerlinModelTransformer.uuidRankSublusus, rankStr, "Rank Sublusus", rankAbbrev, CdmBase.deproxy(Rank.SPECIES().getVocabulary(), OrderedTermVocabulary.class), Rank.CONVAR(), RankClass.Infraspecific);
		}else{
			result = rank;
			logger.warn("Unhandled rank: " + rankAbbrev);
		}
		return result;
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
			Set<String> teamIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			Set<String> refDetailIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, teamIdSet, "AuthorTeamFk");
				handleForeignKey(rs, teamIdSet, "ExAuthorTeamFk");
				handleForeignKey(rs, teamIdSet, "BasAuthorTeamFk");
				handleForeignKey(rs, teamIdSet, "ExBasAuthorTeamFk");
				handleForeignKey(rs, referenceIdSet, "nomRefFk");
				handleForeignKey(rs, refDetailIdSet, "nomRefDetailFk");
			}
			
			//team map
			nameSpace = BerlinModelAuthorTeamImport.NAMESPACE;
			cdmClass = Team.class;
			idSet = teamIdSet;
			Map<String, Person> teamMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, teamMap);

			//nom reference map
			nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> nomReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomReferenceMap);

			//biblio reference map
			nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> biblioReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioReferenceMap);
			
			//nom refDetail map
			nameSpace = BerlinModelRefDetailImport.NOM_REFDETAIL_NAMESPACE;
			cdmClass = Reference.class;
			idSet = refDetailIdSet;
			Map<String, Reference> nomRefDetailMap= (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomRefDetailMap);
			
			//biblio refDetail map
			nameSpace = BerlinModelRefDetailImport.BIBLIO_REFDETAIL_NAMESPACE;
			cdmClass = Reference.class;
			idSet = refDetailIdSet;
			Map<String, Reference> biblioRefDetailMap= (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioRefDetailMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	private boolean makeZoologialName(ResultSet rs, ZoologicalName zooName, int nameId) 
					throws SQLException{
		boolean success = true;
		//publicationYear
		String authorTeamYear = rs.getString("authorTeamYear");
		try {
			if (! "".equals(CdmUtils.Nz(authorTeamYear).trim())){
				Integer publicationYear  = Integer.valueOf(authorTeamYear.trim());
				zooName.setPublicationYear(publicationYear);
			}
		} catch (NumberFormatException e) {
			logger.warn("authorTeamYear could not be parsed for taxonName: "+ nameId);
		}
		//original publication year
		String basAuthorTeamYear = rs.getString("basAuthorTeamYear");
		try {
			if (! "".equals(CdmUtils.Nz(basAuthorTeamYear).trim())){
				Integer OriginalPublicationYear  = Integer.valueOf(basAuthorTeamYear.trim());
				zooName.setOriginalPublicationYear(OriginalPublicationYear);
			}
		} catch (NumberFormatException e) {
			logger.warn("basAuthorTeamYear could not be parsed for taxonName: "+ nameId);
		}
		return success;
	}
	
	private boolean makeBotanicalNamePart(ResultSet rs, BotanicalName botanicalName)throws SQLException{
		boolean success = true;
		String dbAttrName;
		String cdmAttrName;
		
		dbAttrName = "HybridFormulaFlag";
		cdmAttrName = "isHybridFormula";
		success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

		dbAttrName = "MonomHybFlag";
		cdmAttrName = "isMonomHybrid";
		success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

		dbAttrName = "BinomHybFlag";
		cdmAttrName = "isBinomHybrid";
		success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

		dbAttrName = "TrinomHybFlag";
		cdmAttrName = "isTrinomHybrid";
		success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

		try {
			String strCultivarGroupName = rs.getString("CultivarGroupName");
			String strCultivarName = rs.getString("CultivarName");
			if (botanicalName instanceof CultivarPlantName){
				CultivarPlantName cultivarName = (CultivarPlantName)botanicalName;
				String concatCultivarName = CdmUtils.concat("-", strCultivarName, strCultivarGroupName);
				if (CdmUtils.isNotEmpty(strCultivarGroupName) && CdmUtils.isNotEmpty(strCultivarName)){
					logger.warn("CDM does not support cultivarGroupName and CultivarName together: " + concatCultivarName);
				}
				cultivarName.setCultivarName(strCultivarGroupName);
			}
		} catch (SQLException e) {
			throw e;
		}
		return success;
	}
	
	
	private boolean makeNomenclaturalReference(IImportConfigurator config, TaxonNameBase taxonNameBase, 
					int nameId, ResultSet rs, ResultSetPartitioner partitioner) throws SQLException{
		Map<String, Reference> biblioRefMap = partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		Map<String, Reference> nomRefMap = partitioner.getObjectMap(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE);
		Map<String, Reference> biblioRefDetailMap = partitioner.getObjectMap(BerlinModelRefDetailImport.BIBLIO_REFDETAIL_NAMESPACE);
		Map<String, Reference> nomRefDetailMap = partitioner.getObjectMap(BerlinModelRefDetailImport.NOM_REFDETAIL_NAMESPACE);
		
		Object nomRefFkObj = rs.getObject("NomRefFk");
		Object nomRefDetailFkObj = rs.getObject("NomRefDetailFk");
		boolean refDetailPrelim = rs.getBoolean("RefDetailPrelim");
		
		boolean success = true;
		//nomenclatural Reference
		if (biblioRefMap != null){
			if (nomRefFkObj != null){
				String nomRefFk = String.valueOf(nomRefFkObj);
				String nomRefDetailFk = String.valueOf(nomRefDetailFkObj);
				//get nomRef
				Reference nomReference = 
					getReferenceFromMaps(nomRefDetailMap, biblioRefDetailMap, 
							nomRefMap, biblioRefMap, nomRefDetailFk, nomRefFk);
				
				
				//setNomRef
				if (nomReference == null ){
					//TODO
					if (! config.isIgnoreNull()){
						logger.warn("Nomenclatural reference (nomRefFk = " + nomRefFk + ") for TaxonName (nameId = " + nameId + ")"+
							" was not found in reference store. Nomenclatural reference was not set!!");
					}
				}else{
					if (! INomenclaturalReference.class.isAssignableFrom(nomReference.getClass())){
						logger.warn("Nomenclatural reference (nomRefFk = " + nomRefFk + ") for TaxonName (nameId = " + nameId + ")"+
								" is not assignable from INomenclaturalReference. (Class = " + nomReference.getClass()+ ")");
					}
					nomReference.setNomenclaturallyRelevant(true);
					taxonNameBase.setNomenclaturalReference(nomReference);
				}
			}
		}
		return success;
	}
	
	private static TeamOrPersonBase getAuthorTeam(Map<String, Team> teamMap, Object teamIdObject, int nameId, BerlinModelImportConfigurator bmiConfig){
		if (teamIdObject == null){
			return null;
		}else {
			String teamId = String.valueOf(teamIdObject);
			TeamOrPersonBase author = teamMap.get(teamId);
			if (author == null){
				//TODO
				if (!bmiConfig.isIgnoreNull() && ! (teamId.equals(0) && bmiConfig.isIgnore0AuthorTeam()) ){ 
					logger.warn("AuthorTeam (teamId = " + teamId + ") for TaxonName (nameId = " + nameId + ")"+
				" was not found in authorTeam store. Relation was not set!!");}
				return null;
			}else{
				return author;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelTaxonNameImportValidator();
		return validator.validate(state);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoTaxonNames();
	}





	
//FOR FUTURE USE , DONT DELETE	
//	new CdmStringMapper("nameId", "nameId"),
//	new CdmStringMapper("rankFk", "rankFk"),
//	new CdmStringMapper("nameCache", "nameCache"),
//	new CdmStringMapper("unnamedNamePhrase", "unnamedNamePhrase"),
//	new CdmStringMapper("fullNameCache", "fullNameCache"),
//	new CdmStringMapper("preliminaryFlag", "preliminaryFlag"),
//	new CdmStringMapper("supragenericName", "supragenericName"),
//	new CdmStringMapper("genus", "genus"),
//	new CdmStringMapper("genusSubdivisionEpi", "genusSubdivisionEpi"),
//	new CdmStringMapper("speciesEpi", "speciesEpi"),
//	new CdmStringMapper("infraSpeciesEpi", "infraSpeciesEpi"),
//	new CdmStringMapper("authorTeamFk", "authorTeamFk"),
//	new CdmStringMapper("exAuthorTeamFk", "exAuthorTeamFk"),
//	new CdmStringMapper("basAuthorTeamFk", "basAuthorTeamFk"),
//	new CdmStringMapper("exBasAuthorTeamFk", "exBasAuthorTeamFk"),
//	new CdmStringMapper("hybridFormulaFlag", "hybridFormulaFlag"),
//	new CdmStringMapper("monomHybFlag", "monomHybFlag"),
//	new CdmStringMapper("binomHybFlag", "binomHybFlag"),
//	new CdmStringMapper("trinomHybFlag", "trinomHybFlag"),
//	new CdmStringMapper("cultivarGroupName", "cultivarGroupName"),
//	new CdmStringMapper("cultivarName", "cultivarName"),
//	new CdmStringMapper("nomRefFk", "nomRefFk"),
//	new CdmStringMapper("nomRefDetailFk", "nomRefDetailFk"),
//	new CdmStringMapper("nameSourceRefFk", "nameSourceRefFk"),
//	new CdmStringMapper("source_Acc", "source_Acc"),
//	new CdmStringMapper("created_When", "created_When"),
//	new CdmStringMapper("created_Who", "created_Who"),
//	new CdmStringMapper("notes", "notes"),
//	new CdmStringMapper("parsingComments", "parsingComments"),
//	new CdmStringMapper("oldNomRefFk", "oldNomRefFk"),
//	new CdmStringMapper("oldNomRefDetailFk", "oldNomRefDetailFk"),
//	new CdmStringMapper("updated_Who", "updated_Who"),
//	new CdmStringMapper("orthoProjection", "orthoProjection"),

	
}
