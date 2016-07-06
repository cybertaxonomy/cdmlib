// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.mueller
 * @date 15.06.2016
 *
 */
public class EuroPlusMedCondensedDistributionComposerTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DefaultTermInitializer initializer = new DefaultTermInitializer();
        initializer.initialize();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void testGetCondensedDistribution() {

        NamedArea germany = NamedArea.NewInstance("Germany", "", "GER");
        NamedArea berlin = NamedArea.NewInstance("Berlin", "", "GER(B)");
        berlin.setPartOf(germany);
        NamedArea bawue = NamedArea.NewInstance("Baden Württemberg", "", "GER(BW)");
        bawue.setPartOf(germany);
        NamedArea france = NamedArea.NewInstance("France", "", "FR");
        NamedArea ileDeFrance = NamedArea.NewInstance("Ile-de-France", "", "FR(J)");
        ileDeFrance.setPartOf(france);
        NamedArea italy = NamedArea.NewInstance("Italy", "", "IT");
        NamedArea spain = NamedArea.NewInstance("Spain", "", "S");

        Set<Distribution> distributions = new HashSet<Distribution>();
        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE()));
        distributions.add(Distribution.NewInstance(bawue, PresenceAbsenceTerm.NATIVE()));
        distributions.add(Distribution.NewInstance(berlin, PresenceAbsenceTerm.NATIVE()));
        distributions.add(Distribution.NewInstance(italy, PresenceAbsenceTerm.PRESENT_DOUBTFULLY()));

        distributions.add(Distribution.NewInstance(france, PresenceAbsenceTerm.INTRODUCED_ADVENTITIOUS()));
        distributions.add(Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.CULTIVATED()));
        distributions.add(Distribution.NewInstance(spain, PresenceAbsenceTerm.NATURALISED()));

        List<Language> languages = new ArrayList<Language>();

        CondensedDistribution condensedDistribution = EditGeoServiceUtilities.getCondensedDistribution(
                distributions,
                CondensedDistributionRecipe.EuroPlusMed,
                languages);

        Assert.assertEquals("GER(B BW) ?IT [aFR cFR(J) nS]", condensedDistribution.toString());
    }

}
