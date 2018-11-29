/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;

/**
 * @author a.mueller
 * @since 29.11.2018
 *
 */
public class PreferenceResolverTest {

    CdmPreference pref1;
    CdmPreference pref2;
    CdmPreference pref3;
    CdmPreference pref3b;
    CdmPreference pref4;
    PrefKey key;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        pref1 = CdmPreference.NewInstance(PreferenceSubject.NewInstance("/taxeditor/distributionEditor/"),
                PreferencePredicate.AvailableDistributionAreaTerms,
                "abc");
        pref2 = CdmPreference.NewInstance(PreferenceSubject.NewInstance("/"),
                PreferencePredicate.AvailableDistributionAreaTerms,
                "abc");
        pref3 = CdmPreference.NewInstance(PreferenceSubject.NewInstance("/distributionEditor/"),
                PreferencePredicate.AvailableDistributionAreaTerms,
                "abc");
        pref3b = CdmPreference.NewInstance(PreferenceSubject.NewInstance("/distributionEditor/"),
                PreferencePredicate.AvailableDistributionAreaTerms,
                "def");
        pref4 = CdmPreference.NewInstance(PreferenceSubject.NewInstance("/vaadin/distributionEditor/areas/"),
                PreferencePredicate.AvailableDistributionAreaTerms,
                "abc");
        key = CdmPreference.NewKey(PreferenceSubject.NewInstance("/taxeditor/distributionEditor/areas/"),
                PreferencePredicate.AvailableDistributionAreaTerms);
    }

    @Test
    public void test() {
        List<CdmPreference> list = Arrays.asList(new CdmPreference[]{pref2, pref1, pref3, pref4}) ;
        CdmPreference result = PreferenceResolver.resolve(list, key);
        Assert.assertSame(pref1, result);

        list = Arrays.asList(new CdmPreference[]{pref2, pref3, pref4}) ;
        result = PreferenceResolver.resolve(list, key);
        Assert.assertSame(pref3, result);
    }

    @Test
    public void testException() {
        List<CdmPreference> list;
        CdmPreference result;
        //assure pref3 is best matching
        list = Arrays.asList(new CdmPreference[]{pref2, pref3, pref4}) ;
        result = PreferenceResolver.resolve(list, key);
        Assert.assertSame(pref3, result);

        list = Arrays.asList(new CdmPreference[]{pref2, pref3, pref3b, pref4}) ;
        try {
            result = PreferenceResolver.resolve(list, key);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(PreferenceResolver.MULTI_BEST_MATCHING, e.getMessage());
        }
    }



}
