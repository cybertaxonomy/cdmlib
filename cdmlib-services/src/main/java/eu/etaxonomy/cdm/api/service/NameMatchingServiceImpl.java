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

import org.apache.commons.lang3.StringUtils;
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

    //**********CONSTRUCTOR**********

    public NameMatchingServiceImpl() {
    }

    //**********METHODS**********
    
    /**
     * This is a implementation of the Taxamatch algorithm built by Tony Rees.
     * It employs a custom Modified Damerau-Levenshtein Distance algorithm see
     * also https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0107510
     */
    
     // TODO work in progress

    @Override
    public List<DoubleResult<NameMatchingParts, Integer>> findMatchingNames(String taxonName, Integer maxDistance) {

        // Discuss. value of distance if query is only genus, if genus +
        // epithet, if genus + infrageneric + epithet...


        // 0. Parsing and Normalizing

        // TODO? Remove all qualifiers such as cf., aff., ?, <i>, x, etc. from
        // the full input string

        TaxonName name = (TaxonName) NonViralNameParserImpl.NewInstance().parseReferencedName(taxonName); //parsereferencename 

        String genusQuery = name.getGenusOrUninomial();
        String epithetQuery = name.getSpecificEpithet();
        String infraGenericQuery = name.getInfraGenericEpithet();
        String infraSpecificQuery = name.getInfraSpecificEpithet();

        Integer maxDisMonomial = 2;
        Integer maxDisBinomial = 4;
        Integer maxDisTrinomial = 6;

        if (maxDistance == null) {
        	if (epithetQuery != null && infraSpecificQuery == null || infraGenericQuery != null) {
        		maxDistance = maxDisBinomial;
        	} else if (epithetQuery != null && infraSpecificQuery != null){
        		maxDistance = maxDisTrinomial;       		
        	} else {
        		maxDistance = maxDisMonomial;
        	}
        }
        
        int genusComputedDistance = 0;
        int infraSpecificComputedDistance = 0;
        int infragenericComputedDistance = 0;

        List<DoubleResult<NameMatchingParts, Integer>> exactResults;
        List<DoubleResult<NameMatchingParts, Integer>> bestResults;
        /*
         * phonetic normalization of query (genus) this method corresponds to
         * the near match function of Rees 2007 it includes phonetic matches
         * (replace initial characters, soundalike changes, gender endings)
         */

        String normalizedGenusQuery = NameMatchingUtils.normalize(genusQuery);
        String phoneticNormalizedGenusQuery = NameMatchingUtils.nearMatch(normalizedGenusQuery);

        //fetch all genera from DB
        List<String> allGeneraOrUninominalFromDB = allGeneraOrUninominalFromDB();

        // 1. Genus pre-filter
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
            boolean postFilterOK = postfilterGenus(maxDistance, genusQuery, genusComputedDistance,
                    phoneticNormalizedGenusQuery, preFilteredGenusOrUninominal, phoneticNormalizedGenusInDB);
            if (postFilterOK) {
                postFilteredGenusOrUninominalWithDis.put(preFilteredGenusOrUninominal, genusComputedDistance);
            }
        }

        List<DoubleResult<NameMatchingParts, Integer>> taxonNamePartsWithDistance = new ArrayList<>();
        taxonNamePartsWithDistance.addAll(getTaxonNamePartsFromDB(postFilteredGenusOrUninominalWithDis));
        
        List<DoubleResult<NameMatchingParts, Integer>> resultSetOnlyGenusOrUninominal = new ArrayList<>();
        if (epithetQuery == null && infraGenericQuery == null) {
            filterMatchingMonomialFromResultSet(taxonNamePartsWithDistance, resultSetOnlyGenusOrUninominal);
            Collections.sort(resultSetOnlyGenusOrUninominal,
                    (o1, o2) -> o1.getSecondResult().compareTo(o2.getSecondResult()));
            exactResults = exactResults(resultSetOnlyGenusOrUninominal);
            bestResults = bestResults(resultSetOnlyGenusOrUninominal);

            if (!exactResults.isEmpty()) {
                return exactResults;
            } else {
                return bestResults;
            }
            
        } else if (infraGenericQuery != null) {
        	String normalizedInfragenericQuery = NameMatchingUtils.normalize(infraGenericQuery);
            String phoneticNormalizedInfragenericQuery = NameMatchingUtils.nearMatch(normalizedInfragenericQuery);
            List<DoubleResult<NameMatchingParts, Integer>> preFilteredInfragenericListWithDist = new ArrayList<>();
            
            filterNamesWithInfragenericEpithets(taxonNamePartsWithDistance, preFilteredInfragenericListWithDist);
            prefilterInfrageneric(preFilteredInfragenericListWithDist, normalizedInfragenericQuery, maxDistance);
            
            List <DoubleResult<NameMatchingParts, Integer>> resultSetInfraGenericListWithDist = new ArrayList<>();
            for (DoubleResult<NameMatchingParts, Integer> preFilteredInfrageneric:
            	preFilteredInfragenericListWithDist) {
            	String infragenericInDB = preFilteredInfrageneric.getFirstResult().getInfraGenericEpithet();
               	String infragenericNameInDBNormalized = NameMatchingUtils.normalize(infragenericInDB);
            	String phoneticNormalizedInfragenericNameInDB = NameMatchingUtils.nearMatch(infragenericNameInDBNormalized);
            		// 5. comparison of infrageneric
            	infragenericComputedDistance = nameMatchingComputeDistance(phoneticNormalizedInfragenericQuery,
            			phoneticNormalizedInfragenericNameInDB);
            	int totalDist = preFilteredInfrageneric.getSecondResult() + infragenericComputedDistance;
            	preFilteredInfrageneric.setSecondResult(totalDist) ;
            			
            		// 6. infrageneric post-filter
            	postfilterInfrageneric(maxDistance, infraGenericQuery,
            			infragenericComputedDistance, normalizedInfragenericQuery,
            			resultSetInfraGenericListWithDist, preFilteredInfrageneric,
            			infragenericInDB, totalDist);
            	}
            Collections.sort(resultSetInfraGenericListWithDist, (o1,o2) ->
            o1.getSecondResult().compareTo(o2.getSecondResult()));
            exactResults = exactResults(resultSetInfraGenericListWithDist);
            bestResults = bestResults(resultSetInfraGenericListWithDist);
            if(!exactResults.isEmpty()) {
            	return exactResults;
            } else {
            	return bestResults;
            }
            
        } else if (epithetQuery != null && infraSpecificQuery == null){

            String normalizedEphitetQuery = NameMatchingUtils.normalize(epithetQuery);
            String phoneticNormalizedEpithetQuery = NameMatchingUtils.nearMatch(normalizedEphitetQuery);

            // 4. epithet pre-filter

            List<DoubleResult<NameMatchingParts, Integer>> preFilteredEpithetListWithDist= new ArrayList<>();
            filterNamesWithEpithets(taxonNamePartsWithDistance, preFilteredEpithetListWithDist);
            prefilterEpithet(preFilteredEpithetListWithDist, normalizedEphitetQuery, maxDistance);
            List<DoubleResult<NameMatchingParts, Integer>> resultSetEpithetListWithDist = new ArrayList<>();
            for (DoubleResult<NameMatchingParts, Integer> preFilteredEpithet: preFilteredEpithetListWithDist) {
            	String epithetInDB = preFilteredEpithet.getFirstResult().getSpecificEpithet();
            	if (epithetInDB.isEmpty()) {
                    continue;
                }
                String epithetNameInDBNormalized = NameMatchingUtils.normalize(epithetInDB);
                String phoneticNormalizedEpithetNameInDB = NameMatchingUtils.nearMatch(epithetNameInDBNormalized);
            	
            		// 5. comparison of epithet
                infraSpecificComputedDistance = nameMatchingComputeDistance(phoneticNormalizedEpithetQuery,
                        phoneticNormalizedEpithetNameInDB);
                int totalDist = preFilteredEpithet.getSecondResult() + infraSpecificComputedDistance;
                preFilteredEpithet.setSecondResult(totalDist);

                // 6. species post-filter
                resultSetEpithetListWithDist.addAll(postfilterEpithet(maxDistance, epithetQuery, infraSpecificComputedDistance,
                        normalizedEphitetQuery, preFilteredEpithet, epithetInDB, totalDist));
            }

            // 7. Result shaping

            Collections.sort(resultSetEpithetListWithDist, (o1, o2) -> o1.getSecondResult().compareTo(o2.getSecondResult()));
            exactResults = exactResults(resultSetEpithetListWithDist);
            bestResults = bestResults(resultSetEpithetListWithDist);

            if (!exactResults.isEmpty()) {
                return exactResults;
            } else {
                return bestResults;
            }
            
        } else if (infraSpecificQuery != null) {
        	
            String normalizedInfraSpecificQuery = NameMatchingUtils.normalize(infraSpecificQuery);
            String phoneticNormalizedInfraSpecificQuery = NameMatchingUtils.nearMatch(normalizedInfraSpecificQuery);

            // 4. infra specific pre-filter

            List<DoubleResult<NameMatchingParts, Integer>> preFilteredInfraSpecificListWithDist= new ArrayList<>();
            for (int i = 0; i < taxonNamePartsWithDistance.size(); i++) {
                if (taxonNamePartsWithDistance.get(i).getFirstResult().getInfraGenericEpithet().isEmpty()) {
                	preFilteredInfraSpecificListWithDist.add(taxonNamePartsWithDistance.get(i));
                }
            }
            
            preFilteredInfraSpecificListWithDist = prefilterInfraSpecific(taxonNamePartsWithDistance, normalizedInfraSpecificQuery, 6);
            List<DoubleResult<NameMatchingParts, Integer>> resultSetInfraSpecificListWithDist = new ArrayList<>();
            for (DoubleResult<NameMatchingParts, Integer> infraSpecific: preFilteredInfraSpecificListWithDist) {
            	String infraSpecificInDB = infraSpecific.getFirstResult().getInfraSpecificEpithet();
            	if (infraSpecificInDB.isEmpty()) {
                    continue;
                }
            	String infraSpecificNameInDBNormalized = NameMatchingUtils.normalize(infraSpecificInDB);
                String phoneticNormalizedInfraSpecificNameInDB = NameMatchingUtils.nearMatch(infraSpecificNameInDBNormalized);
            	
            		// 5. comparison of infra specific
                infraSpecificComputedDistance = nameMatchingComputeDistance(phoneticNormalizedInfraSpecificQuery ,
                        phoneticNormalizedInfraSpecificNameInDB);
                int totalDist = infraSpecific.getSecondResult() + infraSpecificComputedDistance;
                infraSpecific.setSecondResult(totalDist);

                // 6. infra specific post-filter
                resultSetInfraSpecificListWithDist.addAll(postfilterEpithet(6, infraSpecificQuery, infraSpecificComputedDistance,
                		normalizedInfraSpecificQuery, infraSpecific, infraSpecificInDB, totalDist));
            }

            // 7. Result shaping

            Collections.sort(resultSetInfraSpecificListWithDist, (o1, o2) -> o1.getSecondResult().compareTo(o2.getSecondResult()));
            exactResults = exactResults(resultSetInfraSpecificListWithDist);
            bestResults = bestResults(resultSetInfraSpecificListWithDist);

            if (!exactResults.isEmpty()) {
                return exactResults;
            } else {
                return bestResults;
            }
        } else return null;
    }

	private void filterNamesWithEpithets(List<DoubleResult<NameMatchingParts, Integer>> taxonNamePartsWithDistance,
			List<DoubleResult<NameMatchingParts, Integer>> preFilteredEpithetListWithDist) {
		for (int i = 0; i < taxonNamePartsWithDistance.size(); i++) {
			String infrageneric = taxonNamePartsWithDistance.get(i).getFirstResult().getInfraGenericEpithet();
			String infraspecific = taxonNamePartsWithDistance.get(i).getFirstResult().getInfraSpecificEpithet();
		    if (infrageneric.isEmpty() &&
		    		infraspecific.isEmpty()) {
		    	preFilteredEpithetListWithDist.add(taxonNamePartsWithDistance.get(i));
		    }
		}
	}

    /**
     * @param taxonNamePartsWithDistance
     * @param preFilteredInfragenericListWithDist
     * Filter all names from the DB that contain a infrageneric epithet (exclude all names that have epithet or infraspecfic epithet)
     */
	private void filterNamesWithInfragenericEpithets(
			List<DoubleResult<NameMatchingParts, Integer>> taxonNamePartsWithDistance,
			List<DoubleResult<NameMatchingParts, Integer>> preFilteredInfragenericListWithDist) {
		for (int i = 0; i < taxonNamePartsWithDistance.size(); i++) {
		    if (taxonNamePartsWithDistance.get(i).getFirstResult().getSpecificEpithet().isEmpty() && 
		    		taxonNamePartsWithDistance.get(i).getFirstResult().getInfraSpecificEpithet().isEmpty() &&
		    		StringUtils.isNotEmpty(taxonNamePartsWithDistance.get(i).getFirstResult().getInfraGenericEpithet())) {
		    	preFilteredInfragenericListWithDist.add(taxonNamePartsWithDistance.get(i));
		    }
		}
	}

    /**
	 * @param taxonNamePartsWithDistance
	 * @param resultSetOnlyGenusOrUninominal
	 * 
	 * @return List of only Genus or Uninomial names (filters out all bi/trinomial names)+ distance
	 */
    
	private void filterMatchingMonomialFromResultSet(
			List<DoubleResult<NameMatchingParts, Integer>> taxonNamePartsWithDistance,
			List<DoubleResult<NameMatchingParts, Integer>> resultSetOnlyGenusOrUninominal) {
		for (int i = 0; i < taxonNamePartsWithDistance.size(); i++) {
			String epi = taxonNamePartsWithDistance.get(i).getFirstResult().getSpecificEpithet();
			String infge= taxonNamePartsWithDistance.get(i).getFirstResult().getInfraGenericEpithet();
			String infraspec = taxonNamePartsWithDistance.get(i).getFirstResult().getInfraSpecificEpithet();
			if (epi.isEmpty() && infge.isEmpty() && infraspec.isEmpty()) {
		        resultSetOnlyGenusOrUninominal.add(taxonNamePartsWithDistance.get(i));
		    }
		}
	}

    /**
	 * @param postFilteredGenusOrUninominalWithDis
	 * @return List of all species belonging to the genera that passed the post filter + calculated distance 
	 */
    
    private List<DoubleResult<NameMatchingParts, Integer>> getTaxonNamePartsFromDB(
    		Map<String, Integer> postFilteredGenusOrUninominalWithDis) {
    	List<DoubleResult<NameMatchingParts, Integer>> genusOrUninomialWithDistance = new ArrayList<>();
    	List<NameMatchingParts> fullNameMatchingPartsListTemp = nameDao2.findNameMatchingParts(postFilteredGenusOrUninominalWithDis);
        postFilteredGenusOrUninominalWithDis.forEach((key, value) -> {
        	for (NameMatchingParts fullNameMatchingParts : fullNameMatchingPartsListTemp) {
        		if (fullNameMatchingParts.getGenusOrUninomial() == key) {
        			genusOrUninomialWithDistance.add(new DoubleResult<NameMatchingParts, Integer>(fullNameMatchingParts, value));
                 }
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
        String restantTrimmedQuery = "";
        String restantTrimmedDB="";

        if ("".equals(trimmedStrings)) {
            computedDistanceTemp = 0;
        } else {
        	try {
            restantTrimmedQuery = trimmedStrings.split(" ")[0];
            restantTrimmedDB = trimmedStrings.split(" ")[1];}
        	catch (Exception e) {
        		System.out.println("timmed string is empty");
        	}
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
            List<String> allGeneraOrUninominalFromDB) {

        List<String> prefilteredGenusOrUninominalList = new ArrayList<>();

        /**
         * The genus portion of the input name and the genus portion of the
         * target name are a phonetic match, as indicated by the ‘Rees 2007 near
         * match’ algorithm
         */

        for (String genusOrUninominalFromDB : allGeneraOrUninominalFromDB) {
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

    private boolean postfilterGenus(Integer maxDistance, String genusQuery, int genusComputedDistance,
            String phoneticNormalizedGenusQuery, String preFilteredGenusOrUninominal,
            String phoneticNormalizedGenusInDB) {

        int genusQueryLength = genusQuery.length();
        int genusDBLength = preFilteredGenusOrUninominal.length();
        int halfLength = Math.max(genusQueryLength, genusDBLength) / 2;
        boolean postFilter = false;

        if (genusComputedDistance < maxDistance) {
            postFilter = true;

            // Genera that match in at least 50% are kept. i.e., if genus length
            // = 6(or7) then at least 3 characters must match AND the initial
            // character must match in all cases where ED >2
        } else if (halfLength < maxDistance) {
            if (genusComputedDistance >= 2 && phoneticNormalizedGenusQuery.substring(0, 1)
                    .equals(phoneticNormalizedGenusInDB.substring(0, 1))) {
                postFilter = true;
            }
        }
        return postFilter;
    }

    private void prefilterEpithet(
            List<DoubleResult<NameMatchingParts, Integer>> preFilteredEpithetListWithDist,
            String normalizedEphitetQuery, int maxDistance) {
        List<DoubleResult<NameMatchingParts, Integer>> fullNameMatchingPartsListTemp = new ArrayList<>();
        for (DoubleResult<NameMatchingParts, Integer> fullNameMatchingParts : preFilteredEpithetListWithDist) {
            if (fullNameMatchingParts.getFirstResult().getSpecificEpithet().length()
                    - normalizedEphitetQuery.length() <= maxDistance) {
                fullNameMatchingPartsListTemp.add(fullNameMatchingParts);
                preFilteredEpithetListWithDist = fullNameMatchingPartsListTemp;
            }
        }
    }

    private List<DoubleResult<NameMatchingParts, Integer>> postfilterEpithet(Integer maxDistance, String epithetQuery,
            int epithetComputedDistance, String normalizedEphitetQuery, DoubleResult<NameMatchingParts, Integer> part,
            String epithetInDB, int totalDist) {
        List<DoubleResult<NameMatchingParts, Integer>> epithetListTemp = new ArrayList<>();
        int epithetQueryLength = epithetQuery.length();
        int epithetDBLength = epithetInDB.length();
        int halfLength = Math.max(epithetDBLength, epithetQueryLength) / 2;

        if (totalDist <= maxDistance) {
            epithetListTemp.add(part);
        } else if (halfLength < maxDistance) {
            if ((normalizedEphitetQuery.substring(0, 1).equals(epithetInDB.substring(0, 1))
                    && epithetComputedDistance == 2 || epithetComputedDistance == 3)
                    || (normalizedEphitetQuery.substring(0, 3).equals(epithetInDB.substring(0, 3))
                            && epithetComputedDistance == 4)) {
                epithetListTemp.add(part);
            }
        }
        return epithetListTemp;
    }

    private void postfilterInfrageneric(Integer maxDistance, String infragenericQuery,
            int infragenericComputedDistance, String normalizedInfragenericQuery,
            List<DoubleResult<NameMatchingParts, Integer>> infragenericList,
            DoubleResult<NameMatchingParts, Integer> fullTaxonNamePart, String infragenericInDB, int totalDist) {
        int epithetQueryLength = infragenericQuery.length();
        int epithetDBLength = infragenericInDB.length();
        int halfLength = Math.max(epithetDBLength, epithetQueryLength) / 2;

        if (totalDist <= maxDistance) {
            infragenericList.add(fullTaxonNamePart);
        } else if (halfLength < maxDistance) {
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

    private void prefilterInfrageneric(
            List<DoubleResult<NameMatchingParts, Integer>> preFilteredInfragenericListWithDist,
            String normalizedInfragenericQuery, Integer maxDistance) {
        List<DoubleResult<NameMatchingParts, Integer>> fullNameMatchingPartsListTemp = new ArrayList<>();
        for (DoubleResult<NameMatchingParts, Integer> fullNameMatchingParts : preFilteredInfragenericListWithDist) {
            if (fullNameMatchingParts.getFirstResult().getInfraGenericEpithet().length()
                    - normalizedInfragenericQuery.length() <= maxDistance) {
                fullNameMatchingPartsListTemp.add(fullNameMatchingParts);
                preFilteredInfragenericListWithDist = fullNameMatchingPartsListTemp;
            }
        }
    }

    private List<DoubleResult<NameMatchingParts, Integer>> prefilterInfraSpecific(
			List<DoubleResult<NameMatchingParts, Integer>> taxonNamePartsWithDistance,
			String normalizedInfraSpecificQuery, Integer maxDistance) {
		List<DoubleResult<NameMatchingParts, Integer>> fullNameMatchingPartsListTemp = new ArrayList<>();
		for (DoubleResult<NameMatchingParts, Integer> fullNameMatchingParts : taxonNamePartsWithDistance) {
			if (fullNameMatchingParts.getFirstResult().getInfraSpecificEpithet().length() - normalizedInfraSpecificQuery.length() <= maxDistance) {
				fullNameMatchingPartsListTemp.add(fullNameMatchingParts);
				taxonNamePartsWithDistance = fullNameMatchingPartsListTemp;
			}
		}
		return taxonNamePartsWithDistance;
	}
}