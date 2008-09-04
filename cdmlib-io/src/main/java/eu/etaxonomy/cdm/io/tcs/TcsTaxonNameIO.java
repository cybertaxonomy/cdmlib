package eu.etaxonomy.cdm.io.tcs;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


public class TcsTaxonNameIO  extends TcsIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsTaxonNameIO.class);

	private static int modCount = 5000;
	
	public TcsTaxonNameIO(){
		super();
	}

	@Override
	public boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("BasionymRelations not yet implemented");
		logger.warn("Checking for TaxonNames not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);
		
		return result;
	}
	
	protected static CdmIoXmlMapperBase[] standardMappers = new CdmIoXmlMapperBase[]{
		new CdmTextElementMapper("genusPart", "genusOrUninomial")
		, new CdmTextElementMapper("specificEpithet", "specificEpithet")
		, new CdmTextElementMapper("infraspecificEpithet", "infraSpecificEpithet")
		, new CdmTextElementMapper("infragenericEpithet", "infraGenericEpithet")
		, new CdmTextElementMapper("microReference", nsTcom, "nomenclaturalMicroReference")		
	};

	protected static CdmIoXmlMapperBase[] operationalMappers = new CdmIoXmlMapperBase[]{
		new CdmUnclearMapper("basionymAuthorship")
		, new CdmUnclearMapper("combinationAuthorship")
		, new CdmUnclearMapper("hasAnnotation")
		, new CdmUnclearMapper("rank")
		, new CdmUnclearMapper("nomenclaturalCode")
		, new CdmUnclearMapper("publishedIn", nsTcom)
		, new CdmUnclearMapper("year")
	};
	
	protected static CdmIoXmlMapperBase[] unclearMappers = new CdmIoXmlMapperBase[]{
		new CdmUnclearMapper("authorship")
		, new CdmUnclearMapper("rankString")
		, new CdmUnclearMapper("nameComplete")
		, new CdmUnclearMapper("hasBasionym")
		, new CdmUnclearMapper("dateOfEntry", nsTpalm)	
	};
	
	@Override
	public boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp, Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.AUTHOR_STORE);

		String tcsElementName;
		Namespace tcsNamespace;
		String cdmAttrName;
		String value;
		
		logger.info("start makeTaxonNames ...");
		TcsImportConfigurator tcsConfig = (TcsImportConfigurator)config;
		Element root = tcsConfig.getSourceRoot();
		boolean success =true;
		INameService nameService = cdmApp.getNameService();
		
		Namespace rdfNamespace = tcsConfig.getRdfNamespace();
		Namespace taxonNameNamespace = tcsConfig.getTnNamespace();
		
		String idNamespace = "TaxonName";
		
		List<Element> elTaxonNames = root.getChildren("TaxonName", taxonNameNamespace);
		
		int i = 0;
		//for each taxonName
		for (Element elTaxonName : elTaxonNames){
			
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			
			Attribute about = elTaxonName.getAttribute("about", rdfNamespace);

			//create TaxonName element
			String nameAbout = elTaxonName.getAttributeValue("about", rdfNamespace);
			String strRank = XmlHelp.getChildAttributeValue(elTaxonName, "rank", taxonNameNamespace, "resource", rdfNamespace);
			String strNomenclaturalCode = XmlHelp.getChildAttributeValue(elTaxonName, "nomenclaturalCode", taxonNameNamespace, "resource", rdfNamespace);
			
			try {
				Rank rank = TcsTransformer.rankString2Rank(strRank);
				NomenclaturalCode nomCode = TcsTransformer.nomCodeString2NomCode(strNomenclaturalCode);
				TaxonNameBase nameBase = nomCode.getNewTaxonNameInstance(rank);
				
				Set<String> omitAttributes = null;
				makeStandardMapper(elTaxonName, nameBase, omitAttributes, standardMappers);
				
				//Reference
				//TODO
				tcsElementName = "publishedIn";
				tcsNamespace = tcsConfig.getCommonNamespace();
				value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, tcsNamespace);
				if (value != null){
					Generic nomRef = Generic.NewInstance(); //TODO
					nomRef.setTitleCache(value);
					nameBase.setNomenclaturalReference(nomRef);
					
					//TODO
					tcsElementName = "year";
					tcsNamespace = taxonNameNamespace;
					Integer year = null;
					value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, tcsNamespace);
					if (value != null){
						try {
							year = Integer.valueOf(value);
							Calendar cal = Calendar.getInstance();
							//FIXME
							cal.set(year, 1, 1);
							nomRef.setDatePublished(TimePeriod.NewInstance(cal));
						} catch (RuntimeException e) {
							logger.warn("year could not be parsed");
						}
					}
				}
						
				//Status
				tcsNamespace = taxonNameNamespace;
				Element elAnnotation = elTaxonName.getChild("hasAnnotation", tcsNamespace);
				if (elAnnotation != null){
					Element elNomenclaturalNote = elAnnotation.getChild("NomenclaturalNote", tcsNamespace);
					if (elNomenclaturalNote != null){
						String statusValue = (String)ImportHelper.getXmlInputValue(elNomenclaturalNote, "note", tcsNamespace);
						String type = XmlHelp.getChildAttributeValue(elNomenclaturalNote, "type", tcsNamespace, "resource", rdfNamespace);
						String tdwgType = "http://rs.tdwg.org/ontology/voc/TaxonName#PublicationStatus";
						if (tdwgType.equalsIgnoreCase(type)){
							try {
								NomenclaturalStatusType statusType = TcsTransformer.nomStatusString2NomStatus(statusValue);
								//NomenclaturalStatusType statusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusValue);
								if (statusType != null){
									nameBase.addStatus(NomenclaturalStatus.NewInstance(statusType));
								}
							} catch (UnknownCdmTypeException e) {
								if (! statusValue.equals("valid")){
									logger.warn("Unknown NomenclaturalStatusType: " +  statusValue);
								}
							}
						}
					}
				}
				
				if (nameBase instanceof NonViralName){
					NonViralName nonViralName = (NonViralName)nameBase;
					
					//AuthorTeams
					//TODO
					tcsElementName = "basionymAuthorship";
					value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
					if (value != null){
						INomenclaturalAuthor basionymAuthor = Team.NewInstance();
						basionymAuthor.setNomenclaturalTitle(value);
						nonViralName.setBasionymAuthorTeam(basionymAuthor);
					}
						
					//TODO
					tcsElementName = "combinationAuthorship";
					value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
					if (value != null){
						INomenclaturalAuthor combinationAuthor = Team.NewInstance();
						combinationAuthor.setNomenclaturalTitle(value);
						nonViralName.setCombinationAuthorTeam(combinationAuthor);
					}
						
				}
				ImportHelper.setOriginalSource(nameBase, config.getSourceReference(), nameAbout, idNamespace);
				
				checkAdditionalContents(elTaxonName, standardMappers, operationalMappers, unclearMappers);
				
				//nameId
				//TODO
				//ImportHelper.setOriginalSource(nameBase, tcsConfig.getSourceReference(), nameId);
				taxonNameMap.put(nameAbout, nameBase);
				
			} catch (UnknownCdmTypeException e) {
				//FIXME
				logger.warn("Name with id " + nameAbout + " has unknown rank " + strRank + " and could not be saved.");
				success = false; 
			}
		}
		logger.info(i + " names handled");
		nameService.saveTaxonNameAll(taxonNameMap.objects());
//		makeNameSpecificData(nameMap);
		logger.info("end makeTaxonNames ...");
		return success;

	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoTaxonNames();
	}
}
