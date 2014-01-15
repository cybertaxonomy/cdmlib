/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.lazyloading.CdmLazyLoader;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * This test class is a testing ground for solving the hibernate lazy loading problem using aspects
 *
 * @author c.mathew
 *
 */
@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-ltw-test.xml")
@Transactional(TransactionMode.DISABLED)
public class LTWLazyLoadingTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	private ITaxonService taxonService;
	
	private UUID taxonUuid1 = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");
	private UUID taxonUuid2 = UUID.fromString("ef96fafa-7750-4141-b31b-1ad1daab3e76");
	
	/**
	 */
	
	@Test
	@DataSet
	public void testLazyLoading(){		

		Taxon taxon = CdmBase.deproxy(taxonService.find(taxonUuid1),Taxon.class);
		System.out.println("taxon : " + taxon.getTitleCache());	
		taxon.setTitleCache(taxon.getTitleCache() + " - updated");		
		CdmLazyLoader.enableWeaving = false;
		taxonService.saveOrUpdate(taxon);
		CdmLazyLoader.enableWeaving = true;
		
		NonViralName nvn = CdmBase.deproxy(taxon.getName(),NonViralName.class);		
		System.out.println("name : " + nvn.getTitleCache());				
		nvn.setTitleCache(nvn.getTitleCache() + " - updated");		
//		taxonService.saveOrUpdate(taxon);
		
		Reference ref = taxon.getSec();
		System.out.println("Secundum : " + ref.getTitleCache());
		
		Rank rank = nvn.getRank();
		System.out.println("rank : " + rank.getTitleCache());
		
		NonViralName nvnNew = CdmBase.deproxy(taxon.getName(),NonViralName.class);		
		System.out.println("nameNew : " + nvnNew.getTitleCache());
		
		Set<SynonymRelationship> synRelations = taxon.getSynonymRelations();
		Iterator<SynonymRelationship> srItr = synRelations.iterator();
		while(srItr.hasNext()) {
			SynonymRelationship sr = srItr.next();
			System.out.println("Synonym Relationship : " + sr.getType().getTitleCache());
		}
		
		Set<TaxonRelationship> taxonRelationsFrom = taxon.getRelationsFromThisTaxon();
		Iterator<TaxonRelationship> trItrFrom = taxonRelationsFrom.iterator(); 
		while(trItrFrom.hasNext()) {
			TaxonRelationship tr = trItrFrom.next();
			System.out.println("Taxon From Relationship : " + tr.getType().getTitleCache());
		}
		
		Set<TaxonRelationship> taxonRelationsTo = taxon.getRelationsToThisTaxon();
		Iterator<TaxonRelationship> trItrTo = taxonRelationsTo.iterator(); 
		while(trItrTo.hasNext()) {
			TaxonRelationship tr = trItrTo.next();
			System.out.println("Taxon To Relationship : " + tr.getType().getTitleCache());
		}
	}
}
