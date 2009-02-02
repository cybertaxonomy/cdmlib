/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTaxonNameImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameImport.class);

	private static int modCount = 5000;

	public BerlinModelTaxonNameImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for TaxonNames not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){				
			
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.AUTHOR_STORE);
		
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		boolean success = true ;
		
		logger.info("start makeTaxonNames ...");
		
		try {
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
			
			//get data from database
			String strQuery = 
					"SELECT Name.* , RefDetail.RefDetailId, RefDetail.RefFk, " +
                      		" RefDetail.FullRefCache, RefDetail.FullNomRefCache, RefDetail.PreliminaryFlag AS RefDetailPrelim, RefDetail.Details, " + 
                      		" RefDetail.SecondarySources, Rank.RankAbbrev, Rank.Rank " +
                      		facultativCols +
                    " FROM Name LEFT OUTER JOIN RefDetail ON Name.NomRefDetailFk = RefDetail.RefDetailId AND  " +
                    	" Name.NomRefFk = RefDetail.RefFk " +
                    	" LEFT OUTER JOIN Rank ON Name.RankFk = Rank.rankID " + 
                    " WHERE (1=1) ";
					//strQuery += " AND RefDetail.PreliminaryFlag = 1 ";
					//strQuery += " AND Name.Created_When > '03.03.2004' ";
			
			ResultSet rs = source.getResultSet(strQuery) ;
			String namespace = "Name";
			
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
				
				try {
					boolean useUnknownRank = true;
					Rank rank = BerlinModelTransformer.rankId2Rank(rs, useUnknownRank);
					
					TaxonNameBase taxonNameBase;
					if (bmiConfig.getNomenclaturalCode() != null){
						taxonNameBase = bmiConfig.getNomenclaturalCode().getNewTaxonNameInstance(rank);
					}else{
						taxonNameBase = NonViralName.NewInstance(rank);
					}
					
					if (rank == null){
						//TODO rank should never be null or a more sophisticated algorithm has to be implemented for genus/supraGenericName
						logger.warn("Rank is null. Genus epethiton was imported. May be wrong");
					}
					if (rank != null && rank.isSupraGeneric()){
						dbAttrName = "supraGenericName";
					}else{
						dbAttrName = "genus";
					}
					cdmAttrName = "genusOrUninomial";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "genusSubdivisionEpi";
					cdmAttrName = "infraGenericEpithet";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "speciesEpi";
					cdmAttrName = "specificEpithet";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					

					dbAttrName = "infraSpeciesEpi";
					cdmAttrName = "infraSpecificEpithet";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "unnamedNamePhrase";
					cdmAttrName = "appendedPhrase";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "preliminaryFlag";
					cdmAttrName = "XX" + "protectedTitleCache";
					success &= ImportHelper.addBooleanValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					
					//Details
					dbAttrName = "details";
					cdmAttrName = "nomenclaturalMicroReference";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);

					//nomRef
					success &= makeNomenclaturalReference(bmiConfig, taxonNameBase, nameId, rs, stores);

					//created, notes
					success &= doIdCreatedUpdatedNotes(bmiConfig, taxonNameBase, rs, nameId, namespace);
					
					//Marker
					boolean flag = true;
					Marker marker = Marker.NewInstance(MarkerType.TO_BE_CHECKED() ,flag);
					taxonNameBase.addMarker(marker);

					//NonViralName
					if (taxonNameBase instanceof NonViralName){
						NonViralName nonViralName = (NonViralName)taxonNameBase;
						
						//authorTeams
						if (authorMap != null ){
							boolean ignoreNull = config.isIgnoreNull();
							nonViralName.setCombinationAuthorTeam(getAuthorTeam(authorMap, authorFk, nameId, ignoreNull));
							nonViralName.setExCombinationAuthorTeam(getAuthorTeam(authorMap, exAuthorFk, nameId, ignoreNull));
							nonViralName.setBasionymAuthorTeam(getAuthorTeam(authorMap, basAuthorFk, nameId, ignoreNull));
							nonViralName.setExBasionymAuthorTeam(getAuthorTeam(authorMap, exBasAuthorFk, nameId, ignoreNull));
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
					
					
					//TODO
					//Source_Acc
					//OrthoProjection

					//TODO
					//preliminaryFlag see above
					
					taxonNameMap.put(nameId, taxonNameBase);
					
				}
				catch (UnknownCdmTypeException e) {
					logger.warn("Name with id " + nameId + " has unknown rankId " + " and could not be saved.");
					success = false; 
				}
				
			} //while rs.hasNext()
			logger.info(i + " names handled");
			getNameService().saveTaxonNameAll(taxonNameMap.objects());
			
//			makeNameSpecificData(nameMap);

			logger.info("end makeTaxonNames ...");
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

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
	
	private boolean makeBotanicalNamePart(ResultSet rs, BotanicalName botanicalName){
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

		if (botanicalName instanceof CultivarPlantName){
			//TODO
			//CultivarGroupName
			//CultivarName
		}
		return success;
	}
	
	
	private boolean makeNomenclaturalReference(IImportConfigurator config, TaxonNameBase taxonNameBase, 
					int nameId, ResultSet rs, Map<String, MapWrapper<? extends CdmBase>> stores) 
					throws SQLException{
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
		MapWrapper<ReferenceBase> refDetailMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REF_DETAIL_STORE);
		MapWrapper<ReferenceBase> nomRefDetailMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_DETAIL_STORE);
		
		Object nomRefFk = rs.getObject("NomRefFk");
		int nomRefDetailFk = rs.getInt("NomRefDetailFk");
		boolean refDetailPrelim = rs.getBoolean("RefDetailPrelim");
		
		boolean success = true;
		//nomenclatural Reference
		if (referenceMap != null){
			if (nomRefFk != null){
				int nomRefFkInt = (Integer)nomRefFk;
				
				//get nomRef
				ReferenceBase nomReference = nomRefDetailMap.get(nomRefDetailFk);
				if (nomReference == null){
					nomReference = refDetailMap.get(nomRefDetailFk);
				}	
				if (nomReference == null){
					nomReference = nomRefMap.get(nomRefFkInt);
				}if (nomReference == null){
					nomReference = referenceMap.get(nomRefFkInt);
				}									
				
				//setNomRef
				if (nomReference == null ){
					//TODO
					if (! config.isIgnoreNull()){logger.warn("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
						" was not found in reference store. Nomenclatural reference was not set!!");}
				}else if (! INomenclaturalReference.class.isAssignableFrom(nomReference.getClass())){
					logger.error("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
					" is not assignable from INomenclaturalReference. Relation was not set!! (Class = " + nomReference.getClass()+ ")");
				}else{
					nomReference.setNomenclaturallyRelevant(true);
					taxonNameBase.setNomenclaturalReference((INomenclaturalReference)nomReference);
				}
			}
		}
		return success;
	}
	
	private static TeamOrPersonBase getAuthorTeam(MapWrapper<TeamOrPersonBase> authorMap, Object teamIdObject, int nameId, boolean ignoreNull){
		if (teamIdObject == null){
			return null;
		}else {
			int teamId = (Integer)teamIdObject;
			TeamOrPersonBase author = authorMap.get(teamId);
			if (author == null){
				//TODO
				if (!ignoreNull){ logger.warn("AuthorTeam (teamId = " + teamId + ") for TaxonName (nameId = " + nameId + ")"+
				" was not found in authorTeam store. Relation was not set!!");}
				return null;
//			}else if(teamId == 0){ //casus El Salvador
//				//do nothing 
//				return null;
			}else{
				return author;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoTaxonNames();
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
