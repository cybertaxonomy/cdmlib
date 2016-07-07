// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author a.kohlbecker
 * @date Sep 3, 2015
 *
 */
public class ClassificationLookupDTO {

    private final Map<Integer, Integer> taxonIdToParentId = new HashMap<Integer, Integer>();
    private final Map<Rank,Set<Integer>> taxonIdByRank = new HashMap<Rank, Set<Integer>>();
    private final Map<Integer,Set<Integer>> childTaxonMap = new HashMap<Integer,Set<Integer>>();
    private Classification classification = null;

    /**
     * @return the taxonIds
     */
    public Set<Integer> getTaxonIds() {
        return taxonIdToParentId.keySet();
    }

    /**
     * @return the taxonIdByRank
     */
    public Map<Rank, Set<Integer>> getTaxonIdByRank() {
        return taxonIdByRank;
    }

    /**
     * @return the childTaxonMap
     */
    public Map<Integer, Set<Integer>> getChildTaxonMap() {
        return childTaxonMap;
    }

    /**
     * @return the classification
     */
    public Classification getClassification() {
        return classification;
    }

    /**
     *
     * @param classification
     *      Must never be null the ClassificationLookupDTO always specific to one
     *      Classification.
     */
    public ClassificationLookupDTO(Classification classification) {
        this.classification  = classification;
    }

    public void add(Integer taxonId, Rank rank, Integer parentId) {

        taxonIdToParentId.put(taxonId, parentId);

        if(!childTaxonMap.containsKey(parentId)) {
            childTaxonMap.put(parentId, new HashSet<Integer>());
        }
        childTaxonMap.get(parentId).add(taxonId);

        if(!taxonIdByRank.containsKey(rank)) {
            taxonIdByRank.put(rank, new HashSet<Integer>());
        }
        taxonIdByRank.get(rank).add(taxonId);
    }

    public void dropRank(Rank rank) {
        Collection<Integer> idsForRank = taxonIdByRank.get(rank);
        taxonIdByRank.remove(rank);

        if(idsForRank != null) {
            for(Integer taxonId : idsForRank) {
                Integer parentId = taxonIdToParentId.get(taxonId);
                taxonIdToParentId.remove(taxonId);
                childTaxonMap.remove(parentId);
            }
        }
    }

    /**
     * Drops all ranks from the classifiacationLookupDTO except those
     * listed in ranks.
     *
     * @param includeRanks
     */
    public void filterInclude(List<Rank> includeRanks) {

       Set<Rank> rankSet = new HashSet<Rank>(taxonIdByRank.keySet());
       for(Rank rank : rankSet) {
           if(!includeRanks.contains(rank)) {
               dropRank(rank);
           }
       }
    }

}
