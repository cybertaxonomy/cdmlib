/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;

/**
 * @author a.mueller
 * @since 28.11.2018
 */
public class PreferenceResolver {

    protected static final String MULTI_BEST_MATCHING = "There are 2 best matching preferences with equal key but differing values";

    /**
     * Returns the best matching {@link CdmPreference preference} for the
     * given preference list and for the given {@link PrefKey preference key}
     * or <code>null</code> if none is matching.<BR>
     * A preference is matching if the preference key is matching the given key.
     * Keys are matching if they {@link PreferencePredicate predicates} are
     * equal and if the given {@link PreferenceSubject subject} matches
     * the preferences subject.<BR>
     * A subject A matches another subject B if all parts of A can also be found
     * in B in the same order. However, A may have parts that can not be found in B
     * but still it matches. But B must not have parts that can not be found in A
     * and all parts of B must be in the same order as in A otherwise it does not match.<BR>
     * The <b>best</b> key match is computed from the back recursively.
     * If the last part matches it matches better then matching only the second but last.
     * If !=1 keys  match the last part the best matching is computed on
     * the first n-1 parts of the key.<BR>
     *
     * If key or one of its parts is <code>null</code>, <code>null</code> is returned.
     *
     * @return the best matching preference
     * @throws IllegalArgumentException if the given preferences list contains 2 preference
     *  with completely equal best matching keys but with different values.
     */
    public static CdmPreference resolve(List<CdmPreference> preferences, PrefKey key) throws IllegalArgumentException{
        if (key == null ||key.getPredicate() == null || key.getSubject() == null){
            return null;
        }

        List<CdmPreference> matchingPreferences = new ArrayList<>();
        for (CdmPreference preference : preferences){
            if (preference == null|| preference.getKey()== null){
                continue;
            }
            if (key.getPredicate().equals(preference.getKey().getPredicate())){
                if (subjectMatches(PreferenceSubject.NewInstance(key.getSubject()), PreferenceSubject.NewInstance(preference.getKey().getSubject()))){
                    matchingPreferences.add(preference);
                }
            }
        }
        CdmPreference bestMatching = null;
        boolean multipleBestMatching = false;
        for (CdmPreference preference : matchingPreferences){
            if (bestMatching == null){
                bestMatching = preference;
                continue;
            }else{
                int c = compare(PreferenceSubject.fromPreference(preference), PreferenceSubject.fromPreference(bestMatching), PreferenceSubject.fromKey(key) );
                if (c < 0){
                    bestMatching = preference;
                    multipleBestMatching = false;
                }else if (c == 0){
                    if(!CdmUtils.nullSafeEqual(preference.getValue(), bestMatching.getValue())||
                            preference.isAllowOverride()!= bestMatching.isAllowOverride()){
                        multipleBestMatching = true;
                    }
                }else{
                    multipleBestMatching = false;
                }
            }
        }
        if (multipleBestMatching){
            throw new IllegalArgumentException(MULTI_BEST_MATCHING);
        }
        return bestMatching;
    }


    /**
     * Compares 2 subjects. Returns a value < 0, if subject1 is better matching,
     * returns a value >0 if subject2 is better matching.
     * Returns 0 if both subjects are equal.
     * @param subject1
     * @param subject2
     * @return
     */
    private static int compare(PreferenceSubject subject1, PreferenceSubject subject2,
            PreferenceSubject compareAgainst) {

        String last = compareAgainst.getLastPart();
        String last1 = subject1.getLastPart();
        String last2 = subject2.getLastPart();
        if (compareAgainst.isRoot()){
            return 0;
        }
        if (last.equals(last1)){
            if (!last.equals(last2)){
                return -1;
            }else{
                return compare(subject1.getNextHigher(), subject2.getNextHigher(), compareAgainst.getNextHigher());
            }
        }else if(last.equals(last2)){
            return 1;
        }else{
            return compare(subject1, subject2, compareAgainst.getNextHigher());
        }
    }

    /**
     * @param subject
     * @param subject2
     * @return
     */
    private static boolean subjectMatches(PreferenceSubject subjectA, PreferenceSubject subjectB) {
        List<String> partsA = subjectA.getParts();
        List<String> partsB = subjectB.getParts();
        for (int a = partsA.size()-1 ; a>=0 ; a--){
            String lastA = partsA.get(a);
            String lastB = partsB.get(partsB.size()-1);
            if (lastA.equals(lastB) && !partsB.isEmpty()){
                partsB = partsB.subList(0, partsB.size()-1);
            }
        }
        return partsB.isEmpty();
    }
}
