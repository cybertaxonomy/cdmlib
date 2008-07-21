package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
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


public class BerlinModelTaxonNameIO extends BerlinModelIOBase {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameIO.class);

	private static int modCount = 5000;

	public BerlinModelTaxonNameIO(){
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
	protected boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp, 
			Map<String, MapWrapper<? extends CdmBase>> stores){				
			
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.AUTHOR_STORE);
		
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		boolean success = true ;
		
		logger.info("start makeTaxonNames ...");
		INameService nameService = cdmApp.getNameService();
		
		try {
			
			
			//get data from database
			String strQuery = 
					"SELECT Name.* , RefDetail.RefDetailId, RefDetail.RefFk, " +
                      		" RefDetail.FullRefCache, RefDetail.FullNomRefCache, RefDetail.PreliminaryFlag AS RefDetailPrelim, RefDetail.Details, " + 
                      		" RefDetail.SecondarySources, RefDetail.IdInSource " +
                    " FROM Name LEFT OUTER JOIN RefDetail ON Name.NomRefDetailFk = RefDetail.RefDetailId AND Name.NomRefDetailFk = RefDetail.RefDetailId AND " +
                    	" Name.NomRefFk = RefDetail.RefFk AND Name.NomRefFk = RefDetail.RefFk" +
                    " WHERE (1=1) ";
					//strQuery += " AND Name.Created_When > '03.03.2004' ";
			
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Names handled: " + (i-1));}
				
				//create TaxonName element
				int nameId = rs.getInt("nameId");
				int rankId = rs.getInt("rankFk");
				Object authorFk = rs.getObject("AuthorTeamFk");
				Object exAuthorFk = rs.getObject("ExAuthorTeamFk");
				Object basAuthorFk = rs.getObject("BasAuthorTeamFk");
				Object exBasAuthorFk = rs.getObject("ExBasAuthorTeamFk");
				Object nomRefFk = rs.getObject("NomRefFk");
				
				try {
					if (logger.isDebugEnabled()){logger.debug(rankId);}
					Rank rank = BerlinModelTransformer.rankId2Rank(rankId);
					
					TaxonNameBase taxonNameBase;
					if (bmiConfig.getNomenclaturalCode() != null){
						taxonNameBase = bmiConfig.getNomenclaturalCode().getNewTaxonNameInstance(rank);
					}else{
						taxonNameBase = NonViralName.NewInstance(rank);
					}
					
					if (rankId < 40){
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

					
					//nomenclatural Reference
					if (referenceMap != null){
						if (nomRefFk != null){
							int nomRefFkInt = (Integer)nomRefFk;
							ReferenceBase nomenclaturalReference = nomRefMap.get(nomRefFkInt);
							if (nomenclaturalReference == null){
								nomenclaturalReference = referenceMap.get(nomRefFkInt);
							}									
							if (nomenclaturalReference == null ){
								//TODO
								if (! config.isIgnoreNull()){logger.warn("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
									" was not found in reference store. Nomenclatural reference was not set!!");}
							}else if (! INomenclaturalReference.class.isAssignableFrom(nomenclaturalReference.getClass())){
								logger.error("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
								" is not assignable from INomenclaturalReference. Relation was not set!! (Class = " + nomenclaturalReference.getClass()+ ")");
							}else{
								nomenclaturalReference.setNomenclaturallyRelevant(true);
								taxonNameBase.setNomenclaturalReference((INomenclaturalReference)nomenclaturalReference);
							}
						}
					}

					//created, notes
					doIdCreatedUpdatedNotes(bmiConfig, taxonNameBase, rs, nameId);
					
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

					if (taxonNameBase instanceof ZoologicalName){
						ZoologicalName zooName = (ZoologicalName)taxonNameBase;
						//publicationYear
						String authorTeamYear = rs.getString("authorTeamYear");
						try {
							if (! "".equals(CdmUtils.Nz(authorTeamYear).trim())){
								Integer publicationYear  = Integer.valueOf(authorTeamYear);
								zooName.setPublicationYear(publicationYear);
							}
						} catch (NumberFormatException e) {
							logger.warn("authorTeamYear could not be parsed for taxonName: "+ nameId);
						}
						//original publication year
						String basAuthorTeamYear = rs.getString("basAuthorTeamYear");
						try {
							if (! "".equals(CdmUtils.Nz(basAuthorTeamYear).trim())){
								Integer OriginalPublicationYear  = Integer.valueOf(basAuthorTeamYear);
								zooName.setOriginalPublicationYear(OriginalPublicationYear);
							}
						} catch (NumberFormatException e) {
							logger.warn("basAuthorTeamYear could not be parsed for taxonName: "+ nameId);
						}
						
					}else if (taxonNameBase instanceof BotanicalName){
						BotanicalName botanicalName = (BotanicalName)taxonNameBase;

						dbAttrName = "HybridFormulaFlag";
						cdmAttrName = "isHybridFormula";
						success &= ImportHelper.addBooleanValue(rs, taxonNameBase, dbAttrName, cdmAttrName);

						dbAttrName = "MonomHybFlag";
						cdmAttrName = "isMonomHybrid";
						success &= ImportHelper.addBooleanValue(rs, taxonNameBase, dbAttrName, cdmAttrName);

						dbAttrName = "BinomHybFlag";
						cdmAttrName = "isBinomHybrid";
						success &= ImportHelper.addBooleanValue(rs, taxonNameBase, dbAttrName, cdmAttrName);

						dbAttrName = "TrinomHybFlag";
						cdmAttrName = "isTrinomHybrid";
						success &= ImportHelper.addBooleanValue(rs, taxonNameBase, dbAttrName, cdmAttrName);

						if (taxonNameBase instanceof CultivarPlantName){
							//TODO
							//CultivarGroupName
							//CultivarName
						}	
					}
					
					
					//TODO
					//Source_Acc
					//OrthoProjection

					//TODO
					//preliminaryFlag see above
					
					taxonNameMap.put(nameId, taxonNameBase);
					
				}
				catch (UnknownCdmTypeException e) {
					logger.warn("Name with id " + nameId + " has unknown rankId " + rankId + " and could not be saved.");
					success = false; 
				}
				
			} //while rs.hasNext()
			logger.info(i + " names handled");
			nameService.saveTaxonNameAll(taxonNameMap.objects());
			
//			makeNameSpecificData(nameMap);

			logger.info("end makeTaxonNames ...");
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

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
	
}
