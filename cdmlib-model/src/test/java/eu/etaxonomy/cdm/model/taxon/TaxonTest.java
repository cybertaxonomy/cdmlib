/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
 
package eu.etaxonomy.cdm.model.taxon;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

public class TaxonTest extends EntityTestBase {
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
		name1 = ZoologicalName.NewInstance(Rank.SPECIES(),"Panthera","onca",null,linne,null,"p.1467", null);
		name2 = BotanicalName.NewInstance(Rank.SPECIES(),"Abies","alba",null,linne,null,"p.317", null);
		name3 = BotanicalName.NewInstance(Rank.SUBSPECIES(),"Polygala","vulgaris","alpina",linne,null,"p.191", null);
		name4 = BotanicalName.NewInstance(Rank.SPECIES(),"Cichoria","carminata",null,linne,null,"p.14", null);
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
	}

	@Test
	public void testGenerateTitle() {
		assertTrue(rootT.generateTitle().startsWith(rootT.getName().getTitleCache()));
	}

	@Test
	public void testAddTaxonomicChild() {
		rootT.addTaxonomicChild(freeT, null, null);
		Assert.assertEquals(Integer.valueOf(3), Integer.valueOf(rootT.getTaxonomicChildren().size()));
	}

	@Test
	public void testGetTaxonomicParent() {
		assertEquals(rootT, child2.getTaxonomicParent());
	}

	@Test
	public void testSetTaxonomicParent() {
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
		assertTrue(syn1.getAcceptedTaxa().contains(freeT));
		assertFalse(freeT.getSynonyms().contains(syn2));
	}

}
