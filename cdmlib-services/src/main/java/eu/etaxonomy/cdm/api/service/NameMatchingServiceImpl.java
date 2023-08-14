/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.NameMatchingUtils;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.name.INameMatchingDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dto.NameMatchingParts;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author andreabee90
 * @since 02.08.2023
 */
@Service
@Transactional(readOnly = true)
public class NameMatchingServiceImpl
        // extends IdentifiableServiceBase<TaxonName,ITaxonNameDao>
        implements INameMatchingService {

    @Autowired
    // @Qualifier("defaultBeanInitializer")
    protected IBeanInitializer defaultBeanInitializer;

    @Autowired
    private ITaxonNameDao nameDao;
    @Autowired
    private INameMatchingDao nameDao2;

    // ***************************** CONSTRUCTOR
    // ***********************************/

    public NameMatchingServiceImpl() {
    }

    // ********************* METHODS
    // ***********************************************/

    /*
     * This is a implementation of the Taxamatch algorithm built by Tony Rees.
     * It employs a custom Modified Damerau-Levenshtein Distance algorithm see
     * also
     * https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0107510
     */
    // TODO work in progress

    @Override
    public List<DoubleResult<NameMatchingParts, Integer>> findMatchingNames(String taxonName, Integer maxDisGenus,
            Integer maxDisEpith) {

        // Discuss. value of distance if query is only genus, if genus +
        // epithet, if genus + infrageneric + epithet...

        if (maxDisGenus == null) {
            maxDisGenus = 2;
        }

        if (maxDisEpith == null) {
            maxDisEpith = 4;
        }

        Integer maxDisInfrageneric = 4;

        // 0. Parsing and Normalizing

        // TODO? Remove all qualifiers such as cf., aff., ?, <i>, x, etc. from
        // the full input string

        TaxonName name = (TaxonName) NonViralNameParserImpl.NewInstance().parseFullName(taxonName);

        String genusQuery = name.getGenusOrUninomial();
        String epithetQuery = name.getSpecificEpithet();
        String infraGenericQuery = name.getInfraGenericEpithet();

        int genusComputedDistance = 0;
        int epithetComputedDistance = 0;
        int infragenericComputedDistance = 0;

        /*
         * phonetic normalization of query (genus) this method corresponds to
         * the near match function of Rees 2007 it includes phonetic matches
         * (replace initial characters, soundalike changes, gender endings)
         */

        String normalizedGenusQuery = NameMatchingUtils.normalize(genusQuery);
        String phoneticNormalizedGenusQuery = NameMatchingUtils.nearMatch(normalizedGenusQuery);

        // 1. Genus pre-filter
        List<String> allGeneraOrUninominalFromDB = allGeneraOrUninominalFromDB();
        List<String> preFilteredGenusOrUninominalList = prefilterGenus(genusQuery, phoneticNormalizedGenusQuery,
                allGeneraOrUninominalFromDB);

        Map<String, Integer> postFilteredGenusOrUninominalWithDis = new HashMap<>();
        for (String preFilteredGenusOrUninominal : preFilteredGenusOrUninominalList) {
            String genusNameInDBNormalized = NameMatchingUtils.normalize(preFilteredGenusOrUninominal);
            String phoneticNormalizedGenusInDB = NameMatchingUtils.nearMatch(genusNameInDBNormalized);

            // 2. comparison of genus
            genusComputedDistance = nameMatchingComputeDistance(phoneticNormalizedGenusQuery,
                    phoneticNormalizedGenusInDB);

            // 3. genus post-filter
            boolean postFilterOK = postfilterGenus(maxDisGenus, genusQuery, genusComputedDistance,
                    phoneticNormalizedGenusQuery, preFilteredGenusOrUninominal, phoneticNormalizedGenusInDB);
            if (postFilterOK) {
                postFilteredGenusOrUninominalWithDis.put(preFilteredGenusOrUninominal, genusComputedDistance);
            }
        }

        List<DoubleResult<NameMatchingParts, Integer>> genusOrUninomialWithDistance = new ArrayList<>();
        genusOrUninomialWithDistance = genusOrUninomialList(postFilteredGenusOrUninominalWithDis);

        List<DoubleResult<NameMatchingParts, Integer>> onlyGenusOrUninominalListWithDistance = new ArrayList<>();
        if (epithetQuery == null) {
            for (int i = 0; i < genusOrUninomialWithDistance.size(); i++) {
                if (genusOrUninomialWithDistance.get(i).getFirstResult().getSpecificEpithet() == null) {
                    onlyGenusOrUninominalListWithDistance.add(genusOrUninomialWithDistance.get(i));
                }
            }
            Collections.sort(onlyGenusOrUninominalListWithDistance,
                    (o1, o2) -> o1.getSecondResult().compareTo(o2.getSecondResult()));
            List<DoubleResult<NameMatchingParts, Integer>> exactResults = exactResults(
                    onlyGenusOrUninominalListWithDistance);
            List<DoubleResult<NameMatchingParts, Integer>> bestResults = bestResults(
                    onlyGenusOrUninominalListWithDistance);

            if (!exactResults.isEmpty()) {
                return exactResults;
            } else {
                return bestResults;
            }
        } else {

            String normalizedEphitetQuery = NameMatchingUtils.normalize(epithetQuery);
            String phoneticNormalizedEpithetQuery = NameMatchingUtils.nearMatch(normalizedEphitetQuery);

            // 4. epithet pre-filter

            List<DoubleResult<NameMatchingParts, Integer>> binomialListWithDistTemp = new ArrayList<>();
            binomialListWithDistTemp = prefilterEpithet(genusOrUninomialWithDistance, normalizedEphitetQuery);

            // -----------------CONTINUE HERE----------------

            List<DoubleResult<NameMatchingParts, Integer>> binomialListWithDist = new ArrayList<>();
            for (DoubleResult<NameMatchingParts, Integer> binomial : binomialListWithDistTemp) {
                String epithetInDB = binomial.getFirstResult().getSpecificEpithet();
                if (epithetInDB.isEmpty()) {
                    continue;
                }
                String epithetNameInDBNormalized = NameMatchingUtils.normalize(epithetInDB);
                String phoneticNormalizedEpithetNameInDB = NameMatchingUtils.nearMatch(epithetNameInDBNormalized);

                // 5. comparison of epithet
                epithetComputedDistance = nameMatchingComputeDistance(phoneticNormalizedEpithetQuery,
                        phoneticNormalizedEpithetNameInDB);
                int totalDist = binomial.getSecondResult() + epithetComputedDistance;
                binomial.setSecondResult(totalDist);

                // 6. species post-filter
                binomialListWithDist = postfilterEpithet(maxDisEpith, epithetQuery, epithetComputedDistance,
                        normalizedEphitetQuery, binomialListWithDistTemp, binomial, epithetInDB, totalDist);
            }

            // 7. Result shaping

            Collections.sort(binomialListWithDist, (o1, o2) -> o1.getSecondResult().compareTo(o2.getSecondResult()));
            List<DoubleResult<NameMatchingParts, Integer>> exactResults = exactResults(binomialListWithDist);
            List<DoubleResult<NameMatchingParts, Integer>> bestResults = bestResults(binomialListWithDist);

            if (!exactResults.isEmpty()) {
                return exactResults;
            } else {
                return bestResults;
            }

            // 6b Infraspecific comparison (pre-filter, comparison, post-filter)
            // TODO

            // if (infraGenericQuery == null) {
            //
            // // 7. Result shaping
            //
            // Collections.sort(epithetList, (o1,o2) ->
            // o1.getSecondResult().compareTo(o2.getSecondResult()) );
            // List <DoubleResult<NameMatchingParts, Integer>> exactResults =
            // exactResults(epithetList);
            // List <DoubleResult<NameMatchingParts, Integer>> bestResults =
            // bestResults(epithetList);
            //
            // if(!exactResults.isEmpty()) {
            // return exactResults;
            // } else {
            // return bestResults;
            // }
            // } else {
            //
            // String normalizedInfragenericQuery =
            // NameMatchingUtils.normalize(infraGenericQuery);
            // String phoneticNormalizedInfragenericQuery =
            // NameMatchingUtils.nearMatch(normalizedInfragenericQuery);
            //
            // fullNameMatchingPartsListWithDistance =
            // prefilterInfrageneric(fullNameMatchingPartsListWithDistance,
            // normalizedInfragenericQuery, maxDisInfrageneric);
            // List <DoubleResult<NameMatchingParts, Integer>> infragenericList
            // = new ArrayList<>();
            // for (DoubleResult<NameMatchingParts, Integer> fullTaxonNamePart:
            // fullNameMatchingPartsListWithDistance) {
            // String infragenericInDB =
            // fullTaxonNamePart.getFirstResult().getInfraGenericEpithet();
            // if (infragenericInDB.isEmpty()) {
            // continue;
            // }
            // String infragenericNameInDBNormalized =
            // NameMatchingUtils.normalize(infragenericInDB);
            // String phoneticNormalizedInfragenericNameInDB =
            // NameMatchingUtils.nearMatch(infragenericNameInDBNormalized);
            //
            // // 5. comparison of infrageneric
            // infragenericComputedDistance =
            // nameMatchingComputeDistance(phoneticNormalizedInfragenericQuery,
            // phoneticNormalizedInfragenericNameInDB);
            // int totalDist = fullTaxonNamePart.getSecondResult() +
            // infragenericComputedDistance;
            // fullTaxonNamePart.setSecondResult(totalDist) ;
            //
            // // 6. infrageneric post-filter
            // postfilterInfrageneric(maxDisInfrageneric, infraGenericQuery,
            // infragenericComputedDistance, normalizedInfragenericQuery,
            // infragenericList, fullTaxonNamePart,
            // infragenericInDB, totalDist);
            // }
            // Collections.sort(infragenericList, (o1,o2) ->
            // o1.getSecondResult().compareTo(o2.getSecondResult()));
            // List <DoubleResult<NameMatchingParts, Integer>> exactResults =
            // exactResults(infragenericList);
            // List <DoubleResult<NameMatchingParts, Integer>> bestResults =
            // bestResults(infragenericList);
            //// List <DoubleResult<NameMatchingParts, Integer>>
            // titelCacheResults = new ArrayList<>();
            //// titelCacheResults =
            // titleCacheResults(postFilteredGenusOrUninominalWithDis);
            //
            // if(!exactResults.isEmpty()) {
            // // TODO fetch full titelcache for all postfilteres
            // return exactResults;
            // } else {
            // // TODO fetch full titelcache for all postfilteres
            // return bestResults;
            // }
            // }
        }
    }

    private List<DoubleResult<NameMatchingParts, Integer>> genusOrUninomialList(
            Map<String, Integer> postFilteredGenusOrUninominalWithDis) {
        List<DoubleResult<NameMatchingParts, Integer>> genusOrUninomialWithDistance = new ArrayList<>();
        postFilteredGenusOrUninominalWithDis.forEach((key, value) -> {
            List<NameMatchingParts> fullNameMatchingPartsListTemp = nameDao2
                    .findNameMatchingParts(postFilteredGenusOrUninominalWithDis);
            for (NameMatchingParts fullNameMatchingParts : fullNameMatchingPartsListTemp) {
                // if (fullNameMatchingParts.getSpecificEpithet() == null) {
                genusOrUninomialWithDistance
                        .add(new DoubleResult<NameMatchingParts, Integer>(fullNameMatchingParts, value));
                // }
            }
        });
        return genusOrUninomialWithDistance;
    }

    /**
     * Deletes common characters at the beginning and end of both parameters.
     * Returns the space separated concatenation of the remaining strings. <BR>
     * Returns empty string if input strings are equal.
     */
    public static String trimCommonChar(String queryName, String dbName) {

        String shortenedQueryName = "";
        String shortenedDBName = "";
        String tempQueryName;
        String tempDBName;
        // trim common leading characters of query and document

        int queryNameLength = queryName.length();
        int dbNameLength = dbName.length();
        int largestString = Math.max(queryNameLength, dbNameLength);
        int i;

        for (i = 0; i < largestString; i++) {
            if (i >= queryNameLength || i >= dbNameLength || queryName.charAt(i) != dbName.charAt(i)) {
                // Stop iterating when the characters at the current position
                // are not equal.
                break;
            }
        }

        // Create temp names with common leading characters removed.

        tempQueryName = queryName.substring(i);
        tempDBName = dbName.substring(i);

        // trim common tailing characters between query and document

        int restantQueryNameLenght = tempQueryName.length();
        int restantDBNameLenght = tempDBName.length();
        int shortestString = Math.min(restantQueryNameLenght, restantDBNameLenght);
        int x;
        for (x = 0; x < shortestString; x++) {
            if (tempQueryName.charAt(restantQueryNameLenght - x - 1) != tempDBName
                    .charAt(restantDBNameLenght - x - 1)) {
                break;
            }
        }
        shortenedQueryName = tempQueryName.substring(0, restantQueryNameLenght - x);
        shortenedDBName = tempDBName.substring(0, restantDBNameLenght - x);

        if (shortenedQueryName.equals(shortenedDBName)) {
            return "";
        } else {
            return shortenedQueryName + " " + shortenedDBName;
        }
    }

    private int nameMatchingComputeDistance(String strQuery, String strDB) {
        int computedDistanceTemp;
        String trimmedStrings = trimCommonChar(strQuery, strDB);

        if ("".equals(trimmedStrings)) {
            computedDistanceTemp = 0;
        } else {
            String restantTrimmedQuery = trimmedStrings.split(" ")[0];
            String restantTrimmedDB = trimmedStrings.split(" ")[1];
            computedDistanceTemp = NameMatchingUtils.modifiedDamerauLevenshteinDistance(restantTrimmedQuery,
                    restantTrimmedDB);
        }
        return computedDistanceTemp;
    }

    /**
     * Compares the first (or last if backwards = true) number of characters of
     * the 2 strings.
     *
     * @param count
     *            count of characters to compare
     * @param backwards
     *            if true comparison starts from the end of the words
     */
    private boolean characterMatches(String str1, String str2, int count, boolean backwards) {
        if (!backwards) {
            return str1.substring(0, count).equals(str2.substring(0, count));
        } else {
            return str1.substring((str1.length() - count), str1.length())
                    .equals(str2.substring((str2.length() - count), str2.length()));
        }
    }

    private List<String> allGeneraOrUninominalFromDB() {

        String initial = "*";
        List<String> allGeneraOrUninominalFromDBList = nameDao.distinctGenusOrUninomial(initial, null, null);
        return allGeneraOrUninominalFromDBList;
    }

    private List<String> prefilterGenus(String genusQuery, String phoneticNormalizedGenusQuery,
            List<String> allGeneraOrUninominalFromDBList) {

        List<String> prefilteredGenusOrUninominalList = new ArrayList<>();

        /**
         * The genus portion of the input name and the genus portion of the
         * target name are a phonetic match, as indicated by the ‘Rees 2007 near
         * match’ algorithm
         */

        for (String genusOrUninominalFromDB : allGeneraOrUninominalFromDBList) {
            String genusNormalizedInDB = NameMatchingUtils.normalize(genusOrUninominalFromDB);
            genusNormalizedInDB = NameMatchingUtils.nearMatch(genusOrUninominalFromDB);

            // rule 1a
            if (phoneticNormalizedGenusQuery.equals(genusNormalizedInDB)) {
                prefilteredGenusOrUninominalList.add(genusOrUninominalFromDB);
            }

            // rule 1b
            // TODO rule 1b requires fetching of species epithets. We need
            // further discussion if we
            // want to do this in the same way or how the semantics of this rule
            // can be implemented
            // in the best way.

            // rule 1c
            else if (Math.abs(genusOrUninominalFromDB.length() - genusQuery.length()) <= 2) {

                if (genusQuery.length() < 5) {
                    // rule 1c.1
                    if (characterMatches(genusQuery, genusOrUninominalFromDB, 1, false)
                            || characterMatches(genusQuery, genusOrUninominalFromDB, 1, true)) {
                        prefilteredGenusOrUninominalList.add(genusOrUninominalFromDB);
                    }
                } else if (genusQuery.length() == 5) {
                    // rule 1c.2
                    if (characterMatches(genusQuery, genusOrUninominalFromDB, 2, false)
                            || characterMatches(genusQuery, genusOrUninominalFromDB, 3, true)) {
                        prefilteredGenusOrUninominalList.add(genusOrUninominalFromDB);
                    }
                } else if (genusQuery.length() > 5) {
                    // rule 1c.3
                    if (characterMatches(genusQuery, genusOrUninominalFromDB, 3, false)
                            || characterMatches(genusQuery, genusOrUninominalFromDB, 3, true)) {
                        prefilteredGenusOrUninominalList.add(genusOrUninominalFromDB);
                    }
                }
            }
        }
        return prefilteredGenusOrUninominalList;
    }

    private boolean postfilterGenus(Integer maxDisGenus, String genusQuery, int genusComputedDistance,
            String phoneticNormalizedGenusQuery, String preFilteredGenusOrUninominal,
            String phoneticNormalizedGenusInDB) {

        int genusQueryLength = genusQuery.length();
        int genusDBLength = preFilteredGenusOrUninominal.length();
        int halfLength = Math.max(genusQueryLength, genusDBLength) / 2;
        boolean postFilter = false;

        if (genusComputedDistance < maxDisGenus) {
            postFilter = true;

            // Genera that match in at least 50% are kept. i.e., if genus length
            // = 6(or7) then at least 3 characters must match AND the initial
            // character must match in all cases where ED >2
        } else if (halfLength < maxDisGenus) {
            if (genusComputedDistance >= 2 && phoneticNormalizedGenusQuery.substring(0, 1)
                    .equals(phoneticNormalizedGenusInDB.substring(0, 1))) {
                postFilter = true;
            }
        }
        return postFilter;
    }

    private List<DoubleResult<NameMatchingParts, Integer>> prefilterEpithet(
            List<DoubleResult<NameMatchingParts, Integer>> genusOrUninomialWithDistance,
            String normalizedEphitetQuery) {
        List<DoubleResult<NameMatchingParts, Integer>> fullNameMatchingPartsListTemp = new ArrayList<>();
        for (DoubleResult<NameMatchingParts, Integer> fullNameMatchingParts : genusOrUninomialWithDistance) {
            if (fullNameMatchingParts.getFirstResult().getSpecificEpithet().length()
                    - normalizedEphitetQuery.length() <= 4) {
                fullNameMatchingPartsListTemp.add(fullNameMatchingParts);
                genusOrUninomialWithDistance = fullNameMatchingPartsListTemp;
            }
        }
        return genusOrUninomialWithDistance;
    }

    private List<DoubleResult<NameMatchingParts, Integer>> postfilterEpithet(Integer maxDisEpith, String epithetQuery,
            int epithetComputedDistance, String normalizedEphitetQuery,
            List<DoubleResult<NameMatchingParts, Integer>> epithetList, DoubleResult<NameMatchingParts, Integer> part,
            String epithetInDB, int totalDist) {
        List<DoubleResult<NameMatchingParts, Integer>> epithetListTemp = new ArrayList<>();
        int epithetQueryLength = epithetQuery.length();
        int epithetDBLength = epithetInDB.length();
        int halfLength = Math.max(epithetDBLength, epithetQueryLength) / 2;

        if (totalDist <= maxDisEpith) {
            epithetListTemp.add(part);
        } else if (halfLength < maxDisEpith) {
            if ((normalizedEphitetQuery.substring(0, 1).equals(epithetInDB.substring(0, 1))
                    && epithetComputedDistance == 2 || epithetComputedDistance == 3)
                    || (normalizedEphitetQuery.substring(0, 3).equals(epithetInDB.substring(0, 3))
                            && epithetComputedDistance == 4)) {
                epithetListTemp.add(part);
            }
        }
        return epithetListTemp;
    }

    private void postfilterInfrageneric(Integer maxDisInfrageneric, String infragenericQuery,
            int infragenericComputedDistance, String normalizedInfragenericQuery,
            List<DoubleResult<NameMatchingParts, Integer>> infragenericList,
            DoubleResult<NameMatchingParts, Integer> fullTaxonNamePart, String infragenericInDB, int totalDist) {
        int epithetQueryLength = infragenericQuery.length();
        int epithetDBLength = infragenericInDB.length();
        int halfLength = Math.max(epithetDBLength, epithetQueryLength) / 2;

        if (totalDist <= maxDisInfrageneric) {
            infragenericList.add(fullTaxonNamePart);
        } else if (halfLength < maxDisInfrageneric) {
            if ((normalizedInfragenericQuery.substring(0, 1).equals(infragenericInDB.substring(0, 1))
                    && infragenericComputedDistance == 2 || infragenericComputedDistance == 3)
                    || (normalizedInfragenericQuery.substring(0, 3).equals(infragenericInDB.substring(0, 3))
                            && infragenericComputedDistance == 4)) {
                infragenericList.add(fullTaxonNamePart);
            }
        }
    }


    public static List<DoubleResult<NameMatchingParts, Integer>> exactResults(
            List<DoubleResult<NameMatchingParts, Integer>> resultShapingList) {
        List<DoubleResult<NameMatchingParts, Integer>> exactResults = new ArrayList<>();
        for (DoubleResult<NameMatchingParts, Integer> exactResult : resultShapingList) {
            if (exactResult.getSecondResult() == 0) {
                exactResults.add(exactResult);
            }
        }
        return exactResults;
    }

    public static List<DoubleResult<NameMatchingParts, Integer>> bestResults(
            List<DoubleResult<NameMatchingParts, Integer>> list) {
        List<DoubleResult<NameMatchingParts, Integer>> bestResults = new ArrayList<>();
        for (DoubleResult<NameMatchingParts, Integer> best : list) {
            if (best.getSecondResult() == 1 || best.getSecondResult() == 2 || best.getSecondResult() == 3
                    || best.getSecondResult() == 4) {
                bestResults.add(best);
            }
        }
        return bestResults;
    }

    private List<DoubleResult<NameMatchingParts, Integer>> prefilterInfrageneric(
            List<DoubleResult<NameMatchingParts, Integer>> fullNameMatchingPartsList,
            String normalizedInfragenericQuery, Integer maxDisInfrageneric) {
        List<DoubleResult<NameMatchingParts, Integer>> fullNameMatchingPartsListTemp = new ArrayList<>();
        for (DoubleResult<NameMatchingParts, Integer> fullNameMatchingParts : fullNameMatchingPartsList) {
            if (fullNameMatchingParts.getFirstResult().getInfraGenericEpithet().length()
                    - normalizedInfragenericQuery.length() <= maxDisInfrageneric) {
                fullNameMatchingPartsListTemp.add(fullNameMatchingParts);
                fullNameMatchingPartsList = fullNameMatchingPartsListTemp;
            }
        }
        return fullNameMatchingPartsList;
    }

}