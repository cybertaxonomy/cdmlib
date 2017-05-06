/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonComparator;

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
 * @author k.luther
 * @date 20.03.2017
 *
 */
public class HomotypicalGroupNameComparator implements Comparator<TaxonNameBase>, Serializable{

    private static final Logger logger = Logger.getLogger(HomotypicalGroupNameComparator.class);


        private final TaxonNameBase<?,?> firstNameInGroup;
        private boolean includeRanks = false;

        /**
         * @param firstNameInGroup
         */
        public HomotypicalGroupNameComparator(@SuppressWarnings("rawtypes") TaxonNameBase firstNameInGroup, boolean includeRanks) {
            super();
            this.firstNameInGroup = firstNameInGroup;
            this.includeRanks = includeRanks;
        }


        /**
         *
         * @see TaxonComparator#compare(TaxonBase, TaxonBase)
         * @see java.lang.String#compareTo(String)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(
                @SuppressWarnings("rawtypes") TaxonNameBase taxonNameBase1,
                @SuppressWarnings("rawtypes") TaxonNameBase taxonNameBase2) {


            if (logger.isDebugEnabled()){logger.debug(taxonNameBase1.getTitleCache() +" : "+ taxonNameBase2.getTitleCache());}


            int compareStatus = compareStatus(taxonNameBase1, taxonNameBase2);
            if (compareStatus != 0){
                return compareStatus;
            }

            //not same homotypical group -
            //NOTE: this comparator should usually not be used
            //      for comparing names of different homotypical groups.
            //      The following is only to have a defined compare behavior
            //      which follows the contract of Comparator#compare.
            if (taxonNameBase1 == null ||
                    taxonNameBase2 == null ||
                ! taxonNameBase1.getHomotypicalGroup().equals(taxonNameBase2.getHomotypicalGroup())){

                String compareString1 =
                        taxonNameBase1.getHomotypicalGroup().getUuid().toString() ;
                String compareString2 =
                        taxonNameBase2.getHomotypicalGroup().getUuid().toString() ;
                int result = compareString1.compareTo(compareString2);
                return result;
            }

            //same homotypical group ...
            //one taxon is first in group
            if (taxonNameBase1.equals(firstNameInGroup)){
                return -1;
            }else if (taxonNameBase2.equals(firstNameInGroup)){
                return 1;
            }



            TaxonNameBase<?,?> basionym1 = getPreferredInBasionymGroup(taxonNameBase1);
            TaxonNameBase<?,?> basionym2 = getPreferredInBasionymGroup(taxonNameBase2);

            int compareResult;
            if (basionym1.equals(basionym2)){
                //both names belong to same basionym sub-group
                compareResult = handleSameBasionym(basionym1, taxonNameBase1, taxonNameBase2);
            }else{
                compareResult = compareBasionyms(basionym1, basionym2);
            }

           return compareResult;
           }


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
                this.compare(name1, name2, false);
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
            return this.compare(basionym1, basionym2, false);

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
                    if (this.compare(result, candidate) > 0){
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


    //  /**
    //   * @param homotypicalGroup
    //   * @return
    //   */
    //  private TaxonBase<?> getFirstInHomotypicalGroup(HomotypicalGroup homotypicalGroup, Collection<TaxonBase<?>> existing) {
//          List<TaxonBase<?>> candidates =  new ArrayList<TaxonBase<?>>();
//          for (TaxonBase<?> candidate : existing){
//              if (homotypicalGroup.getTypifiedNames().contains(candidate.getName())){
//                  candidates.add(candidate);
//              }
//          }
//          Collections.sort(candidates, this);
//          return candidates.isEmpty() ? null : candidates.get(0);
    //  }

        /**
         * @param taxonNameBase
         * @param taxonNameBase2
         * @param statusCompareWeight
         * @return
         */
        protected int compareStatus(TaxonNameBase<?,?> taxonNameBase, TaxonNameBase<?,?> taxonNameBase2) {
            int statusCompareWeight = 0;
            statusCompareWeight += computeStatusCompareWeight(taxonNameBase);
            statusCompareWeight -= computeStatusCompareWeight(taxonNameBase2);
            return statusCompareWeight;
        }

        /**
         * @param taxonBase1
         * @param statusCompareWeight
         * @return
         */
        private int computeStatusCompareWeight(TaxonNameBase<?,?> taxonNameBase) {
            int result = 0;
            if (taxonNameBase == null || taxonNameBase.getStatus() == null){
                return 0;
            }
            Set<NomenclaturalStatus> status1 = taxonNameBase.getStatus();
            for (NomenclaturalStatus nomStatus1 : status1){
                NomenclaturalStatusType type = nomStatus1.getType();
                if (type != null && type.isInvalidType()){
                    if(type.equals(NomenclaturalStatusType.PROVISIONAL())){
                        result += 1;
                    }else if (type.equals(NomenclaturalStatusType.INVALID())){
                        result += 2;
                    }else if(type.equals(NomenclaturalStatusType.COMBINATION_INVALID())){
                        result += 2;
                    }else if (type.equals(NomenclaturalStatusType.OPUS_UTIQUE_OPPR())){
                        result += 2;
                    }else if(type.equals(NomenclaturalStatusType.NUDUM())){
                        result += 3;
                    }
                    result += 1;
                }
            }
            return result;
        }
        /**
        *
        * @param name1
        * @param name2
        * @param includeNomIlleg if true and if both names have no date or same date, the only
        * name having nom. illeg. state is handled as if the name was published later than the name
        * without status nom. illeg.
        * @return
        */
       protected int compare(TaxonNameBase<?,?> name1, TaxonNameBase<?,?> name2, boolean includeNomIlleg) {
           int result;

           //dates
           Integer intDate1 = getIntegerDate(name1);
           Integer intDate2 = getIntegerDate(name2);

           if (intDate1 == null && intDate2 == null){
               result = 0;
           }else if (intDate1 == null){
               return 1;
           }else if (intDate2 == null){
               return -1;
           }else{
               result = intDate1.compareTo(intDate2);
           }

           //nom. illeg.
           if (result == 0 && includeNomIlleg){
               result = compareNomIlleg(name1, name2);
               if (result != 0){
                   return result;
               }
           }

           if (result == 0 && includeRanks){
               Rank rank1 = name1 == null? null : name1.getRank();
               Rank rank2 = name2 == null? null : name2.getRank();

               if (rank1 == null && rank2 == null){
                   result = 0;
               }else if (rank1 == null){
                   return 1;
               }else if (rank2 == null){
                   return -1;
               }else{
                   //for some strange reason compareTo for ranks returns 1 if rank2 is lower. So we add minus (-)
                   result = - rank1.compareTo(rank2);
               }
           }

           if (result == 0 && name1 != null && name2 != null){
               result = name1.compareToName(name2);
               if (result != 0){
                   return result;
               }
           }
           return result;
       }

       private Integer getIntegerDate(TaxonNameBase<?,?> name){
           Integer result;

          if (name == null){
               result = null;
           }else{
               if (name.isZoological()){
                   result = name.getPublicationYear();
               }else{
                   Reference ref = (Reference) name.getNomenclaturalReference();
                   if (ref == null){
                       result = null;
                   }else{
                       if (ref.getDatePublished() == null){
                           Reference inRef = ref.getInReference();
                           if (inRef == null){
                               result = null;
                           }else{
                               if (inRef.getDatePublished() == null){
                                   result = null;
                               }else{
                                   result = ref.getInReference().getDatePublished().getStartYear();
                               }
                           }
                       }else{
                           result = ref.getDatePublished().getStartYear();
                       }
                   }
               }
           }

           return result;
       }

       protected int compareNomIlleg(TaxonNameBase<?,?> taxonNameBase1, TaxonNameBase<?,?> taxonNameBase2) {
           int isNomIlleg1 = isNomIlleg(taxonNameBase1);
           int isNomIlleg2 = isNomIlleg(taxonNameBase2);
           return isNomIlleg1 - isNomIlleg2;
       }

       private int isNomIlleg(TaxonNameBase<?,?> taxonNameBase) {
           if (taxonNameBase == null || taxonNameBase.getStatus() == null){
               return 0;
           }
           Set<NomenclaturalStatus> status = taxonNameBase.getStatus();
           for (NomenclaturalStatus nomStatus : status){
               if (nomStatus.getType() != null){
                   if (nomStatus.getType().equals(NomenclaturalStatusType.ILLEGITIMATE())){
                       return 1;
                   }
               }
           }
           return 0;
       }

}
