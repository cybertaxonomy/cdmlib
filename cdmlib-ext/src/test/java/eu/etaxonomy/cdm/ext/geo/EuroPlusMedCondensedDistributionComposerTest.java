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
import org.junit.Test;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 15.06.2016
 */
public class EuroPlusMedCondensedDistributionComposerTest extends TermTestBase {

    @Test
    public void testGetCondensedDistribution() {

        //Germany
        NamedArea germany = NamedArea.NewInstance("Germany", "", "GER");
        NamedArea berlin = NamedArea.NewInstance("Berlin", "", "GER(B)");
        berlin.setPartOf(germany);
        NamedArea bawue = NamedArea.NewInstance("Baden Württemberg", "", "GER(BW)");
        bawue.setPartOf(germany);
        //France
        NamedArea france = NamedArea.NewInstance("France", "", "FR");
        NamedArea ileDeFrance = NamedArea.NewInstance("Ile-de-France", "", "FR(J)");
        ileDeFrance.setPartOf(france);
        //Italy
        NamedArea italy = NamedArea.NewInstance("Italy", "", "IT");
        //Spain
        NamedArea spain = NamedArea.NewInstance("Spain", "", "S");

        Set<Distribution> distributions = new HashSet<>();
        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE()));
        distributions.add(Distribution.NewInstance(bawue, PresenceAbsenceTerm.NATIVE()));
        distributions.add(Distribution.NewInstance(berlin, PresenceAbsenceTerm.NATIVE()));

        distributions.add(Distribution.NewInstance(italy, PresenceAbsenceTerm.PRESENT_DOUBTFULLY()));

        distributions.add(Distribution.NewInstance(france, PresenceAbsenceTerm.CASUAL()));
        distributions.add(Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.CULTIVATED()));

        distributions.add(Distribution.NewInstance(spain, PresenceAbsenceTerm.NATURALISED()));

        List<Language> languages = new ArrayList<>();

        CondensedDistribution condensedDistribution = EditGeoServiceUtilities.getCondensedDistribution(
                distributions,
                CondensedDistributionRecipe.EuroPlusMed,
                languages);

        Assert.assertEquals("GER(B BW) ?IT [aFR cFR(J) nS]", condensedDistribution.toString());
    }
}