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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * This test class tries to describe how hibernate works with objects (save, update, merge).
 *
 * Note by AM: Therefore this is not a real product test as a failing test does not necessarily
 *             indicate that something goes wrong in cdmlib itself. However, it indicates that something
 *             has changed in the hibernate behaviour or in the model behaviour.
 *
 * @author cmathew
 * @since 17 Sep 2014
 */
public class HandlingCdmEntitiesTest extends CdmServicesTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private static final String LIE_TEAMMEMBERS_NOSESSION = "failed to lazily initialize a collection of role: eu.etaxonomy.cdm.model.agent.Team.teamMembers, could not initialize proxy - no Session";
    /** Hibernate 6: "Could not initialize proxy [Type#id] - no session" (was: "could not initialize proxy - no Session") */
    private static final String LIE_NOSESSION = "(?i)could not initialize proxy .* no session";

    private static final UUID taxonUuid = UUID.fromString("23c35977-01b5-452c-9225-ecce440034e0");

    @Autowired
    private IReferenceService referenceService;

    @Autowired
    private IAnnotationService annotationService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IAgentService agentService;

    @Autowired
    private ICommonService commonService;

    public static final String[] includeTables = new String[]{
        "TAXONBASE",
        "TAXONNAME",
        "AGENTBASE",
        "AGENTBASE_AGENTBASE",
        "HOMOTYPICALGROUP"
    };

    @Override
    public final void createTestDataSet() {

        Team combAuthor = Team.NewTitledInstance("Avengers", "Avengers");
        combAuthor.addTeamMember(Person.NewTitledInstance("Iron Man"));
        IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(null, "Abies alba", null, null, null, null, null, null, null);
        name.setCombinationAuthorship(combAuthor);
        Taxon taxon = Taxon.NewInstance(name, null);
        taxonService.save(taxon).getUuid();
        printDataSet(System.out,false,null,includeTables);
    }

    @Test
    @CdmDataSet
    public void testNonTransactionalMergeForExistingTaxon() {
        // this method tests the updating of a 'truely' detached object
        // using merge
        endTransaction();

        // ---- loading taxon with find (uuid) ----

        Taxon taxon = (Taxon)taxonService.find(taxonUuid);

        // at this point the taxonNew object is detached and all lazy loaded proxy
        // objects in the object graph (including teamMembers) will have values of
        // initialized=false and session=null

        taxonService.merge(taxon);

        // ---- loading taxon with find (id) ----

        taxon = commonService.find(taxon.getClass(), taxon.getId());

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
