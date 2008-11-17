/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.jaxb;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.babadshanjan
 * @created 13.11.2008
 */
public class CdmImporter extends CdmIoBase implements ICdmIO {

	private static final Logger logger = Logger.getLogger(CdmImporter.class);
	private CdmDocumentBuilder cdmDocumentBuilder = null;

	
    /** Reads data from an XML file and stores them into a CDM DB.
     * 
     * @param exImpConfig
     * @param stores (not used)
     */
	@Override
	protected boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores) {
		
		JaxbImportConfigurator jaxbImpConfig = (JaxbImportConfigurator)config;
    	String dbname = jaxbImpConfig.getDestination().getDatabase();
    	String fileName = jaxbImpConfig.getSourceNameString();
		logger.info("Deserializing file " + fileName + " to DB " + dbname);

		DataSet dataSet = new DataSet();
		
        // unmarshalling XML file
		
		try {
			cdmDocumentBuilder = new CdmDocumentBuilder();
			logger.info("Unmarshalling file: " + fileName);
			dataSet = cdmDocumentBuilder.unmarshal(dataSet, new File(fileName));

		} catch (Exception e) {
			logger.error("Unmarshalling error");
			e.printStackTrace();
		} 
		
		// save data in DB
		logger.info("Saving data to DB: " + dbname);
		saveData(jaxbImpConfig, dataSet);
		
		return true;
	}

	
	/**  Saves data in DB */
    private void saveData (JaxbImportConfigurator jaxbImpConfig, DataSet dataSet) {

		Collection<TaxonBase> taxonBases;
		List<Agent> agents;
		List<DefinedTermBase> terms;
		List<ReferenceBase> references;
		List<TaxonNameBase> taxonomicNames;
		List<DescriptionBase> descriptions;
		List<ReferencedEntityBase> referencedEntities;
		List<SpecimenOrObservationBase> occurrences;
		List<VersionableEntity> featureData;
		List<VersionableEntity> media;
		List<LanguageStringBase> languageData;
		List<TermVocabulary<DefinedTermBase>> termVocabularies;
		List<HomotypicalGroup> homotypicalGroups;

        // Get an app controller that omits term loading
		// CdmApplicationController.getCdmAppController(boolean createNew, boolean omitTermLoading){
		CdmApplicationController appCtr = jaxbImpConfig.getCdmAppController(false, true);
		TransactionStatus txStatus = appCtr.startTransaction();
		
		// If data of a certain type, such as terms, are not saved here explicitly, 
		// then only those data of this type that are referenced by other objects are saved implicitly.
		// For example, if taxa are saved all other data referenced by those taxa, such as synonyms, 
		// are automatically saved as well.
		
		if (jaxbImpConfig.isDoTerms() == true) {
			if ((terms = dataSet.getTerms()).size() > 0) {
				logger.info("Terms: " + terms.size());
				appCtr.getTermService().saveTermsAll(terms);
			}
		}

		if (jaxbImpConfig.isDoTermVocabularies() == true) {
			if ((termVocabularies = dataSet.getTermVocabularies()).size() > 0) {
				logger.info("Language data: " + termVocabularies.size());
				appCtr.getTermService().saveTermVocabulariesAll(termVocabularies);
			}
		}

		if (jaxbImpConfig.isDoAuthors() == true) {
			if ((agents = dataSet.getAgents()).size() > 0) {
				logger.info("Agents: " + agents.size());
				appCtr.getAgentService().saveAgentAll(agents);
			}
		}
		
		if (jaxbImpConfig.getDoReferences() != IImportConfigurator.DO_REFERENCES.NONE) {
			if ((references = dataSet.getReferences()).size() > 0) {
				logger.info("References: " + references.size());
				appCtr.getReferenceService().saveReferenceAll(references);
			}
		}

		if (jaxbImpConfig.isDoTaxonNames() == true) {
			if ((taxonomicNames = dataSet.getTaxonomicNames()).size() > 0) {
				logger.info("Taxonomic names: " + taxonomicNames.size());
				appCtr.getNameService().saveTaxonNameAll(taxonomicNames);
			}
		}

		if (jaxbImpConfig.isDoHomotypicalGroups() == true) {
			if ((homotypicalGroups = dataSet.getHomotypicalGroups()).size() > 0) {
				logger.info("Homotypical groups: " + homotypicalGroups.size());
				appCtr.getNameService().saveAllHomotypicalGroups(homotypicalGroups);
			}
		}
		
		// Need to get the taxa and the synonyms here.
		if (jaxbImpConfig.isDoTaxa() == true) {
			if ((taxonBases = dataSet.getTaxonBases()).size() > 0) {
				logger.info("Taxon bases: " + taxonBases.size());
				appCtr.getTaxonService().saveTaxonAll(taxonBases);
			}
		}

	    // NomenclaturalStatus, TypeDesignations
		if (jaxbImpConfig.isDoReferencedEntities() == true) {
			if ((referencedEntities = dataSet.getReferencedEntities()).size() > 0) {
				logger.info("Referenced entities: " + referencedEntities.size());
				appCtr.getNameService().saveReferencedEntitiesAll(referencedEntities);
			}
		}

		// TODO: Implement dataSet.getDescriptions() and IDescriptionService.saveDescriptionAll()
//		if ((descriptions = dataSet.getDescriptions()) != null) {
//		logger.info("Saving " + descriptions.size() + " descriptions");
//		appCtr.getDescriptionService().saveDescriptionAll(descriptions);
//		}

		if (jaxbImpConfig.isDoOccurrence() == true) {
			if ((occurrences = dataSet.getOccurrences()).size() > 0) {
				logger.info("Occurrences: " + occurrences.size());
				appCtr.getOccurrenceService().saveSpecimenOrObservationBaseAll(occurrences);
			}
		}

		if (jaxbImpConfig.isDoFeatureData() == true) {
			if ((featureData = dataSet.getFeatureData()).size() > 0) {
				logger.info("Feature data: " + featureData.size());
				appCtr.getDescriptionService().saveFeatureDataAll(featureData);
			}
		}

		if (jaxbImpConfig.isDoMedia() == true) {
			if ((media = dataSet.getMedia()).size() > 0) {
				logger.info("Media: " + media.size());
				appCtr.getMediaService().saveMediaAll(media);
			}
		}

		if (jaxbImpConfig.isDoLanguageData() == true) {
			if ((languageData = dataSet.getLanguageData()).size() > 0) {
				logger.info("Language data: " + languageData.size());
				appCtr.getTermService().saveLanguageDataAll(languageData);
			}
		}

		logger.info("All data saved");

		appCtr.commitTransaction(txStatus);
		appCtr.close();

    }

	
	@Override
	protected boolean doCheck(IImportConfigurator config) {
		boolean result = true;
		logger.warn("No check implemented for Jaxb import");
		return result;
	}
	

	@Override
	protected boolean isIgnore(IImportConfigurator config) {
		return false;
	}
}
