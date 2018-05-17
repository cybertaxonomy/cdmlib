/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.kohlbecker
 * @since Jan 27, 2014
 *
 */
public class DescriptionUtilityTest extends CdmTransactionalIntegrationTest {


    @SpringBeanByType
    private ITermService termService;

    Collection<Distribution> distributions = null;
    Collection<Distribution> filteredDistributions = null;
    boolean subAreaPreference = false;
    boolean statusOrderPreference = false;
    Set<MarkerType> hideMarkedAreas = null;
    MarkerType fallbackAreaMarkerType = null;
    NamedArea berlin = null;

    @Before
    public void setup(){
        distributions = new ArrayList<Distribution>();

        berlin = NamedArea.NewInstance("Berlin", "Berlin", "BER");
        berlin.setPartOf(Country.GERMANY());
        termService.saveOrUpdate(berlin);
    }

    @Test
    public void testFilterDistributions_computed(){

        /* 1.
         * Computed elements are preferred over entered or imported elements.
         * (Computed description elements are identified by the
         * MarkerType.COMPUTED()). This means if a entered or imported status
         * information exist for the same area for which computed data is
         * available, the computed data has to be given preference over other
         * data.
         */
        distributions.add(Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.NATIVE()));

        Distribution computedDistribution = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.INTRODUCED());
        computedDistribution.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
        distributions.add(computedDistribution);

        statusOrderPreference= true;
        filteredDistributions = DescriptionUtility.filterDistributions(distributions, hideMarkedAreas, true, statusOrderPreference, subAreaPreference);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals("expecting to see computed status INTRODUCED even it has lower preference than NATIVE", PresenceAbsenceTerm.INTRODUCED(), filteredDistributions.iterator().next().getStatus());

       /* distributions for parent areas are only
        * removed if direct sub areas have the same status and if subAreaPreference=TRUE which is not the case here
        */
        Distribution parentComputedDistribution = Distribution.NewInstance(berlin, PresenceAbsenceTerm.INTRODUCED());
        parentComputedDistribution.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
        distributions.add(parentComputedDistribution);

        filteredDistributions = DescriptionUtility.filterDistributions(distributions, hideMarkedAreas, true, statusOrderPreference, subAreaPreference);
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
        filteredDistributions = DescriptionUtility.filterDistributions(distributions, hideMarkedAreas, true, statusOrderPreference, subAreaPreference);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(PresenceAbsenceTerm.NATIVE(), filteredDistributions.iterator().next().getStatus());
    }


    @Test
    public void testFilterDistributions_subAreaPreference(){
        subAreaPreference = true;

        /*
         * Sub area preference rule: If there is an area with a direct sub area
         * and both areas have the same status only the information on
         * the sub area should be reported, whereas the super area should be
         * ignored.
         */
        Distribution distGermany = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.NATIVE());
        Distribution distBerlin = Distribution.NewInstance(berlin, PresenceAbsenceTerm.NATIVE());

        // no computed data
        distributions.add(distGermany);
        distributions.add(distBerlin);
        filteredDistributions = DescriptionUtility.filterDistributions(distributions, hideMarkedAreas, true, statusOrderPreference, subAreaPreference);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(berlin, filteredDistributions.iterator().next().getArea());

        // mixed situation
        distGermany.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
        filteredDistributions = DescriptionUtility.filterDistributions(distributions, hideMarkedAreas, true, statusOrderPreference, subAreaPreference);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(berlin, filteredDistributions.iterator().next().getArea());

        // all computed
        distBerlin.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
        filteredDistributions = DescriptionUtility.filterDistributions(distributions, hideMarkedAreas, true, statusOrderPreference, subAreaPreference);
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

        hideMarkedAreas = new HashSet<MarkerType>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        hideMarkedAreas.add(MarkerType.IMPORTED());

        filteredDistributions = DescriptionUtility.filterDistributions(distributions, hideMarkedAreas, true, statusOrderPreference, subAreaPreference);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(germany, filteredDistributions.iterator().next().getArea());
    }

    @Test
    public void testFilterDistributions_fallbackArea_hidden(){

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

        hideMarkedAreas = new HashSet<MarkerType>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());

        filteredDistributions = DescriptionUtility.filterDistributions(
                distributions,
                hideMarkedAreas,
                true,
                statusOrderPreference,
                subAreaPreference);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(serbia, filteredDistributions.iterator().next().getArea());
    }

    @Test
    public void testFilterDistributions_fallbackArea_shown_1(){

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
        // this hides serbia so jugoslavia should be shown
        serbia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        hideMarkedAreas = new HashSet<MarkerType>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());

        filteredDistributions = DescriptionUtility.filterDistributions(
                distributions,
                hideMarkedAreas,
                true,
                statusOrderPreference,
                subAreaPreference);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(jugoslavia, filteredDistributions.iterator().next().getArea());
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

        hideMarkedAreas = new HashSet<MarkerType>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());

        filteredDistributions = DescriptionUtility.filterDistributions(
                distributions,
                hideMarkedAreas,
                true,
                statusOrderPreference,
                subAreaPreference);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(jugoslavia, filteredDistributions.iterator().next().getArea());
    }



    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
