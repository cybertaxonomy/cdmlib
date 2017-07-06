/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class CommonServiceImplTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CommonServiceImplTest.class);

	@SpringBeanByType
	private ICommonService service;

	@SpringBeanByType
	private ITaxonService taxonService;

	@SpringBeanByType
	private IReferenceService referenceService;
//
//	@SpringBeanByType
//	private IAgentService agentService;
//
//	@SpringBeanByType
//	private IUserService userService;

	@SpringBeanByType
	private IOccurrenceService occurrenceService;

/****************** TESTS *****************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao)}.
	 */
	@Test
	public final void testSetDao() {
		Assert.assertNotNull(service);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#getTaxonByUuid(java.util.UUID)}.
	 */
	@Test
	@DataSet
	@Ignore
	public final void testGetReferencingObjects() {
		IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name.setTitleCache("A name", true);
		Reference ref1 = ReferenceFactory.newArticle();
		Taxon taxon = Taxon.NewInstance(name, ref1);
		Person author = Person.NewInstance();
		author.setTitleCache("Author", true);
		ref1.addAnnotation(Annotation.NewInstance("A1", Language.DEFAULT()));
		ref1.setAuthorship(author);
		name.setBasionymAuthorship(author);

		name.setNomenclaturalReference(ref1);

		taxonService.save(taxon);
//		UUID uuid = UUID.fromString("613980ac-9bd5-43b9-a374-d71e1794688f");
//		Reference ref1 = referenceService.findByUuid(uuid);


		Set<CdmBase> referencedObjects = service.getReferencingObjects(ref1);
		System.out.println("############## RESULT ###################");
		for (CdmBase obj: referencedObjects){
			System.out.println("Object: " + obj.getClass().getSimpleName() + " - " + obj);
		}
		assertEquals(3, referencedObjects.size());
		System.out.println("############## ENDE ###################");



//		UUID uuidAuthor = UUID.fromString("4ce66544-a5a3-4601-ab0b-1f0a1338327b");
//		AgentBase author = agentService.findByUuid(uuidAuthor);

		referencedObjects = service.getReferencingObjects(author);
		System.out.println("############## RESULT ###################");
		for (CdmBase obj: referencedObjects){
			System.out.println("Object: " + obj.getClass().getSimpleName() + " - " + obj);
		}
		assertEquals(2, referencedObjects.size());
		System.out.println("############## ENDE ###################");
	}

	/**
	 * Test method for getReferencingObject.
	 * Test
	 */
	@Test
	@DataSet
	public final void testGetReferencingObjects2() {
//		SpecimenDescription desc1 = SpecimenDescription.NewInstance();
//		desc1.setTitleCache("desc1");
//		SpecimenDescription desc2 = SpecimenDescription.NewInstance();
//		desc2.setTitleCache("desc2");
//
//		SpecimenOrObservationBase spec1 = Specimen.NewInstance();
//
//		desc1.addDescribedSpecimenOrObservation(spec1);
//		//Taxon taxon = Taxon.NewInstance(taxonName, sec)
//		spec1.addDescription(desc2);
//
//		occurrenceService.save(spec1);

		UUID uuidSpec = UUID.fromString("41539e9c-3764-4f14-9712-2d07d00c8e4c");
		SpecimenOrObservationBase<?> spec1 = occurrenceService.find(uuidSpec);


		Set<CdmBase> referencedObjects = service.getReferencingObjects(spec1);
		System.out.println("############## RESULT ###################");
		for (CdmBase obj: referencedObjects){
			System.out.println("Object: " + obj.getClass().getSimpleName() + " - " + obj);
		}
		assertEquals(2, referencedObjects.size());
		System.out.println("############## ENDE ###################");

	}


	@Test
	@DataSet
	public final void testLoadCacheStrategyForReference(){
		Reference ref = referenceService.load(UUID.fromString("613980ac-9bd5-43b9-a374-d71e1794688f"));
		ref.setType(ReferenceType.Article);
		referenceService.update(ref);
		referenceService.updateTitleCache();
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
