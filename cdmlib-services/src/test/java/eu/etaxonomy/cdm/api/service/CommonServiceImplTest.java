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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
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
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dto.ReferencingObjectDto;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 */
public class CommonServiceImplTest extends CdmIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

	@SpringBeanByType
	private ICommonService service;

	@SpringBeanByType
	private ITaxonService taxonService;

	@SpringBeanByType
	private IReferenceDao referenceDao;

    @SpringBeanByType
    private IReferenceService referenceService;

    @SpringBeanByType
    private IAgentService agentService;

	@SpringBeanByType
	private IOccurrenceService occurrenceService;

/****************** TESTS *****************************/

	@Test
	public final void testSetDao() {
		Assert.assertNotNull(service);
	}

    @Test
    @DataSet
    public final void testGetReferencingObjectsDto() {

        IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name.setTitleCache("A name", true);
        Reference ref1 = save(ReferenceFactory.newArticle());
        Taxon taxon = Taxon.NewInstance(name, ref1);
        taxon.addImportSource("id1", null, ref1, null);
        Person author = Person.NewInstance();
        author.setTitleCache("Author", true);
        ref1.addAnnotation(Annotation.NewInstance("A1", Language.DEFAULT()));
        ref1.setAuthorship(author);
        name.setBasionymAuthorship(author);
        name.setNomenclaturalReference(ref1);

        agentService.save(author);
        taxonService.save(taxon);

        Set<ReferencingObjectDto> referencedObjects = service.getReferencingObjectDtos(ref1);
        String debug = "############## RESULT ###################\n";
        for (ReferencingObjectDto obj: referencedObjects){
            debug += "Object: " + obj.getClass().getSimpleName() + " - " + obj + "\n";
        }
        assertEquals(3, referencedObjects.size());  //AM: was expected=3 first, as annotations are not reported I reduced to 2 (this is not related to not having a commit before, I tested it)
        //should not throw an exception
        referencedObjects = service.initializeReferencingObjectDtos(referencedObjects, true, true, true, null);
        debug += "############## END ###################\n";

        referencedObjects = service.getReferencingObjectDtos(author);
        debug += "############## RESULT ###################\n";
        for (ReferencingObjectDto obj: referencedObjects){
            debug += "Object: " + obj.getClass().getSimpleName() + " - " + obj + "\n";
        }
        assertEquals(2, referencedObjects.size());
        referencedObjects = service.initializeReferencingObjectDtos(referencedObjects, true, true, true, null);
        debug += "############## END ###################\n";
        logger.info(debug);
    }

	@Test
	@DataSet
	public final void testGetReferencingObjects() {

		IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name.setTitleCache("A name", true);
		Reference ref1 = save(ReferenceFactory.newArticle());
		Taxon taxon = Taxon.NewInstance(name, ref1);
		Person author = Person.NewInstance();
		author.setTitleCache("Author", true);
		ref1.addAnnotation(Annotation.NewInstance("A1", Language.DEFAULT()));
		ref1.setAuthorship(author);
		name.setBasionymAuthorship(author);
		name.setNomenclaturalReference(ref1);
		agentService.save(author);
		taxonService.save(taxon);

		Set<CdmBase> referencedObjects = service.getReferencingObjects(ref1);
		System.out.println("############## RESULT ###################\n");
		for (CdmBase obj: referencedObjects){
			System.out.println("Object: " + obj.getClass().getSimpleName() + " - " + obj + "\n");
		}
		assertEquals(2, referencedObjects.size());  //AM: was expected=3 first, as annotations are not reported I reduced to 2 (this is not related to not having a commit before, I tested it)
		System.out.println("############## END ###################\n");

		referencedObjects = service.getReferencingObjects(author);
		System.out.println("############## RESULT ###################\n");
		for (CdmBase obj: referencedObjects){
			System.out.println("Object: " + obj.getClass().getSimpleName() + " - " + obj + "\n");
		}
		assertEquals(2, referencedObjects.size());
		System.out.println("############## END ###################\n");
	}

    private Reference save(Reference ref) {
        referenceDao.save(ref);
        return ref;
    }

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
//		System.out.println("############## RESULT ###################");
//		for (CdmBase obj: referencedObjects){
//			System.out.println("Object: " + obj.getClass().getSimpleName() + " - " + obj);
//		}
		assertEquals(2, referencedObjects.size());
		System.out.println("############## ENDE ###################");
	}

	@Test
	@DataSet
	public final void testLoadCacheStrategyForReference(){
		Reference ref = referenceDao.load(UUID.fromString("613980ac-9bd5-43b9-a374-d71e1794688f"));
		ref.setType(ReferenceType.Article);
		referenceDao.update(ref);
		referenceService.updateCaches();
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}