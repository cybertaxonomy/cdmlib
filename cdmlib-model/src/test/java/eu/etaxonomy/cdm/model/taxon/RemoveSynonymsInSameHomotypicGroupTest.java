// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;

/**
 * @author n.hoffmann
 * @created Sep 28, 2009
 * @version 1.0
 */
public class RemoveSynonymsInSameHomotypicGroupTest {
	private static final Logger logger = Logger
			.getLogger(RemoveSynonymsInSameHomotypicGroupTest.class);
	private static BotanicalName taxonName;
	private static BotanicalName synonymName2;
	private static BotanicalName synonymName1;
	private static Taxon taxon;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		taxonName = BotanicalName.NewInstance(null);
		taxon = Taxon.NewInstance(taxonName, null);
		
		synonymName1 = BotanicalName.NewInstance(null);
		
		
		synonymName2 = BotanicalName.NewInstance(null);

		
	}

	
	@Test
	public void testAddRemoveSynonymInSameGroup(){
		
		// add a synonym to the taxon
		Synonym synonym1 = taxon.addHeterotypicSynonymName(synonymName1).getSynonym();
		// get the homotypic group of that synonym
		HomotypicalGroup homotypicGroupOfSynonym = synonym1.getHomotypicGroup();
		// add another synonym into the homotypic group we just created
		Synonym synonym2 = taxon.addHeterotypicSynonymName(synonymName2, homotypicGroupOfSynonym, null, null).getSynonym();
		// everything is fine
		Assert.assertTrue("We should have two synonyms in the group", homotypicGroupOfSynonym.getSynonymsInGroup(null).size() == 2);
		
		// removing the synonym from the taxon
		taxon.removeSynonym(synonym2);
		
		// get the homotypical group via the methods in Taxon
		HomotypicalGroup homotypicGroupViaTaxon = taxon.getHeterotypicSynonymyGroups().iterator().next();
		
		// the group is for sure the same as the synonyms one
		Assert.assertSame("Accessing the homotypic group via the taxon methods should result in the same object", homotypicGroupOfSynonym, homotypicGroupViaTaxon);
		
		// although it might be correct that the synonym is not deleted from the taxonomic group 
		// we would not expect it to be here, since we just deleted it from the taxon and are accessing synonyms
		// via methods in Taxon
		Assert.assertTrue("When accessing the homotypic groups via taxon we would not expect the synonym we just deleted", 
				homotypicGroupViaTaxon.getSynonymsInGroup(null).size() == 1); 
	}
}
