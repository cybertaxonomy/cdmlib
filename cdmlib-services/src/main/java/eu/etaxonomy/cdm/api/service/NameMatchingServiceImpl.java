/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.NameMatchingConfigurator;
import eu.etaxonomy.cdm.api.service.exception.NameMatchingParserException;
import eu.etaxonomy.cdm.common.NameMatchingUtils;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.persistence.dao.name.INameMatchingDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dto.NameMatchingParts;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * This class implements name matching according to the algorithm built by Tony Rees.
 * It employs a custom Modified Damerau-Levenshtein Distance algorithm to calculate the
 * differences among characters of names. See publication
 * https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0107510

 * @see https://dev.e-taxonomy.eu/redmine/issues/10178
 * @author andreabee90
 * @since 02.08.2023
 */
@Service
@Transactional(readOnly = true)
public class NameMatchingServiceImpl
        // extends IdentifiableServiceBase<TaxonName,ITaxonNameDao>
        implements INameMatchingService {

//    @Autowired
    // @Qualifier("defaultBeanInitializer")
//    protected IBeanInitializer defaultBeanInitializer;

    @Autowired
    private ITaxonNameDao nameDao;
    @Autowired
    private INameMatchingDao nameMatchingDao;

    //**********CONSTRUCTOR**********

    public NameMatchingServiceImpl() {}

    //************* RESULT CLASS *******************/

    public class SingleNameMatchingResult extends NameMatchingParts {

    	private Double distance;

        public SingleNameMatchingResult(NameMatchingParts parts, Double distance) {
            super(parts.getTaxonNameId(), parts.getTaxonNameUuid(), parts.getTitleCache(),
                    parts.getAuthorshipCache(), parts.getGenusOrUninomial(),
                    parts.getInfraGenericEpithet(), parts.getSpecificEpithet(),
                    parts.getInfraSpecificEpithet(), parts.getNameCache(), parts.getRank(),
                    parts.getCombinationAuthorship(), parts.getExCombinationAuthorship(),
                    parts.getBasionymAuthorship(), parts.getExBasionymAuthorship());
            this.distance = distance;
        }
        public Double getDistance() {
            return distance;
        }
        public void setDistance(Double distance) {
            this.distance = distance;
        }
    }

    public class NameMatchingResult{

        List<SingleNameMatchingResult> exactResults = new ArrayList<>();
        List<SingleNameMatchingResult> bestFuzzyResults = new ArrayList<>();
        String warning;

        public List<SingleNameMatchingResult> getExactResults() {
            return exactResults;
        }
        public void setExactResults(List<SingleNameMatchingResult> exactResults) {
            this.exactResults = exactResults;
        }
        public List<SingleNameMatchingResult> getBestFuzzyResults() {
            return bestFuzzyResults;
        }
        public void setBestFuzzyResults(List<SingleNameMatchingResult> bestFuzzyResults) {
            this.bestFuzzyResults = bestFuzzyResults;
        }
        public void setWarning (String warning) {
            this.warning = warning;
        }
        public String getWarning () {
            return warning;
        }
    }

    //**********METHODS**********

    /**
     * Compares a list of input names with names in the Database.
     * @return A map with input names as key, and results as values.
     * Matches without a perfect match return a list of best matches
     * @throws NameMatchingParserException
     *
     */

    @Override
    public Map<String, NameMatchingResult> compareTaxonListName(List<String> input, boolean compareAuthor, boolean excludeBasionymAuthors, boolean excludeExAuthors,
            Double maxDistance) throws NameMatchingParserException{

        for (int i = 0 ; i < input.size(); i++) {
            String name = input.get(i);
            name = name.replaceAll("^\\s+", "");
            name = name.replaceAll("\\s+$", "");
            input.set(i,name);
        }
        Map<String, NameMatchingResult> inputAndResults = new HashMap<>();
        for (String inputName : input) {
            NameMatchingResult individualResults = new NameMatchingResult();
            individualResults = findMatchingNames(inputName, compareAuthor, excludeBasionymAuthors, excludeExAuthors, maxDistance);
            inputAndResults.put(inputName, individualResults);
        }
        return inputAndResults;
    }

    @Override
    public NameMatchingResult findMatchingNames(String nameCache, boolean compareAuthor, boolean excludeBasionymAuthors,
            boolean excludeExAuthors, Double distance)
            throws NameMatchingParserException{

        NameMatchingResult result = new NameMatchingResult();
        List<SingleNameMatchingResult> resultInput;

        try {
            resultInput = findMatchingNamesUnshaped(nameCache, null, compareAuthor, excludeBasionymAuthors, excludeExAuthors, distance);
        } catch (NameMatchingParserException e) {
            result.setWarning(e.getWarning());
            result.exactResults = new ArrayList<>();
            result.bestFuzzyResults = new ArrayList<>();
            return result;
        }

        for (SingleNameMatchingResult part : resultInput) {
            if (compareAuthor) {
                if (part.getDistance() == 1 || part.getTitleCache().equals(nameCache)) {
                    result.exactResults.add(part);
                } else {
                    result.bestFuzzyResults.add(part);
                }
            } else if (compareAuthor == false) {
                if (part.getDistance() == 1){
                    result.exactResults.add(part);
                } else {
                    result.bestFuzzyResults.add(part);
                }
            }
        }
        return result;
    }

    /**
     * Compares two names and calculates number of differences.
     * @return list of exact matching names (distance = 0), or list of best matches if exact matches are not found.
     */

    private List<SingleNameMatchingResult> findMatchingNamesUnshaped(String taxonName, NameMatchingConfigurator config,
            boolean compareAuthor, boolean excludeBasionymAuthors, boolean excludeExAuthors, Double inputDistance)
                    throws NameMatchingParserException{

        List<SingleNameMatchingResult> result = new ArrayList<>();

        // 0. Normalizing and parsing input name
        taxonName = normalizeInput(taxonName);

    	if (config == null) {
    		//default configurator
    		config = new NameMatchingConfigurator();
    	}

        TaxonName name = (TaxonName) NonViralNameParserImpl.NewInstance().parseFullName(taxonName);
        String genusQuery = name.getGenusOrUninomial();
        String specificEpithetQuery = name.getSpecificEpithet();
        String infraGenericQuery = name.getInfraGenericEpithet();
        String infraSpecificQuery = name.getInfraSpecificEpithet();
        String authorshipCacheQuery = name.getAuthorshipCache();
        Rank rank = name.getRank();

        String combinationAuthor = name.getCombinationAuthorship() == null ? "" : name.getCombinationAuthorship().getNomenclaturalTitleCache();
        String exCombinationAuthor = name.getExCombinationAuthorship() == null ? "" : name.getExCombinationAuthorship().getNomenclaturalTitleCache();
        String basionymAuthor = name.getBasionymAuthorship() == null ? "" : name.getBasionymAuthorship().getNomenclaturalTitleCache();
        String exBasionymAuthor = name.getExBasionymAuthorship() == null ? "" : name.getExBasionymAuthorship().getNomenclaturalTitleCache();

        if (genusQuery == null) {
            throw new NameMatchingParserException ("input name could not be parsed");
        }

        Double maxDistance = defineDistances(config, inputDistance, specificEpithetQuery, infraGenericQuery,
                infraSpecificQuery, rank);

        /**
         * phonetic normalization of query (genus) this method corresponds to
         * the near match function of Rees 2007. It includes phonetic matches
         * (replace initial characters, soundalike changes, gender endings).
         * See #10178
         */
        String normalizedGenusQuery = NameMatchingUtils.normalize(genusQuery);
        String phoneticNormalizedGenusQuery = NameMatchingUtils.nearMatch(normalizedGenusQuery);

        //fetch all genera from DB
        List<String> allGeneraOrUninominalFromDB = allGeneraOrUninominalFromDB();

        // 1. Genus pre-filter
        List<String> preFilteredGenusOrUninominalList = prefilterGenus(genusQuery, phoneticNormalizedGenusQuery,
                allGeneraOrUninominalFromDB);

        Map<String, Double> postFilteredGenusOrUninominalWithDis = compareGenus(genusQuery, maxDistance, phoneticNormalizedGenusQuery,
                preFilteredGenusOrUninominalList);

        List<SingleNameMatchingResult> taxonNamePartsWithDistance = new ArrayList<>();
        List<SingleNameMatchingResult> taxonNamePartsFromDb = getTaxonNamePartsFromDB(postFilteredGenusOrUninominalWithDis);
        taxonNamePartsWithDistance.addAll(taxonNamePartsFromDb);

        List<SingleNameMatchingResult> resultSetOnlyGenusOrUninominal;

        if (specificEpithetQuery == null && infraGenericQuery == null) {
            resultSetOnlyGenusOrUninominal = new ArrayList<>();
            filterMatchingMonomialFromResultSet(taxonNamePartsWithDistance, resultSetOnlyGenusOrUninominal);
        } else if (infraGenericQuery != null) {
            resultSetOnlyGenusOrUninominal = compareInfrageneric(infraGenericQuery, maxDistance, taxonNamePartsWithDistance);
        } else if (specificEpithetQuery != null && infraSpecificQuery == null) {
            resultSetOnlyGenusOrUninominal = compareSpecificEptihtet(specificEpithetQuery, maxDistance, taxonNamePartsWithDistance, false);
        } else if (infraSpecificQuery != null) {
            resultSetOnlyGenusOrUninominal = compareInfraSpecific(specificEpithetQuery, infraSpecificQuery, maxDistance, taxonNamePartsWithDistance, rank);
        } else {
            return null;
        }

        Collections.sort(resultSetOnlyGenusOrUninominal, (o1, o2) -> o1.getDistance().compareTo(o2.getDistance()));
        result = candidatesResults(resultSetOnlyGenusOrUninominal, maxDistance);

        if (compareAuthor) {
            result = authorMatch(result, excludeBasionymAuthors, excludeExAuthors, authorshipCacheQuery, combinationAuthor,
                                 exCombinationAuthor, basionymAuthor, maxDistance);
        }
        normalizedMatchingScoreInOutputList(result, genusQuery, specificEpithetQuery, infraGenericQuery, infraSpecificQuery,
                             combinationAuthor, exCombinationAuthor, basionymAuthor, exBasionymAuthor,
                             compareAuthor, excludeBasionymAuthors, excludeExAuthors, rank);
        return result;
    }

    private List<SingleNameMatchingResult> normalizedMatchingScoreInOutputList (List<SingleNameMatchingResult> result,  String genusQuery,
            String specificEpithetQuery,
            String infraGenericQuery,
            String infraSpecificQuery,
            String combinationAuthor,
            String exCombinationAuthor,
            String basionymAuthor,
            String exBasionymAuthor,
            boolean compareAuthor,
            boolean excludeBasionymAuthors,
            boolean excludeExAuthors,
            Rank rank){

            Double lengthFullInputString = calculateLengthFullString(genusQuery, specificEpithetQuery, infraGenericQuery,
                    infraSpecificQuery, combinationAuthor, exCombinationAuthor, basionymAuthor, exBasionymAuthor, compareAuthor,
                    excludeBasionymAuthors, excludeExAuthors, rank);
            Double lenghtFullStringDB = 0.0;
            for (int x = 0 ; x < result.size(); x++) {
                String genusDB = result.get(x).getGenusOrUninomial();
                String specificEpithetDB = result.get(x).getSpecificEpithet();
                String infraGenericDB = result.get(x).getInfraGenericEpithet();
                String infraSpecificDB = result.get(x).getInfraSpecificEpithet();
                String combinationAuthorDB = result.get(x).getCombinationAuthorship();
                String exCombinationAuthorDB = result.get(x).getExCombinationAuthorship();
                String basionymAuthorDB = result.get(x).getBasionymAuthorship();
                String exBasionymAuthorDB = result.get(x).getExBasionymAuthorship();

                lenghtFullStringDB = calculateLengthFullString(genusDB, specificEpithetDB, infraGenericDB,
                        infraSpecificDB, combinationAuthorDB, exCombinationAuthorDB, basionymAuthorDB, exBasionymAuthorDB,
                        compareAuthor, excludeBasionymAuthors, excludeExAuthors, rank);

                Double largestStringLenght = Math.max(lenghtFullStringDB, lengthFullInputString);
                Double score = 1 - (result.get(x).getDistance()/largestStringLenght);
                String pattern = "#.###";
                DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
                otherSymbols.setDecimalSeparator('.');
                DecimalFormat decimalFormat = new DecimalFormat (pattern,otherSymbols);
                String formattedString = decimalFormat.format(score);
                Double scoreDouble = null;
                try {
                    scoreDouble = decimalFormat.parse(formattedString).doubleValue();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                result.get(x).setDistance(scoreDouble);
            }
            return result;
    }

    private Double calculateLengthFullString (
            String genus,
            String specificEpithet,
            String infraGeneric,
            String infraSpecific,
            String combinationAuthor,
            String exCombinationAuthor,
            String basionymAuthor,
            String exBasionymAuthor,
            boolean compareAuthor,
            boolean excludeBasionymAuthors,
            boolean excludeExAuthors,
            Rank rank){

        Double lengthFullInputString = 0.0;
        Double lengthInputName = calculateLengthName(genus, specificEpithet, infraGeneric,
                infraSpecific, rank);

        if (compareAuthor) {
            Double lengthFullAuthorship = calculateLenghtAuthorship(combinationAuthor, exCombinationAuthor,
                    basionymAuthor, exBasionymAuthor,
                    excludeBasionymAuthors, excludeExAuthors);
            lengthFullInputString = lengthInputName + lengthFullAuthorship;
            return lengthFullInputString;
        }
        return lengthInputName;
    }

    private Double calculateLenghtAuthorship (
          String combinationAuthor,
          String exCombinationAuthor,
          String basionymAuthor,
          String exBasionymAuthor,
          boolean excludeBasionymAuthors,
          boolean excludeExAuthors) {

        Double lengthCombinationAuthor = 0.0;
        Double lengthExCombinationAuthor = 0.0;
        Double lengthBasionymAuthor  = 0.0;
        Double lengthExBasionymAuthor = 0.0;
        Double lengthFullAuthorship = 0.0;

        if (combinationAuthor != null) {
            combinationAuthor = combinationAuthor.replace("&", "");
            combinationAuthor = combinationAuthor.replace(" ", "");
            lengthCombinationAuthor = new Double (combinationAuthor.length());
        }
        if (exCombinationAuthor != null) {
            lengthExCombinationAuthor = new Double (exCombinationAuthor.length());
        }
        if (basionymAuthor != null) {
            basionymAuthor = basionymAuthor.replace("&", "");
            basionymAuthor = basionymAuthor.replace(" ", "");
            lengthBasionymAuthor =  new Double (basionymAuthor.length());
        }
        if (exBasionymAuthor != null) {
            lengthExBasionymAuthor = new Double (exBasionymAuthor.length());
        }
        if (excludeBasionymAuthors) {
            if (excludeExAuthors) {
                lengthFullAuthorship = lengthCombinationAuthor;
            } else {
                lengthFullAuthorship = lengthCombinationAuthor + lengthExCombinationAuthor;
            }
        } else {
            if (excludeExAuthors) {
                lengthFullAuthorship = lengthCombinationAuthor + lengthBasionymAuthor;
            } else {
                lengthFullAuthorship = lengthCombinationAuthor + lengthExCombinationAuthor + lengthBasionymAuthor + lengthExBasionymAuthor;
            }
        }
        return lengthFullAuthorship/3;
    }

    private Double calculateLengthName(String genus, String specificEpithet, String infraGeneric,
            String infraSpecific, Rank rank) {

        Double lengthGenus = 0.0;
        Double lengthInfraGeneric = 0.0;
        Double lengthSpecificEpithet = 0.0;
        Double lengthInfraSpecific = 0.0;
        Double lenghtFullInputName;

        if (genus != null) {
            lengthGenus = new Double (genus.length());
        }
        if (infraGeneric != null) {
            lengthInfraGeneric = new Double (infraGeneric.length());
        }
        if (specificEpithet != null) {
            lengthSpecificEpithet= new Double (specificEpithet.length());
        }
        if (infraSpecific != null) {
            lengthInfraSpecific = new Double (infraSpecific.length());
        }
        lenghtFullInputName = lengthGenus + lengthInfraGeneric + lengthSpecificEpithet + lengthInfraSpecific;

        if (rank.getId() != 765 && rank.getId() != 774) {
            lenghtFullInputName = lenghtFullInputName + 1;
        }
        return lenghtFullInputName;
    }

    private List<SingleNameMatchingResult> compareInfraSpecific(String specificEpithetQuery, String infraSpecificQuery,
            Double maxDistance, List<SingleNameMatchingResult> taxonNamePartsWithDistance, Rank rank) {
        String normalizedInfraSpecificQuery = NameMatchingUtils.normalize(infraSpecificQuery);
        String phoneticNormalizedInfraSpecificQuery = NameMatchingUtils.nearMatch(normalizedInfraSpecificQuery);

        // 4. infra specific pre-filter
        List<SingleNameMatchingResult> preFilteredInfraSpecificListWithDistTemp = new ArrayList<>();
        for (int i = 0; i < taxonNamePartsWithDistance.size(); i++) {
           String x = taxonNamePartsWithDistance.get(i).getInfraSpecificEpithet();
            if (x != null && !x.isEmpty()) {
                preFilteredInfraSpecificListWithDistTemp.add(taxonNamePartsWithDistance.get(i));
            }
        }
        preFilteredInfraSpecificListWithDistTemp = compareSpecificEptihtet(specificEpithetQuery, maxDistance,
                preFilteredInfraSpecificListWithDistTemp, true);

        List<SingleNameMatchingResult> preFilteredInfraSpecificListWithDist = prefilterInfraSpecific(preFilteredInfraSpecificListWithDistTemp, normalizedInfraSpecificQuery, maxDistance);
        List<SingleNameMatchingResult> resultSetInfraSpecificListWithDist = new ArrayList<>();
        Double infraSpecificComputedDistance = 0.0;
        for (SingleNameMatchingResult infraSpecific: preFilteredInfraSpecificListWithDist) {
        	String infraSpecificInDB = infraSpecific.getInfraSpecificEpithet();
        	if (infraSpecificInDB.isEmpty()) {
                continue;
            }
        	String infraSpecificNameInDBNormalized = NameMatchingUtils.normalize(infraSpecificInDB);
            String phoneticNormalizedInfraSpecificNameInDB = NameMatchingUtils.nearMatch(infraSpecificNameInDBNormalized);

        		// 5. comparison of infra specific
            infraSpecificComputedDistance = nameMatchingComputeDistance(phoneticNormalizedInfraSpecificQuery ,
                    phoneticNormalizedInfraSpecificNameInDB);

            boolean rankEquals = infraSpecific.getRank().equals(rank);
            if (!rankEquals) {
                infraSpecificComputedDistance++;
            }
            Double totalDist = infraSpecific.getDistance() + infraSpecificComputedDistance;
            infraSpecific.setDistance(totalDist);

            // 6. infra specific post-filter
            resultSetInfraSpecificListWithDist.addAll(postfilterEpithet(6.0, infraSpecificQuery, infraSpecificComputedDistance,
            		normalizedInfraSpecificQuery, infraSpecific, infraSpecificInDB, totalDist));
        }
        return resultSetInfraSpecificListWithDist;
    }

    private List<SingleNameMatchingResult> compareSpecificEptihtet(String specificEpithetQuery, Double maxDistance,
            List<SingleNameMatchingResult> taxonNamePartsWithDistance, boolean isInfraSpecific) {

        List<SingleNameMatchingResult> resultSetEpithetListWithDist = new ArrayList<>();

        String normalizedEphitetQuery = NameMatchingUtils.normalize(specificEpithetQuery);
        String phoneticNormalizedEpithetQuery = NameMatchingUtils.nearMatch(normalizedEphitetQuery);

        // 4. epithet pre-filter
        Double infraSpecificComputedDistance = 0.0;
        List<SingleNameMatchingResult> preFilteredEpithetListWithDist= new ArrayList<>();
        if (isInfraSpecific == false) {
            filterNamesWithEpithets(taxonNamePartsWithDistance, preFilteredEpithetListWithDist);
            prefilterEpithet(preFilteredEpithetListWithDist, normalizedEphitetQuery, maxDistance);
            for (SingleNameMatchingResult preFilteredEpithet: preFilteredEpithetListWithDist) {
            	String epithetInDB = preFilteredEpithet.getSpecificEpithet();
            	if (epithetInDB == null || epithetInDB.isEmpty()) {
                    continue;
                }
                String epithetNameInDBNormalized = NameMatchingUtils.normalize(epithetInDB);
                String phoneticNormalizedEpithetNameInDB = NameMatchingUtils.nearMatch(epithetNameInDBNormalized);

            	// 5. comparison of epithet
                infraSpecificComputedDistance = nameMatchingComputeDistance(phoneticNormalizedEpithetQuery,
                        phoneticNormalizedEpithetNameInDB);
                Double totalDist = preFilteredEpithet.getDistance() + infraSpecificComputedDistance;
                preFilteredEpithet.setDistance(totalDist);

                // 6. species post-filter
                resultSetEpithetListWithDist.addAll(postfilterEpithet(maxDistance, specificEpithetQuery, infraSpecificComputedDistance,
                        normalizedEphitetQuery, preFilteredEpithet, epithetInDB, totalDist));
            }
        } else {
            for (SingleNameMatchingResult preFilteredEpithet: taxonNamePartsWithDistance) {
                String epithetInDB = preFilteredEpithet.getSpecificEpithet();
                String epithetNameInDBNormalized = NameMatchingUtils.normalize(epithetInDB);
                String phoneticNormalizedEpithetNameInDB = NameMatchingUtils.nearMatch(epithetNameInDBNormalized);

                // 5. comparison of epithet
                infraSpecificComputedDistance = nameMatchingComputeDistance(phoneticNormalizedEpithetQuery,
                        phoneticNormalizedEpithetNameInDB);
                Double totalDist = preFilteredEpithet.getDistance() + infraSpecificComputedDistance;
                preFilteredEpithet.setDistance(totalDist);

                // 6. species post-filter
                resultSetEpithetListWithDist.addAll(postfilterEpithet(maxDistance, specificEpithetQuery, infraSpecificComputedDistance,
                        normalizedEphitetQuery, preFilteredEpithet, epithetInDB, totalDist));
            }
        }
        return resultSetEpithetListWithDist;
    }

    /**
     * Define the maxDistance value also if not defined by the user
     * @param config
     * @param inputDistance
     * @param specificEpithetQuery
     * @param infraGenericQuery
     * @param infraSpecificQuery
     * @return maxDistance
     */
    private Double defineDistances(NameMatchingConfigurator config, Double inputDistance, String specificEpithetQuery,
            String infraGenericQuery, String infraSpecificQuery, Rank rank) {

        Double maxDistance;
        Double maxDisMonomial;
        Double maxDisBinomial;
        Double maxDisTrinomial;

        if (inputDistance != null) {
            config.setMaxDistance(inputDistance);
            maxDistance = config.getMaxDistance();
            maxDisMonomial = maxDistance;
            maxDisBinomial = maxDistance;
            maxDisTrinomial = maxDistance;
        } else {
            maxDistance = config.getMaxDistance();
            maxDisMonomial = 2.0;
            maxDisBinomial = 4.0;
            maxDisTrinomial = 6.0;
        }

        if (maxDistance == null) {
        	if (specificEpithetQuery != null && infraSpecificQuery == null || infraGenericQuery != null) {
        		maxDistance = maxDisBinomial;
        	} else if (rank.isInfraSpecific()){
        		maxDistance = maxDisTrinomial;
        	} else {
        		maxDistance = maxDisMonomial;
        	}
        }
        return maxDistance;
    }

    private List<SingleNameMatchingResult> compareInfrageneric(String infraGenericQuery, Double maxDistance,
            List<SingleNameMatchingResult> taxonNamePartsWithDistance) {
        String normalizedInfragenericQuery = NameMatchingUtils.normalize(infraGenericQuery);
        String phoneticNormalizedInfragenericQuery = NameMatchingUtils.nearMatch(normalizedInfragenericQuery);
        List<SingleNameMatchingResult> preFilteredInfragenericListWithDist = new ArrayList<>();

        filterNamesWithInfragenericEpithets(taxonNamePartsWithDistance, preFilteredInfragenericListWithDist);
        prefilterInfrageneric(preFilteredInfragenericListWithDist, normalizedInfragenericQuery, maxDistance);

        List <SingleNameMatchingResult> resultSetInfraGenericListWithDist = new ArrayList<>();
        for (SingleNameMatchingResult preFilteredInfrageneric:
        	preFilteredInfragenericListWithDist) {
        	String infragenericInDB = preFilteredInfrageneric.getInfraGenericEpithet();
           	String infragenericNameInDBNormalized = NameMatchingUtils.normalize(infragenericInDB);
        	String phoneticNormalizedInfragenericNameInDB = NameMatchingUtils.nearMatch(infragenericNameInDBNormalized);
        		// 5. comparison of infrageneric
        	Double infragenericComputedDistance = nameMatchingComputeDistance(phoneticNormalizedInfragenericQuery,
        			phoneticNormalizedInfragenericNameInDB);
        	Double totalDist = preFilteredInfrageneric.getDistance() + infragenericComputedDistance;
        	preFilteredInfrageneric.setDistance(totalDist) ;

        		// 6. infrageneric post-filter
        	postfilterInfrageneric(maxDistance, infraGenericQuery,
        			infragenericComputedDistance, normalizedInfragenericQuery,
        			resultSetInfraGenericListWithDist, preFilteredInfrageneric,
        			infragenericInDB, totalDist);
        }
        return resultSetInfraGenericListWithDist;
    }

    private Map<String, Double> compareGenus(String genusQuery, Double maxDistance, String phoneticNormalizedGenusQuery,
            List<String> preFilteredGenusOrUninominalList) {
        Map<String, Double> postFilteredGenusOrUninominalWithDis = new HashMap<>();
        Double genusComputedDistance;
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
        return postFilteredGenusOrUninominalWithDis;
    }

    private static String normalizeInput(String input) {
        if (input != null && !input.isEmpty()){
            input = input.replace(" and ", " & ");
            input = input.substring(0,1).toUpperCase() + input.substring(1);
            return input;
        } else {
            return "input name is empty";
        }
    }

    private static List<SingleNameMatchingResult> authorMatch(List<SingleNameMatchingResult> resultInput, boolean excludeBasionymAuthors,
            boolean excludeExAuthors, String authorshipCacheQuery, String combinationAuthor, String exCombinationAuthor, String basionymAuthor,
            Double maxDistance) {

        List<SingleNameMatchingResult> resultOutput = new ArrayList<>();

        for (SingleNameMatchingResult singleResult : resultInput){
            String authorCacheDB = singleResult.getAuthorshipCache();
            Double distanceAuthorComparison = new Double (NameMatchingUtils.modifiedDamerauLevenshteinDistance(
                    authorshipCacheQuery, authorCacheDB));
            distanceAuthorComparison = (distanceAuthorComparison/3)+singleResult.getDistance();
            if (distanceAuthorComparison == 0) {
                singleResult.setDistance(distanceAuthorComparison);
                resultOutput.add(singleResult);
            }
        }
        resultInput.removeAll(resultOutput);

        if (excludeBasionymAuthors == false) {
            if (excludeExAuthors == false) {
                for (SingleNameMatchingResult singleResult : resultInput){
                    String combinationAuthorshipResult = singleResult.getCombinationAuthorship();
                    if (combinationAuthorshipResult == null) {
                        combinationAuthorshipResult = "";
                    }
                    Double distanceCombinationAuthor = new Double(NameMatchingUtils.modifiedDamerauLevenshteinDistance(
                            combinationAuthor, combinationAuthorshipResult));
                    singleResult.setDistance((distanceCombinationAuthor / 3) + singleResult.getDistance());
                    if (singleResult.getDistance() <= maxDistance) {
                        resultOutput.add(singleResult);
                    }
                }
                return resultOutput;
            }
        }
        if (excludeBasionymAuthors) {
            if (excludeExAuthors) {
                for (SingleNameMatchingResult singleResult : resultInput){
                    String combinationAuthorshipResult = singleResult.getCombinationAuthorship();
                    if (combinationAuthorshipResult == null) {
                        combinationAuthorshipResult = "";
                    }
                    Double distanceCombinationAuthor = new Double(NameMatchingUtils.modifiedDamerauLevenshteinDistance(
                            combinationAuthor, combinationAuthorshipResult));
                    singleResult.setDistance((distanceCombinationAuthor / 3) + singleResult.getDistance());
                    if (singleResult.getDistance() <= maxDistance) {
                        resultOutput.add(singleResult);
                    }
                }
                return resultOutput;
            }

            for (SingleNameMatchingResult singleResult : resultInput){
                String exCombinationAuthorDB = singleResult.getExCombinationAuthorship();
                String combinationAuthorDB = singleResult.getCombinationAuthorship();
                String combinedAuthorCacheDB = exCombinationAuthorDB  + " ex " +  combinationAuthorDB;
                String combinedAuthorCacheQuery = exCombinationAuthor + " ex " + combinationAuthor;
                Double distanceAuthorComparison = new Double (NameMatchingUtils.modifiedDamerauLevenshteinDistance(combinedAuthorCacheDB, combinedAuthorCacheQuery));
                singleResult.setDistance((distanceAuthorComparison/3) + singleResult.getDistance());
            }
            for (SingleNameMatchingResult singleResult : resultInput){
                if (singleResult.getDistance() <= maxDistance) {
                    resultOutput.add(singleResult);
                }
            }
            return resultOutput;
        }
        if (excludeBasionymAuthors == false) {
            if (excludeExAuthors == false) {
                for (SingleNameMatchingResult singleResult : resultInput){
                    Double distanceAuthorComparison = new Double (NameMatchingUtils.modifiedDamerauLevenshteinDistance(authorshipCacheQuery,
                            singleResult.getAuthorshipCache()));
                    singleResult.setDistance((distanceAuthorComparison/3) + singleResult.getDistance());
                }
                for (SingleNameMatchingResult singleResult : resultInput){
                    if (singleResult.getDistance() <= maxDistance) {
                        resultOutput.add(singleResult);
                    }
                }
                return resultOutput;
            } else if (excludeExAuthors) {
                for (SingleNameMatchingResult singleResult : resultInput){
                    String combinationAuthorDB = singleResult.getCombinationAuthorship();
                    String basionymAuthorDB = singleResult.getBasionymAuthorship();
                    Double distanceAuthorComparison = new Double (NameMatchingUtils.modifiedDamerauLevenshteinDistance(combinationAuthorDB, combinationAuthor));
                    Double distanceAuthorComparison2 = new Double (NameMatchingUtils.modifiedDamerauLevenshteinDistance(basionymAuthorDB, basionymAuthor));

                    singleResult.setDistance((distanceAuthorComparison/3)+(distanceAuthorComparison2/3) + singleResult.getDistance());
                }
                for (SingleNameMatchingResult singleResult : resultInput){
                    if (singleResult.getDistance() <= maxDistance) {
                        resultOutput.add(singleResult);
                    }
                }
            }
            return resultOutput;
        } else {
            return null;
        }
    }

    private void filterNamesWithEpithets(List<SingleNameMatchingResult> taxonNamePartsWithDistance,
            List<SingleNameMatchingResult> preFilteredEpithetListWithDist) {
        for (int i = 0; i < taxonNamePartsWithDistance.size(); i++) {
            String infrageneric = taxonNamePartsWithDistance.get(i).getInfraGenericEpithet();
            String infraspecific = taxonNamePartsWithDistance.get(i).getInfraSpecificEpithet();
            if (infrageneric == null ||  infrageneric.isEmpty()){
                if (infraspecific == null || infraspecific.isEmpty()) {
                    preFilteredEpithetListWithDist.add(taxonNamePartsWithDistance.get(i));
                }
            }
        }
    }

    /**
     * Filter all names from the DB that contain an infrageneric epithet (exclude all names that have epithet or infraspecfic epithet)
     * @param taxonNamePartsWithDistance
     * @param preFilteredInfragenericListWithDist
     */
	private void filterNamesWithInfragenericEpithets(
			List<SingleNameMatchingResult> taxonNamePartsWithDistance,
			List<SingleNameMatchingResult> preFilteredInfragenericListWithDist) {
		for (int i = 0; i < taxonNamePartsWithDistance.size(); i++) {
		    if (taxonNamePartsWithDistance.get(i).getSpecificEpithet().isEmpty() &&
		    		taxonNamePartsWithDistance.get(i).getInfraSpecificEpithet().isEmpty() &&
		    		StringUtils.isNotEmpty(taxonNamePartsWithDistance.get(i).getInfraGenericEpithet())) {
		    	preFilteredInfragenericListWithDist.add(taxonNamePartsWithDistance.get(i));
		    }
		}
	}

    /** Filters only names with genus or uninomial part
	 * @param taxonNamePartsWithDistance
	 * @param resultSetOnlyGenusOrUninominal
	 * @return List of Genus or Uninomial names only + score (filters out all bi/trinomial names)
	 */
	private void filterMatchingMonomialFromResultSet(
			List<SingleNameMatchingResult> taxonNamePartsWithDistance,
			List<SingleNameMatchingResult> resultSetOnlyGenusOrUninominal) {
		for (int i = 0; i < taxonNamePartsWithDistance.size(); i++) {
			String epi = taxonNamePartsWithDistance.get(i).getSpecificEpithet();
			String infge= taxonNamePartsWithDistance.get(i).getInfraGenericEpithet();
			String infraspec = taxonNamePartsWithDistance.get(i).getInfraSpecificEpithet();
			if ((epi == null || epi.isEmpty()) &&
			        (infge == null || infge.isEmpty()) &&
			        (infraspec == null || infraspec.isEmpty())) {
			        resultSetOnlyGenusOrUninominal.add(taxonNamePartsWithDistance.get(i));
			}
		}
	}

    /** Fetch all name parts of genera or uninomial that passed the post filter
	 * @param postFilteredGenusOrUninominalWithDis
	 * @return List of taxons under genera or uninomial rank + calculated distance
	 */
    private List<SingleNameMatchingResult> getTaxonNamePartsFromDB(
    		Map<String, Double> postFilteredGenusOrUninominalWithDis) {

        List<SingleNameMatchingResult> genusOrUninomialWithDistance = new ArrayList<>();

        if (postFilteredGenusOrUninominalWithDis.isEmpty()) {
            return genusOrUninomialWithDistance;
        } else {
            List<NameMatchingParts> fullNameMatchingPartsListTemp = nameMatchingDao.findNameMatchingParts(postFilteredGenusOrUninominalWithDis);
            postFilteredGenusOrUninominalWithDis.forEach((key, value) -> {
                for (NameMatchingParts fullNameMatchingParts : fullNameMatchingPartsListTemp) {
                    if (fullNameMatchingParts.getGenusOrUninomial().equals(key)) {
        			genusOrUninomialWithDistance.add(new SingleNameMatchingResult(fullNameMatchingParts, value));
                    }
                }
            });
        return genusOrUninomialWithDistance;
        }
    }

    /**
     * Deletes common characters at the beginning and end of both parameters.
     * Returns the space separated concatenation of the remaining strings. <BR>
     */
    public static String trimCommonChar(String queryName, String dbName) {

        String shortenedQueryName = "";
        String shortenedDBName = "";
        String tempQueryName;
        String tempDBName;

        // trim common leading characters of two strings

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

        // trim common tailing characters between two strings

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

    private Double nameMatchingComputeDistance(String strQuery, String strDB) {
        Double computedDistanceTemp;
        String trimmedStrings = trimCommonChar(strQuery, strDB);
        String restantTrimmedQuery = "";
        String restantTrimmedDB="";

        if ("".equals(trimmedStrings)) {
            computedDistanceTemp = 0.0;
        } else {
        	try {
                restantTrimmedQuery = trimmedStrings.split(" ")[0];
                restantTrimmedDB = trimmedStrings.split(" ")[1];}
        	catch (Exception e) {
        	}
            computedDistanceTemp = new Double (NameMatchingUtils.modifiedDamerauLevenshteinDistance(restantTrimmedQuery,
                    restantTrimmedDB));
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

    private boolean postfilterGenus(Double maxDistance, String genusQuery, Double genusComputedDistance,
            String phoneticNormalizedGenusQuery, String preFilteredGenusOrUninominal,
            String phoneticNormalizedGenusInDB) {

        int genusQueryLength = genusQuery.length();
        int genusDBLength = preFilteredGenusOrUninominal.length();
        int halfLength = Math.max(genusQueryLength, genusDBLength) / 2;
        boolean postFilter = false;

        if (genusComputedDistance <= maxDistance) {
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
            List<SingleNameMatchingResult> preFilteredEpithetListWithDist,
            String normalizedEphitetQuery, Double maxDistance) {
        List<SingleNameMatchingResult> fullNameMatchingPartsListTemp = new ArrayList<>();
        for (SingleNameMatchingResult fullNameMatchingParts : preFilteredEpithetListWithDist) {
            String specificEpithet = fullNameMatchingParts.getSpecificEpithet();
            if (specificEpithet != null && fullNameMatchingParts.getSpecificEpithet().length()
                    - normalizedEphitetQuery.length() <= maxDistance) {
                fullNameMatchingPartsListTemp.add(fullNameMatchingParts);
                preFilteredEpithetListWithDist = fullNameMatchingPartsListTemp;
            }
        }
    }

    private List<SingleNameMatchingResult> postfilterEpithet(Double maxDistance, String epithetQuery,
            Double epithetComputedDistance, String normalizedEphitetQuery, SingleNameMatchingResult part,
            String epithetInDB, Double totalDist) {
        List<SingleNameMatchingResult> epithetListTemp = new ArrayList<>();
        Double epithetQueryLength = new Double (epithetQuery.length());
        Double epithetDBLength = new Double (epithetInDB.length());
        Double halfLength = Math.max(epithetDBLength, epithetQueryLength) / 2;

        if (totalDist <= maxDistance) {
            epithetListTemp.add(part);
        } else if (epithetQueryLength <= maxDistance) {
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

    private void postfilterInfrageneric(Double maxDistance, String infragenericQuery,
            Double infragenericComputedDistance, String normalizedInfragenericQuery,
            List<SingleNameMatchingResult> infragenericList,
            SingleNameMatchingResult fullTaxonNamePart, String infragenericInDB, Double totalDist) {
        int epithetQueryLength = infragenericQuery.length();
        int epithetDBLength = infragenericInDB.length();
        int halfLength = Math.max(epithetDBLength, epithetQueryLength) / 2;

        if (totalDist <= maxDistance) {
            infragenericList.add(fullTaxonNamePart);
        } else if (epithetQueryLength < maxDistance) {
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

    public static List<SingleNameMatchingResult> candidatesResults(
            List<SingleNameMatchingResult> list, Double maxDistance) {
        List<SingleNameMatchingResult> bestResults = new ArrayList<>();
        for (SingleNameMatchingResult best : list) {
            Double calculatedDistance = best.getDistance();
            if (calculatedDistance <= maxDistance) {
                bestResults.add(best);
            }
        }
        return bestResults;
    }

    private void prefilterInfrageneric(
            List<SingleNameMatchingResult> preFilteredInfragenericListWithDist,
            String normalizedInfragenericQuery, Double maxDistance) {
        List<SingleNameMatchingResult> fullNameMatchingPartsListTemp = new ArrayList<>();
        for (SingleNameMatchingResult fullNameMatchingParts : preFilteredInfragenericListWithDist) {
            if (fullNameMatchingParts.getInfraGenericEpithet().length()
                    - normalizedInfragenericQuery.length() <= maxDistance) {
                fullNameMatchingPartsListTemp.add(fullNameMatchingParts);
                preFilteredInfragenericListWithDist = fullNameMatchingPartsListTemp;
            }
        }
    }

    private List<SingleNameMatchingResult> prefilterInfraSpecific(
			List<SingleNameMatchingResult> preFilteredInfraSpecificListWithDistTemp,
			String normalizedInfraSpecificQuery, Double maxDistance) {
		List<SingleNameMatchingResult> fullNameMatchingPartsListTemp = new ArrayList<>();
		for (SingleNameMatchingResult fullNameMatchingParts : preFilteredInfraSpecificListWithDistTemp) {
		    int infraSpecificEpithetLength = fullNameMatchingParts.getInfraSpecificEpithet().length();
		    int infraSpecificEpithetQueryLength = normalizedInfraSpecificQuery.length();
			if (infraSpecificEpithetLength - infraSpecificEpithetQueryLength <= maxDistance) {
				fullNameMatchingPartsListTemp.add(fullNameMatchingParts);
				preFilteredInfraSpecificListWithDistTemp = fullNameMatchingPartsListTemp;
			}
		}
		return preFilteredInfraSpecificListWithDistTemp;
	}
}