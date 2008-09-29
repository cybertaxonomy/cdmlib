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
	
    /* SerializeFrom DB **/
	private static final String serializeFromDb = "cdm_test_anahit2";
	//private static final String serializeFromDb = "cdm_test_anahit";
	
    /* SerializeTo DB */
	private static final String deserializeToDb = "cdm_test_anahit";
	//private static final String deserializeToDb = "cdm_test_jaxb2";
	
	/** NUMBER_ROWS_TO_RETRIEVE = 0 is the default case to retrieve all rows. */
	private static final int NUMBER_ROWS_TO_RETRIEVE = 0;
	
	/** For testing purposes: If NUMBER_ROWS_TO_RETRIEVE >0 then retrieve 
	 *  as many rows as specified for agents, references, etc. 
	 *  Only root taxa and no synonyms and relationships are retrieved. */
	//private static final int NUMBER_ROWS_TO_RETRIEVE = 10;
	
//	private boolean doAgents = false;
//	private boolean doAgentData = false;
//	private boolean doLanguageData = false;
//	private boolean doFeatureData = false;
//	private boolean doDescriptions = false;
//	private boolean doMedia = false;
//	private boolean doOccurrences = false;
//	private boolean doReferences = false;
//	private boolean doReferencedEntities = false;
//	private boolean doRelationships = false;
//	private boolean doSynonyms = false;
//	private boolean doTaxonNames = false;
//	private boolean doTaxa = false;
//	private boolean doTerms = false;
//	private boolean doTermVocabularies = false;
//	private boolean doHomotypicalGroups = true;
	
	private boolean doAgents = true;
	private boolean doAgentData = true;
	private boolean doLanguageData = true;
	private boolean doFeatureData = true;
	private boolean doDescriptions = true;
	private boolean doMedia = true;
	private boolean doOccurrences = true;
	private boolean doReferences = true;
	private boolean doReferencedEntities = true;
	private boolean doRelationships = true;
	private boolean doSynonyms = true;
	private boolean doTaxonNames = true;
	private boolean doTaxa = true;
	private boolean doTerms = true;
	private boolean doTermVocabularies = true;
	private boolean doHomotypicalGroups = true;
	
	private String server = "192.168.2.10";
	private String username = "edit";
	private String marshOutOne = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_marshalled.xml");
	private String marshOutTwo = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_roundtrip.xml");

	private CdmDocumentBuilder cdmDocumentBuilder = null;
	
    public CdmExporter() {	
    }

    public void initDb(String dbname) {
    	
    	CdmApplicationController appCtr = initPreloadedDb(dbname);
    	loadTestData(dbname, appCtr);
    }
    
    public CdmApplicationController initPreloadedDb(String dbname) {
    	
		logger.info("Initializing DB " + dbname);
		
		CdmApplicationController appCtr = null;
		try {
			String pwd = AccountStore.readOrStorePassword(dbname, server, username, null);
			
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, pwd);
			appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
		
		return appCtr;
    }

    public void loadTestData(String dbname, CdmApplicationController appCtr) {
    	
		logger.info("Loading test data into " + dbname);
		
		DataSet dataSet = buildDataSet();
		
		TransactionStatus txStatus = appCtr.startTransaction();
		
		logger.info("Initializing DB " + dbname + " with test data");
		appCtr.getTaxonService().saveTaxonAll(dataSet.getTaxa());

		appCtr.commitTransaction(txStatus);
		appCtr.close();
    }

    private void retrieveAllDataFlat (CdmApplicationController appCtr, DataSet dataSet, int numberOfRows) {
    	
        final int MAX_ROWS = 50000;

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

    	if (doTermVocabularies == true) {
    		if (termVocabularyRows == 0) { termVocabularyRows = MAX_ROWS; }
    		logger.info("# TermVocabulary");
    		dataSet.setTermVocabularies(appCtr.getTermService().getAllTermVocabularies(MAX_ROWS, 0));;
    	}
    	
    	if (doLanguageData == true) {
    		if (languageDataRows == 0) { languageDataRows = MAX_ROWS; }
    		logger.info("# Representation, Language String");
    		dataSet.setLanguageData(appCtr.getTermService().getAllRepresentations(MAX_ROWS, 0));
    		dataSet.addLanguageData(appCtr.getTermService().getAllLanguageStrings(MAX_ROWS, 0));
    	}
    	
    	if (doTerms == true) {
    		if (definedTermBaseRows == 0) { definedTermBaseRows = appCtr.getTermService().count(DefinedTermBase.class); }
    		logger.info("# DefinedTermBase: " + definedTermBaseRows);
    		dataSet.setTerms(appCtr.getTermService().getAllDefinedTerms(definedTermBaseRows, 0));
    	}

    	if (doAgents == true) {
    		if (agentRows == 0) { agentRows = appCtr.getAgentService().count(Agent.class); }
    		logger.info("# Agents: " + agentRows);
    		//logger.info("    # Team: " + appCtr.getAgentService().count(Team.class));
    		dataSet.setAgents(appCtr.getAgentService().getAllAgents(agentRows, 0));
    	}

    	if (doReferences == true) {
    		if (referenceBaseRows == 0) { referenceBaseRows = appCtr.getReferenceService().count(ReferenceBase.class); }
    		logger.info("# ReferenceBase: " + referenceBaseRows);
    		dataSet.setReferences(appCtr.getReferenceService().getAllReferences(referenceBaseRows, 0));
    	}

    	if (doTaxonNames == true) {
    		if (taxonNameBaseRows == 0) { taxonNameBaseRows = appCtr.getNameService().count(TaxonNameBase.class); }
    		logger.info("# TaxonNameBase: " + taxonNameBaseRows);
    		//logger.info("    # Taxon: " + appCtr.getNameService().count(BotanicalName.class));
    		dataSet.setTaxonomicNames(appCtr.getNameService().getAllNames(taxonNameBaseRows, 0));
    	}

    	if (doHomotypicalGroups == true) {
    		if (homotypicalGroupRows == 0) { homotypicalGroupRows = MAX_ROWS; }
    		logger.info("# Homotypical Groups");
    		dataSet.setHomotypicalGroups(appCtr.getNameService().getAllHomotypicalGroups(homotypicalGroupRows, 0));
    	}
    	
    	if (doTaxa == true) {
    		if (taxonBaseRows == 0) { taxonBaseRows = appCtr.getTaxonService().count(TaxonBase.class); }
    		logger.info("# TaxonBase: " + taxonBaseRows);
    		dataSet.setTaxa(new ArrayList<Taxon>());
    		dataSet.setSynonyms(new ArrayList<Synonym>());
    		List<TaxonBase> tb = appCtr.getTaxonService().getAllTaxa(taxonBaseRows, 0);
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

    	if (doRelationships == true) {
    		if (relationshipRows == 0) { relationshipRows = MAX_ROWS; }
    		logger.info("# Relationships");
    		List<RelationshipBase> relationList = appCtr.getTaxonService().getAllRelationships(relationshipRows, 0);
    		Set<RelationshipBase> relationSet = new HashSet<RelationshipBase>(relationList);
    		dataSet.setRelationships(relationSet);
    	}

    	if (doReferencedEntities == true) {
    		logger.info("# Referenced Entities");
    		dataSet.setReferencedEntities(appCtr.getNameService().getAllNomenclaturalStatus(MAX_ROWS, 0));
    		dataSet.addReferencedEntities(appCtr.getNameService().getAllTypeDesignations(MAX_ROWS, 0));
    	}

    	if (doOccurrences == true) {
    		if (occurrencesRows == 0) { occurrencesRows = appCtr.getOccurrenceService().count(SpecimenOrObservationBase.class); }
    		logger.info("# SpecimenOrObservationBase: " + occurrencesRows);
    		dataSet.setOccurrences(appCtr.getOccurrenceService().getAllSpecimenOrObservationBases(occurrencesRows, 0));
    	}

    	if (doMedia == true) {
    		if (mediaRows == 0) { mediaRows = MAX_ROWS; }
    		logger.info("# Media");
    		dataSet.setMedia(appCtr.getMediaService().getAllMedia(mediaRows, 0));
//    		dataSet.addMedia(appCtr.getMediaService().getAllMediaRepresentations(mediaRows, 0));
//    		dataSet.addMedia(appCtr.getMediaService().getAllMediaRepresentationParts(mediaRows, 0));
    	}
    	
    	if (doFeatureData == true) {
    		if (featureDataRows == 0) { featureDataRows = MAX_ROWS; }
    		logger.info("# Feature Tree, Feature Node");
    		dataSet.setFeatureData(appCtr.getDescriptionService().getFeatureNodesAll());
    		dataSet.addFeatureData(appCtr.getDescriptionService().getFeatureTreesAll());
    	}
    }

	/**  Saves data in DB */
    private void saveData (CdmApplicationController appCtr, DataSet dataSet) {

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
		List<LanguageStringBase> languageData = new ArrayList<LanguageStringBase>();
		List<TermVocabulary<DefinedTermBase>> termVocabularies
		    = new ArrayList<TermVocabulary<DefinedTermBase>>();
		List<HomotypicalGroup> homotypicalGroups = new ArrayList<HomotypicalGroup>();

		TransactionStatus txStatus = appCtr.startTransaction();
		
		// If data of a certain type, such as terms, are not saved here explicitly, 
		// then only those data of this type that are referenced by other objects are saved implicitly.
		// For example, if taxa are saved all other data referenced by those taxa, such as synonyms, 
		// are automatically saved as well.
		
		if ((terms = dataSet.getTerms()) != null) {
			logger.info("Terms: " + terms.size());
			appCtr.getTermService().saveTermsAll(terms);
		}

		if (doTermVocabularies == true) {
			if ((termVocabularies = dataSet.getTermVocabularies()).size() > 0) {
				logger.info("Language data: " + termVocabularies.size());
				appCtr.getTermService().saveTermVocabulariesAll(termVocabularies);
			}
		}

		if (doAgents == true) {
			if ((agents = dataSet.getAgents()) != null) {
				logger.info("Agents: " + agents.size());
				appCtr.getAgentService().saveAgentAll(agents);
			}
		}

		if (doReferences == true) {
			if ((references = dataSet.getReferences()) != null) {
				logger.info("References: " + references.size());
				appCtr.getReferenceService().saveReferenceAll(references);
			}
		}

		if (doTaxonNames == true) {
			if ((taxonomicNames = dataSet.getTaxonomicNames()) != null) {
				logger.info("Taxonomic names: " + taxonomicNames.size());
				appCtr.getNameService().saveTaxonNameAll(taxonomicNames);
			}
		}

		if (doHomotypicalGroups == true) {
			if ((homotypicalGroups = dataSet.getHomotypicalGroups()) != null) {
				logger.info("Homotypical groups: " + homotypicalGroups.size());
				appCtr.getNameService().saveAllHomotypicalGroups(homotypicalGroups);
			}
		}
		
		// Need to get the taxa and the synonyms here.
		if (doTaxa == true) {
			if ((taxonBases = dataSet.getTaxonBases()) != null) {
				logger.info("Taxon bases: " + taxonBases.size());
				appCtr.getTaxonService().saveTaxonAll(taxonBases);
			}
		}

	    // NomenclaturalStatus, TypeDesignations
		if (doReferencedEntities == true) {
			if ((referencedEntities = dataSet.getReferencedEntities()) != null) {
				logger.info("Referenced entities: " + referencedEntities.size());
				appCtr.getNameService().saveReferencedEntitiesAll(referencedEntities);
			}
		}

		// TODO: Implement dataSet.getDescriptions() and IDescriptionService.saveDescriptionAll()
//		if ((descriptions = dataSet.getDescriptions()) != null) {
//		logger.info("Saving " + descriptions.size() + " descriptions");
//		appCtr.getDescriptionService().saveDescriptionAll(descriptions);
//		}

		if (doOccurrences == true) {
			if ((occurrences = dataSet.getOccurrences()) != null) {
				logger.info("Occurrences: " + occurrences.size());
				appCtr.getOccurrenceService().saveSpecimenOrObservationBaseAll(occurrences);
			}
		}

		if (doFeatureData == true) {
			if ((featureData = dataSet.getFeatureData()) != null) {
				logger.info("Feature data: " + featureData.size());
				appCtr.getDescriptionService().saveFeatureDataAll(featureData);
			}
		}

		if (doMedia == true) {
			if ((media = dataSet.getMedia()) != null) {
				logger.info("Media: " + media.size());
				appCtr.getMediaService().saveMediaAll(media);
			}
		}

		if (doLanguageData == true) {
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
    
    
    /** Starts with root taxa and traverses the taxonomic tree to retrieve children taxa, synonyms and relationships.
     * Taxa that are not part of the taxonomic tree are not found.
     * @param dbname
     * @param filename
     */
    public void doSerializeTaxonTree(String dbname, String filename) {
    	
		logger.info("Serializing DB " + dbname + " to file " + filename);

		CdmApplicationController appCtr = null;

    	try {
    		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
    		
    		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
    		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
    		appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation, true);

    	} catch (DataSourceNotFoundException e) {
    		logger.error("datasource error");
    	} catch (TermNotFoundException e) {
    		logger.error("defined terms not found");
    	}
    	
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
    	
    	if (NUMBER_ROWS_TO_RETRIEVE <= 0) { traverse(taxa, dataSet); }
    	
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
    

    // get all data directly from the services, including taxa, synonyms, and relationships
    public void doSerialize(String dbname, String filename) {
    	
		logger.info("Serializing DB " + dbname + " to file " + filename);

		CdmApplicationController appCtr = null;

    	try {
    		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
    		
    		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
    		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
    		appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation, true);

    	} catch (DataSourceNotFoundException e) {
    		logger.error("datasource error");
    	} catch (TermNotFoundException e) {
    		logger.error("defined terms not found");
    	}
    	
    	TransactionStatus txStatus = appCtr.startTransaction(true);
    	DataSet dataSet = new DataSet();
    	List<Taxon> taxa = null;
    	List<DefinedTermBase> terms = null;

    	// get data from DB

    	try {
    		logger.info("Retrieving data from DB");

    		retrieveAllDataFlat(appCtr, dataSet, NUMBER_ROWS_TO_RETRIEVE);
    		
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
    

	public void doDeserialize(String dbname, String filename) {
		
		logger.info("Deserializing file " + filename + " to DB " + dbname);

		CdmApplicationController appCtr = null;

		try {
    		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
			
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
			appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation, true);
			
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}

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
		
		saveData(appCtr, dataSet);
		
	}
	
	private void test(){
		
		// Init DB with pre-loaded terms only.
		//initPreloadedDb(serializeFromDb);
		
		// Init Db with pre-loaded terms and some test data.
		//initDb(serializeFromDb);
		
		// Retrieve taxa, synonyms, and relationships through traversing the taxonomic tree.
		// Retrieve the other data from services.
	    //doSerializeTaxonTree(serializeFromDb, marshOutOne);
		
		// Retrieve data, including taxa, synonyms, and relationships
		// via services rather than traversing the tree.
	    doSerialize(serializeFromDb, marshOutOne);
	    
		doDeserialize(deserializeToDb, marshOutOne);
	    
		//doSerialize(deserializeToDb, marshOutTwo);
		}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		CdmExporter sc = new CdmExporter();
    	sc.test();
	}

	/**
	 * This method constructs a small sample taxonomic tree to test JAXB marshaling.
	 * The sample tree contains four taxa. The root taxon has two children taxa, and
	 * there is one "free" taxon without a parent and children.
	 */
	private DataSet buildDataSet() {

		List<Agent> agents = new ArrayList<Agent>();
	    List<VersionableEntity> agentData = new ArrayList<VersionableEntity>();
	    //List<TermBase> terms = new ArrayList<TermBase>();
	    List<DefinedTermBase> terms = new ArrayList<DefinedTermBase>();
	    List<ReferenceBase> references = new ArrayList<ReferenceBase>();
	    List<TaxonNameBase> taxonomicNames = new ArrayList<TaxonNameBase>();
	    List<Taxon> taxa = new ArrayList<Taxon>();
	    List<Synonym> synonyms = new ArrayList<Synonym>();
	    List<AnnotatableEntity> homotypicalGroups;

		StrictReferenceBase citRef, sec;
		BotanicalName name1, name2, name21, nameRoot, nameFree, synName11, synName12, synName2, synNameFree;
		Taxon child1, child2, child21, rootT, freeT;
		Synonym syn11, syn12, syn2, synFree;
		Rank rankSpecies, rankSubspecies, rankGenus;

		// agents 
		// - persons, institutions 

		Person linne = new Person("Carl", "Linné", "L.");
		GregorianCalendar birth = new GregorianCalendar(1707, 4, 23);
		GregorianCalendar death = new GregorianCalendar(1778, 0, 10);
		TimePeriod period = new TimePeriod(birth, death);
		linne.setLifespan(period);

		Keyword keyword = Keyword.NewInstance("plantarum", "lat", "");
		linne.addKeyword(keyword);

		Institution institute = Institution.NewInstance();

		agents.add(linne);
		agents.add(institute);

		// agent data
		// - contacts, addresses, memberships

		//Contact contact1 = new Contact();
		//contact1.setEmail("someone@somewhere.org");
		InstitutionalMembership membership 
		= new InstitutionalMembership(institute, linne, period, "Biodiversity", "Head");
		//agentData.add(contact1);

		agentData.add(membership);

		// terms
		// - ranks, keywords

		rankSpecies = Rank.SPECIES();
		rankSubspecies = Rank.SUBSPECIES();
		rankGenus = Rank.GENUS();
		
		terms.add(keyword);
		
        // taxonomic names
		
		nameRoot = BotanicalName.NewInstance(rankGenus,"Calendula",null,null,null,linne,null,"p.100", null);
		
		name1 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"arvensis",null,linne,null,"p.1", null);
		synName11 = BotanicalName.NewInstance(rankSpecies,"Caltha",null,"arvensis",null,linne,null,"p.11", null);
		synName12 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"sancta",null,linne,null,"p.12", null);
		
		name2 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzae",null,linne,null,"p.2", null);
		synName2 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"echinata",null,linne,null,"p.2", null);
		
		name21 = BotanicalName.NewInstance(rankSubspecies,"Calendula",null,"lanzea","something",linne,null,"p.1", null);
		//name211 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzea",null,linne,null,"p.1", null);
		//name212 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzea",null,linne,null,"p.1", null);
		
		nameFree = BotanicalName.NewInstance(rankSpecies,"Cichorium",null,"intybus",null,linne,null,"p.200", null);
		synNameFree = BotanicalName.NewInstance(rankSpecies,"Cichorium",null,"balearicum",null,linne,null,"p.2", null);

		taxonomicNames.add(nameRoot);
		taxonomicNames.add(name1);
		taxonomicNames.add(synName11);
		taxonomicNames.add(synName12);
		taxonomicNames.add(name2);
		taxonomicNames.add(name21);
		taxonomicNames.add(synName2);
		taxonomicNames.add(nameFree);
		taxonomicNames.add(synNameFree);
		
        // references
		
		sec = Book.NewInstance();
		sec.setAuthorTeam(linne);
		sec.setTitleCache("Plant Speciation");
		references.add(sec);
		
		citRef = Database.NewInstance();
		citRef.setAuthorTeam(linne);
		citRef.setTitleCache("BioCASE");
		references.add(citRef);

		// taxa
		
		rootT = Taxon.NewInstance(nameRoot, sec);
		freeT = Taxon.NewInstance(nameFree, sec);
		child1 = Taxon.NewInstance(name1, sec);
		child2 = Taxon.NewInstance(name2, sec);
		child21 = Taxon.NewInstance(name21, sec);
		
		// synonyms
		
		synFree = Synonym.NewInstance(synNameFree, sec);
		syn11 = Synonym.NewInstance(synName11, sec);
		syn12 = Synonym.NewInstance(synName12, sec);
		syn2 = Synonym.NewInstance(synName2, sec);
		
		child1.addSynonym(syn11, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
		child1.addSynonym(syn12, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		child2.addSynonym(syn2, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		freeT.addSynonym(synFree, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());

		synonyms.add(synFree);
		synonyms.add(syn11);
		synonyms.add(syn12);
		synonyms.add(syn2);
		
		// taxonomic children
		
		rootT.addTaxonomicChild(child1, sec, "p.1010");
		rootT.addTaxonomicChild(child2, sec, "p.1020");
		child2.addTaxonomicChild(child21, sec, "p.2000");
				
		taxa.add(rootT);
		taxa.add(freeT);
		taxa.add(child1);
		taxa.add(child2);
		taxa.add(child21);
		
		DataSet dataSet = new DataSet();
		
		dataSet.setAgents(agents);
		dataSet.setAgentData(agentData);
		dataSet.setTerms(terms);
		dataSet.setReferences(references);
		dataSet.setTaxonomicNames(taxonomicNames);
		dataSet.setTaxa(taxa);
		dataSet.setSynonyms(synonyms);
		
		return dataSet;

	}
	
}
