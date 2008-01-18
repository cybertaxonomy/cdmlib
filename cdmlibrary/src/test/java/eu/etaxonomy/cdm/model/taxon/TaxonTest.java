package eu.etaxonomy.cdm.model.taxon;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class TaxonTest {
	private ReferenceBase sec;
	private ZoologicalName name1;
	private BotanicalName name2;
	private Taxon rootT;
	private Taxon child1;
	private Taxon child2;
	private Synonym syn1;
	private Synonym syn2;
	private BotanicalName name3;
	private BotanicalName name4;
	private Taxon freeT;

	@Before
	public void setUpBeforeClass() throws Exception {
		Person linne =new Person("Carl", "Linné", "L.");
		sec=new Book();
		sec.setAuthorTeam(linne);
		sec.setTitleCache("Schönes saftiges Allgäu");
		name1=new ZoologicalName(Rank.SPECIES(),"Panthera","onca",null,linne,null,"p.1467");
		name2=new BotanicalName(Rank.SPECIES(),"Abies","alba",null,linne,null,"p.317");
		name3=new BotanicalName(Rank.SUBSPECIES(),"Polygala","vulgaris","alpina",linne,null,"p.191");
		name4=new BotanicalName(Rank.SPECIES(),"Cichoria","carminata",null,linne,null,"p.14");
		rootT=Taxon.NewInstance(name1,sec);
		freeT=Taxon.NewInstance(name4,sec);
		// taxonomic children
		child1=Taxon.NewInstance(name2,sec);
		child2=Taxon.NewInstance(name3,sec);
		rootT.addTaxonomicChild(child1, sec, "p.998");
		rootT.addTaxonomicChild(child2, sec, "p.987");
		// synonymy
		syn1=Synonym.NewInstance(name1,sec);
		syn2=Synonym.NewInstance(name2,sec);
		child1.addSynonym(syn1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		child2.addSynonym(syn2, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
	}

	@Test
	public void testGenerateTitle() {
		assertTrue(rootT.generateTitle().startsWith("Taxon"));
	}

	@Test
	public void testAddTaxonomicChild() {
		rootT.addTaxonomicChild(freeT, null, null);
		assertTrue(rootT.getTaxonomicChildren().size()==3);
	}

	@Test
	public void testGetTaxonomicParent() {
		assertEquals(rootT, child2.getTaxonomicParent());
	}

	@Test
	public void testSssetTaxonomicParent() {
		child2.setTaxonomicParent(child1, null, null);
		assertEquals(child1, child2.getTaxonomicParent());
	}

	@Test
	public void testGetTaxonomicChildren() {
		Set<Taxon> kids=rootT.getTaxonomicChildren();
		assertTrue(kids.size()==2 && kids.contains(child1) && kids.contains(child2));
	}

	@Test
	public void testHasTaxonomicChildren() {
		assertTrue(rootT.hasTaxonomicChildren());
		assertFalse(child2.hasTaxonomicChildren());
	}

	@Test
	public void testGetSynonyms() {
		assertTrue(child1.getSynonyms().contains(syn1));
		assertTrue(child2.getSynonyms().contains(syn2));
		assertTrue(rootT.getSynonyms().isEmpty());
	}

	@Test
	public void testGetSynonymNames() {
		assertTrue(child1.getSynonymNames().contains(name1));
		assertTrue(child2.getSynonymNames().contains(name2));
		assertTrue(rootT.getSynonymNames().isEmpty());
	}

	@Test
	public void testAddSynonym() {
		freeT.addSynonym(syn1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		assertTrue(freeT.getSynonyms().contains(syn1));
		assertFalse(freeT.getSynonyms().contains(syn2));
	}

}
