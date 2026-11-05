/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 18.06.2009
 */
public class HomotypicalGroupTest extends EntityTestBase {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private static HomotypicalGroup group1;
	private static HomotypicalGroup group2;
	private static TaxonName name1;
	private static TaxonName name2;
	private static TaxonName name3;
	private static TaxonName name4;

	@BeforeEach
	public void setUp() throws Exception {
		name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name2 = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		name3 = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
		name4 = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY());
	}

//*********************** TESTS ***********************************************/

	@Test
	public void testGetBasionyms() {
		name1.addBasionym(name2);
		Set<TaxonName> basionyms =  name1.getHomotypicalGroup().getBasionyms();
		assertNotNull(basionyms, "Basionym set should not be null");
		assertEquals(1, basionyms.size(), "Number of basionyms should be 1");
		name3.addBasionym(name2);
		basionyms =  name2.getHomotypicalGroup().getBasionyms();
		assertEquals(1, basionyms.size(), "Number of basionyms should be 1");
		assertEquals(name2, basionyms.iterator().next(), "");
		name3.addBasionym(name4);
		basionyms =  name2.getHomotypicalGroup().getBasionyms();
		assertEquals(2, basionyms.size(), "Number of basionyms should be 2");
//		assertEquals(name2, basionyms.iterator().next(), "");
	}

	@Test
	public void testGetReplacedSynonym() {
		name3.addReplacedSynonym(name4, null, null, null, null);
		Set<TaxonName> replacedSyn =  name3.getHomotypicalGroup().getReplacedSynonym();
		assertNotNull(replacedSyn, "Replaced synonym set should not be null");
		assertEquals(1, replacedSyn.size(), "Number of replaced synonym should be 1");
	}

	@Test
	public void testGetBasionymAndReplacedSynonymRelations(){
		name1.addBasionym(name2);
		name3.addBasionym(name2);
		name4.addReplacedSynonym(name2, null, null, null, null);
		Set<NameRelationship> rels = name2.getHomotypicalGroup().getBasionymAndReplacedSynonymRelations();
		assertEquals(3, rels.size(), "Number of relations should be 3");
	}

	@Test
	public void testGetUnrelatedNames(){
		name1.addBasionym(name2);
		name4.addReplacedSynonym(name2, null, null, null, null);
		Set<TaxonName> unrelatedNames = name2.getHomotypicalGroup().getUnrelatedNames();
		assertEquals(0, unrelatedNames.size(), "Number of unrelatedNames should be 0");
		name1.getHomotypicalGroup().merge(name3.getHomotypicalGroup());
		unrelatedNames = name2.getHomotypicalGroup().getUnrelatedNames();
		assertEquals(1, unrelatedNames.size(), "Number of unrelatedNames should be 1");
	}

	@Test
	public void testSetGroupBasionym(){

		name1.mergeHomotypicGroups(name2);
		name1.mergeHomotypicGroups(name3);
		name2.mergeHomotypicGroups(name4);

		name3.getHomotypicalGroup().setGroupBasionym(name1);

		assertEquals(1, name2.getBasionyms().size());
		assertEquals(1, name3.getBasionyms().size());
		assertEquals(1, name4.getBasionyms().size());
		assertEquals(name1, name4.getBasionym());
	}

	@Test
	public void testRemoveGroupBasionym(){

		name1.mergeHomotypicGroups(name2);
		name1.mergeHomotypicGroups(name3);
		name2.mergeHomotypicGroups(name4);

		HomotypicalGroup hg = name3.getHomotypicalGroup();
		hg.setGroupBasionym(name1);

		assertEquals(1, name2.getBasionyms().size());
		assertEquals(1, name3.getBasionyms().size());
		assertEquals(1, name4.getBasionyms().size());
		assertEquals(name1, name4.getBasionym());
		assertEquals(1, hg.getBasionyms().size());

		HomotypicalGroup.removeGroupBasionym(name1);
		assertEquals(0, hg.getBasionyms().size());

		assertEquals(0, name2.getBasionyms().size());
		assertEquals(0, name3.getBasionyms().size());
		assertEquals(0, name4.getBasionyms().size());
		assertEquals(null, name4.getBasionym());

	}

// TODO clone not yet implemented in HomotypicalGroup
//	@Test
//	public void testClone(){
//		group1 = HomotypicalGroup.NewInstance();
//
//		group1.addTypifiedName(name1);
//		group1.addTypifiedName(name2);
//		group1.setGroupBasionym(name1);
//		HomotypicalGroup clone =(HomotypicalGroup)group1.clone();
//
//		assertEquals(clone.getTypifiedNames().size(), group1.getTypifiedNames().size());
//		TaxonName cloneBasionym = clone.getBasionyms().iterator().next();
//		TaxonName group1Basionym = group1.getBasionyms().iterator().next();
//		assertNotSame(cloneBasionym, group1Basionym);
//	}
}
