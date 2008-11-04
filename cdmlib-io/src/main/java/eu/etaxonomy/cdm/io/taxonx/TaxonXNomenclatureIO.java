/**
 * 
 */
package eu.etaxonomy.cdm.io.taxonx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


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
		Element nomenclature = elTreatment.getChild("nomenclature", nsTaxonx);
		
		isChanged |= doCollectionEvent(txConfig, nomenclature, nsTaxonx, taxon);
		
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
	 * Reads the collection_event tag, creates the according data and stores it.
	 * TODO under work
	 * @param elNomenclature
	 * @param nsTaxonx
	 * @param taxonBase
	 * @return
	 */
	private boolean doCollectionEvent(TaxonXImportConfigurator config, Element elNomenclature, Namespace nsTaxonx, TaxonBase taxonBase){
		boolean result = false;
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
