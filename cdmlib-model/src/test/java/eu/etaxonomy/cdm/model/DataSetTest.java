package eu.etaxonomy.cdm.model;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.BotanicalName;
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
    private List<Taxon> taxa;
	
	@Before
	public void onSetUp() throws Exception {
		dataSet = new DataSet();
	}
	
	@Test
	public void testCdmDocumentBuilderInit() {
		Assert.assertNotNull(dataSet);
	}

	public List<Taxon> buildTaxa(TeamOrPersonBase author) {

		// create some Taxa
		taxa = new ArrayList<Taxon>();
		ReferenceBase sec;
		ZoologicalName name1;
		BotanicalName name2;
		Taxon rootT;
		Taxon child1;
		Taxon child2;
		Synonym syn1;
		Synonym syn2;
		BotanicalName name3;
		BotanicalName name4;
		Taxon freeT;
		sec=new Book();
		sec.setAuthorTeam(author);
		sec.setTitleCache("Schönes saftiges Allgäu");
		name1 = ZoologicalName.NewInstance(Rank.SPECIES(),"Panthera",null,"onca",null,author,null,"p.1467", null);
		name2 = BotanicalName.NewInstance(Rank.SPECIES(),"Abies",null,"alba",null,author,null,"p.317", null);
		name3 = BotanicalName.NewInstance(Rank.SUBSPECIES(),"Polygala",null,"vulgaris","alpina",author,null,"p.191", null);
		name4 = BotanicalName.NewInstance(Rank.SPECIES(),"Cichoria",null,"carminata",null,author,null,"p.14", null);
		rootT = Taxon.NewInstance(name1,sec);
		freeT = Taxon.NewInstance(name4,sec);
		// taxonomic children
		child1 = Taxon.NewInstance(name2,sec);
		child2 = Taxon.NewInstance(name3,sec);
		rootT.addTaxonomicChild(child1, sec, "p.998");
		rootT.addTaxonomicChild(child2, sec, "p.987");
		// synonymy
		syn1=Synonym.NewInstance(name1,sec);
		syn2=Synonym.NewInstance(name2,sec);
		child1.addSynonym(syn1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		child2.addSynonym(syn2, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());

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

		taxa = buildTaxa(linne);
		dataSet.setTaxa(taxa);

		return dataSet;

	}

}
