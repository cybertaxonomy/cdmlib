/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.jaxb;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
public class CdmImporter extends CdmIoBase<IImportConfigurator> implements ICdmIO<IImportConfigurator> {

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
		
		boolean success = true;
        URI uri = null;
		JaxbImportConfigurator jaxbImpConfig = (JaxbImportConfigurator)config;
    	String dbname = jaxbImpConfig.getDestination().getDatabase();
    	
    	String urlFileName = (String)config.getSource();
		logger.debug("urlFileName: " + urlFileName);
    	try {
    		uri = new URI(urlFileName);
			logger.debug("uri: " + uri.toString());
    	} catch (URISyntaxException ex) {
			logger.error("File not found");
			return false;
    	}

		logger.info("Deserializing file " + urlFileName + " to DB " + dbname);

		DataSet dataSet = new DataSet();
		
        // unmarshalling XML file
		
		try {
			cdmDocumentBuilder = new CdmDocumentBuilder();
//			logger.info("Unmarshalling file: " + urlFileName);
			File file = new File(uri);
			logger.debug("Absolute path: " + file.getAbsolutePath());
			dataSet = cdmDocumentBuilder.unmarshal(dataSet, file);

		} catch (Exception e) {
			logger.error("Unmarshalling error");
			e.printStackTrace();
		} 
		
		// save data in DB
		logger.info("Saving data to DB: " + dbname);
		success = saveData(jaxbImpConfig, dataSet);
		
		return success;
	}

	
	/**  Saves data in DB */
	private boolean saveData (JaxbImportConfigurator jaxbImpConfig, DataSet dataSet) {

		boolean ret = true;
		Collection<TaxonBase> taxonBases;
		List<Agent> agents;
		List<DefinedTermBase> terms;
		List<ReferenceBase> references;
		List<TaxonNameBase> taxonomicNames;
		List<DescriptionBase> descriptions;
		List<ReferencedEntityBase> referencedEntities;
		List<SpecimenOrObservationBase> occurrences;
		List<VersionableEntity<?>> featureData;
		List<VersionableEntity> media;
		List<LanguageStringBase> languageData;
		List<TermVocabulary<DefinedTermBase>> termVocabularies;
		List<HomotypicalGroup> homotypicalGroups;

		// Get an app controller that omits term loading
		// CdmApplicationController.getCdmAppController(boolean createNew, boolean omitTermLoading){
		CdmApplicationController appCtr = jaxbImpConfig.getCdmAppController(false, true);
		//TransactionStatus txStatus = appCtr.startTransaction();
		TransactionStatus txStatus = null;

		// Have single transactions per service save call. Otherwise, getting
		// H2 HYT00 error (timeout locking table DEFINEDTERMBASE) when running from editor.

		// If data of a certain type, such as terms, are not saved here explicitly, 
		// then only those data of this type that are referenced by other objects are saved implicitly.
		// For example, if taxa are saved all other data referenced by those taxa, such as synonyms, 
		// are automatically saved as well.

		if ((jaxbImpConfig.isDoTermVocabularies() == true) 
				&& (termVocabularies = dataSet.getTermVocabularies()).size() > 0) {
			txStatus = appCtr.startTransaction();
			ret &= saveTermVocabularies(termVocabularies, appCtr);
			appCtr.commitTransaction(txStatus);
		}
		
		if ((jaxbImpConfig.isDoTerms() == true)
				&& (terms = dataSet.getTerms()).size() > 0) {
			txStatus = appCtr.startTransaction();
			ret &= saveTerms(terms, appCtr);
			appCtr.commitTransaction(txStatus);
		}
		
		// TODO: Have separate data save methods

		txStatus = appCtr.startTransaction();
		try {
			if (jaxbImpConfig.isDoLanguageData() == true) {
				if ((languageData = dataSet.getLanguageData()).size() > 0) {
					logger.info("Language data: " + languageData.size());
					appCtr.getTermService().saveLanguageDataAll(languageData);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving language data");
			ret = false;
		}
		appCtr.commitTransaction(txStatus);

		
		txStatus = appCtr.startTransaction();
		try {
			if (jaxbImpConfig.isDoAuthors() == true) {
				if ((agents = dataSet.getAgents()).size() > 0) {
					logger.info("Agents: " + agents.size());
					appCtr.getAgentService().saveAgentAll(agents);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving agents");
			ret = false;
		}
		appCtr.commitTransaction(txStatus);


		txStatus = appCtr.startTransaction();
		try {
			if (jaxbImpConfig.getDoReferences() != IImportConfigurator.DO_REFERENCES.NONE) {
				if ((references = dataSet.getReferences()).size() > 0) {
					logger.info("References: " + references.size());
					appCtr.getReferenceService().saveReferenceAll(references);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving references");
			ret = false;
		}
		appCtr.commitTransaction(txStatus);


		txStatus = appCtr.startTransaction();
		try {
			if (jaxbImpConfig.isDoTaxonNames() == true) {
				if ((taxonomicNames = dataSet.getTaxonomicNames()).size() > 0) {
					logger.info("Taxonomic names: " + taxonomicNames.size());
					appCtr.getNameService().saveTaxonNameAll(taxonomicNames);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving taxon names");
			ret = false;
		}
		appCtr.commitTransaction(txStatus);


		txStatus = appCtr.startTransaction();
		try {
			if (jaxbImpConfig.isDoHomotypicalGroups() == true) {
				if ((homotypicalGroups = dataSet.getHomotypicalGroups()).size() > 0) {
					logger.info("Homotypical groups: " + homotypicalGroups.size());
					appCtr.getNameService().saveAllHomotypicalGroups(homotypicalGroups);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving homotypical groups");
			ret = false;
		}
		appCtr.commitTransaction(txStatus);


		txStatus = appCtr.startTransaction();
		// Need to get the taxa and the synonyms here.
		try {
			if (jaxbImpConfig.isDoTaxa() == true) {
				if ((taxonBases = dataSet.getTaxonBases()).size() > 0) {
					logger.info("Taxon bases: " + taxonBases.size());
					appCtr.getTaxonService().saveTaxonAll(taxonBases);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving taxa");
			ret = false;
		}
		appCtr.commitTransaction(txStatus);


		txStatus = appCtr.startTransaction();
		// NomenclaturalStatus, TypeDesignations
		try {
			if (jaxbImpConfig.isDoReferencedEntities() == true) {
				if ((referencedEntities = dataSet.getReferencedEntities()).size() > 0) {
					logger.info("Referenced entities: " + referencedEntities.size());
					appCtr.getNameService().saveReferencedEntitiesAll(referencedEntities);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving referenced entities");
			ret = false;
		}
		appCtr.commitTransaction(txStatus);


		// TODO: Implement dataSet.getDescriptions() and IDescriptionService.saveDescriptionAll()
//		if ((descriptions = dataSet.getDescriptions()) != null) {
//		logger.info("Saving " + descriptions.size() + " descriptions");
//		appCtr.getDescriptionService().saveDescriptionAll(descriptions);
//		}

		txStatus = appCtr.startTransaction();
		try {
			if (jaxbImpConfig.isDoOccurrence() == true) {
				if ((occurrences = dataSet.getOccurrences()).size() > 0) {
					logger.info("Occurrences: " + occurrences.size());
					appCtr.getOccurrenceService().saveSpecimenOrObservationBaseAll(occurrences);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving occurrences");
			ret = false;
		}
		appCtr.commitTransaction(txStatus);


		txStatus = appCtr.startTransaction();
		try {
			if (jaxbImpConfig.isDoFeatureData() == true) {
				if ((featureData = dataSet.getFeatureData()).size() > 0) {
					logger.info("Feature data: " + featureData.size());
					appCtr.getDescriptionService().saveFeatureDataAll(featureData);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving feature data");
			ret = false;
		}
		appCtr.commitTransaction(txStatus);


		txStatus = appCtr.startTransaction();
		try {
			if (jaxbImpConfig.isDoMedia() == true) {
				if ((media = dataSet.getMedia()).size() > 0) {
					logger.info("Media: " + media.size());
					appCtr.getMediaService().saveMediaAll(media);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving media");
			ret = false;
		}
		appCtr.commitTransaction(txStatus);

//		appCtr.commitTransaction(txStatus);
		logger.info("All data saved");

		appCtr.close();

		return ret;

	}
	
	
	private boolean saveTermVocabularies(
			List<TermVocabulary<DefinedTermBase>> termVocabularies, CdmApplicationController appCtr) {

		boolean success = true;
		logger.info("Term vocabularies: " + termVocabularies.size());
		try {
			appCtr.getTermService().saveTermVocabulariesAll(termVocabularies);
		} catch (Exception ex) {
			logger.error("Error saving term vocabularies");
			success = false;
		}
		return success;
	}

	private boolean saveTerms(List<DefinedTermBase> terms, CdmApplicationController appCtr) {

		boolean success = true;
		logger.info("Terms: " + terms.size());
		try {
			appCtr.getTermService().saveTermsAll(terms);
		} catch (Exception ex) {
			logger.error("Error saving terms");
			success = false;
		}
		return success;
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
