package tcsxml;

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
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


public class TcsXmlTaxonNameIO  extends TcsXmlIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsXmlTaxonNameIO.class);

	private static int modCount = 5000;
	
	public TcsXmlTaxonNameIO(){
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
		, new CdmTextElementMapper("uninomial", "genusOrUninomial")  //TODO make it a more specific Mapper for both attributes
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
	public boolean doInvoke(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores){
		
		logger.info("start make TaxonNames ...");
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		INameService nameService = config.getCdmAppController().getNameService();

		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "TaxonName";

		TcsXmlImportConfigurator tcsConfig = (TcsXmlImportConfigurator)config;
		Element elDataSet = getDataSetElement(tcsConfig);
		Namespace tcsNamespace = tcsConfig.getTcsXmlNamespace();
		
		DoubleResult<Element, Boolean> doubleResult;
		childName = "TaxonNames";
		obligatory = false;
		Element elTaxonNames = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
		
		String tcsElementName = "TaxonName";
		List<Element> elTaxonNameList = elTaxonNames.getChildren(tcsElementName, tcsNamespace);
		
		int i = 0;
		//for each taxonName
		for (Element elTaxonName : elTaxonNameList){
			
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			
			Attribute id = elTaxonName.getAttribute("id", tcsNamespace);

			
			//create TaxonName element
			String strId = elTaxonName.getAttributeValue("id", tcsNamespace);
			String strNomenclaturalCode = elTaxonName.getAttributeValue("nomenclaturalCode", tcsNamespace);
			
			childName = "TaxonNames";
			obligatory = false;
			Element elRank = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
			Rank rank = makeRank(elRank);
			
			
			try {
				NomenclaturalCode nomCode = TcsXmlTransformer.nomCodeString2NomCode(strNomenclaturalCode);
				TaxonNameBase nameBase = nomCode.getNewTaxonNameInstance(rank);

				childName = "Simple";
				obligatory = true;
				Element elSimple = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
				String simple = (elSimple == null)? "" : elSimple.getTextNormalize();
				
				childName = "CanonicalName";
				obligatory = false;
				Element elCanonicalName = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
				makeCanonicalName(nameBase, elCanonicalName, success);
				
				childName = "CanonicalAuthorship";
				obligatory = false;
				Element elCanonicalAuthorship = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
				makeCanonicalAuthorship(nameBase, elCanonicalAuthorship, success);
				
				childName = "PublishedIn";
				obligatory = false;
				Element elPublishedIn = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
				makePublishedIn(nameBase, elPublishedIn, success);
				
				childName = "Year";
				obligatory = false;
				Element elYear = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
				makeYear(nameBase, elYear, success);
				
				childName = "MicroReference";
				obligatory = false;
				Element elMicroReference = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
				makeMicroReference(nameBase, elMicroReference, success);
				
				childName = "Typification";
				obligatory = false;
				Element elTypification = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
				makeTypification(nameBase, elTypification, success);
				
				childName = "PublicationStatus";
				obligatory = false;
				Element elPublicationStatus = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
				makePublicationStatus(nameBase, elPublicationStatus, success);
				
				childName = "ProviderLink";
				obligatory = false;
				Element elProviderLink = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
				makeProviderLink(nameBase, elProviderLink, success);
				
				childName = "ProviderSpecificData";
				obligatory = false;
				Element elProviderSpecificData = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
				makeProviderSpecificData(nameBase, elProviderSpecificData, success);
				
				//TODO relations 
				
				testAdditionalElements();
				ImportHelper.setOriginalSource(nameBase, config.getSourceReference(), strId, idNamespace);
				checkAdditionalContents(elTaxonName, standardMappers, operationalMappers, unclearMappers);
				
				//nameId
				//TODO
				//ImportHelper.setOriginalSource(nameBase, tcsConfig.getSourceReference(), nameId);
				
				
				taxonNameMap.put(strId, nameBase);
				
			} catch (UnknownCdmTypeException e) {
				logger.warn("Name with id " + strId + " has unknown nomenclatural code.");
				success.setValue(false); 
			}
		}
		logger.info(i + " names handled");
		nameService.saveTaxonNameAll(taxonNameMap.objects());
//		makeNameSpecificData(nameMap);
		logger.info("end makeTaxonNames ...");
		return success.getValue();

	}
	
	/**
	 * Returns the rank represented by the rank element.<br>
	 * Returns <code>null</code> if the element is null.<br>
	 * Returns <code>null</code> if the code and the text are both either empty or do not exists.<br>
	 * Returns the rank represented by the code attribute, if the code attribute is not empty and could be resolved.<br>
	 * If the code could not be resolved it returns the rank represented most likely by the elements text.<br>
	 * Returns UNKNOWN_RANK if code attribute and element text could not be resolved.
	 * @param elRank tcs rank element
	 * @return 
	 */
	private Rank makeRank(Element elRank){
		Namespace ns = elRank.getNamespace();
		Rank result;
		if (elRank == null){
			return null;
		}
		String strRankCode = elRank.getAttributeValue("code", ns);
		String strRankString = elRank.getTextNormalize();
		if (strRankCode == null || "".equals(strRankCode.trim()) &&
				strRankString == null || "".equals(strRankString.trim())
			){
			return null;
		}
		
		Rank codeRank = null;
		try {
			codeRank = TcsXmlTransformer.rankCode2Rank(strRankCode);
		} catch (UnknownCdmTypeException e1) {
			codeRank = Rank.UNKNOWN_RANK();
		}
		Rank stringRank = null;
		try {
			boolean useUnknown = true;
			stringRank = Rank.getRankByNameOrAbbreviation(strRankString, useUnknown);
		} catch (UnknownCdmTypeException e1) {
			//does not happen because of useUnknown = true
		}
		
		//codeRank exists
		if (! (codeRank == null) && ! codeRank.equals(Rank.UNKNOWN_RANK())){
			result = codeRank;
			if (! codeRank.equals(stringRank) && ! stringRank.equals(Rank.UNKNOWN_RANK())){
				logger.warn("code rank and string rank are unequal. code: " + codeRank.getLabel() + stringRank.getLabel());
			}
		}
		//codeRank does not exist
		else{
			result = stringRank;
			logger.warn("string rank used, because code rank does not exist or was not recognized: " + stringRank.getLabel());
		}
		return result;
	}

	private void makeCanonicalName(TaxonNameBase<?,?> name, Element elCanonicalName, ResultWrapper<Boolean> success){
		boolean cacheProtected = false;
		
		if (elCanonicalName == null){
			return;
		}
		Namespace ns = elCanonicalName.getNamespace();
		
		String childName = "Simple";
		boolean obligatory = true;
		Element elSimple = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
		String simple = (elSimple == null)? "" : elSimple.getTextNormalize();
		name.setFullTitleCache(simple, cacheProtected);
		
		
		if (name instanceof NonViralName<?>){
			NonViralName<?> nonViralName = (NonViralName<?>)name;
			childName = "Uninomial";
			obligatory = false;
			Element elUninomial = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
			String uninomial = (elUninomial == null)? "" : elUninomial.getTextNormalize();
			nonViralName.setGenusOrUninomial(uninomial);
			if (! nonViralName.getRank().isSupraGeneric() ){  // TODO check
				logger.warn("Name is not of supra generic rank, but has a canonical name part 'Uninomial'.");
			}
			testNoMoreElements();
			
			childName = "Genus";
			obligatory = false;
			Element elGenus = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
			String genus = (elGenus == null)? "" : elGenus.getTextNormalize();
			//TODO do Attributes reference
			makeRefAndLinkType(name, elGenus, success);
			nonViralName.setGenusOrUninomial(genus);
			if ( nonViralName.getRank().isSupraGeneric() ){  // TODO check
				logger.warn("Name is supra generic but has canonical name part 'Genus'.");
			}
			
			childName = "InfragenericEpithet";
			obligatory = false;
			Element elInfrageneric = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
			String infraGenericEpithet = (elInfrageneric == null)? "" : elInfrageneric.getTextNormalize();
			nonViralName.setInfraGenericEpithet(infraGenericEpithet);
			if (! name.getRank().isInfraGeneric()){
				logger.warn("Name is not infra generic but has canonical name part 'InfragenericEpithet'.");
			}
			testNoMoreElements();
			
			childName = "SpecificEpithet";
			obligatory = false;
			Element elSpecificEpithet = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
			String specificEpithet = (elSpecificEpithet == null)? "" : elSpecificEpithet.getTextNormalize();
			nonViralName.setSpecificEpithet(specificEpithet);
			if (name.getRank().isHigher(Rank.SPECIES()) ){
				logger.warn("Name is not species or below but has canonical name part 'SpecificEpithet'.");
			}
			testNoMoreElements();
			
			childName = "InfraspecificEpithet";
			obligatory = false;
			Element elInfraspecificEpithet = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
			String infraspecificEpithet = (elSpecificEpithet == null)? "" : elSpecificEpithet.getTextNormalize();
			nonViralName.setInfraSpecificEpithet(infraspecificEpithet);
			if (! name.getRank().isInfraSpecific() ){
				logger.warn("Name is not infraspecific but has canonical name part 'InfraspecificEpithet'.");
			}
			testNoMoreElements();
			
	
		}else{ //ViralName
			logger.warn("Non NonViralNames not yet supported by makeCanonicalName");
		}
				
		
		
		childName = "CultivarNameGroup";
		obligatory = false;
		Element elCultivarNameGroup = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
		String cultivarNameGroup = (elCultivarNameGroup == null)? "" : elCultivarNameGroup.getTextNormalize();
		if (! "".equals(cultivarNameGroup.trim())){
			if (name instanceof CultivarPlantName){
				
			}else{
				logger.warn("Non cultivar name has 'cultivar name group' element. Omitted");
			}
		}
		
		return;
	}
	
	protected void testNoMoreElements(){
		logger.warn("Not yet implemented");
	}
	
	private void makeRefAndLinkType(TaxonNameBase name, Element elGenus, ResultWrapper<Boolean> success){
		logger.warn("'makeRefAndLinkType' Not yet implemented");
	}
	
	
	private INomenclaturalAuthor makeNameCitation(Element elNameCitation, ResultWrapper<Boolean> success){
		INomenclaturalAuthor result = null; 
		Namespace ns = elNameCitation.getNamespace();
		String childName;
		boolean obligatory;
		if (elNameCitation != null){
			
			childName = "Authors";
			obligatory = true;
			Element elAuthors = XmlHelp.getSingleChildElement(success, elNameCitation, childName, ns, obligatory);
			testNoMoreElements();

			if (elAuthors != null){
				childName = "AgentName";
				List<Element> elAgentList = elAuthors.getChildren(childName, ns);
				Team team = Team.NewInstance();
				result = team;
				if (elAgentList.size() > 1){
					for(Element elAgent : elAgentList){
						team.addTeamMember(makeAgent(elAgent, ns, success));
					}
				}else if(elAgentList.size() == 1){
					result = makeAgent(elAgentList.get(0), ns, success);
				}
			}else{
				childName = "Simple";
				obligatory = true;
				Element elSimple = XmlHelp.getSingleChildElement(success, elNameCitation, childName, ns, obligatory);
				String simple = (elSimple == null)? "" : elSimple.getTextNormalize();
				result = Team.NewInstance();
				result.setNomenclaturalTitle(simple);
			}
		}
		return result;
	}

	private Person makeAgent(Element agentName, Namespace ns, ResultWrapper<Boolean> success){
		if (agentName != null){
			String authorTitle = agentName.getTextNormalize();
			Person result = Person.NewTitledInstance(authorTitle);
			makeReferenceType(success);
			return result;
		}else{
			return null;
		}
	}
	
	private void makeCanonicalAuthorship(TaxonNameBase name, Element elCanonicalAuthorship, ResultWrapper<Boolean> success){
		if (elCanonicalAuthorship != null){
			Namespace ns = elCanonicalAuthorship.getNamespace();
			boolean cacheProtected = false;
	
			if (name instanceof NonViralName){
				NonViralName nonViralName = (NonViralName)name;
				
				String childName = "Simple";
				boolean obligatory = true;
				Element elSimple = XmlHelp.getSingleChildElement(success, elCanonicalAuthorship, childName, ns, obligatory);
				String simple = (elSimple == null)? "" : elSimple.getTextNormalize();
				nonViralName.setAuthorshipCache(simple, cacheProtected);

				childName = "Authorship";
				obligatory = true;
				Element elAuthorship = XmlHelp.getSingleChildElement(success, elCanonicalAuthorship, childName, ns, obligatory);
				INomenclaturalAuthor author = makeNameCitation(elAuthorship, success); 
				nonViralName.setCombinationAuthorTeam(author);
				testNoMoreElements();
				
				childName = "BasionymAuthorship";
				obligatory = true;
				Element elBasionymAuthorship = XmlHelp.getSingleChildElement(success, elCanonicalAuthorship, childName, ns, obligatory);
				INomenclaturalAuthor basionymAuthor = makeNameCitation(elBasionymAuthorship, success); 
				nonViralName.setBasionymAuthorTeam(basionymAuthor);
				testNoMoreElements();

				
				childName = "CombinationAuthorship";
				obligatory = true;
				Element elCombinationAuthorship = XmlHelp.getSingleChildElement(success, elCanonicalAuthorship, childName, ns, obligatory);
				INomenclaturalAuthor combinationAuthor = makeNameCitation(elBasionymAuthorship, success); 
				nonViralName.setCombinationAuthorTeam(combinationAuthor);
				testNoMoreElements();
				
			}	
		}

	}

	
	private void makePublishedIn(TaxonNameBase name, Element elPublishedIn, ResultWrapper<Boolean> success){
		if (elPublishedIn != null){
			makeReferenceType(success);
			logger.warn("Not yet implemented");
			success.setValue(false);
		}
	}

	
	private void makeYear(TaxonNameBase name, Element elYear, ResultWrapper<Boolean> success){
		if (elYear != null){
			String year = elYear.getTextNormalize();
			if (name instanceof ZoologicalName){
				((ZoologicalName)name).setPublicationYear(getIntegerYear(year));
			}else{
				logger.warn("Year can be set only for a zoological name");
			}
		}
	}
	
	private Integer getIntegerYear(String year){
		try {
			Integer result = Integer.valueOf(year);
			return result;
		} catch (NumberFormatException e) {
			logger.warn("Year string could not be parsed. Set = 9999 instead");
			return 9999;
		}
	}

	
	private void makeMicroReference(TaxonNameBase name, Element elMicroReference, ResultWrapper<Boolean> success){
		if (elMicroReference != null){
			String microReference = elMicroReference.getTextNormalize();
			name.setNomenclaturalMicroReference(microReference);
		}
	}

	
	private void makeTypification(TaxonNameBase name, Element elTypifiacation, ResultWrapper<Boolean> success){
		if (elTypifiacation != null){
			logger.warn("Not yet implemented");
			success.setValue(false);
		}
	}

	
	private void makePublicationStatus(TaxonNameBase name, Element elPublicationStatus, ResultWrapper<Boolean> success){
		//Status
			
		if (elPublicationStatus != null){
			logger.warn("Not yet implemented");
			success.setValue(false);
		}
	}
	
	private void makeProviderLink(TaxonNameBase name, Element elProviderLink, ResultWrapper<Boolean> success){
		if (elProviderLink != null){
			logger.warn("Not yet implemented");
			success.setValue(false);
		}
	}
	

	private void makeProviderSpecificData(TaxonNameBase name, Element elProviderSpecificData, ResultWrapper<Boolean> success){
		if (elProviderSpecificData != null){
			logger.warn("Not yet implemented");
			success.setValue(false);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoTaxonNames();
	}
	
	private void makeReferenceType(ResultWrapper<Boolean> success){
		logger.warn("Not yet implemented");
		success.setValue(false);
		
	}

}
