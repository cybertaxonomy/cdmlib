/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.jaxb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringApplicationContext;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
 
/**
 * @author a.babadshanjan
 * @created 25.09.2008
 */
public class CdmExporter {
	
	private static final Logger logger = Logger.getLogger(CdmExporter.class);
	
	private CdmDocumentBuilder cdmDocumentBuilder = null;
	
    public CdmExporter() {	
    }

    private void retrieveData (JaxbExportImportConfigurator expImpConfig, DataSet dataSet) {
    	
        final int MAX_ROWS = 50000;
        int numberOfRows = expImpConfig.getMaxRows();
		CdmApplicationController appCtr = 
			expImpConfig.getSourceAppController(expImpConfig.getCdmSource(), false);
        
    	int agentRows = numberOfRows;
    	int definedTermBaseRows = numberOfRows;
    	int referenceBaseRows = numberOfRows;
    	int taxonNameBaseRows = numberOfRows;
    	int taxonBaseRows = numberOfRows;
    	int relationshipRows = numberOfRows;
    	int occurrencesRows = numberOfRows;
    	int mediaRows = numberOfRows;
    	int featureDataRows = numberOfRows;
    	int languageDataRows = numberOfRows;
    	int termVocabularyRows = numberOfRows;
    	int homotypicalGroupRows = numberOfRows;

    	if (expImpConfig.isDoTermVocabularies() == true) {
    		if (termVocabularyRows == 0) { termVocabularyRows = MAX_ROWS; }
    		logger.info("# TermVocabulary");
    		dataSet.setTermVocabularies(appCtr.getTermService().getAllTermVocabularies(MAX_ROWS, 0));;
    	}
    	
    	if (expImpConfig.isDoLanguageData() == true) {
    		if (languageDataRows == 0) { languageDataRows = MAX_ROWS; }
    		logger.info("# Representation, Language String");
    		dataSet.setLanguageData(appCtr.getTermService().getAllRepresentations(MAX_ROWS, 0));
    		dataSet.addLanguageData(appCtr.getTermService().getAllLanguageStrings(MAX_ROWS, 0));
    	}
    	
    	if (expImpConfig.isDoTerms() == true) {
    		if (definedTermBaseRows == 0) { definedTermBaseRows = appCtr.getTermService().count(DefinedTermBase.class); }
    		logger.info("# DefinedTermBase: " + definedTermBaseRows);
    		dataSet.setTerms(appCtr.getTermService().getAllDefinedTerms(definedTermBaseRows, 0));
    	}

    	if (expImpConfig.isDoAgents() == true) {
    		if (agentRows == 0) { agentRows = appCtr.getAgentService().count(Agent.class); }
    		logger.info("# Agents: " + agentRows);
    		//logger.info("    # Team: " + appCtr.getAgentService().count(Team.class));
    		dataSet.setAgents(appCtr.getAgentService().getAllAgents(agentRows, 0));
    	}

    	if (expImpConfig.isDoReferences() == true) {
    		if (referenceBaseRows == 0) { referenceBaseRows = appCtr.getReferenceService().count(ReferenceBase.class); }
    		logger.info("# ReferenceBase: " + referenceBaseRows);
    		dataSet.setReferences(appCtr.getReferenceService().getAllReferences(referenceBaseRows, 0));
    	}

    	if (expImpConfig.isDoTaxonNames() == true) {
    		if (taxonNameBaseRows == 0) { taxonNameBaseRows = appCtr.getNameService().count(TaxonNameBase.class); }
    		logger.info("# TaxonNameBase: " + taxonNameBaseRows);
    		//logger.info("    # Taxon: " + appCtr.getNameService().count(BotanicalName.class));
    		dataSet.setTaxonomicNames(appCtr.getNameService().getAllNames(taxonNameBaseRows, 0));
    	}

    	if (expImpConfig.isDoHomotypicalGroups() == true) {
    		if (homotypicalGroupRows == 0) { homotypicalGroupRows = MAX_ROWS; }
    		logger.info("# Homotypical Groups");
    		dataSet.setHomotypicalGroups(appCtr.getNameService().getAllHomotypicalGroups(homotypicalGroupRows, 0));
    	}
    	
    	if (expImpConfig.isDoTaxa() == true) {
    		if (taxonBaseRows == 0) { taxonBaseRows = appCtr.getTaxonService().count(TaxonBase.class); }
    		logger.info("# TaxonBase: " + taxonBaseRows);
//    		dataSet.setTaxa(new ArrayList<Taxon>());
//    		dataSet.setSynonyms(new ArrayList<Synonym>());
    		List<TaxonBase> tb = appCtr.getTaxonService().getAllTaxonBases(taxonBaseRows, 0);
    		for (TaxonBase taxonBase : tb) {
    			if (taxonBase instanceof Taxon) {
    				dataSet.addTaxon((Taxon)taxonBase);
    			} else if (taxonBase instanceof Synonym) {
    				dataSet.addSynonym((Synonym)taxonBase);
    			} else {
    				logger.error("entry of wrong type: " + taxonBase.toString());
    			}
    		}
    	}

    	// TODO: 
    	// retrieve taxa and synonyms separately
    	// need correct count for taxa and synonyms
//  	if (taxonBaseRows == 0) { taxonBaseRows = appCtr.getTaxonService().count(TaxonBase.class); }
//  	logger.info("# Synonym: " + taxonBaseRows);
//  	dataSet.setSynonyms(new ArrayList<Synonym>());
//  	dataSet.setSynonyms(appCtr.getTaxonService().getAllSynonyms(taxonBaseRows, 0));

    	if (expImpConfig.isDoRelationships() == true) {
    		if (relationshipRows == 0) { relationshipRows = MAX_ROWS; }
    		logger.info("# Relationships");
    		List<RelationshipBase> relationList = appCtr.getTaxonService().getAllRelationships(relationshipRows, 0);
    		Set<RelationshipBase> relationSet = new HashSet<RelationshipBase>(relationList);
    		dataSet.setRelationships(relationSet);
    	}

    	if (expImpConfig.isDoReferencedEntities() == true) {
    		logger.info("# Referenced Entities");
    		dataSet.setReferencedEntities(appCtr.getNameService().getAllNomenclaturalStatus(MAX_ROWS, 0));
    		dataSet.addReferencedEntities(appCtr.getNameService().getAllTypeDesignations(MAX_ROWS, 0));
    	}

    	if (expImpConfig.isDoOccurrences() == true) {
    		if (occurrencesRows == 0) { occurrencesRows = appCtr.getOccurrenceService().count(SpecimenOrObservationBase.class); }
    		logger.info("# SpecimenOrObservationBase: " + occurrencesRows);
    		dataSet.setOccurrences(appCtr.getOccurrenceService().getAllSpecimenOrObservationBases(occurrencesRows, 0));
    	}

    	if (expImpConfig.isDoMedia() == true) {
    		if (mediaRows == 0) { mediaRows = MAX_ROWS; }
    		logger.info("# Media");
    		dataSet.setMedia(appCtr.getMediaService().getAllMedia(mediaRows, 0));
//    		dataSet.addMedia(appCtr.getMediaService().getAllMediaRepresentations(mediaRows, 0));
//    		dataSet.addMedia(appCtr.getMediaService().getAllMediaRepresentationParts(mediaRows, 0));
    	}
    	
    	if (expImpConfig.isDoFeatureData() == true) {
    		if (featureDataRows == 0) { featureDataRows = MAX_ROWS; }
    		logger.info("# Feature Tree, Feature Node");
    		dataSet.setFeatureData(appCtr.getDescriptionService().getFeatureNodesAll());
    		dataSet.addFeatureData(appCtr.getDescriptionService().getFeatureTreesAll());
    	}
    }

	/**  Saves data in DB */
    private void saveData (JaxbExportImportConfigurator expImpConfig, DataSet dataSet) {

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
//		List<LanguageStringBase> languageData = new ArrayList<LanguageStringBase>();
//		List<TermVocabulary<DefinedTermBase>> termVocabularies
//		    = new ArrayList<TermVocabulary<DefinedTermBase>>();
//		List<HomotypicalGroup> homotypicalGroups = new ArrayList<HomotypicalGroup>();
		List<LanguageStringBase> languageData;
		List<TermVocabulary<DefinedTermBase>> termVocabularies;
		List<HomotypicalGroup> homotypicalGroups;

		CdmApplicationController appCtr = 
			//expImpConfig.getSourceAppController(expImpConfig.getCdmSource(), true);
		    expImpConfig.getDestinationAppController(expImpConfig.getCdmDestination(), true);
		TransactionStatus txStatus = appCtr.startTransaction();
		
		// If data of a certain type, such as terms, are not saved here explicitly, 
		// then only those data of this type that are referenced by other objects are saved implicitly.
		// For example, if taxa are saved all other data referenced by those taxa, such as synonyms, 
		// are automatically saved as well.
		
		if (expImpConfig.isDoTerms() == true) {
			if ((terms = dataSet.getTerms()).size() > 0) {
				logger.info("Terms: " + terms.size());
				appCtr.getTermService().saveTermsAll(terms);
			}
		}

//		if ((terms = dataSet.getTerms()) != null) {
//			logger.info("Terms: " + terms.size());
//			appCtr.getTermService().saveTermsAll(terms);
//		}
		
		if (expImpConfig.isDoTermVocabularies() == true) {
			if ((termVocabularies = dataSet.getTermVocabularies()).size() > 0) {
				logger.info("Language data: " + termVocabularies.size());
				appCtr.getTermService().saveTermVocabulariesAll(termVocabularies);
			}
		}

		if (expImpConfig.isDoAgents() == true) {
//			if ((agents = dataSet.getAgents()) != null) {
			if ((agents = dataSet.getAgents()).size() > 0) {
				logger.info("Agents: " + agents.size());
				appCtr.getAgentService().saveAgentAll(agents);
			}
		}

		if (expImpConfig.isDoReferences() == true) {
//			if ((references = dataSet.getReferences()) != null) {
			if ((references = dataSet.getReferences()).size() > 0) {
				logger.info("References: " + references.size());
				appCtr.getReferenceService().saveReferenceAll(references);
			}
		}

		if (expImpConfig.isDoTaxonNames() == true) {
//			if ((taxonomicNames = dataSet.getTaxonomicNames()) != null) {
			if ((taxonomicNames = dataSet.getTaxonomicNames()).size() > 0) {
				logger.info("Taxonomic names: " + taxonomicNames.size());
				appCtr.getNameService().saveTaxonNameAll(taxonomicNames);
			}
		}

		if (expImpConfig.isDoHomotypicalGroups() == true) {
//			if ((homotypicalGroups = dataSet.getHomotypicalGroups()) != null) {
			if ((homotypicalGroups = dataSet.getHomotypicalGroups()).size() > 0) {
				logger.info("Homotypical groups: " + homotypicalGroups.size());
				appCtr.getNameService().saveAllHomotypicalGroups(homotypicalGroups);
			}
		}
		
		// Need to get the taxa and the synonyms here.
		if (expImpConfig.isDoTaxa() == true) {
//			if ((taxonBases = dataSet.getTaxonBases()) != null) {
			if ((taxonBases = dataSet.getTaxonBases()).size() > 0) {
				logger.info("Taxon bases: " + taxonBases.size());
				appCtr.getTaxonService().saveTaxonAll(taxonBases);
			}
		}

	    // NomenclaturalStatus, TypeDesignations
		if (expImpConfig.isDoReferencedEntities() == true) {
//			if ((referencedEntities = dataSet.getReferencedEntities()) != null) {
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

		if (expImpConfig.isDoOccurrences() == true) {
//			if ((occurrences = dataSet.getOccurrences()) != null) {
			if ((occurrences = dataSet.getOccurrences()).size() > 0) {
				logger.info("Occurrences: " + occurrences.size());
				appCtr.getOccurrenceService().saveSpecimenOrObservationBaseAll(occurrences);
			}
		}

		if (expImpConfig.isDoFeatureData() == true) {
//			if ((featureData = dataSet.getFeatureData()) != null) {
			if ((featureData = dataSet.getFeatureData()).size() > 0) {
				logger.info("Feature data: " + featureData.size());
				appCtr.getDescriptionService().saveFeatureDataAll(featureData);
			}
		}

		if (expImpConfig.isDoMedia() == true) {
//			if ((media = dataSet.getMedia()) != null) {
			if ((media = dataSet.getMedia()).size() > 0) {
				logger.info("Media: " + media.size());
				appCtr.getMediaService().saveMediaAll(media);
			}
		}

		if (expImpConfig.isDoLanguageData() == true) {
			if ((languageData = dataSet.getLanguageData()).size() > 0) {
				logger.info("Language data: " + languageData.size());
				appCtr.getTermService().saveLanguageDataAll(languageData);
			}
		}

		logger.info("All data saved");

		appCtr.commitTransaction(txStatus);
		appCtr.close();

    }

	private void traverse (List<Taxon> taxonCollection, DataSet dataSet) {

    	if (taxonCollection == null) {
    		return;
    	}
    	
    	// The following collections store data of a particular horizontal level, 
    	// such as all synonyms, relationships, and children of all taxa of this level.

    	ArrayList<Taxon> children_ = new ArrayList<Taxon>();
    	Set<Synonym> synonyms_ = new HashSet();
    	Set<TaxonRelationship> taxonRelationships_ = new HashSet();
    	Set<SynonymRelationship> synonymRelationships_ = new HashSet();
    	// TODO: Count number of taxa etc. to restrict number of retrieved objects
    	int numberOfTaxa = 0;

    	for (Taxon taxon: taxonCollection) {
    		
    		numberOfTaxa++;

    		try {
    			
    			logger.debug("taxon: " + taxon.toString());
    			
    			// get the synonyms and synonym relationships
    			if (taxon.hasSynonyms() == true) {

    				Set<Synonym> synonyms = taxon.getSynonyms();
    				Set<SynonymRelationship> synonymRelationships = taxon.getSynonymRelations();

    				for (Synonym synonym: synonyms) {
    					logger.debug("synonym: " + synonym.toString());
    					synonyms_.add(synonym);
    				}

    				for (SynonymRelationship synonymRelationship: synonymRelationships) {
    					logger.debug("synonym relationship: " + synonymRelationship.toString());
    					synonymRelationships_.add(synonymRelationship);
    				}

    				// If calling dataSet.addSynonyms() inside this for loop
    				// get ConcurrentModificationException.
    			}

    			// get the taxon relationships
    			if (taxon.hasTaxonRelationships() == true) {

    				Set<TaxonRelationship> taxonRelationships = taxon.getTaxonRelations();

    				for (TaxonRelationship taxonRelationship: taxonRelationships) {
    					logger.debug("taxon relationship: " + taxonRelationship.toString());
    					taxonRelationships_.add(taxonRelationship);
    				}
    			}

    			// get the children
    			logger.debug("# children: " + taxon.getTaxonomicChildrenCount());
    			if (taxon.hasTaxonomicChildren() == true) {

    				Set<Taxon> children = taxon.getTaxonomicChildren();

    				for (Taxon child: children) {
    					children_.add(child);
    					logger.debug("child: "+ child.toString());
    				}
    			}
    		} catch (Exception e) {
    			logger.error("error retrieving taxon data");
    			e.printStackTrace();
    		}
    	}

    	try {
    		if (synonyms_ != null) {
    			dataSet.addSynonyms(synonyms_);
    		}
    		if (synonymRelationships_ != null) {
    			dataSet.addRelationships(synonymRelationships_);
    		}
    		if (taxonRelationships_ != null) {
    			dataSet.addRelationships(taxonRelationships_);
    		}
    		if (children_ != null) {
    			dataSet.addTaxa(children_);
    		} 

    	} catch (Exception e) {
    		logger.error("error setting DataSet structure");
    		e.printStackTrace();
    	}

    	if (children_ != null && children_.size() > 0) {
    		traverse(children_, dataSet);
    	} 
    }
    
    
    /** Retrieves data from a CDM DB and serializes them CDM to XML.
     * Starts with root taxa and traverses the taxonomic tree to retrieve children taxa, synonyms and relationships.
     * Taxa that are not part of the taxonomic tree are not found.
     * 
     * @param exImpConfig
     * @param dbname
     * @param filename
     */
    public void doSerializeTaxonTree(JaxbExportImportConfigurator exImpConfig, String filename) {

    	String dbname = exImpConfig.getCdmSource().getName();
    	logger.info("Serializing DB " + dbname + " to file " + filename);

		CdmApplicationController appCtr = 
			exImpConfig.getSourceAppController(exImpConfig.getCdmSource(), true);

    	TransactionStatus txStatus = appCtr.startTransaction(true);
    	DataSet dataSet = new DataSet();
    	List<Taxon> taxa = null;
    	List<DefinedTermBase> terms = null;

    	// get data from DB

    	try {
    		logger.info("Load data from DB: " + dbname);

    		taxa = appCtr.getTaxonService().getRootTaxa(null, null, false);
    		// CdmFetch options not yet implemented
    		//appCtr.getTaxonService().getRootTaxa(null, CdmFetch.NO_FETCH(), false);
    		dataSet.setTaxa(taxa);
    		
    		dataSet.setSynonyms(new ArrayList<Synonym>());
    		dataSet.setRelationships(new HashSet<RelationshipBase>());
    		
    	} catch (Exception e) {
    		logger.error("error setting root data");
    	}

        // traverse the taxonomic tree
    	
    	if (exImpConfig.getMaxRows() <= 0) { traverse(taxa, dataSet); }
    	
		logger.info("All data retrieved");
		
    	try {
    		cdmDocumentBuilder = new CdmDocumentBuilder();
    		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"), true);
    		cdmDocumentBuilder.marshal(dataSet, writer);
    		
    		// TODO: Split into one file per data set member to see whether performance improves?

    		logger.info("XML file written");
    		logger.info("Filename is: " + filename);
    		
    	} catch (Exception e) {
    		logger.error("marshalling error");
    		e.printStackTrace();
    	} 
    	appCtr.commitTransaction(txStatus);
    	appCtr.close();
    	
    }
    

    /** Retrieves data from a CDM DB and serializes them CDM to XML.
     * Starts with root taxa and traverses the taxonomic tree to retrieve children taxa, synonyms and relationships.
     * Taxa that are not part of the taxonomic tree are not found.
     * 
     * @param exImpConfig
     * @param dbname
     * @param filename
     */
    public void doSerialize(JaxbExportImportConfigurator expImpConfig, String filename) {
    	
    	String dbname = expImpConfig.getCdmSource().getName();
		logger.info("Serializing DB " + dbname + " to file " + filename);
		logger.debug("DbSchemaValidation = " + expImpConfig.getCdmSourceSchemaValidation());

		CdmApplicationController appCtr = 
			expImpConfig.getSourceAppController(expImpConfig.getCdmSource(), true);

    	TransactionStatus txStatus = appCtr.startTransaction(true);
    	DataSet dataSet = new DataSet();
    	List<Taxon> taxa = null;
    	List<DefinedTermBase> terms = null;

    	// get data from DB

    	try {
    		logger.info("Retrieving data from DB");

    		retrieveData(expImpConfig, dataSet);
//    		retrieveAllDataFlat(appCtr, dataSet, NUMBER_ROWS_TO_RETRIEVE);
    		
    	} catch (Exception e) {
    		logger.error("error setting data");
    		e.printStackTrace();
    	}

		logger.info("All data retrieved");
		
    	try {
    		cdmDocumentBuilder = new CdmDocumentBuilder();
    		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"), true);
    		cdmDocumentBuilder.marshal(dataSet, writer);
    		
    		// TODO: Split into one file per data set member to see whether performance improves?

    		logger.info("XML file written");
    		logger.info("Filename is: " + filename);
    		
    	} catch (Exception e) {
    		logger.error("marshalling error");
    		e.printStackTrace();
    	} 
    	appCtr.commitTransaction(txStatus);
    	appCtr.close();
    	
    }
    

    /** Reads data from an XML and stores them into a CDM DB.
     * 
     * @param exImpConfig
     * @param dbname
     * @param filename
     */
	public void doDeserialize(JaxbExportImportConfigurator expImpConfig, String filename) {
		
    	String dbname = expImpConfig.getCdmDestination().getName();
		logger.info("Deserializing file " + filename + " to DB " + dbname);

		DataSet dataSet = new DataSet();
		
        // unmarshalling XML file
		
		try {
			cdmDocumentBuilder = new CdmDocumentBuilder();
			logger.info("Unmarshalling file: " + filename);
			dataSet = cdmDocumentBuilder.unmarshal(dataSet, new File(filename));

		} catch (Exception e) {
			logger.error("unmarshalling error");
			e.printStackTrace();
		} 
		
		// save data in DB
		logger.info("Saving data to DB: " + dbname);
		
		saveData(expImpConfig, dataSet);
		
	}
		
}
