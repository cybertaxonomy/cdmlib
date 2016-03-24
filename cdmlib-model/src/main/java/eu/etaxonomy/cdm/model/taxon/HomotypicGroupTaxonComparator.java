/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

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
 * Details on ordering are explained at http://dev.e-taxonomy.eu/trac/ticket/3338<BR>
 *
 * @author a.mueller
 * @created 02.03.2016
 */
public class HomotypicGroupTaxonComparator extends TaxonComparator {
    private static final long serialVersionUID = -5088210641256430878L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(HomotypicGroupTaxonComparator.class);

    private final TaxonBase<?> firstTaxonInGroup;
    private final TaxonNameBase<?,?> firstNameInGroup;
//    private final HomotypicalGroupComparator homotypicGroupComparator;

    /**
     * @param firstNameInGroup
     */
    public HomotypicGroupTaxonComparator(@SuppressWarnings("rawtypes") TaxonBase firstTaxonInGroup) {
        super(true);
        this.firstTaxonInGroup = firstTaxonInGroup;
        this.firstNameInGroup = firstTaxonInGroup == null ? null: firstTaxonInGroup.getName();
    }

    /**
     * @param firstNameInGroup
     */
    public HomotypicGroupTaxonComparator(@SuppressWarnings("rawtypes") TaxonBase firstTaxonInGroup, boolean includeRanks) {
        super(includeRanks);
        this.firstTaxonInGroup = firstTaxonInGroup;
        this.firstNameInGroup = firstTaxonInGroup == null ? null: firstTaxonInGroup.getName();
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

        TaxonNameBase<?,?> name1 = taxonBase1.getName();
        TaxonNameBase<?,?> name2 = taxonBase2.getName();
//      System.out.println(name1.getTitleCache() +" : "+ name2.getTitleCache());


        int compareStatus = compareStatus(name1, name2);
        if (compareStatus != 0){
            return compareStatus;
        }

        //not same homotypical group -
        //NOTE: this comparator should usually not be used
        //      for comparing names of different homotypical groups.
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

        //same homotypical group ...
        //one taxon is first in group
        if (taxonBase1.equals(firstTaxonInGroup)){
            return -1;
        }else if (taxonBase2.equals(firstTaxonInGroup)){
            return 1;
        }

        //same name => compare on taxon level
        if (name1.equals(name2)){
            return super.compare(taxonBase1, taxonBase2);  //if name is the same compare on taxon level
        }

        TaxonNameBase<?,?> basionym1 = getPreferredInBasionymGroup(name1);
        TaxonNameBase<?,?> basionym2 = getPreferredInBasionymGroup(name2);

        int compareResult;
        if (basionym1.equals(basionym2)){
            //both names belong to same basionym sub-group
            compareResult = handleSameBasionym(basionym1, name1, name2);
        }else{
            compareResult = compareBasionyms(basionym1, basionym2);
        }

        if (compareResult != 0){
//          System.out.println(": " + compareResult);
            return compareResult;
        }else{
            //names are uncomparable on name level (except for uuid, id, etc.)
            int result = super.compare(taxonBase1, taxonBase2);
//          System.out.println(": = " + result);
            return result;
        }
    }

//    /**
//     * @param homotypicalGroup
//     * @return
//     */
//    private TaxonBase<?> getFirstInHomotypicalGroup(HomotypicalGroup homotypicalGroup, Collection<TaxonBase<?>> existing) {
//        List<TaxonBase<?>> candidates =  new ArrayList<TaxonBase<?>>();
//        for (TaxonBase<?> candidate : existing){
//            if (homotypicalGroup.getTypifiedNames().contains(candidate.getName())){
//                candidates.add(candidate);
//            }
//        }
//        Collections.sort(candidates, this);
//        return candidates.isEmpty() ? null : candidates.get(0);
//    }


    /**
     * Compare 2 names which have the same basionym.
     * The names must not be equal to each other but may be equal
     * to the basionym.
     * @param basionym the basionym
     * @param name1 first name to compare
     * @param name2 second name to compare
     * @return compare value according to the {@link Comparator#compare(Object, Object)} contract.
     */
    private int handleSameBasionym(TaxonNameBase<?, ?> basionym,
            TaxonNameBase<?, ?> name1,
            TaxonNameBase<?, ?> name2) {

        if (basionym.equals(name1)){
            return -1;
        }else if (basionym.equals(name2)){
            return 1;
        }else{
            super.compare(name1, name2);
        }
        return 0;
    }

    /**
     * @param basionym1
     * @param basionym2
     * @return
     */
    private int compareBasionyms(TaxonNameBase<?,?> basionym1Orig, TaxonNameBase<?,?> basionym2Orig) {
        //one taxon is first in group
        TaxonNameBase<?,?> basionym1 = getFirstNameInGroup(basionym1Orig);
        TaxonNameBase<?,?> basionym2 = getFirstNameInGroup(basionym2Orig);

        if (basionym1.equals(firstNameInGroup)){
            return -1;
        }else if (basionym2.equals(firstNameInGroup)){
            return 1;
        }

        boolean basio2IsReplacedSynForBasio1 = getReplacedSynonymClosure(basionym1).contains(basionym2);
        boolean basio1IsReplacedSynForBasio2 = getReplacedSynonymClosure(basionym2).contains(basionym1);

        if (basio2IsReplacedSynForBasio1 && !basio1IsReplacedSynForBasio2){
            return 1;
        }else if (basio1IsReplacedSynForBasio2 && !basio2IsReplacedSynForBasio1){
            return -1;
        }

        int result = super.compare(basionym1, basionym2);
        return result;
    }

    /**
     * @param basionym
     * @return
     */
    private TaxonNameBase<?, ?> getFirstNameInGroup(TaxonNameBase<?, ?> basionym) {
        for (NameRelationship nameRel : basionym.getRelationsFromThisName()){
            if (nameRel.getType() != null && nameRel.getType().equals(NameRelationshipType.BASIONYM())){
                if (nameRel.getToName().equals(firstNameInGroup)){
                    return firstNameInGroup;
                }
            }
        }
        return basionym;
    }

    /**
     * @param basionym1
     * @return
     */
    @SuppressWarnings("rawtypes")
    private Set<TaxonNameBase> getReplacedSynonymClosure(TaxonNameBase<?, ?> name) {
        Set<TaxonNameBase> set = name.getReplacedSynonyms();
        if (set.isEmpty()){
            return set;
        }
        Set<TaxonNameBase> result = new HashSet<TaxonNameBase>();
        for (TaxonNameBase<?,?> replSyn : set){
            boolean notYetContained = result.add(replSyn);
            if (notYetContained){
                result.addAll(replSyn.getReplacedSynonyms());
            }
        }
        return result;
    }

    /**
     * @param name
     * @return
     */
    private TaxonNameBase<?,?> getPreferredInBasionymGroup(TaxonNameBase<?,?> name) {
        Set<TaxonNameBase<?,?>> candidates = new HashSet<TaxonNameBase<?,?>>();
        //get all final basionyms, except for those being part of a basionym circle
        for (TaxonNameBase<?,?> candidate : name.getBasionyms()){
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
            TaxonNameBase<?,?> result = candidates.iterator().next();
            candidates.remove(result);
            for (TaxonNameBase<?,?> candidate : candidates){
                if (super.compare(result, candidate) > 0){
                    result = candidate;
                }
            }
            return result;
        }
    }

    /**
     * @param candidate
     * @return
     */
    private boolean hasBasionymCircle(TaxonNameBase<?, ?> name, Set<TaxonNameBase<?,?>> existing) {
        if (existing == null){
            existing = new HashSet<TaxonNameBase<?,?>>();
        }
        if (existing.contains(name)){
            return true;
        }else{
            Set<TaxonNameBase> basionyms = name.getBasionyms();
            if (basionyms.isEmpty()){
                return false;
            }
            existing.add(name);
            for (TaxonNameBase basionym : basionyms){
                if (hasBasionymCircle(basionym, existing)){
                    return true;
                }
            }
            return false;
        }
    }

}
