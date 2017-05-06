/**
 * Copyright (C) 2014 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * This test class tries to describe how hibernate works with objects (save, update, merge).
 *
 * @author cmathew
 * @date 17 Sep 2014
 *
 */

public class HandlingCdmEntitiesTest extends CdmIntegrationTest {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CommonServiceImplTest.class);

    private static final String LIE_TEAMMEMBERS_NOSESSION = "failed to lazily initialize a collection of role: eu.etaxonomy.cdm.model.agent.Team.teamMembers, could not initialize proxy - no Session";
    private static final String LIE_NOSESSION = "could not initialize proxy - no Session";

    @SpringBeanByType
    private IReferenceService referenceService;

    @SpringBeanByType
    private IAnnotationService annotationService;

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private IAgentService agentService;

    @SpringBeanByType
    private ICommonService commonService;

    public static final String[] includeTables = new String[]{
        "TAXONBASE",
        "TAXONNAMEBASE",
        "AGENTBASE",
        "AGENTBASE_AGENTBASE",
        "HOMOTYPICALGROUP"
    };

    @Override
    @Ignore
    @Test
    @Transactional(TransactionMode.DISABLED)
    public final void createTestDataSet() {
        Team combAuthor = Team.NewTitledInstance("Avengers", "Avengers");
        combAuthor.addTeamMember(Person.NewTitledInstance("Iron Man"));
        IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(null, "Abies alba", null, null, null, null, null, null, null);
        name.setCombinationAuthorship(combAuthor);
        Taxon taxon = Taxon.NewInstance(name, null);
        UUID taxonUuid = taxonService.save(taxon).getUuid();
        printDataSetWithNull(System.out,false,null,includeTables);
    }

    @Test
    @DataSet
    @Transactional(TransactionMode.DISABLED)
    public void testNonTransactionalUpdateForExistingTaxon() {
        // this method tests the updating of a 'truely' detached object which
        // attempts to initialize a lazy loaded proxy object while trying to
        // update the same.

        // setting the TransactionMode for this method to DISABLED is important
        // to ensure that transaction boundaries remain at the service layer calls
        // to simulate detachment and update of the persisted object

        UUID taxonUuid = UUID.fromString("23c35977-01b5-452c-9225-ecce440034e0");

        // ---- loading taxon with find (uuid) ----

        Taxon taxon = (Taxon)taxonService.find(taxonUuid);

        // at this point the taxonNew object is detached and all lazy loaded proxy
        // objects in the object graph (including teamMembers) will have values of
        // initialized=false and session=null

        // since name is lazy loaded the call to getName should fail
        try {
            CdmBase.deproxy(taxon.getName(), TaxonNameBase.class);
            Assert.fail("LazyInitializationException not thrown for lazy loaded Taxon.name");
        } catch(LazyInitializationException lie) {

            if(!lie.getMessage().equals(LIE_NOSESSION)) {
                Assert.fail("LazyInitializationException thrown, but not : " + LIE_NOSESSION);
            }
        }

        // ---- loading taxon with find (id) ----

        taxon = (Taxon)commonService.find(taxon.getClass(), taxon.getId());

        // at this point the taxonNew object is detached and all lazy loaded proxy
        // objects in the object graph (including teamMembers) will have values of
        // initialized=false and session=null

        // since name is lazy loaded the call to getName should fail
        try {
            CdmBase.deproxy(taxon.getName(),TaxonNameBase.class);
            Assert.fail("LazyInitializationException not thrown for lazy loaded Taxon.name");
        } catch(LazyInitializationException lie) {

            if(!lie.getMessage().equals(LIE_NOSESSION)) {
                Assert.fail("LazyInitializationException thrown, but not : " + LIE_NOSESSION);
            }
        }

        // ---- loading taxon with findTaxonByUuid ----

        List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String[] {
                "name"
        });

        // loading the taxon with its taxon name object pre-initialized
        taxon = (Taxon)taxonService.findTaxonByUuid(taxonUuid, TAXON_INIT_STRATEGY);

        // at this point the taxonNew object is detached and all lazy loaded proxy
        // objects in the object graph (including teamMembers) will have values of
        // initialized=false and session=null

        INonViralName nvn = CdmBase.deproxy(taxon.getName(),TaxonNameBase.class);

        // normally this call should throw a lazy loading exception since
        // the combinationAuthorship object is not initialized, but
        // the findTaxonByUuid performs this initialization (via the
        // AdvancedBeanInitializer) since the combinationAuthorship
        // is annotated with CacheUpdate

        Team team = CdmBase.deproxy(nvn.getCombinationAuthorship(),Team.class);

        // setting the protected title cache to false to ensure that
        // TeamDefaultCacheStrategy.getTitleCache is called, which in turn tries
        // to initialize the teamMembers persistent collection which fails

        team.setProtectedTitleCache(false);

        try {
            taxonService.update(taxon);
            Assert.fail("LazyInitializationException not thrown for lazy loaded Team.teamMembers");
        } catch(LazyInitializationException lie) {

            if(!lie.getMessage().equals(LIE_TEAMMEMBERS_NOSESSION)) {
                Assert.fail("LazyInitializationException thrown, but not : " + LIE_TEAMMEMBERS_NOSESSION);
            }
        }

        // the above fails due to the fact that hibernate does not resolve lazy
        // loading on a detached object until the object is persisted. The attempt
        // to initialize teamMembers before the object graph is persisted means that
        // the current session is not yet attached to the proxy objects and hibernate
        // tries to use the existing session set in the teamMembers object which is
        // null, leading to the exception

        // setting the protected title cache to true to ensure that
        // TeamDefaultCacheStrategy.getTitleCache is not called, implying
        // that the teamMembers are not initialized, so no exception is thrown

        team.setProtectedTitleCache(true);
        taxonService.update(taxon);

    }

    @Test
    @DataSet
    public void testTransactionalUpdateAfterFindTaxonByUuidForExistingTaxon() {
        // this method tests the updating of a detached object inside a single
        // transaction.

        // this method is transactional, meaning that a transaction is started
        // at the start of the method and ends only with the end of the method

        // since this method is transactional, any object initialized within this method
        // will have a valid session attached to any lazy loaded proxy objects
        // in the object graph (including teamMembers)

        UUID taxonUuid = UUID.fromString("23c35977-01b5-452c-9225-ecce440034e0");

        // ---- loading taxon with find (uuid) ----

        Taxon taxon = (Taxon)taxonService.find(taxonUuid);

        // at this point the taxonNew object is detached and all lazy loaded proxy
        // objects in the object graph (including teamMembers) will have a new
        // session attached implying that all the following calls will succeed

        INonViralName nvn =  CdmBase.deproxy(taxon.getName());
        Team team = CdmBase.deproxy(nvn.getCombinationAuthorship(),Team.class);
        taxonService.update(taxon);

        // ---- loading taxon with find (id) ----

        taxon = (Taxon)commonService.find(taxon.getClass(), taxon.getId());

        // at this point the taxonNew object is detached and all lazy loaded proxy
        // objects in the object graph (including teamMembers) will have a new
        // session attached implying that all the following calls will succeed

        nvn =  CdmBase.deproxy(taxon.getName());
        team = CdmBase.deproxy(nvn.getCombinationAuthorship(), Team.class);
        taxonService.update(taxon);

        // ---- loading taxon with findTaxonByUuid ----

        List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String[] {
                "name"
        });

        // loading the taxon with its taxon name object pre-initialized
        taxon = (Taxon)taxonService.findTaxonByUuid(taxonUuid, TAXON_INIT_STRATEGY);

        nvn = CdmBase.deproxy(taxon.getName(),TaxonNameBase.class);
        team = CdmBase.deproxy(nvn.getCombinationAuthorship(),Team.class);

        // since a valid session is now attached to teamMembers, forcing the
        // initializing of the teamMembers (in TeamDefaultCacheStrategy.getTitleCache)
        // by setting the protected title cache to false does not throw an exception
        // because the teamMember persistent collection now has a valid session,
        // which is used to initialize the persistent collection

        team.setProtectedTitleCache(false);
        taxonService.update(taxon);
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void testNonTransactionalUpdateForNewTaxon() {

        // this test is only to prove that the update problem occurs
        // also for newly created objects (as expected)

        // create / save new taxon with name and author team with team member

        Team combAuthor = Team.NewTitledInstance("X-Men", "X-Men");
        combAuthor.addTeamMember(Person.NewTitledInstance("Wolverine"));


        IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(null, "Pinus Alba", null, null, null, null, null, null,  null);
        name.setCombinationAuthorship(combAuthor);

        Taxon taxon = Taxon.NewInstance(name, null);

        UUID taxonUuid = taxonService.save(taxon).getUuid();

        List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String[] {
                "name"
        });

        taxon = (Taxon)taxonService.findTaxonByUuid(taxonUuid, TAXON_INIT_STRATEGY);

        INonViralName nvn = CdmBase.deproxy(taxon.getName());
        Team team = CdmBase.deproxy(nvn.getCombinationAuthorship(), Team.class);
        team.setProtectedTitleCache(false);

        try {
            taxonService.update(taxon);
            Assert.fail("LazyInitializationException not thrown for lazy loaded Team.teamMembers");
        } catch(LazyInitializationException lie) {

            if(!lie.getMessage().equals(LIE_TEAMMEMBERS_NOSESSION)) {
                Assert.fail("LazyInitializationException thrown, but not : " + LIE_TEAMMEMBERS_NOSESSION);
            }
        }

    }

    @Test
    @DataSet
    @Transactional(TransactionMode.DISABLED)
    public void testNonTransactionalMergeForExistingTaxon() {
        // this method tests the updating of a 'truely' detached object
        // using merge

        // setting the TransactionMode for this method to DISABLED is important
        // to ensure that transaction boundaries remain at the service layer calls
        // to simulate detachment and update of the persisted object

        UUID taxonUuid = UUID.fromString("23c35977-01b5-452c-9225-ecce440034e0");

        // ---- loading taxon with find (uuid) ----

        Taxon taxon = (Taxon)taxonService.find(taxonUuid);

        // at this point the taxonNew object is detached and all lazy loaded proxy
        // objects in the object graph (including teamMembers) will have values of
        // initialized=false and session=null

        taxonService.merge(taxon);

        // ---- loading taxon with find (id) ----

        taxon = (Taxon)commonService.find(taxon.getClass(), taxon.getId());

        taxonService.merge(taxon);

        // ---- loading taxon with findTaxonByUuid ----

        List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String[] {
                "name"
        });

        // loading the taxon with its taxon name object pre-initialized
        taxon = (Taxon)taxonService.findTaxonByUuid(taxonUuid, TAXON_INIT_STRATEGY);

        // at this point the taxonNew object is detached and all lazy loaded proxy
        // objects in the object graph (including teamMembers) will have values of
        // initialized=false and session=null

        INonViralName nvn = CdmBase.deproxy(taxon.getName());

        // normally this call should throw a lazy loading exception since
        // the combinationAuthorship object is not initialized, but
        // the findTaxonByUuid performs this initialization (via the
        // AdvancedBeanInitializer) since the combinationAuthorship
        // is annotated with CacheUpdate

        Team team = CdmBase.deproxy(nvn.getCombinationAuthorship(),Team.class);

        // setting the protected title cache to false to ensure that
        // TeamDefaultCacheStrategy.getTitleCache is called, which in turn tries
        // to initialize the teamMembers persistent collection which fails
        team.setProtectedTitleCache(false);


        taxonService.merge(taxon);

    }


    @Test
    public final void testTaxonDescriptionMerge() {

        IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(null, "Abies alba", null, null, null, null, null, null, null);
        Taxon taxon = Taxon.NewInstance(name, null);
        TaxonDescription description = TaxonDescription.NewInstance(taxon);

        TextData textData = TextData.NewInstance();

        textData.setFeature(Feature.ECOLOGY());
        description.addElement(textData);

        DescriptionElementSource descriptionElementSource = DescriptionElementSource.NewInstance(OriginalSourceType.PrimaryTaxonomicSource);

        textData.addSource(descriptionElementSource);

        taxonService.merge(taxon);
    }

    @Test  //testing of bidirectionality of supplemental data #5743
    public final void testReferenceWithAnnotationMerge() {

        Reference ref = ReferenceFactory.newBook();

        ref.addAnnotation(Annotation.NewDefaultLanguageInstance("ref"));

        referenceService.merge(ref);
    }

    @Test //testing of bidirectionality of supplemental data #5743
    public final void testAnnotationMerge() {

        Reference ref = ReferenceFactory.newBook();

        Annotation annotation = Annotation.NewDefaultLanguageInstance("anno");
        ref.addAnnotation(annotation);

        annotationService.merge(annotation);
    }
}
