/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.kohlbecker
 * @since Jan 27, 2014
 */
public class DescriptionUtilityTest extends TermTestBase {

    private Collection<Distribution> distributions = null;
    private Collection<Distribution> filteredDistributions = null;
    private boolean subAreaPreference = false;
    private boolean statusOrderPreference = false;
    private Set<MarkerType> hideMarkedAreas = null;
    private NamedArea berlin = null;

    @Before
    public void setup(){
        distributions = new ArrayList<>();

        berlin = NamedArea.NewInstance("Berlin", "Berlin", "BER");
        berlin.setPartOf(Country.GERMANY());
    }

    @Test
    public void testFilterDistributions_aggregated(){

        /* 1.
         * Aggregated elements are preferred over entered or imported elements
         * if the according flag ist set to true.
         * (Aggregated description elements are identified by belonging to descriptions
         * which have the type DescriptionType#AGGREGATED_DISTRIBUTION).
         * This means if a non-aggregated status information exist for the same
         * area for which aggregated data is available, the aggregated data has to be
         * given preference over other data.
         * Note by AM: be aware that according to #5050 the preference of aggregated
         * distributions is not valid anymore (for the E+M usecase). However, the functionality
         * might be interesting for future use-cases.
         */
        TaxonDescription aggregatedDescription = TaxonDescription.NewInstance();
        aggregatedDescription.addType(DescriptionType.AGGREGATED_DISTRIBUTION);

        Distribution germanyNative = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.NATIVE());
        distributions.add(germanyNative);

        Distribution germanyIntroduced = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.INTRODUCED());
        aggregatedDescription.addElement(germanyIntroduced);

        distributions.add(germanyIntroduced);

        statusOrderPreference= true;
        boolean preferAggregated = true;
        filteredDistributions = DescriptionUtility.filterDistributions(distributions, hideMarkedAreas, preferAggregated, statusOrderPreference, subAreaPreference, false, false);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals("expecting to see computed status INTRODUCED even it has lower preference than NATIVE", PresenceAbsenceTerm.INTRODUCED(), filteredDistributions.iterator().next().getStatus());

       /* distributions for parent areas are only
        * removed if direct sub areas have the same status and if subAreaPreference=TRUE which is not the case here
        */
        TaxonDescription aggParentDescription = TaxonDescription.NewInstance();
        aggParentDescription.addType(DescriptionType.AGGREGATED_DISTRIBUTION);

        Distribution parentComputedDistribution = Distribution.NewInstance(berlin, PresenceAbsenceTerm.INTRODUCED());
        aggParentDescription.addElement(parentComputedDistribution);
        distributions.add(parentComputedDistribution);

        filteredDistributions = DescriptionUtility.filterDistributions(
                distributions, hideMarkedAreas, preferAggregated, statusOrderPreference, subAreaPreference, true, false);
        Assert.assertEquals(2, filteredDistributions.size());
    }

    @Test
    public void testFilterDistributions_statusOrderPreference(){
        statusOrderPreference = true;

        /*
         * Status order preference rule: In case of multiple distribution status
         * (PresenceAbsenceTermBase) for the same area the status with the
         * highest order is preferred, see
         * OrderedTermBase.compareTo(OrderedTermBase)
         */
        distributions.add(Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.NATIVE()));
        distributions.add(Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.INTRODUCED()));
        filteredDistributions = DescriptionUtility.filterDistributions(
                distributions, hideMarkedAreas, false, statusOrderPreference, subAreaPreference, true, false);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(PresenceAbsenceTerm.NATIVE(), filteredDistributions.iterator().next().getStatus());
    }

    @Test
    public void testFilterDistributions_subAreaPreference(){
        subAreaPreference = true;
        boolean preferAggregated = false;

        /*
         * Sub area preference rule: If there is an area with a direct sub area
         * and both areas have the same status only the information on
         * the sub area should be reported, whereas the super area should be
         * ignored.
         * TODO Note by AM:  to me this test is unclear, there seems to be no difference between
         * "no", "mixed" and "all". From what I saw in the code the "preferAggregated" rule
         * works only on the exact same area so as we use Germany versus Berlin here it may not
         * have any influence and the last 2 tests could be deleted.
         * NOTE2: From now on the marker computed on distributions has no effect anymore.
         * Computed (or better Aggregated_Distribution) can only be defined on description
         * level not on description element level. But this change had no effect on this test
         * so also from this perspective the 2 "Computed" tests can be deleted.
         */
        Distribution distGermany = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.NATIVE());
        Distribution distBerlin = Distribution.NewInstance(berlin, PresenceAbsenceTerm.NATIVE());

        // no computed data
        distributions.add(distGermany);
        distributions.add(distBerlin);
        filteredDistributions = DescriptionUtility.filterDistributions(distributions,
                hideMarkedAreas, preferAggregated, statusOrderPreference, subAreaPreference, true, false);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(berlin, filteredDistributions.iterator().next().getArea());

        // mixed situation
        distGermany.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
        filteredDistributions = DescriptionUtility.filterDistributions(distributions,
                hideMarkedAreas, preferAggregated, statusOrderPreference, subAreaPreference, true, false);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(berlin, filteredDistributions.iterator().next().getArea());

        // all computed
        distBerlin.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
        filteredDistributions = DescriptionUtility.filterDistributions(distributions,
                hideMarkedAreas, preferAggregated, statusOrderPreference, subAreaPreference, true, false);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(berlin, filteredDistributions.iterator().next().getArea());
    }

    @Test
    public void testFilterDistributions_markedAreaFilter(){
        /*
         * Marked area filter: Skip distributions where the area has a Marker
         * with one of the specified MarkerTypes
         */
        NamedArea germany = NamedArea.NewInstance("Germany", "Germany", "GER");
        NamedArea france = NamedArea.NewInstance("France", "France", "FRA");
        NamedArea belgium = NamedArea.NewInstance("Belgium", "Belgium", "BEL");

        Distribution distGermany = Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE());
        Distribution distFrance = Distribution.NewInstance(france, PresenceAbsenceTerm.NATIVE());
        Distribution distBelgium = Distribution.NewInstance(belgium, PresenceAbsenceTerm.NATIVE());
        distributions.add(distGermany);
        distributions.add(distFrance);
        distributions.add(distBelgium);

        belgium.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
        france.addMarker(Marker.NewInstance(MarkerType.IMPORTED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        hideMarkedAreas.add(MarkerType.IMPORTED());

        filteredDistributions = DescriptionUtility.filterDistributions(distributions, hideMarkedAreas, false,
                statusOrderPreference, subAreaPreference, true, false);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(germany, filteredDistributions.iterator().next().getArea());
    }

    @Test
    public void testFilterDistributions_fallbackArea_hidden(){

        boolean preferAggregated = false;

        NamedArea jugoslavia = NamedArea.NewInstance("Former Yugoslavia ", "", "Ju");
        jugoslavia.setIdInVocabulary("Ju");
        NamedArea serbia = NamedArea.NewInstance("Serbia", "", "Sr");
        serbia.setIdInVocabulary("Sr");
        jugoslavia.addIncludes(serbia);

        Distribution distJugoslavia = Distribution.NewInstance(jugoslavia, PresenceAbsenceTerm.NATIVE());
        Distribution distSerbia = Distribution.NewInstance(serbia, PresenceAbsenceTerm.NATIVE());

        distributions.add(distSerbia);
        distributions.add(distJugoslavia);

        // using TO_BE_CHECKED to mark Ju as fallback area
        jugoslavia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());

        filteredDistributions = DescriptionUtility.filterDistributions(
                distributions,
                hideMarkedAreas,
                preferAggregated,
                statusOrderPreference,
                subAreaPreference,
                true, false);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(serbia, filteredDistributions.iterator().next().getArea());
    }

    @Test
    public void testFilterDistributions_fallbackArea_shown_1(){

        boolean preferAggregated = false;

        NamedArea jugoslavia = NamedArea.NewInstance("Former Yugoslavia ", "", "Ju");
        jugoslavia.setIdInVocabulary("Ju");
        NamedArea serbia = NamedArea.NewInstance("Serbia", "", "Sr");
        serbia.setIdInVocabulary("Sr");
        jugoslavia.addIncludes(serbia);

        Distribution distJugoslavia = Distribution.NewInstance(jugoslavia, PresenceAbsenceTerm.NATIVE());
        Distribution distSerbia = Distribution.NewInstance(serbia, PresenceAbsenceTerm.NATIVE());

        distributions.add(distSerbia);
        distributions.add(distJugoslavia);

        // using TO_BE_CHECKED to mark Ju as fallback area
        jugoslavia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
        // this hides serbia so jugoslavia does not become fallback but is still hidden area
        serbia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());

        filteredDistributions = DescriptionUtility.filterDistributions(
                distributions,
                hideMarkedAreas,
                preferAggregated,
                statusOrderPreference,
                subAreaPreference,
                true, false);

        Assert.assertEquals(0, filteredDistributions.size());
//        Assert.assertEquals(jugoslavia, filteredDistributions.iterator().next().getArea());
    }

    @Test
    public void testFilterDistributions_fallbackArea_shown_2(){

        NamedArea jugoslavia = NamedArea.NewInstance("Former Yugoslavia ", "", "Ju");
        jugoslavia.setIdInVocabulary("Ju");
        NamedArea serbia = NamedArea.NewInstance("Serbia", "", "Sr");
        serbia.setIdInVocabulary("Sr");
        jugoslavia.addIncludes(serbia);

        Distribution distJugoslavia = Distribution.NewInstance(jugoslavia, PresenceAbsenceTerm.NATIVE());
        distributions.add(distJugoslavia);
        // no Distribution for any of the sub areas of jugoslavia, so it should be shown

        // using TO_BE_CHECKED to mark Ju as fallback area
        jugoslavia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());

        filteredDistributions = DescriptionUtility.filterDistributions(
                distributions,
                hideMarkedAreas,
                false,
                statusOrderPreference,
                subAreaPreference,
                true, false);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(jugoslavia, filteredDistributions.iterator().next().getArea());
    }
}
