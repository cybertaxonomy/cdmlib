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
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class DataSetTest {

	private DataSet dataSet;
	private List<Agent> agents;
    private List<VersionableEntity> agentData;
    private List<TermBase> terms;
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

    // not used
	public List<AnnotatableEntity> buildHomotypicalGroups(TeamOrPersonBase author) {
		
		homotypicalGroups = new ArrayList<AnnotatableEntity>();
		
		// TODO
		
		return homotypicalGroups;
	}

    // not used
	public void setSynonyms(TeamOrPersonBase author) {

		// create some Synonyms
		//synonyms = new ArrayList<Synonym>();

		NonViralName scientificNames[] = {
				ZoologicalName.NewInstance(Rank.SPECIES(),"Panthera",null,"onca",null,author,null,"p.1467", null),
				BotanicalName.NewInstance(Rank.SPECIES(),"Abies",null,"alba",null,author,null,"p.317", null)
		};

		String titleCaches[] = {
				"Schönes saftiges Allgäu",
				"Weisstanne"
		};
		
		ReferenceBase sec;
		sec= Book.NewInstance();
		sec.setAuthorTeam(author);

		for (int i = 0; i < 2; i++) {
			sec.setTitleCache(titleCaches[i]);
			synonyms.add(Synonym.NewInstance(scientificNames[i], sec));
		}
	}

	public List<Taxon> buildTaxa(TeamOrPersonBase author) {

		// create some Taxa
		taxa = new ArrayList<Taxon>();
		synonyms = new ArrayList<Synonym>();
		
		ReferenceBase sec1, sec2, secRoot, secFree;
		BotanicalName name1, name2, nameRoot, nameFree;
		Taxon child1, child2, rootT, freeT;
		Synonym syn1, syn2, synRoot, synFree;

		nameRoot = BotanicalName.NewInstance(Rank.SPECIES(),"Panthera",null,"onca",null,author,null,"p.1467", null);
		name1 = BotanicalName.NewInstance(Rank.SPECIES(),"Abies",null,"alba",null,author,null,"p.317", null);
		name2 = BotanicalName.NewInstance(Rank.SUBSPECIES(),"Polygala",null,"vulgaris","alpina",author,null,"p.191", null);
		nameFree = BotanicalName.NewInstance(Rank.SPECIES(),"Cichoria",null,"carminata",null,author,null,"p.14", null);

		secRoot= Book.NewInstance();
		secRoot.setAuthorTeam(author);
		secRoot.setTitleCache("Root Taxon");
		synRoot = Synonym.NewInstance(nameRoot, secRoot);
		synonyms.add(synRoot);
		rootT = Taxon.NewInstance(nameRoot, secRoot);
		
		secFree = Book.NewInstance();
		secFree.setAuthorTeam(author);
		secFree.setTitleCache("Free Taxon");
		synFree = Synonym.NewInstance(nameFree, secFree);
		synonyms.add(synFree);
		freeT = Taxon.NewInstance(nameFree, secFree);
		
		// taxonomic children
		sec1 = Book.NewInstance();
		sec1.setAuthorTeam(author);
		sec1.setTitleCache("Child 1");
		syn1 = Synonym.NewInstance(name1, sec1);
		synonyms.add(syn1);
		child1 = Taxon.NewInstance(name1, sec1);
		rootT.addTaxonomicChild(child1, sec1, "p.998");
		
		sec2 = Book.NewInstance();
		sec2.setAuthorTeam(author);
		sec2.setTitleCache("Child 2");
		syn2 = Synonym.NewInstance(name2, sec2);
		synonyms.add(syn2);
		child2 = Taxon.NewInstance(name2, sec2);
		rootT.addTaxonomicChild(child2, sec2, "p.987");
				
		child1.addSynonym(syn1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		child2.addSynonym(syn2, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
		rootT.addSynonym(synRoot, SynonymRelationshipType.PRO_PARTE_SYNONYM_OF());
		freeT.addSynonym(synFree, SynonymRelationshipType.PARTIAL_SYNONYM_OF());

		taxa.add(rootT);
		taxa.add(child1);
		taxa.add(child2);
		taxa.add(freeT);
		
		return taxa;
	}
		
	public DataSet buildDataSet() {

		// create some Persons 
		agents = new ArrayList<Agent>();

		Person linne = new Person("Carl", "Linné", "L.");
		GregorianCalendar birth = new GregorianCalendar(1707, 4, 23);
		GregorianCalendar death = new GregorianCalendar(1778, 0, 10);
		TimePeriod period = new TimePeriod(birth, death);
		linne.setLifespan(period);
		linne.addKeyword(Keyword.NewInstance("plantarum", "lat", ""));

		agents.add(linne);
		dataSet.setAgents(agents);

		// create some contacts, addresses, memberships

		agentData = new ArrayList<VersionableEntity>();

		//Contact contact1 = new Contact();
		//contact1.setEmail("someone@somewhere.org");
		Institution institute = Institution.NewInstance();
		InstitutionalMembership membership 
		= new InstitutionalMembership(institute, linne, period, "Biodiversity", "Head");
		//agentData.add(contact1);

		agentData.add(membership);
		dataSet.setAgentData(agentData);


		// create some Ranks

		terms = new ArrayList<TermBase>();

		Rank rank = new Rank();
		terms.add(rank);
		//rank = new Rank("term", "label");
		//assertEquals("label", rank.getLabel());

		terms.add(rank);
		dataSet.setTerms(terms);

		// taxa
		
		taxa = buildTaxa(linne);
		dataSet.setTaxa(taxa);
		
		// synonyms
		
		dataSet.setSynonyms(synonyms);

		return dataSet;

	}

}
