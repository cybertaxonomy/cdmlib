/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;

/**
 * @author a.mueller
 * @since 29.11.2018
 */
public class PreferenceResolverTest {

    private CdmPreference pref1;
    private CdmPreference pref2;
    private CdmPreference pref3;
    private CdmPreference pref3b;
    private CdmPreference pref4;
    private PrefKey key;

    @BeforeEach
    public void setUp() throws Exception {
        pref1 = CdmPreference.NewInstance(PreferenceSubject.NewInstance("/taxeditor/distributionEditor/"),
                PreferencePredicate.AvailableDistributionAreaVocabularies,
                "abc");
        pref2 = CdmPreference.NewInstance(PreferenceSubject.NewInstance("/"),
                PreferencePredicate.AvailableDistributionAreaVocabularies,
                "abc");
        pref3 = CdmPreference.NewInstance(PreferenceSubject.NewInstance("/distributionEditor/"),
                PreferencePredicate.AvailableDistributionAreaVocabularies,
                "abc");
        //same as pref3 for testing exception handling
        pref3b = CdmPreference.NewInstance(PreferenceSubject.NewInstance("/distributionEditor/"),
                PreferencePredicate.AvailableDistributionAreaVocabularies,
                "def");
        pref4 = CdmPreference.NewInstance(PreferenceSubject.NewInstance("/vaadin/distributionEditor/areas/"),
                PreferencePredicate.AvailableDistributionAreaVocabularies,
                "abc");
        key = CdmPreference.NewKey(PreferenceSubject.NewInstance("/taxeditor/distributionEditor/areas/"),
                PreferencePredicate.AvailableDistributionAreaVocabularies);
    }

    @Test
    public void test() {
        List<CdmPreference> list = Arrays.asList(new CdmPreference[]{pref2, pref1, pref3, pref4}) ;
        CdmPreference result = PreferenceResolver.resolve(list, key);
        assertSame(pref1, result);

        list = Arrays.asList(new CdmPreference[]{pref2, pref3, pref4}) ;
        result = PreferenceResolver.resolve(list, key);
        assertSame(pref3, result);
    }

    //test exception for >1 preferences with equally matching keys
    @Test
    public void testException() {
        List<CdmPreference> list;
        CdmPreference result;
        //assure pref3 is best matching
        list = Arrays.asList(new CdmPreference[]{pref2, pref3, pref4}) ;
        result = PreferenceResolver.resolve(list, key);
        assertSame(pref3, result);

        list = Arrays.asList(new CdmPreference[]{pref2, pref3, pref3b, pref4}) ;
        try {
            result = PreferenceResolver.resolve(list, key);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(PreferenceResolver.MULTI_BEST_MATCHING, e.getMessage());
        }
    }
}
