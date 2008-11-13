/**
 * 
 */
package eu.etaxonomy.cdm.io.taxonx;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 *
 */
public class TaxonXNomenclatureIO extends CdmIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TaxonXNomenclatureIO.class);

	private static int modCount = 10000;

	public TaxonXNomenclatureIO(){
		super();
	}
	
	public boolean doCheck(IImportConfigurator config){
		
		boolean result = true;
		logger.warn("Checking for Types not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	public boolean doInvoke(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores){
		logger.info("start make Nomenclature ...");
		
		TaxonXImportConfigurator txConfig = (TaxonXImportConfigurator)config;
		Element root = txConfig.getSourceRoot();
		Namespace nsTaxonx = root.getNamespace();
				
		ITaxonService taxonService = config.getCdmAppController().getTaxonService();
		
		//for testing only
		Taxon taxon = getTaxon(txConfig);
		boolean isChanged = false;
		
		Element elTaxonBody = root.getChild("taxonxBody", nsTaxonx);
		Element elTreatment = elTaxonBody.getChild("treatment", nsTaxonx);
		Element elNomenclature = elTreatment.getChild("nomenclature", nsTaxonx);
		
		isChanged |= doCollectionEvent(txConfig, elNomenclature, nsTaxonx, taxon);
		
		if (taxon != null && taxon.getName() != null){
			isChanged |= doNomenclaturalType(txConfig, elNomenclature, nsTaxonx, taxon.getName());
		}
		if (isChanged){
			taxonService.saveTaxon(taxon);
		}
		return true;
	}
	
	private Taxon getTaxon(TaxonXImportConfigurator config){
		Taxon result;
//		result =  Taxon.NewInstance(BotanicalName.NewInstance(null), null);
		ICommonService commonService =config.getCdmAppController().getCommonService();
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
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoFacts();
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
			ReferenceBase citation = null;
			String citationMicroReference = null;
			String originalNameString = null;
			boolean isNotDesignated = true;
			boolean addToAllHomotypicNames = true;
			unlazyTypeDesignation(config, taxonName);
	
			
			SimpleSpecimen simpleSpecimen = SimpleSpecimen.NewInstance();
			if (elType != null){
				doElType(elType, simpleSpecimen);
			}//elType
			
			//typeLoc
			HashMap<Specimen, TypeDesignationStatus> typeLocMap = null; 
			if (elTypeLoc != null){
				typeLocMap = doElTypeLoc(elTypeLoc, simpleSpecimen, taxonName, config);
			}
			if (typeLocMap != null && typeLocMap.size() >0){
				for (Specimen specimen : typeLocMap.keySet()){
					TypeDesignationStatus status = typeLocMap.get(specimen);
					taxonName.addSpecimenTypeDesignation(specimen, status, citation, citationMicroReference, originalNameString, isNotDesignated, addToAllHomotypicNames);
				}
			}else{ // no type_loc
				TypeDesignationStatus status = null;
				taxonName.addSpecimenTypeDesignation(simpleSpecimen.getSpecimen(), status, citation, citationMicroReference, originalNameString, isNotDesignated, addToAllHomotypicNames);
			}
			return true;
		}
		return false;
	}

	private boolean doElType(Element elType, SimpleSpecimen simpleSpecimen){
		//type
		String[] type = elType.getTextNormalize().split(";");
		if (type.length != 3 ){
			logger.warn("<nomenclature><type> is of unsupported format: " + elType.getTextNormalize());
			simpleSpecimen.setTitleCache(elType.getTextNormalize());
		}else{
			String strLocality = type[0].trim();
			if (! "".equals(strLocality)){
				simpleSpecimen.setLocality(strLocality);
			}
			
			String strCollector = type[1].trim();
			if (! "".equals(strCollector)){
				Agent collector = Person.NewTitledInstance(strCollector);
				simpleSpecimen.setCollector(collector);
			}
			
			String strCollectorNumber = type[2].trim();
			if (! "".equals(strCollectorNumber)){
				simpleSpecimen.setCollectorsNumber(strCollectorNumber);
			}
			
			String title = CdmUtils.concat(" ", new String[]{strLocality, strCollector, strCollectorNumber});
			simpleSpecimen.setTitleCache(title);
		}
		return true;
	}
	
	private HashMap<Specimen, TypeDesignationStatus> doElTypeLoc(Element elTypeLoc, 
			SimpleSpecimen simpleSpecimen, 
			TaxonNameBase taxonName,
			TaxonXImportConfigurator config){
		
		HashMap<Specimen, TypeDesignationStatus> result = new HashMap<Specimen, TypeDesignationStatus>();

		String typeLocFullString = elTypeLoc.getTextTrim();
		typeLocFullString = typeLocFullString.replace("(", "").replace(")", "");
		String[] typeLocStatusList = typeLocFullString.split(";");
		
		Specimen originalSpecimen = simpleSpecimen.getSpecimen();
		
		
		for (String typeLocStatus : typeLocStatusList){
			typeLocStatus = typeLocStatus.trim();
			int pos = typeLocStatus.indexOf(" ");
			if (pos == -1){
				logger.warn("Unknown format: " + typeLocStatus);
			}else{
				String statusString = typeLocStatus.substring(0,pos); 
				TypeDesignationStatus status = getStatusByStatusString(statusString.trim());
				String[] collectionStrings = typeLocStatus.substring(pos).split(",");
				for(String collectionString : collectionStrings){
					Specimen specimen;
					specimen = (Specimen)originalSpecimen.clone();
					result.put(specimen, status);
				}
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
			Agent collector = Person.NewTitledInstance(type[0]);
			simpleSpecimen.setCollector(collector);
			
			String collectorNumber = type[1];
			simpleSpecimen.setCollectorsNumber(collectorNumber);
		}
		
		//typeLoc
		String typeLocFullString = elTypeLoc.getTextTrim();
		typeLocFullString = typeLocFullString.replace("(", "").replace(")", "");
		String[] typeLocStatusList = typeLocFullString.split(";");
		
		Specimen originalSpecimen = simpleSpecimen.getSpecimen();
		
		for (String typeLocStatus : typeLocStatusList){
			typeLocStatus = typeLocStatus.trim();
			int pos = typeLocStatus.indexOf(" ");
			if (pos == -1){
				logger.warn("Unknown format: " + typeLocStatus);
			}else{
				String statusString = typeLocStatus.substring(0,pos); 
				TypeDesignationStatus status = getStatusByStatusString(statusString.trim());
				String[] collectionStrings = typeLocStatus.substring(pos).split(",");
				for(String collectionString : collectionStrings){
					if (taxonBase != null){
						TaxonNameBase<?, ?> taxonName = taxonBase.getName();
						if (taxonName != null){
							ReferenceBase citation = null;
							String citationMicroReference = null;
							String originalNameString = null;
							boolean isNotDesignated = true;
							boolean addToAllHomotypicNames = true;
							Specimen specimen = (Specimen)originalSpecimen.clone();
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
	
	
	//TODO move to TypeDesignation class
	/**
	 * Returns the typeDesignationStatus according to a type designation status string
	 * @param statusString
	 * @return TypeDesignationStatus
	 */
	private static TypeDesignationStatus getStatusByStatusString(String statusString){
		if (statusString == null || "".equals(statusString.trim())){
			return null;
		}
		statusString = statusString.trim();
		statusString = statusString.replace("typi", "typus");
		statusString = statusString.replace("typus", "type");
		statusString = statusString.replace("types", "type");
		TypeDesignationStatus result = null;
		Map<String, TypeDesignationStatus> statusMap = new HashMap<String, TypeDesignationStatus>();
		statusMap.put("holotype", TypeDesignationStatus.HOLOTYPE());
		statusMap.put("isotype", TypeDesignationStatus.ISOTYPE());
		statusMap.put("lectotype", TypeDesignationStatus.LECTOTYPE());
		//TODO to be continued
		
		statusString = statusString.toLowerCase();
		result = statusMap.get(statusString);
		if (result == null){
			logger.warn("Unknown type status string: " + statusString);
		}
		return result;
	}
	
	
	/**
	 * TODO Preliminary to avoid laizy loading errors
	 */
	private void unlazyTypeDesignation(TaxonXImportConfigurator config, TaxonNameBase taxonNameBase){
		TransactionStatus txStatus = config.getCdmAppController().startTransaction();
		INameService taxonNameService = config.getCdmAppController().getNameService();
		taxonNameService.saveTaxonName(taxonNameBase);
		Set<TaxonNameBase> typifiedNames = taxonNameBase.getHomotypicalGroup().getTypifiedNames();
		for(TaxonNameBase typifiedName: typifiedNames){
			typifiedName.getTypeDesignations().size();	
		}
		taxonNameService.saveTaxonName(taxonNameBase);
		config.getCdmAppController().commitTransaction(txStatus);
	}

	
}
