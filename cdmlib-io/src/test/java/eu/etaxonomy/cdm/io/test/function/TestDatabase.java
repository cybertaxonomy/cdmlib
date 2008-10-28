/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.test.function;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.io.jaxb.DataSet;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.babadshanjan
 * @created 28.10.2008
 */
public class TestDatabase {

	private static final Logger logger = Logger.getLogger(TestDatabase.class);

	public static void loadTestData(String dbname, CdmApplicationController appCtr) {
    	
		logger.info("Loading test data into " + dbname);
		
		TransactionStatus txStatus = appCtr.startTransaction();
		DataSet dataSet = buildDataSet();
		
		appCtr.getTaxonService().saveTaxonAll(dataSet.getTaxa());

		appCtr.commitTransaction(txStatus);
		appCtr.close();
    }
    
	/**
	 * This method constructs a small sample taxonomic tree to test JAXB marshaling.
	 * The sample tree contains four taxa. The root taxon has two children taxa, and
	 * there is one "free" taxon without a parent and children.
	 */
	private static DataSet buildDataSet() {

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
