/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.jaxb;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.babadshanjan
 * @created 13.11.2008
 * @version 1.0
 */
@Component
public class JaxbImport extends CdmIoBase<IImportConfigurator> implements ICdmIO<IImportConfigurator> {

	private static final Logger logger = Logger.getLogger(JaxbImport.class);
	private CdmDocumentBuilder cdmDocumentBuilder = null;
	

	/** Reads data from an XML file and stores them into a CDM DB.
     * 
     * @param config
     * @param stores (not used)
     */
	@Override
	protected boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores) {
		
		boolean success = true;
        URI uri = null;
		JaxbImportConfigurator jaxbImpConfig = (JaxbImportConfigurator)config;
    	
    	String urlFileName = (String)config.getSource();
		logger.debug("urlFileName: " + urlFileName);
    	try {
    		uri = new URI(urlFileName);
			logger.debug("uri: " + uri.toString());
    	} catch (URISyntaxException ex) {
			logger.error("File not found");
			return false;
    	}

		logger.info("Deserializing file " + urlFileName + " to DB " ); //+ dbname

		DataSet dataSet = new DataSet();
		
        // unmarshalling XML file
		try {
			cdmDocumentBuilder = new CdmDocumentBuilder();
			logger.info("Unmarshalling file: " + urlFileName);
			File file = new File(uri);
			logger.debug("Absolute path: " + file.getAbsolutePath());
			dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, file);

		} catch (Exception e) {
			logger.error("Unmarshalling error");
			e.printStackTrace();
		} 
		
		// save data in DB
		logger.info("Saving data to DB: " ); //+ dbname
		success = saveData(jaxbImpConfig, dataSet);
		
		return success;
	}

	
	/**  Saves data in DB */
	private boolean saveData (JaxbImportConfigurator jaxbImpConfig, DataSet dataSet) {

		boolean ret = true;
		Collection<TaxonBase> taxonBases;
		List<? extends AgentBase> agents;
		List<DefinedTermBase> terms;
		List<ReferenceBase> references;
		List<TaxonNameBase> taxonomicNames;
		List<DescriptionBase> descriptions;
		List<TypeDesignationBase> typeDesignations;
		List<SpecimenOrObservationBase> occurrences;
		List<FeatureTree> featureTrees;
		List<Media> media;
		List<LanguageStringBase> languageData;
		List<TermVocabulary<DefinedTermBase>> termVocabularies;
		List<HomotypicalGroup> homotypicalGroups;

		// Get an app controller that omits term loading
		// CdmApplicationController.getCdmAppController(boolean createNew, boolean omitTermLoading){
//		CdmApplicationController appCtr = jaxbImpConfig.getCdmAppController(false, true);
		//TransactionStatus txStatus = startTransaction();
		//TransactionStatus txStatus = null;

		// Have single transactions per service save call. Otherwise, getting
		// H2 HYT00 error (timeout locking table DEFINEDTERMBASE) when running from editor.

		// If data of a certain type, such as terms, are not saved here explicitly, 
		// then only those data of this type that are referenced by other objects are saved implicitly.
		// For example, if taxa are saved all other data referenced by those taxa, such as synonyms, 
		// are automatically saved as well.

		if ((jaxbImpConfig.isDoTermVocabularies() == true) 
				&& (termVocabularies = dataSet.getTermVocabularies()).size() > 0) {
			//txStatus = startTransaction();
			ret &= saveTermVocabularies(termVocabularies);
			//commitTransaction(txStatus);
		}
		
		if ((jaxbImpConfig.isDoTerms() == true)
				&& (terms = dataSet.getTerms()).size() > 0) {
			//txStatus = startTransaction();
			ret &= saveTerms(terms);
			//commitTransaction(txStatus);
		}
		
		// TODO: Have separate data save methods

//		txStatus = startTransaction();
//		try {
//			if (jaxbImpConfig.isDoLanguageData() == true) {
//				if ((languageData = dataSet.getLanguageData()).size() > 0) {
//					logger.info("Language data: " + languageData.size());
//					getTermService().saveLanguageDataAll(languageData);
//				}
//			}
//		} catch (Exception ex) {
//			logger.error("Error saving language data");
//			ret = false;
//		}
//		commitTransaction(txStatus);

		
		//txStatus = startTransaction();
		try {
			if (jaxbImpConfig.isDoAuthors() == true) {
				if ((agents = dataSet.getAgents()).size() > 0) {
					logger.info("Agents: " + agents.size());
					getAgentService().saveAgentAll(agents);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving agents");
			ret = false;
		}
		//commitTransaction(txStatus);


		//txStatus = startTransaction();
		try {
			if (jaxbImpConfig.getDoReferences() != IImportConfigurator.DO_REFERENCES.NONE) {
				if ((references = dataSet.getReferences()).size() > 0) {
					logger.info("References: " + references.size());
					getReferenceService().saveReferenceAll(references);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving references");
			ret = false;
		}
		//commitTransaction(txStatus);


		//txStatus = startTransaction();
		try {
			if (jaxbImpConfig.isDoTaxonNames() == true) {
				if ((taxonomicNames = dataSet.getTaxonomicNames()).size() > 0) {
					logger.info("Taxonomic names: " + taxonomicNames.size());
					getNameService().saveTaxonNameAll(taxonomicNames);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving taxon names");
			ret = false;
		}
		//commitTransaction(txStatus);


		//txStatus = startTransaction();
		try {
			if (jaxbImpConfig.isDoHomotypicalGroups() == true) {
				if ((homotypicalGroups = dataSet.getHomotypicalGroups()).size() > 0) {
					logger.info("Homotypical groups: " + homotypicalGroups.size());
					getNameService().saveAllHomotypicalGroups(homotypicalGroups);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving homotypical groups");
			ret = false;
		}
		//commitTransaction(txStatus);


		//txStatus = startTransaction();
		// Need to get the taxa and the synonyms here.
		try {
			if (jaxbImpConfig.isDoTaxa() == true) {
				if ((taxonBases = dataSet.getTaxonBases()).size() > 0) {
					logger.info("Taxon bases: " + taxonBases.size());
					getTaxonService().saveTaxonAll(taxonBases);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving taxa");
			ret = false;
		}
		//commitTransaction(txStatus);


		//txStatus = startTransaction();
		// NomenclaturalStatus, TypeDesignations
		try {
			if (jaxbImpConfig.isDoTypeDesignations() == true) {
				if ((typeDesignations = dataSet.getTypeDesignations()).size() > 0) {
					logger.info("Type Designations: " + typeDesignations.size());
					getNameService().saveTypeDesignationAll(typeDesignations);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving type designations");
			ret = false;
		}
		//commitTransaction(txStatus);


		// TODO: Implement dataSet.getDescriptions() and IDescriptionService.saveDescriptionAll()
//		if ((descriptions = dataSet.getDescriptions()) != null) {
//		logger.info("Saving " + descriptions.size() + " descriptions");
//		getDescriptionService().saveDescriptionAll(descriptions);
//		}

		//txStatus = startTransaction();
		try {
			if (jaxbImpConfig.isDoOccurrence() == true) {
				if ((occurrences = dataSet.getOccurrences()).size() > 0) {
					logger.info("Occurrences: " + occurrences.size());
					getOccurrenceService().saveSpecimenOrObservationBaseAll(occurrences);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving occurrences");
			ret = false;
		}
		//commitTransaction(txStatus);


		//txStatus = startTransaction();
		try {
			if (jaxbImpConfig.isDoFeatureData() == true) {
				if ((featureTrees = dataSet.getFeatureTrees()).size() > 0) {
					logger.info("Feature data: " + featureTrees.size());
					getDescriptionService().saveFeatureTreeAll(featureTrees);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving feature data");
			ret = false;
		}
		//commitTransaction(txStatus);


		//txStatus = startTransaction();
		try {
			if (jaxbImpConfig.isDoMedia() == true) {
				if ((media = dataSet.getMedia()).size() > 0) {
					logger.info("Media: " + media.size());
					getMediaService().saveMediaAll(media);
				}
			}
		} catch (Exception ex) {
			logger.error("Error saving media");
			ret = false;
		}
		//commitTransaction(txStatus);

		//commitTransaction(txStatus);
		logger.info("All data saved");

		return ret;

	}
	
	
	private boolean saveTermVocabularies(
			List<TermVocabulary<DefinedTermBase>> termVocabularies) {

		boolean success = true;
		logger.info("Term vocabularies: " + termVocabularies.size());
		try {
			getTermService().saveTermVocabulariesAll(termVocabularies);
		} catch (Exception ex) {
			logger.error("Error saving term vocabularies");
			success = false;
		}
		return success;
	}

	private boolean saveTerms(List<DefinedTermBase> terms) {

		boolean success = true;
		logger.info("Terms: " + terms.size());
		try {
			getTermService().saveTermsAll(terms);
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
