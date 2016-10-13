// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.babadshanjan
 * @author a.mueller
 * @created 25.05.2009
 */
public class TypeDesignationDaoHibernateImplTest extends CdmTransactionalIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TypeDesignationDaoHibernateImplTest.class);

	@SpringBeanByType
	ITypeDesignationDao typeDesignationDao;

	@SpringBeanByType
	ITaxonNameDao nameDao;

	@SpringBeanByType
	IOccurrenceDao occurrenceDao;


	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.name.TypeDesignationHibernateImpl#TypeDesignationHibernateImpl()}.
	 */
	@Test
	public void testInit() {
		assertNotNull("Instance of ITypeDesignationDao expected", typeDesignationDao);
	}
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.name.TypeDesignationHibernateImpl#getAllTypeDesignations(java.lang.Integer, java.lang.Integer)}.
	 */
	@Test
	@DataSet
	public void testGetAllTypeDesignations() {
		List<TypeDesignationBase> typeDesignations = typeDesignationDao.getAllTypeDesignations(100, 0);
		assertEquals(2, typeDesignations.size());
		SpecimenTypeDesignation specTypeDesig = null;
		for (TypeDesignationBase typeDesignation : typeDesignations) {
			if (typeDesignation.isInstanceOf(NameTypeDesignation.class)) {
				assertTrue(typeDesignation.getTypeStatus().isInstanceOf(NameTypeDesignationStatus.class));
			} else if (typeDesignation.isInstanceOf(SpecimenTypeDesignation.class)) {
				Assert.assertNull("There should be only 1 specimen type designation but this is already the second", specTypeDesig);
				TypeDesignationStatusBase typeDesignationStatus = typeDesignation.getTypeStatus();
				assertTrue(typeDesignationStatus.isInstanceOf(SpecimenTypeDesignationStatus.class));
				specTypeDesig = CdmBase.deproxy(typeDesignation,SpecimenTypeDesignation.class);
			}
		}
		Set<TaxonNameBase> names = specTypeDesig.getTypifiedNames();
		Assert.assertEquals("There should be exactly 1 typified name for the the specimen type designation", 1, names.size());
		TaxonNameBase<?,?> singleName = names.iterator().next();
		Assert.assertEquals("", UUID.fromString("61b1dcae-8aa6-478a-bcd6-080cf0eb6ad7"), singleName.getUuid());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.name.TypeDesignationHibernateImpl#saveOrUpdate(CdmBase)}.
	 */
	@Test
	@DataSet
	@ExpectedDataSet  //not yet necessary with current test
	public void testSaveTypeDesignations() {
		List<TypeDesignationBase> typeDesignations = typeDesignationDao.getAllTypeDesignations(100, 0);
		assertEquals(typeDesignations.size(), 2);
		SpecimenTypeDesignation specTypeDesig = null;
		for (TypeDesignationBase typeDesignation : typeDesignations) {
			if (typeDesignation.isInstanceOf(SpecimenTypeDesignation.class)) {
				specTypeDesig = CdmBase.deproxy(typeDesignation,SpecimenTypeDesignation.class);
			}
		}

		TaxonNameBase<?,?> newName = BotanicalName.NewInstance(Rank.SPECIES());
		newName.setUuid(UUID.fromString("c16c3bc5-d3d0-4676-91a1-848ebf011e7c"));
		newName.setTitleCache("Name used as typified name", true);
		newName.addTypeDesignation(specTypeDesig, false);

		nameDao.saveOrUpdate(newName);
//		typeDesignationDao.saveOrUpdate(specTypeDesig);

		commitAndStartNewTransaction(null);
		specTypeDesig = (SpecimenTypeDesignation)typeDesignationDao.load(specTypeDesig.getUuid());
		Assert.assertNotNull("specimen type designation should exists in db", specTypeDesig);
		specTypeDesig.getTypifiedNames().size();
		Set<TaxonNameBase> typifiedNames = specTypeDesig.getTypifiedNames();
		Assert.assertEquals("There should be 2 typified names for this type designation now", 2, typifiedNames.size());

//		printDataSet(System.out, new String[]{"TaxonNameBase","TaxonNameBase_AUD",
//				"HomotypicalGroup","HomotypicalGroup_AUD",
//				"TypeDesignationBase","TypeDesignationBase_AUD",
//				"TaxonNameBase_TypeDesignationBase", "TaxonNameBase_TypeDesignationBase_AUD"
//				});

	}

	@Test
	@ExpectedDataSet
	public void testSaveTypeDesignationsBidirectionalCascade() {
		SpecimenTypeDesignation desig1 = SpecimenTypeDesignation.NewInstance();
		desig1.setUuid(UUID.fromString("a1b8af89-b724-469b-b0ce-027c2199aadd"));

		TaxonNameBase<?,?> name = BotanicalName.NewInstance(Rank.SPECIES());
		name.setUuid(UUID.fromString("503d78dc-5d4c-4eb6-b682-0ab90fdee02c"));
		name.setTitleCache("Name saved via cascade", true);
		name.addTypeDesignation(desig1, false);

		typeDesignationDao.saveOrUpdate(desig1);
		commit();
	}

	@Test
	@ExpectedDataSet
	//Auditing didn't work for SpecimenTypeDesignations. See #2396
	public void testSaveTypeDesignationsWithAuditing() {


		// creating new Typedesignation for a new Name:

		//  1. new TaxonName with UUID 8564287e-9654-4b8b-a38c-0ccdd9e885db
		BotanicalName name1 = BotanicalName.NewInstance(Rank.SPECIES());
		name1.setTitleCache("Name1", true);
		name1.setUuid(UUID.fromString("8564287e-9654-4b8b-a38c-0ccdd9e885db"));
		//   2. new TypeDesignation with uuid ceca086e-e8d3-444e-abfb-c47f76835130
		SpecimenTypeDesignation desig1 = SpecimenTypeDesignation.NewInstance();
		desig1.setUuid(UUID.fromString("ceca086e-e8d3-444e-abfb-c47f76835130"));

//		//REMOVE
//		desig1.setOriginalNameString("orig");
//
		name1.addTypeDesignation(desig1, true);

		nameDao.saveOrUpdate(name1);
		commitAndStartNewTransaction(new String[]{"TypeDesignationBase", "TypeDesignationBase_AUD"});
//		System.out.println(desig1.getId());
//		System.out.println(desig1.getUuid());

//		printDataSet(System.err, new String[]{"TaxonNameBase","TaxonNameBase_AUD",
//				"HomotypicalGroup","HomotypicalGroup_AUD",
//				"TypeDesignationBase","TypeDesignationBase_AUD",
//				"TaxonNameBase_TypeDesignationBase","TaxonNameBase_TypeDesignationBase_AUD"
//				});

	}

	@Test
	@ExpectedDataSet
	public void testSaveTypeDesignationsCascadeToSpecimen() {
		BotanicalName name1 = BotanicalName.NewInstance(Rank.SPECIES());
		name1.setTitleCache("Name1", true);
		name1.setUuid(UUID.fromString("eb41f549-4a70-499b-a9a5-f2314880df07"));

		SpecimenTypeDesignation desig1 = SpecimenTypeDesignation.NewInstance();
		desig1.setUuid(UUID.fromString("6df85e4c-49fe-4eb5-acde-cf6c0c9fc3c5"));
		name1.addTypeDesignation(desig1, true);

		DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
		specimen.setUuid(UUID.fromString("f1a7c3b9-169c-4845-9b21-e77f863a8bce"));
		specimen.setTitleCache("Specimen to cascade", true);
		desig1.setTypeSpecimen(specimen);

		nameDao.saveOrUpdate(name1);

		this.setComplete();
		this.endTransaction();

//		printDataSet(System.out, new String[]{"TaxonNameBase","TaxonNameBase_AUD","TypeDesignationBase","TypeDesignationBase_AUD",
//				"TaxonNameBase_TypeDesignationBase","TaxonNameBase_TypeDesignationBase_AUD",
//				"TaxonNameBase_TypeDesignationBase","SpecimenOrObservationBase","SpecimenOrObservationBase_AUD",
//				"HomotypicalGroup","HomotypicalGroup_AUD"});

	}

	@Test
	@ExpectedDataSet
	//test save from specimen to name via type designation
	public void testSaveTypeDesignationsCascadeFromSpecimen() {
		BotanicalName name1 = BotanicalName.NewInstance(Rank.SPECIES());
		name1.setTitleCache("Name1", true);
		name1.setUuid(UUID.fromString("7ce3a355-8f7c-4417-a0b3-41869de4f60b"));

		SpecimenTypeDesignation desig1 = SpecimenTypeDesignation.NewInstance();
		desig1.setUuid(UUID.fromString("c0e03472-b9f9-4886-b3bd-c1c70dd21a5f"));
		name1.addTypeDesignation(desig1, true);

		DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
		specimen.setUuid(UUID.fromString("4c3231a9-336e-4b21-acf2-129683627de4"));
		specimen.setTitleCache("Specimen to cascade", true);
		desig1.setTypeSpecimen(specimen);

		occurrenceDao.saveOrUpdate(specimen);
		commit();

	}

	@Test
//	@ExpectedDataSet
	public void testRemoveTypeDesignationsFromName() {
		BotanicalName name1 = BotanicalName.NewInstance(Rank.SPECIES());
		name1.setTitleCache("Name1");
		name1.setUuid(UUID.fromString("2cfc05fc-138e-452d-b4ea-8798134c7410"));

		BotanicalName name2 = BotanicalName.NewInstance(Rank.SPECIES());
		name2.setTitleCache("Name2");
		name2.setUuid(UUID.fromString("7a12057d-2e99-471e-ac7e-633f1d0b5686"));

		SpecimenTypeDesignation desig1 = SpecimenTypeDesignation.NewInstance();
		desig1.setUuid(UUID.fromString("fe9f7711-de4a-4789-8045-86b2cb5c4358"));
		name1.addTypeDesignation(desig1, true);
		name2.addTypeDesignation(desig1, true);

		SpecimenTypeDesignation desig2 = SpecimenTypeDesignation.NewInstance();
		desig2.setUuid(UUID.fromString("bf357711-e752-44e9-bd3d-aef0a0bb5b91"));
		name1.addTypeDesignation(desig2, true);

		typeDesignationDao.saveOrUpdate(desig1);
		typeDesignationDao.saveOrUpdate(desig2);

		this.setComplete();
		this.endTransaction();
		this.startNewTransaction();

		name1 = (BotanicalName)nameDao.load(name1.getUuid());
		Assert.assertNotNull(name1);
		Assert.assertEquals("Name1 should have 2 type designations", 2, name1.getTypeDesignations().size());

		desig1 = (SpecimenTypeDesignation)typeDesignationDao.load(desig1.getUuid());
		name1.removeTypeDesignation(desig1);
		typeDesignationDao.saveOrUpdate(desig1);

		this.setComplete();
		this.endTransaction();
		this.startNewTransaction();

		name1 = (BotanicalName)nameDao.load(name1.getUuid());
		Assert.assertNotNull(name1);
		Assert.assertEquals("Name1 should have 1 type designation", 1, name1.getTypeDesignations().size());

		desig2 = (SpecimenTypeDesignation)typeDesignationDao.load(desig2.getUuid());
		Assert.assertNotNull(desig2);
		name1.removeTypeDesignation(desig2);
		typeDesignationDao.saveOrUpdate(desig2);

		this.setComplete();
		this.endTransaction();
		this.startNewTransaction();

		name1 = (BotanicalName)nameDao.load(name1.getUuid());
		Assert.assertNotNull(name1);
		Assert.assertEquals("Name1 should have no type designations", 0, name1.getTypeDesignations().size());

		name2 = (BotanicalName)nameDao.load(name2.getUuid());
		Assert.assertNotNull(name1);
		Assert.assertEquals("Name2 should have 1 type designation", 1, name2.getTypeDesignations().size());
		SpecimenTypeDesignation desig1New = (SpecimenTypeDesignation)name2.getTypeDesignations().iterator().next();
		desig1 = (SpecimenTypeDesignation)typeDesignationDao.load(desig1.getUuid());
		Assert.assertSame("Desig1New should be same as desig1", desig1, desig1New);

		try{
			typeDesignationDao.delete(desig1);
			this.setComplete();
			this.endTransaction();
			Assert.fail("desig1 should not be deletable as it is still connected to name2");
		}catch (Exception e){
			//OK
			this.startNewTransaction();
		}
		name2 = (BotanicalName)nameDao.load(name2.getUuid());
		Assert.assertNotNull(name1);
		desig1 = (SpecimenTypeDesignation)typeDesignationDao.load(desig1.getUuid());
		name2.removeTypeDesignation(desig1);

		typeDesignationDao.delete(desig1);  //now it can be deleted

		this.setComplete();
		this.endTransaction();
		this.startNewTransaction();

		desig2 = (SpecimenTypeDesignation)typeDesignationDao.load(desig2.getUuid());
		typeDesignationDao.delete(desig2); //desig2 is already orphaned and therefore can be deleted

		this.setComplete();
		this.endTransaction();


//		printDataSet(System.out, new String[]{"TaxonNameBase","TaxonNameBase_AUD","TypeDesignationBase","TypeDesignationBase_AUD",
//				"TaxonNameBase_TypeDesignationBase","TaxonNameBase_TypeDesignationBase_AUD",
//				"SpecimenOrObservationBase","SpecimenOrObservationBase_AUD",
//				"HomotypicalGroup","HomotypicalGroup_AUD"});
	}
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
