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
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.NameMatchingUtils;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dto.TaxonNameParts;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author andreabee90
 * @since 11.07.2023
 */
@Service
@Transactional(readOnly = true)
public class NameMatchingServiceImpl
//			extends IdentifiableServiceBase<TaxonName,ITaxonNameDao>
			implements INameMatchingService {

    @Autowired
    // @Qualifier("defaultBeanInitializer")
    protected IBeanInitializer defaultBeanInitializer;

    @Autowired
    private ITaxonNameDao nameDao;

//***************************** CONSTRUCTOR **********************************/

    public NameMatchingServiceImpl(){}

//********************* METHODS ***********************************************//

    /* This is a implementation of the Taxamatch algorithm built by Tony Rees.
     * It employs a custom Modified Damerau-Levenshtein Distance algorithm
     * see also https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0107510
     */
    //TODO work in progress
    @Override
    public List<DoubleResult<TaxonNameParts, Integer>> findMatchingNames(String taxonName,
            Integer maxDisGenus, Integer maxDisEpith) {

    	// only one (total) distance should be used.

        if (maxDisGenus == null) {
            maxDisGenus = 4;
        }

        if (maxDisEpith == null) {
            maxDisEpith = 4;
        }


        //0. Parsing and Normalizing

//      TODO? Remove all qualifiers such as cf., aff., ?, <i>, x, etc. from the whole input string
//        taxonName=CdmUtilsBelen.removeExtraElements(taxonName);
//        taxonName=CdmUtilsBelen.removeHTMLAmpersand(taxonName);

        TaxonName name = (TaxonName) NonViralNameParserImpl.NewInstance().parseFullName(taxonName);

        String genusQuery = name.getGenusOrUninomial();
        String epithetQuery = name.getSpecificEpithet();
//        String infraGenericQuery = name.getInfraGenericEpithet();
        
        int genusComputedDistance = 0;
        int epithetComputedDistance = 0;

        String normalizedGenusQuery = NameMatchingUtils.normalize(genusQuery);

       /* phonetic normalization of query (genus)
        * this method corresponds to the near match function of Rees 2007
        * it includes phonetic matches (replace initial characters, soundalike changes, gender endings)
        */

        String phoneticNormalizedGenusQuery = NameMatchingUtils.nearMatch(normalizedGenusQuery);

        //1. Genus pre-filter

        List<String> preFilteredGenusList = prefilterGenus(genusQuery);

        //create result list
        List<DoubleResult<TaxonNameParts,Integer>> fullTaxonNamePartsList = new ArrayList<>();

        for (String preFilteredGenus : preFilteredGenusList) {

        //2. comparison of genus

            String genusNameInDBNormalized = NameMatchingUtils.normalize(preFilteredGenus);
            String phoneticNormalizedGenusInDB = NameMatchingUtils.nearMatch(genusNameInDBNormalized);

            genusComputedDistance = nameMatchingComputeDistance(phoneticNormalizedGenusQuery, phoneticNormalizedGenusInDB);

        //3. genus post-filter

            postfilterGenus(maxDisGenus, genusQuery, genusComputedDistance, phoneticNormalizedGenusQuery,
                    fullTaxonNamePartsList, preFilteredGenus, phoneticNormalizedGenusInDB);
        }

        	//if only genus is given

        if (epithetQuery==null) {
            Collections.sort(fullTaxonNamePartsList, (o1,o2) -> o1.getSecondResult().compareTo(o2.getSecondResult()));

            List <DoubleResult<TaxonNameParts, Integer>> exactResults = exactResults(fullTaxonNamePartsList);
            List <DoubleResult<TaxonNameParts, Integer>> bestResults = bestResults(fullTaxonNamePartsList);

            if(!exactResults.isEmpty()) {
                return exactResults;
            } else {
                return bestResults;
            }

        } else {

            String normalizedEphitetQuery = NameMatchingUtils.normalize(epithetQuery);
            String phoneticNormalizedEpithetQuery = NameMatchingUtils.nearMatch(normalizedEphitetQuery);

            // 4. epithet pre-filter

            fullTaxonNamePartsList = prefilterEpithet(fullTaxonNamePartsList, normalizedEphitetQuery);

            List <DoubleResult<TaxonNameParts, Integer>> epithetList = new ArrayList<>();
            for (DoubleResult<TaxonNameParts, Integer> part: fullTaxonNamePartsList) {

            	String epithetInDB = part.getFirstResult().getSpecificEpithet();
            	String epithetNameInDBNormalized = NameMatchingUtils.normalize(epithetInDB);
            	String phoneticNormalizedEpithetNameInDB = NameMatchingUtils.nearMatch(epithetNameInDBNormalized);

            // 5. comparison of epithet
            	epithetComputedDistance = nameMatchingComputeDistance(phoneticNormalizedEpithetQuery, phoneticNormalizedEpithetNameInDB);
            	int totalDist = part.getSecondResult() + epithetComputedDistance;
            	part.setSecondResult(totalDist)  ;

            	///aqui hay error cuando la base solo tiene genero sin epiteto

            // 6. species post-filter

            	postfilterEpithet(maxDisEpith, epithetQuery, epithetComputedDistance, normalizedEphitetQuery, epithetList, part,
						epithetInDB, totalDist);
            }

            // 6b Infraspecific comparison (pre-filter, comparison, post-filter)
            //TODO

            // 7. Result shaping

            //-------------------CONTINUE HERE------------------

            Collections.sort(epithetList, (o1,o2) -> o1.getSecondResult().compareTo(o2.getSecondResult()) );

            List <DoubleResult<TaxonNameParts, Integer>> exactResults = exactResults(epithetList);
            List <DoubleResult<TaxonNameParts, Integer>> bestResults = bestResults(epithetList);

            if(!exactResults.isEmpty()) {
                return exactResults;
            } else {
                return bestResults;
            }
        }
    }

    /**
     * Deletes common characters at the beginning and end of both parameters.
     * Returns the space separated concatenation of the remaining strings.
     *<BR>
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
                // Stop iterating when the characters at the current position are not equal.
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
        }else {
            return shortenedQueryName +" "+ shortenedDBName;
        }
    }

    private int nameMatchingComputeDistance(String strQuery, String strDB) {
        int computedDistanceTemp;
        String trimmedStrings = trimCommonChar(strQuery, strDB);

        if ("".equals(trimmedStrings)) {
            computedDistanceTemp = 0;
        } else {
            String restantTrimmedQuery= trimmedStrings.split(" ")[0];
            String restantTrimmedDB=trimmedStrings.split(" ")[1];
            computedDistanceTemp = NameMatchingUtils.modifiedDamerauLevenshteinDistance(restantTrimmedQuery,restantTrimmedDB);
        }
        return computedDistanceTemp;
    }

    /**
     * Compares the first (or last if backwards = true) number of characters
     * of the 2 strings.
     * @param count count of characters to compare
     * @param backwards if true comparison starts from the end of the words
     */
    private boolean characterMatches(String str1, String str2, int count, boolean backwards) {
        if (!backwards) {
            return str1.substring(0,count).equals(str2.substring(0,count)) ;
        }else {
            return str1.substring((str1.length()-count),str1.length()).equals(str2.substring((str2.length()-count),str2.length()));
        }
    }

    private List<String> prefilterGenus(String genusQuery) {

        List<String> genusResultList = new ArrayList <>();

        // get a list with all genus/uninomial in the DB
        String initial= "*";
        List<String> genusListDB = nameDao.distinctGenusOrUninomial(initial, null, null);

       /* The genus portion of the input name and the genus portion of the target name are
        * a phonetic match, as indicated by the ‘Rees 2007 near match’ algorithm
        */

        for (String genusDB: genusListDB) {
            //TODO
            //if phonetic match add to result
        }

        //TODO rule 1b requires fetching of species epithets. We need further discussion if we
        //     want to do this in the same way or how the semantics of this rule can be implemented
        //     in the best way.

        // see Rees algorithm rule 1c
        for (String genusDB: genusListDB) {
            //check if already in result list
            if (genusResultList.contains(genusDB)) {
                continue;
            }
            if (Math.abs(genusDB.length()-genusQuery.length()) <= 2) {

                if(genusQuery.length()<5) {
                    // rule 1c.1
                    if ( characterMatches(genusQuery, genusDB, 1, false) ||
                            characterMatches(genusQuery, genusDB, 1, true)) {
                        genusResultList.add(genusDB);
                    }
                } else if (genusQuery.length()==5) {
                    // rule 1c.2
                    if (characterMatches(genusQuery, genusDB, 2, false) ||
                            characterMatches(genusQuery, genusDB, 3, true)){
                        genusResultList.add(genusDB);
                    }
                } else if (genusQuery.length()>5){
                    // rule 1c.3
                    if (characterMatches(genusQuery, genusDB, 3, false) ||
                            characterMatches(genusQuery, genusDB, 3, true)){
                        genusResultList.add(genusDB);
                    }
                }
            }
        }
        return genusResultList;
    }

    private void postfilterGenus(Integer maxDistanceGenus, String genusQuery, int distance,
            String normalizedGenusQuery, List<DoubleResult<TaxonNameParts, Integer>> fullTaxonNamePartsList,
            String preFilteredGenus, String genusNameInDBNormalized) {

        int genusQueryLength = genusQuery.length();
        int genusDBLength = preFilteredGenus.length();
        int halfLength = Math.max(genusQueryLength, genusDBLength)/2;

        //Genera that match in at least 50% are kept. i.e., if genus length = 6(or7) then at least 3 characters must match AND the initial character must match in all cases where ED >1
        if (distance <= maxDistanceGenus) {
            List<TaxonNameParts> tempParts1 = nameDao.findTaxonNameParts(Optional.of(preFilteredGenus), null, null, null, null, null, null, null, null);
            for (TaxonNameParts namePart1: tempParts1) {
                fullTaxonNamePartsList.add(new DoubleResult<TaxonNameParts, Integer>(namePart1, distance));
            }
        } else if(halfLength < maxDistanceGenus && normalizedGenusQuery.substring(0,1).equals(genusNameInDBNormalized.substring(0,1))) {
            List<TaxonNameParts> tempParts2 = nameDao.findTaxonNameParts(Optional.of(preFilteredGenus),null, null, null, null, null, null, null, null);
            for (TaxonNameParts namePart2: tempParts2) {
                fullTaxonNamePartsList.add(new DoubleResult<TaxonNameParts, Integer>(namePart2, distance));
            }
        }
    }

	private List<DoubleResult<TaxonNameParts, Integer>> prefilterEpithet(
			List<DoubleResult<TaxonNameParts, Integer>> fullTaxonNamePartsList, String normalizedEphitetQuery) {
		List<DoubleResult<TaxonNameParts,Integer>> fullTaxonNamePartsListTemp = new ArrayList<>();
		for (DoubleResult<TaxonNameParts, Integer> fullTaxonNameParts: fullTaxonNamePartsList) {
		    if (fullTaxonNameParts.getFirstResult().getSpecificEpithet().length()- normalizedEphitetQuery.length() <= 4) {
		        fullTaxonNamePartsListTemp.add(fullTaxonNameParts);
		        fullTaxonNamePartsList = fullTaxonNamePartsListTemp;
		    }
		}
		return fullTaxonNamePartsList;
	}

	private void postfilterEpithet(Integer maxDisEpith, String epithetQuery, int epithetDistance,
			String normalizedEphitetQuery, List<DoubleResult<TaxonNameParts, Integer>> epithetList,
			DoubleResult<TaxonNameParts, Integer> part, String epithetInDB, int totalDist) {
		int epithetQueryLength=epithetQuery.length();
		int epithetDBLength=epithetInDB.length();
		int halfLength=Math.max(epithetDBLength,epithetQueryLength)/2;

		if (totalDist <= maxDisEpith) {
			epithetList.add(part);
		}else if (halfLength<maxDisEpith) {
			if ((normalizedEphitetQuery.substring(0,1).equals(epithetInDB.substring(0,1))
					&& epithetDistance == 2||epithetDistance == 3)||
					(normalizedEphitetQuery.substring(0,3).equals(epithetInDB.substring(0,3))
							&& epithetDistance == 4)) {
				epithetList.add(part);
			}
		}
	}

	//checken!!!

    public static List <DoubleResult<TaxonNameParts, Integer>> exactResults (List <DoubleResult<TaxonNameParts, Integer>> list){
        List <DoubleResult<TaxonNameParts, Integer>> exactResults = new ArrayList<>();
        for (DoubleResult<TaxonNameParts, Integer> best:list) {
            if (best.getSecondResult()==0){
                exactResults.add(best);
            }
        }
        return exactResults;
    }

    public static List <DoubleResult<TaxonNameParts, Integer>> bestResults (List <DoubleResult<TaxonNameParts, Integer>> list){
        List <DoubleResult<TaxonNameParts, Integer>> bestResults = new ArrayList<>();
        for (DoubleResult<TaxonNameParts, Integer> best:list) {
            if (best.getSecondResult()==1||best.getSecondResult()==2||best.getSecondResult()==3||best.getSecondResult()==4){
                bestResults.add(best);
            }
        }
        return bestResults;
    }
}