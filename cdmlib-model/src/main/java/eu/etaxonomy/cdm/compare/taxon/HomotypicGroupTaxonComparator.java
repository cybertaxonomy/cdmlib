/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.compare.taxon;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * This class orders synonyms of a homotypic group,
 * first by
 * <ul>
 *  <li>Basionym groups (the basionym and all names derived from this basionym)
 *      should be kept together in a subgroup</li>
 *  <li>The order of the subgroups is defined by the ordering of their
 *       basionyms (according to the following ordering)</li>
 *  <li>If a name is illegitimate or not does play a role for ordering</li>
 *  <li>Names with publication year should always come first</li>
 *  <li>Names with no publication year are sorted by rank</li>
 *  <li>Names with no publication year and equal rank are sorted alphabetically</li>
 *  <li>If 2 names have a replaced synonym relationship the replaced synonym comes first,
 *      the replacement name comes later as this reflects the order of publication</li>
 *  </ul>
 *
 * Details on ordering are explained at https://dev.e-taxonomy.eu/redmine/issues/3338<BR>
 *
 * @author a.mueller
 * @since 02.03.2016
 */
public class HomotypicGroupTaxonComparator extends TaxonComparator {

    private static final long serialVersionUID = -5088210641256430878L;
    private static final Logger logger = LogManager.getLogger();

    private final TaxonBase<?> firstTaxonInGroup;
    private final TaxonName firstNameInGroup;

    public HomotypicGroupTaxonComparator(@SuppressWarnings("rawtypes") TaxonBase firstTaxonInGroup) {
        super(true);
        this.firstTaxonInGroup = firstTaxonInGroup;
        this.firstNameInGroup = firstTaxonInGroup == null ? null: firstTaxonInGroup.getName();
    }

    public HomotypicGroupTaxonComparator(@SuppressWarnings("rawtypes") TaxonBase firstTaxonInGroup, boolean includeRanks) {
        super(includeRanks);
        this.firstTaxonInGroup = firstTaxonInGroup;
        this.firstNameInGroup = firstTaxonInGroup == null ? null: firstTaxonInGroup.getName();
    }

    public HomotypicGroupTaxonComparator(TaxonName firstNameInGroup, boolean includeRanks) {
        super(includeRanks);
        firstTaxonInGroup = null;
        this.firstNameInGroup = firstNameInGroup;
    }

    /**
     *
     * @see TaxonComparator#compare(TaxonBase, TaxonBase)
     * @see java.lang.String#compareTo(String)
     * @see	java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(
            @SuppressWarnings("rawtypes") TaxonBase taxonBase1,
            @SuppressWarnings("rawtypes") TaxonBase taxonBase2) {

        TaxonName name1 = taxonBase1.getName();
        TaxonName name2 = taxonBase2.getName();

        return compareNames(name1, name2, taxonBase1, taxonBase2);
    }

    public int compareNames(TaxonName name1,  TaxonName name2, TaxonBase<?> taxonBase1, TaxonBase<?> taxonBase2) {
        if (logger.isDebugEnabled()){logger.debug(name1.getTitleCache() +" : "+ name2.getTitleCache());}
        if (name1 == null && taxonBase1 == null ||
                name2 == null && taxonBase2 == null){
            throw new IllegalArgumentException("There should always be either a name or a taxon to be compared");
        }

        int compareStatus = compareStatus(name1, name2);
        if (compareStatus != 0){
            return compareStatus;
        }

        //not same homotypic group -
        //NOTE: this comparator should usually not be used
        //      for comparing names of different homotypic groups.
        //      The following is only to have a defined compare behavior
        //      which follows the contract of Comparator#compare.
        if (name1 == null ||
            name2 == null ||
            ! name1.getHomotypicalGroup().equals(name2.getHomotypicalGroup())){

            String compareString1 = name1 != null ?
                    name1.getHomotypicalGroup().getUuid().toString() :
                    taxonBase1.getUuid().toString();
            String compareString2 = name2 != null ?
                    name2.getHomotypicalGroup().getUuid().toString() :
                    taxonBase2.getUuid().toString();
            int result = compareString1.compareTo(compareString2);
            return result;
        }

        //same homotypic group ...
        //one taxon is first in group
        if (isFirstInGroup(taxonBase1, name1)){
            return -1;
        }else if (taxonBase2 != null && taxonBase2.equals(firstTaxonInGroup)){
            return 1;
        }

        //same name => compare on taxon level
        if (name1.equals(name2)){
            return super.compare(taxonBase1, taxonBase2);  //if name is the same compare on taxon level
        }

        TaxonName basionym1 = getPreferredInBasionymGroup(name1);
        TaxonName basionym2 = getPreferredInBasionymGroup(name2);

        int compareResult;
        if (basionym1.equals(basionym2)){
            //both names belong to same basionym sub-group
            compareResult = handleSameBasionym(basionym1, name1, name2);
        }else{
            compareResult = compareBasionyms(basionym1, basionym2);
        }

        if (compareResult != 0){
//            if (logger.isDebugEnabled()){logger.debug(": " + compareResult);}
            return compareResult;
        }else{
            //names are uncomparable on name level (except for uuid, id, etc.)
            int result = super.compare(taxonBase1, taxonBase2);
            if (logger.isDebugEnabled()){logger.debug(": = " + result);}
            return result;
        }
    }

    private boolean isFirstInGroup(TaxonBase<?> taxonBase, TaxonName name) {
        if (taxonBase != null){
            return taxonBase.equals(firstTaxonInGroup);
        }else{
            return name.equals(firstNameInGroup);
        }
    }

    /**
     * Compare 2 names which have the same basionym.
     * The names must not be equal to each other but may be equal
     * to the basionym.
     *
     * @param basionym the basionym
     * @param name1 first name to compare
     * @param name2 second name to compare
     * @return compare value according to the {@link Comparator#compare(Object, Object)} contract.
     */
    private int handleSameBasionym(TaxonName basionym,
            TaxonName name1,
            TaxonName name2) {

        if (basionym.equals(name1)){
            return -1;
        }else if (basionym.equals(name2)){
            return 1;
        }else{
            return super.compare(name1, name2, false);
        }

    }

    private int compareBasionyms(TaxonName basionym1Orig, TaxonName basionym2Orig) {
        //one taxon is first in group
        TaxonName basionym1 = getFirstNameInGroup(basionym1Orig);
        TaxonName basionym2 = getFirstNameInGroup(basionym2Orig);

        //handle accepted taxon case
        if (basionym1.equals(firstNameInGroup)){
            return -1;
        }else if (basionym2.equals(firstNameInGroup)){
            return 1;
        }

        //handle replaced synonyms
        boolean basio2IsReplacedSynForBasio1 = getReplacedSynonymClosure(basionym1).contains(basionym2);
        boolean basio1IsReplacedSynForBasio2 = getReplacedSynonymClosure(basionym2).contains(basionym1);

        if (basio2IsReplacedSynForBasio1 && !basio1IsReplacedSynForBasio2){
            return 1;
        }else if (basio1IsReplacedSynForBasio2 && !basio2IsReplacedSynForBasio1){
            return -1;
        }

        //compare by date, nom. illeg., rank and alphabetically
        return super.compare(basionym1, basionym2, true);
    }

    private TaxonName getFirstNameInGroup(TaxonName basionym) {
        for (NameRelationship nameRel : basionym.getRelationsFromThisName()){
            if (nameRel.getType() != null && nameRel.getType().equals(NameRelationshipType.BASIONYM())){
                if (nameRel.getToName().equals(firstNameInGroup)){
                    return firstNameInGroup;
                }
            }
        }
        return basionym;
    }

    private Set<TaxonName> getReplacedSynonymClosure(TaxonName name) {
        Set<TaxonName> set = name.getReplacedSynonyms();
        if (set.isEmpty()){
            return set;
        }
        Set<TaxonName> result = new HashSet<>();
        for (TaxonName replSyn : set){
            boolean notYetContained = result.add(replSyn);
            if (notYetContained){
                result.addAll(replSyn.getReplacedSynonyms());
            }
        }
        return result;
    }

    private TaxonName getPreferredInBasionymGroup(TaxonName name) {
        Set<TaxonName> candidates = new HashSet<>();
        //get all final basionyms, except for those being part of a basionym circle
        for (TaxonName candidate : name.getBasionyms()){
            if (candidate != null
                    && candidate.getHomotypicalGroup().equals(name.getHomotypicalGroup())
                    && !hasBasionymCircle(candidate, null)){
                candidate = getPreferredInBasionymGroup(candidate);
                candidates.add(candidate);
            }
        }

        if (candidates.isEmpty()){
            return name;
        }else if (candidates.size() == 1){
            return candidates.iterator().next();
        }else{
            TaxonName result = candidates.iterator().next();
            candidates.remove(result);
            for (TaxonName candidate : candidates){
                if (super.compare(result, candidate, false) > 0){
                    result = candidate;
                }
            }
            return result;
        }
    }

    private boolean hasBasionymCircle(TaxonName name, Set<TaxonName> existing) {
        if (existing == null){
            existing = new HashSet<>();
        }
        if (existing.contains(name)){
            return true;
        }else{
            Set<TaxonName> basionyms = name.getBasionyms();
            if (basionyms.isEmpty()){
                return false;
            }
            existing.add(name);
            for (TaxonName basionym : basionyms){
                if (hasBasionymCircle(basionym, existing)){
                    return true;
                }
            }
            return false;
        }
    }
}