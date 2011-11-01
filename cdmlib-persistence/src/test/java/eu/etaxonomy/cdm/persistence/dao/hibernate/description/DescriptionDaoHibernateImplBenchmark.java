/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

@DataSet(value="DescriptionDaoHibernateImplTest.xml")
public class DescriptionDaoHibernateImplBenchmark extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    IDescriptionDao descriptionDao;

    @SpringBeanByType
    IDescriptionElementDao descriptionElementDao;

    @SpringBeanByType
    IDefinedTermDao definedTermDao;

    @SpringBeanByType
    ITaxonDao taxonDao;

    private Set<Feature> features;

    private UUID acherontia_lachesis_descriptionUuid;

    private static final int BENCHMARK_ROUNDS = 20;


    @Before
    public void setUp() {

        acherontia_lachesis_descriptionUuid = UUID.fromString("fd6cdb64-142c-4df1-b366-c5e76f08a1fc");
        features = new HashSet<Feature>();
    }




    @Test
    public void addCharacterWithStates() {

        Feature feature = Feature.UNKNOWN();

        logger.warn("feature.id=" + feature.getId());

        int numStatesToAdd = 5;

        TaxonDescription description = (TaxonDescription) descriptionDao.findByUuid(acherontia_lachesis_descriptionUuid);
        assertNotNull("searchDescriptionByDistribution must not be null", description);

        long startMillis = System.currentTimeMillis();

        for(int rnd = 0; rnd < BENCHMARK_ROUNDS; rnd++){

            CategoricalData categoricalDataElement = CategoricalData.NewInstance();
            categoricalDataElement.setFeature(feature);

            for(int std = 0 ; std < numStatesToAdd; std++){
                String stateText = String.valueOf(std);
                State state = State.NewInstance(stateText, stateText, stateText);
                StateData stateData = StateData.NewInstance();
                stateData.setState(state);
                categoricalDataElement.addState(stateData);
            }
            descriptionDao.saveOrUpdate(description);
        }

        double duration = ((double)(System.currentTimeMillis() - startMillis) ) / BENCHMARK_ROUNDS ;
        logger.info("Benchmark result - [add 1 character with " + numStatesToAdd + " states] : " + duration + "ms (" + BENCHMARK_ROUNDS +" benchmark rounds )");

    }



}
