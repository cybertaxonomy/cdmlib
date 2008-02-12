package eu.etaxonomy.cdm.io.berlinModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownRankException;

public class BerlinModelImport {
	private static final Logger logger = Logger.getLogger(BerlinModelImport.class);
	
	private boolean deleteAll = false;
	
	//Connection
	private Source source;
	
	//CdmApplication
	private CdmApplicationController cdmApp;

	//	DatabaseData
	static String dbms = "SQLServer";
	static String strServer = "BGBM111";
	static String strDB = "EuroPlusMed_00_Edit";
	static int port = 1247;
	static String userName = "webUser";
	static String pwd = "";
	
	static DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
	static String cdmServer = "192.168.2.10";
	static String cdmDB = "cdm_test_lib";
	//static int cdmPort = 1247;
	static String cdmUserName = "edit";
	static String cdmPwd = "wp5";
	
	//Constants
	//final boolean OBLIGATORY = true; 
	//final boolean FACULTATIVE = false; 
	final int modCount = 100;

	
	//Hashmaps for Joins
	private Map<Integer, UUID> referenceMap = new HashMap<Integer, UUID>();
	private Map<Integer, UUID> taxonNameMap = new HashMap<Integer, UUID>();
	private Map<Integer, UUID> taxonMap = new HashMap<Integer, UUID>();


	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from BerlinModel ...");
		BerlinModelImport bmImport = new BerlinModelImport();
		bmImport.doImport();
		System.out.println("End import from BerlinModel ...");
	}
	
	/**
	 * Executes the whole 
	 */
	private void doImport(){
		makeSource(dbms, strServer, strDB, port, userName, pwd);
			
		//Start
		String dataSourceName = "cdmImportLibrary";
		CdmDataSource dataSource;
		try {
			dataSource = CdmDataSource.NewInstance(dataSourceName);
		} catch (DataSourceNotFoundException e1) {
			dataSource = CdmDataSource.save(dataSourceName, dbType, cdmServer, cdmDB, cdmUserName, cdmPwd);
		}
		try {
			cdmApp = new CdmApplicationController(dataSource);
		} catch (DataSourceNotFoundException e) {
			logger.error(e.getMessage());
			return;
		}
		
		//make and save Names
		makeTaxonNames();
		
			
			
		if (false){
			//make and save publications
/*				makePublications(root);
				saveToXml(root.getChild("Publications", nsTcs), outputPath, outputFileName + "Publications", format);
				
				saveToXml(root.getChild("TaxonNames", nsTcs), outputPath, outputFileName + "_TaxonNames", format);
				
				//make and save Concepts
				makeConcepts(root);
				saveToXml(root.getChild("TaxonConcepts", nsTcs), outputPath, outputFileName + "_TaxonConcepts", format);
*/		}
	}
	
	
	/**
	 * @param dataSet
	 * @return
	 */
	private boolean makeTaxonNames(){
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxonNames ...");
		INameService nameService = cdmApp.getNameService();
		boolean delete = deleteAll;
		
		if (delete){
			List<TaxonNameBase> listAllNames =  nameService.getAllNames(0, 1000);
			while(listAllNames.size() > 0 ){
				for (TaxonNameBase name : listAllNames ){
					//FIXME
					//nameService.remove(name);
				}
				listAllNames =  nameService.getAllNames(0, 1000);
			}
		}
		
		try {
			
			
			//get data from database
			String strQuery = 
					"SELECT TOP 102   Name.* , RefDetail.RefDetailId, RefDetail.RefFk, " +
                      		" RefDetail.FullRefCache, RefDetail.FullNomRefCache, RefDetail.PreliminaryFlag AS RefDetailPrelim, RefDetail.Details, " + 
                      		" RefDetail.SecondarySources, RefDetail.IdInSource " +
                    " FROM Name LEFT OUTER JOIN RefDetail ON Name.NomRefDetailFk = RefDetail.RefDetailId AND Name.NomRefDetailFk = RefDetail.RefDetailId AND " +
                    " Name.NomRefFk = RefDetail.RefFk AND Name.NomRefFk = RefDetail.RefFk"; 
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Names handled: " + (i-1));}
				
				//create TaxonName element
				int nameId = rs.getInt("nameId");
				int rankId = rs.getInt("rankFk");
				
				try {
					logger.info(rankId);
					Rank rank = BerlinModelTransformer.rankId2Rank(rankId);
					//FIXME
					//BotanicalName name = BotanicalName.NewInstance(BerlinModelTransformer.rankId2Rank(rankId));
					BotanicalName botanicalName = new BotanicalName(rank);
					
					if (rankId < 40){
						dbAttrName = "supraGenericName";
					}else{
						dbAttrName = "genus";
					}
					cdmAttrName = "uninomial";
					ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "genusSubdivisionEpi";
					cdmAttrName = "infraGenericEpithet";
					ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "speciesEpi";
					cdmAttrName = "specificEpithet";
					ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					

					dbAttrName = "infraSpeciesEpi";
					cdmAttrName = "infraSpecificEpithet";
					ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "unnamedNamePhrase";
					cdmAttrName = "appendedPhrase";
					ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "preliminaryFlag";
					cdmAttrName = "XX" + "protectedTitleCache";
					ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "HybridFormulaFlag";
					cdmAttrName = "isHybridFormula";
					ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "MonomHybFlag";
					cdmAttrName = "isMonomHybrid";
					ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "BinomHybFlag";
					cdmAttrName = "isBinomHybrid";
					ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "TrinomHybFlag";
					cdmAttrName = "isTrinomHybrid";
					ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					//botanicalName.setProtectedTitleCache(protectedTitleCache)

//					dbAttrName = "notes";
//					cdmAttrName = "isTrinomHybrid";
//					ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					//TODO
					//Created
					//Note
					//makeAuthorTeams
					//CultivarGroupName
					//CultivarName
					//Source_Acc
					//OrthoProjection
					
					//Details
					
					dbAttrName = "details";
					cdmAttrName = "nomenclaturalmicroreference";
					ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);

					
					
					UUID nameUuid = nameService.saveTaxonName(botanicalName);
					taxonNameMap.put(nameId, nameUuid);
					
				} catch (UnknownRankException e) {
					logger.warn("Name with id " + nameId + " has unknown rankId " + rankId + " and could not be saved.");
				}
				
			}	
				
//				//id
//				strDbAttr = "NameId";
//					//save id  for later
//				int intID = rs.getInt(strDbAttr);
//				
//				String nameId = rs.getString(strDbAttr);
//				
//				//add TaxonName to nameMap
//				nameMap.put(nameId, elTaxonName);
//				
//				
//				strValue = nameId;
//				strAttrName = "id";
//				parent = elTaxonName;
//				xml.addStringAttribute(strValue, parent, strAttrName, NS_NULL);
//				
//				//Code
//				strAttrName = "nomenclaturalCode";
//				strValue = "Botanical";
//				parent = elTaxonName;
//				xml.addStringAttribute(strValue, parent,  strAttrName, NS_NULL);
//				
//				//Simple
//				strDbAttr = "FullNameCache";
//				strElName = "Simple";
//				parent = elTaxonName;
//				xml.addElement(rs,strDbAttr, parent, strElName, nsTcs, OBLIGATORY);
//				
//				if (fullVersion){
//					//Rank
//					strDbAttr = "RankAbbrev";
//					strElName = "Rank";
//					parent = elTaxonName;
//					xml.addElement(rs,strDbAttr, parent, strElName, nsTcs, OBLIGATORY);
//					
//					//CanonicalName
//					parent = elTaxonName;
//					makeCanonicalName(rs, parent);
//					
//					
//					//CanonicalAuthorship
//					parent = elTaxonName;
//					makeCanonicalAuthorship(rs, parent);
//				
//				}  //fi fullVersion
//				
//				//PublishedIn
//				strDbAttr = "NomRefFk";
//				strAttrName = "ref";
//				strElName = "PublishedIn";
//				parent = elTaxonName;
//				Attribute attrPublRef = xml.addAttributeInElement(rs, strDbAttr, parent, strAttrName, strElName, nsTcs, FACULTATIVE);
//				
//				if (attrPublRef != null){
//					//does Publication exist?
//					String ref = attrPublRef.getValue();
//					if (! publicationMap.containsKey(ref)){
//						logger.error("PublishedIn ref " + ref + " for " + nameId + " does not exist.");
//					}
//				}
//				
//				
//				if (fullVersion){
//					//Year
//					String year = rs.getString("RefYear");
//					if (year == null) {
//						year = rs.getString("HigherRefYear");
//					}
//					strValue = year;
//					strElName = "Year";
//					parent = elTaxonName;
//					xml.addStringElement(strValue, parent, strElName, nsTcs, FACULTATIVE);
//					
//					//MicroReference
//					strDbAttr = "Details";
//					strElName = "MicroReference";
//					parent = elTaxonName;
//					xml.addElement(rs,strDbAttr, parent, strElName, nsTcs, FACULTATIVE);
//
//				}//fi fullversion
//			}//while
//			
//			//insert related Names (Basionyms, ReplacedSyns, etc.
//			makeSpellingCorrections(nameMap);
//			makeBasionyms(nameMap);
//			makeLaterHomonyms(nameMap);
//			makeReplacedNames(nameMap);
//			
//			//insert Status infos
//			makeNameSpecificData(nameMap);
			//cdmApp.flush();
			logger.info("end makeTaxonNames ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	
	
	/**
	 * initializes source
	 * @return true, if connection establisehd
	 */
	private boolean makeSource(String dbms, String strServer, String strDB, int port, String userName, String pwd){
		//establish connection
		try {
			source = new Source(dbms, strServer, strDB);
			source.setPort(port);
			source.setUserAndPwd(userName, pwd);
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

}
