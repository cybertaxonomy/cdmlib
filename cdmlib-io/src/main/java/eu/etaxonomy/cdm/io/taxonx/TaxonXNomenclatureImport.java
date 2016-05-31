/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.taxonx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
@Component
public class TaxonXNomenclatureImport extends CdmIoBase<TaxonXImportState> implements ICdmIO<TaxonXImportState> {
	private static final Logger logger = Logger.getLogger(TaxonXNomenclatureImport.class);

	@SuppressWarnings("unused")
	private static int modCount = 10000;

	public TaxonXNomenclatureImport(){
		super();
	}

	@Override
    public boolean doCheck(TaxonXImportState state){
		boolean result = true;
		logger.warn("Checking for Types not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);

		return result;
	}

	@Override
    public void doInvoke(TaxonXImportState state){
		logger.info("start make Nomenclature ...");
		TransactionStatus tx = startTransaction();
		TaxonXImportConfigurator config = state.getConfig();
		Element root = config.getSourceRoot();
		Namespace nsTaxonx = root.getNamespace();

		//for testing only
		Taxon taxon = getTaxon(config);
		boolean isChanged = false;

		Element elTaxonBody = root.getChild("taxonxBody", nsTaxonx);
		Element elTreatment = elTaxonBody.getChild("treatment", nsTaxonx);
		Element elNomenclature = elTreatment.getChild("nomenclature", nsTaxonx);

		//isChanged |= doCollectionEvent(txConfig, elNomenclature, nsTaxonx, taxon);

		if (taxon != null && taxon.getName() != null && elNomenclature != null){
			isChanged |= doNomenclaturalType(config, elNomenclature, nsTaxonx, taxon.getName());
			List<Element> elSynonymyList = new ArrayList<Element>();
			elSynonymyList.addAll(elNomenclature.getChildren("synonomy", nsTaxonx));
			elSynonymyList.addAll(elNomenclature.getChildren("synonymy", nsTaxonx)); //wrong spelling in TaxonX-Schema
			for (Element elSynonymy : elSynonymyList){
				String synonymName = elSynonymy.getChildTextTrim("name");
				if (elSynonymy.getChild("type", nsTaxonx) != null || elSynonymy.getChild("type_loc", nsTaxonx) != null){
					Synonym synonym = getSynonym(config, taxon, synonymName);
					if (synonym != null){
						isChanged |= doNomenclaturalType(config, elSynonymy, nsTaxonx, synonym.getName());
					}
				}
			}
		}


		if (isChanged){
			getTaxonService().save(taxon);
		}
		commitTransaction(tx);
		return;
	}

	private Synonym getSynonym(TaxonXImportConfigurator config, Taxon taxon, String synName){
		Synonym result = null;
		unlazySynonym(config, taxon);
		Set<Synonym> synList = taxon.getSynonyms();
		for (Synonym syn : synList){
			TaxonNameBase<?,?> nameBase = syn.getName();
			if (nameBase != null){
				if (nameBase.isInstanceOf(NonViralName.class)){
					NonViralName<?> nonViralName = nameBase.deproxy(nameBase, NonViralName.class);
					if (nonViralName.getNameCache().equals(synName)){
						return syn;  //only first synonym is returned
					}
				}
			}
		}
		logger.warn("Synonym ("+synName+ ")not found for taxon " + taxon.getTitleCache() + getBracketSourceName(config));
		return result;
	}

	private Taxon getTaxon(TaxonXImportConfigurator config){
		Taxon result;
//		result =  Taxon.NewInstance(BotanicalName.NewInstance(null), null);
		//ICommonService commonService =config.getCdmAppController().getCommonService();
		ICommonService commonService = getCommonService();
		String originalSourceId = config.getOriginalSourceId();
		String namespace = config.getOriginalSourceTaxonNamespace();
		result = (Taxon)commonService.getSourcedObjectByIdInSource(Taxon.class, originalSourceId , namespace);
		if (result == null){
			logger.warn("Taxon (id: " + originalSourceId + ", namespace: " + namespace + ") could not be found");
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
    protected boolean isIgnore(TaxonXImportState state){
		return ! state.getConfig().isDoTypes();
	}

	/**
	 *
	 * Reads the collection_event tag, creates the according data and stores it.
	 * TODO under work
	 * @param elNomenclature
	 * @param nsTaxonx
	 * @param taxonBase
	 * @return
	 */
	private boolean doNomenclaturalType(TaxonXImportConfigurator config, Element elNomenclature, Namespace nsTaxonx, TaxonNameBase taxonName){
		boolean success = true;
		if (taxonName == null){
			logger.warn("taxonName is null");
			return false;
		}
		if (elNomenclature == null){
			logger.warn("elNomenclature is null");
			return false;
		}


		Element elType = elNomenclature.getChild("type", nsTaxonx);
		Element elTypeLoc = elNomenclature.getChild("type_loc", nsTaxonx);

		if (elType != null || elTypeLoc != null){
			unlazyTypeDesignation(config, taxonName);

			if (taxonName.isInfraGeneric() || taxonName.isSupraGeneric() || taxonName.isGenus()){
				success &= doNameType(elType, taxonName, config);
			}else{
				success &= doSpecimenType(config, elType, elTypeLoc, taxonName);


			}
			return success;
		}
		return false;
	}


	private boolean doSpecimenType(TaxonXImportConfigurator config, Element elType, Element elTypeLoc, TaxonNameBase taxonName){
		Reference citation = null;
		String citationMicroReference = null;
		String originalNameString = null;
		boolean isNotDesignated = true;
		boolean addToAllHomotypicNames = true;

		SimpleSpecimen simpleSpecimen = SimpleSpecimen.NewInstance();
		//elType
		if (elType != null){
			doElType(elType, simpleSpecimen, config);
		}//elType

		//typeLoc
		HashMap<DerivedUnit, SpecimenTypeDesignationStatus> typeLocMap = null;
		if (elTypeLoc != null){
			typeLocMap = doElTypeLoc(elTypeLoc, simpleSpecimen, taxonName, config);
		}
		if (typeLocMap != null && typeLocMap.size() >0){
			for (DerivedUnit specimen : typeLocMap.keySet()){
				SpecimenTypeDesignationStatus status = typeLocMap.get(specimen);
				taxonName.addSpecimenTypeDesignation(specimen, status, citation, citationMicroReference, originalNameString, isNotDesignated, addToAllHomotypicNames);
			}
		}else{ // no type_loc
			SpecimenTypeDesignationStatus status = null;
			taxonName.addSpecimenTypeDesignation(simpleSpecimen.getSpecimen(), status, citation, citationMicroReference, originalNameString, isNotDesignated, addToAllHomotypicNames);
		}
		return true;
	}

	private boolean doElType(Element elType, SimpleSpecimen simpleSpecimen, TaxonXImportConfigurator config){
		//type
		String text = elType.getTextNormalize();
		if (text.endsWith(";")){
			text = text + " ";
		}
		String[] type = text.split(";");
		if (type.length != 3 ){
			if (text.equals("")){
				logger.info("<nomenclature><type> is empty: " + getBracketSourceName(config));
			}else{
				logger.warn("<nomenclature><type> is of unsupported format: " + elType.getTextNormalize() + getBracketSourceName(config));
			}
			simpleSpecimen.setTitleCache(elType.getTextNormalize());
		}else{
			String strLocality = type[0].trim();
			if (! "".equals(strLocality)){
//				simpleSpecimen.setLocality(strLocality);
			}

			String strCollector = type[1].trim();
			if (! "".equals(strCollector)){
				AgentBase collector = Person.NewTitledInstance(strCollector);
//				simpleSpecimen.setCollector(collector);
			}

			String strCollectorNumber = type[2].trim();
			if (! "".equals(strCollectorNumber)){
//				simpleSpecimen.setCollectorsNumber(strCollectorNumber);
			}

			String title = CdmUtils.concat(" ", new String[]{strLocality, strCollector, strCollectorNumber});
			simpleSpecimen.setTitleCache(title);
		}
		return true;
	}

	private boolean doNameType(Element elType, TaxonNameBase taxonName, TaxonXImportConfigurator config){
		boolean success = true;
		//type
		String text = elType.getTextNormalize();
		logger.info("Type: " + text);
		if (text.endsWith(";")){
			text = text + " ";
		}
		String[] type = text.split(";");
		if (type.length != 3 ){
			if (text.equals("")){
				logger.info("<nomenclature><type> is empty: " + getBracketSourceName(config));
			}else{
				logger.warn("<nomenclature><type> is of unsupported format: " + elType.getTextNormalize() + getBracketSourceName(config));
			}
			success = false;
		}else{
			String statusStr = type[0].trim();
			String taxonNameStr = type[1].trim();
			String authorStr = type[2].trim();
			NameTypeDesignationStatus status = getNameTypeStatus(statusStr);
			/*boolean isLectoType = getIsLectoType(statusStr);*/

//			if (status == null){
//				logger.warn("<nomenclature><type> is of unsupported format: " + elType.getTextNormalize() + getBracketSourceName(config));
//				success = false;
//			}else{
//				TaxonNameBase childType = getChildrenNameType(taxonName, taxonNameStr, authorStr);
//				if (childType != null){
//					return doNameTypeDesignation(taxonName, childType, status);
//				}else{
					String[] epis = taxonNameStr.split(" ");
					String uninomial = epis[0].trim();
					String specEpi = epis[1].trim();

					Pager<TaxonNameBase> nameTypes = getNameService().searchNames(uninomial, null, specEpi, null, Rank.SPECIES(), null, null, null, null);

					List<NonViralName> result = new ArrayList<NonViralName>();
					for (TaxonNameBase nt : nameTypes.getRecords()){
						NonViralName nameType = CdmBase.deproxy(nt, NonViralName.class);
						if (compareAuthorship(nameType, authorStr)){
							result.add(nameType);
							success &= doNameTypeDesignation(taxonName, nameType, status/*, isLectoType*/);
						}else{
							//TODO ?
						}
					}
					if (result.size() > 1){
						logger.warn("More than 1 name matches: " + text);
						success = false;
					}else if (result.size() == 0){
						logger.warn("No name matches: " + text + "(" + config.getSourceNameString() + ")");
						success = false;
					}
//				}
//			}
		}
		return success;
	}


	private TaxonNameBase getChildrenNameType(TaxonNameBase name, String typeStr, String authorStr){
		TaxonNameBase result = null;
		Set<TaxonBase> list = name.getTaxonBases();
		for (TaxonBase taxonBase : list){
			Taxon taxon;
			if (taxonBase.isInstanceOf(Taxon.class)){
				taxon = CdmBase.deproxy(taxonBase, Taxon.class);
			}else{
				Synonym syn = CdmBase.deproxy(taxonBase, Synonym.class);
				taxon = syn.getAcceptedTaxa().iterator().next();
			}
			Set<Taxon> children = taxon.getTaxonomicChildren();
			for (Taxon child: children){
				NonViralName childName = (CdmBase.deproxy(child.getName(), NonViralName.class));
				if (childName.getNameCache().equals(typeStr)){
					if (compareAuthorship(childName, authorStr)){
						return childName;
					}
				}
			}
		}
		return result;
	}

	private boolean compareAuthorship(NonViralName typeName, String authorStr){
		 boolean result = false;
		 authorStr = authorStr.replaceAll("\\s+and\\s+", "&");
		 authorStr = authorStr.replaceAll("\\s*", "");
		 authorStr = authorStr.replaceAll("\\.$", "");
		 String typeCache = typeName.getAuthorshipCache().replaceAll("\\s*", "");
		 typeCache = typeCache.replaceAll("\\.$", "");
		 if (authorStr.equals(typeCache)){
			 return true;
		 }else{
			 logger.info("   Authors different: " + authorStr + " <-> " + typeCache);
		 }
		 return result;
	}

	private NameTypeDesignationStatus getNameTypeStatus(String statusString){
		//FIXME some types (not further defined types) do not exist yet
		if (true){
			return null;
		}
		if (statusString.trim().equalsIgnoreCase("Type")){
			return NameTypeDesignationStatus.ORIGINAL_DESIGNATION();
		}else if (statusString.trim().equalsIgnoreCase("Lectotype")){
			return NameTypeDesignationStatus.LECTOTYPE();
		}else if (statusString.trim().equalsIgnoreCase("Holotype")){
			logger.warn("Holotype does not yet exist in CDM");
			return NameTypeDesignationStatus.NOT_APPLICABLE();
		}else if (statusString.trim().equalsIgnoreCase("paratype")){
			logger.warn("paratype does not yet exist in CDM");
			return NameTypeDesignationStatus.NOT_APPLICABLE();
		}
		else{
			logger.warn("Status not recognized: " + statusString);
			return null;
		}
	}

	private boolean getIsLectoType(String statusString){
		//FIXME may be deleted once getNameTypeStatus works finde
		if (statusString.trim().equals("Lectotype")){
			return true;
		}else{
			return false;
		}
	}


	private boolean doNameTypeDesignation(TaxonNameBase name, TaxonNameBase type, NameTypeDesignationStatus status/*, boolean isLectoType*/){
		Reference citation = null;
		String citationMicroReference = null;
		String originalNameString = null;
		boolean addToAllHomotypicNames = true;

//		name.addNameTypeDesignation(type, citation, citationMicroReference, originalNameString, status, addToAllHomotypicNames);
		name.addNameTypeDesignation(type, citation, citationMicroReference, originalNameString,status, false, false, /*isLectoType, */false, addToAllHomotypicNames);
		return true;
	}

	/**
	 * Reads the typeLoc element split in parts for eacht type (holo, iso,...)
	 * @param elTypeLoc
	 * @param simpleSpecimen
	 * @param taxonName
	 * @param config
	 * @return
	 */
	private HashMap<DerivedUnit, SpecimenTypeDesignationStatus> doElTypeLoc(Element elTypeLoc,
			SimpleSpecimen simpleSpecimen,
			TaxonNameBase<?,?> taxonName,
			TaxonXImportConfigurator config){

		HashMap<DerivedUnit, SpecimenTypeDesignationStatus> result = new HashMap<DerivedUnit, SpecimenTypeDesignationStatus>();

		String typeLocFullString = elTypeLoc.getTextTrim();
		typeLocFullString = typeLocFullString.replace("(", "").replace(")", "");
		String[] typeLocStatusList = typeLocFullString.split(";");

		DerivedUnit originalSpecimen = simpleSpecimen.getSpecimen();


		for (String typeLocStatus : typeLocStatusList){
			typeLocStatus = typeLocStatus.trim();
			int pos = typeLocStatus.indexOf(" ");
			if (pos == -1){
				logger.warn("Unknown format or empty type_loc : '" +typeLocStatus + "'" + getBracketSourceName(config));
				result.put(originalSpecimen, null);
			}else{
				String statusString = typeLocStatus.substring(0,pos);
				SpecimenTypeDesignationStatus status = getStatusByStatusString(statusString.trim(), config);
				//TODO
				//String[] collectionStrings = typeLocStatus.substring(pos).split(",");
				String tmpCollString = typeLocStatus.substring(pos).trim();
				//for(String collectionString : collectionStrings){
					if (tmpCollString.contains("typ")){
						logger.warn("Is this really only a collection string? : "  + tmpCollString + getBracketSourceName(config));
					}
					DerivedUnit specimen;
					specimen = (DerivedUnit)originalSpecimen.clone();
					String title = originalSpecimen.getTitleCache();
					title = title + "(" + tmpCollString + ")";
					specimen.setTitleCache(title, true );
					result.put(specimen, status);
				//}
			}
		}

		return result;
	}

	/**
	 *
	 * Reads the collection_event tag, creates the according data and stores it.
	 * TODO under work
	 * @param elNomenclature
	 * @param nsTaxonx
	 * @param taxonBase
	 * @return
	 */
	private boolean doCollectionEvent(TaxonXImportConfigurator config, Element elNomenclature, Namespace nsTaxonx, TaxonBase taxonBase){
		boolean result = false;
		if (elNomenclature == null){
			return false;
		}
		Element elCollectionEvent = elNomenclature.getChild("collection_event", nsTaxonx);
		if (elCollectionEvent == null){
			return result;
		}
		Element elLocality = elCollectionEvent.getChild("locality", nsTaxonx);
		Element elType = elCollectionEvent.getChild("type", nsTaxonx);
		Element elTypeLoc = elCollectionEvent.getChild("type_loc", nsTaxonx);

		//locality
		SimpleSpecimen simpleSpecimen = SimpleSpecimen.NewInstance();
		String locality = elLocality.getTextNormalize();
		if (! "".equals(locality)){
			simpleSpecimen.setLocality(locality);
		}

		//type
		String[] type = elType.getTextNormalize().split(" ");
		if (type.length != 2 ){
			logger.warn("<collecion_even><type> is of unsupported format: " + elType.getTextNormalize());
		}else{
			AgentBase collector = Person.NewTitledInstance(type[0]);
			simpleSpecimen.setCollector(collector);

			String collectorNumber = type[1];
			simpleSpecimen.setCollectorsNumber(collectorNumber);
		}

		//typeLoc
		String typeLocFullString = elTypeLoc.getTextTrim();
		typeLocFullString = typeLocFullString.replace("(", "").replace(")", "");
		String[] typeLocStatusList = typeLocFullString.split(";");

		DerivedUnit originalSpecimen = simpleSpecimen.getSpecimen();

		//TODO special character ?, �, !

		for (String typeLocStatus : typeLocStatusList){
			typeLocStatus = typeLocStatus.trim();
			int pos = typeLocStatus.indexOf(" ");
			if (pos == -1){
				logger.warn("Unknown format: " + typeLocStatus);
			}else{
				String statusString = typeLocStatus.substring(0,pos);
				SpecimenTypeDesignationStatus status = getStatusByStatusString(statusString.trim(), config);
				String[] collectionStrings = typeLocStatus.substring(pos).split(",");
				for(String collectionString : collectionStrings){
					if (taxonBase != null){
						TaxonNameBase<?, ?> taxonName = taxonBase.getName();
						if (taxonName != null){
							Reference citation = null;
							String citationMicroReference = null;
							String originalNameString = null;
							boolean isNotDesignated = true;
							boolean addToAllHomotypicNames = true;
							DerivedUnit specimen = (DerivedUnit)originalSpecimen.clone();
							unlazyTypeDesignation(config, taxonName);
							taxonName.addSpecimenTypeDesignation(specimen, status, citation, citationMicroReference, originalNameString, isNotDesignated, addToAllHomotypicNames);
							result = true;
						}
					}
				}
			}
		}
		return result;
	}


	private static Map<String, SpecimenTypeDesignationStatus> statusMap;
	private static void fillTypeStatusMap(){
		statusMap = new HashMap<String, SpecimenTypeDesignationStatus>();
		statusMap.put("epitype", SpecimenTypeDesignationStatus.EPITYPE());
		statusMap.put("holotype", SpecimenTypeDesignationStatus.HOLOTYPE());
		statusMap.put("iconotype", SpecimenTypeDesignationStatus.ICONOTYPE());
		statusMap.put("isotype", SpecimenTypeDesignationStatus.ISOTYPE());
		statusMap.put("isoneotype", SpecimenTypeDesignationStatus.ISONEOTYPE());
		statusMap.put("isosyntype", SpecimenTypeDesignationStatus.ISOSYNTYPE());
		statusMap.put("isolectotype", SpecimenTypeDesignationStatus.ISOLECTOTYPE());
		statusMap.put("lectotype", SpecimenTypeDesignationStatus.LECTOTYPE());
		statusMap.put("syntype", SpecimenTypeDesignationStatus.SYNTYPE());
		statusMap.put("paratype", SpecimenTypeDesignationStatus.PARATYPE());
		statusMap.put("neotype", SpecimenTypeDesignationStatus.NEOTYPE());
		statusMap.put("isoepitype", SpecimenTypeDesignationStatus.ISOEPITYPE());
		statusMap.put("originalmaterial", SpecimenTypeDesignationStatus.ORIGINAL_MATERIAL());
		statusMap.put("paralectotype", SpecimenTypeDesignationStatus.PARALECTOTYPE());
		statusMap.put("paraneotype", SpecimenTypeDesignationStatus.PARANEOTYPE());
		statusMap.put("phototype", SpecimenTypeDesignationStatus.PHOTOTYPE());
		statusMap.put("secondsteplectotype", SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE());
		statusMap.put("secondstepneotype", SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE());
		statusMap.put("type", null);
	}


	//TODO move to TypeDesignation class
	/**
	 * Returns the typeDesignationStatus according to a type designation status string
	 * @param statusString
	 * @return TypeDesignationStatus
	 */
	private static SpecimenTypeDesignationStatus getStatusByStatusString(String statusString, TaxonXImportConfigurator config){
		SpecimenTypeDesignationStatus result = null;
		if (statusString == null || "".equals(statusString.trim())){
			return null;
		}
		statusString = statusString.trim().toLowerCase();
		statusString = statusString.replace("typi", "typus");
		statusString = statusString.replace("typus", "type");
		statusString = statusString.replace("types", "type");
		statusString = statusString.toLowerCase();

		if (statusMap == null){
			fillTypeStatusMap();
		}
		result = statusMap.get(statusString);
		if (statusString.equals("type")){
			logger.info("No type designation type" + getBracketSourceName(config));
		}else if (result == null){
			logger.warn("Unknown type status string: " + statusString + getBracketSourceName(config));
		}
		return result;
	}


	/**
	 * TODO Preliminary to avoid laizy loading errors
	 */
	private void unlazyTypeDesignation(TaxonXImportConfigurator config, TaxonNameBase taxonNameBase){
		TransactionStatus txStatus = startTransaction();
		//INameService taxonNameService = config.getCdmAppController().getNameService();
		INameService taxonNameService = getNameService();

		taxonNameService.save(taxonNameBase);
		Set<TaxonNameBase> typifiedNames = taxonNameBase.getHomotypicalGroup().getTypifiedNames();
		for(TaxonNameBase typifiedName: typifiedNames){
			typifiedName.getTypeDesignations().size();
		}
		//taxonNameService.saveTaxonName(taxonNameBase);
		commitTransaction(txStatus);
	}

	/**
	 * TODO Preliminary to avoid laizy loading errors
	 */
	private void unlazySynonym(IImportConfigurator config, Taxon taxon){
		TransactionStatus txStatus = startTransaction();
		ITaxonService taxonService = getTaxonService();
		taxonService.save(taxon);
		Set<Synonym> synonyms = taxon.getSynonyms();
		logger.debug(synonyms.size());
		//taxonService.saveTaxon(taxon);
		commitTransaction(txStatus);
	}

	private static String getBracketSourceName(TaxonXImportConfigurator config){
		return "(" + config.getSourceNameString() + ")";
	}


}
