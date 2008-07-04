package eu.etaxonomy.cdm.model;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

public class DataSetTest {

	private DataSet dataSet;
	private List<Agent> agents;
    private List<VersionableEntity> agentData;
    private List<TermBase> terms;
    private List<StrictReferenceBase> references;
    private List<NonViralName> taxonomicNames;
    private List<Taxon> taxa;
    private List<Synonym> synonyms;
    private List<AnnotatableEntity> homotypicalGroups;
	
	@Before
	public void onSetUp() throws Exception {
		dataSet = new DataSet();
	}
	
	@Test
	public void testCdmDocumentBuilderInit() {
		Assert.assertNotNull(dataSet);
	}

	/**
	 * This method constructs a small sample taxonomic tree to test JAXB marshaling.
	 * The sample tree contains four taxa. The root taxon has two children taxa, and
	 * there is one "free" taxon without a parent and children.
	 */
	public void buildTaxa() {

		agents = new ArrayList<Agent>();
		agentData = new ArrayList<VersionableEntity>();
		terms = new ArrayList<TermBase>();
	    references = new ArrayList<StrictReferenceBase>();
		taxonomicNames = new ArrayList<NonViralName>();
		taxa = new ArrayList<Taxon>();
		synonyms = new ArrayList<Synonym>();
		
		StrictReferenceBase citRef, sec;
		BotanicalName name1, name2, nameRoot, nameFree;
		Taxon child1, child2, rootT, freeT;
		Synonym syn1, syn2, synRoot, synFree;

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

		Rank rankRoot = new Rank();
		Rank rankChildren = new Rank();
		Rank rankFree = new Rank();
		
//      Do something like this? If yes, FIXME: Stack overflow.
//		try {
//			rankRoot = Rank.getRankByName("Species");
//			rankChildren = Rank.getRankByName("Subspecies");
//			rankFree = Rank.getRankByName("Genus");
//			
//		} catch (UnknownCdmTypeException ex) {
//			ex.printStackTrace();
//		}
		
		terms.add(rankRoot);
		terms.add(rankChildren);
		terms.add(rankFree);
		
		terms.add(keyword);
		
        // taxonomic names
		
		nameRoot = BotanicalName.NewInstance(rankRoot,"Panthera",null,"onca",null,linne,null,"p.1467", null);
		name1 = BotanicalName.NewInstance(rankChildren,"Abies",null,"alba",null,linne,null,"p.317", null);
		name2 = BotanicalName.NewInstance(rankChildren,"Polygala",null,"vulgaris","alpina",linne,null,"p.191", null);
		nameFree = BotanicalName.NewInstance(rankFree,"Cichoria",null,"carminata",null,linne,null,"p.14", null);

		taxonomicNames.add(nameRoot);
		taxonomicNames.add(name1);
		taxonomicNames.add(name2);
		taxonomicNames.add(nameFree);
		
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
		
		// synonyms
		
		synRoot = Synonym.NewInstance(nameRoot, sec);
		synFree = Synonym.NewInstance(nameFree, sec);
		syn1 = Synonym.NewInstance(name1, sec);
		syn2 = Synonym.NewInstance(name2, sec);
		
		child1.addSynonym(syn1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		child2.addSynonym(syn2, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
		rootT.addSynonym(synRoot, SynonymRelationshipType.PRO_PARTE_SYNONYM_OF());
		freeT.addSynonym(synFree, SynonymRelationshipType.PARTIAL_SYNONYM_OF());

		synonyms.add(synRoot);
		synonyms.add(synFree);
		synonyms.add(syn2);
		synonyms.add(syn1);
		
		// taxonomic children
		rootT.addTaxonomicChild(child1, sec, "p.998");
		rootT.addTaxonomicChild(child2, sec, "p.987");
				
		taxa.add(rootT);
		taxa.add(freeT);
		taxa.add(child1);
		taxa.add(child2);
		
	}
		
	public DataSet buildDataSet() {

		buildTaxa();
		
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
