/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

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

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

/**
 * This test class is a testing ground for solving the hibernate lazy loading problem using aspects
 *
 * @author c.mathew
 *
 */
@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-ltw-test.xml")
@Transactional(TransactionMode.DISABLED)
public class TestLTWLazyLoading extends UnitilsJUnit4 {
	
	@SpringBeanByType
	private ITaxonService taxonService;
	
	private UUID taxonUuid1 = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");
	private UUID taxonUuid2 = UUID.fromString("822d98dc-9ef7-44b7-a870-94573a3bcb46");
	
	/**
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@DataSet("LTWLazyLoadingTest.xml")
	public void testLazyLoading(){		

		Taxon taxon = CdmBase.deproxy(taxonService.find(taxonUuid1),Taxon.class);
		NonViralName nvn = CdmBase.deproxy(taxon.getName(),NonViralName.class);
		
		System.out.println("name : " + nvn.getNameCache());
		Reference ref = taxon.getSec();
		
		System.out.println("reference : " + ref.getTitleCache());
		
		Set<TaxonRelationship> taxonRelationsFrom = taxon.getRelationsFromThisTaxon();
		Iterator<TaxonRelationship> trItrFrom = taxonRelationsFrom.iterator(); 
		while(trItrFrom.hasNext()) {
			TaxonRelationship tr = trItrFrom.next();
			System.out.println("Taxon From Relationship : " + tr.getType().getTitleCache());
		}
		
		Set<TaxonRelationship> taxonRelationsTo = taxon.getRelationsFromThisTaxon();
		Iterator<TaxonRelationship> trItrTo = taxonRelationsTo.iterator(); 
		while(trItrTo.hasNext()) {
			TaxonRelationship tr = trItrTo.next();
			System.out.println("Taxon To Relationship : " + tr.getType().getTitleCache());
		}
	}
}
