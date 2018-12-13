/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author k.luther
 * @since 27.11.2018
 *
 */
public class TaxonDistributionDTO implements Serializable{

    private static final long serialVersionUID = -6565463192135410612L;
    UUID taxonUuid;
    String nameCache;
    Rank rank;
    TaxonDescriptionDTO descriptionsWrapper;
    Map<NamedArea,Set<DescriptionElementBase>> distributionMap = new HashMap();



    public TaxonDistributionDTO(Taxon taxon){
        this.taxonUuid = taxon.getUuid();
        this.nameCache = taxon.getName().getNameCache() != null ? taxon.getName().getNameCache(): taxon.getName().getTitleCache();
        if (nameCache == null){
            nameCache = taxon.getTitleCache();
        }
        this.descriptionsWrapper = new TaxonDescriptionDTO(taxon);
        for (TaxonDescription desc: descriptionsWrapper.descriptions){
            for (DescriptionElementBase descElement: desc.getElements()){
                if (descElement instanceof Distribution){
                    Set<DescriptionElementBase> distributions = distributionMap.get(((Distribution)descElement).getArea());
                    if (distributions == null){
                        distributions = new HashSet();
                        distributionMap.put(((Distribution)descElement).getArea(), distributions);
                    }
                    distributions.add(descElement);

                }
            }
        }

    }

   /* ------ Getter / Setter -----------*/

    /**
     * @return the taxonUuid
     */
    public UUID getTaxonUuid() {
        return taxonUuid;
    }

    /**
     * @param taxonUuid the taxonUuid to set
     */
    public void setTaxonUuid(UUID taxonUuid) {
        this.taxonUuid = taxonUuid;
    }

    /**
     * @return the nameCache
     */
    public String getNameCache() {
        return nameCache;
    }

    /**
     * @param nameCache the nameCache to set
     */
    public void setNameCache(String nameCache) {
        this.nameCache = nameCache;
    }

    /**
     * @return the rankString
     */
    public String getRankString() {
        return rank.getLabel();
    }

    /**
     * @return the rank
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(Rank rank) {
        this.rank = rank;
    }


    /**
     * @return the descriptionsWrapper
     */
    public TaxonDescriptionDTO getDescriptionsWrapper() {
        return descriptionsWrapper;
    }

    /**
     * @param descriptionsWrapper the descriptionsWrapper to set
     */
    public void setDescriptionsWrapper(TaxonDescriptionDTO descriptionsWrapper) {
        this.descriptionsWrapper = descriptionsWrapper;
    }

    /**
     * @return the distributionMap
     */
    public Map<NamedArea, Set<DescriptionElementBase>> getDistributionMap() {
        return distributionMap;
    }

    /**
     * @param distributionMap the distributionMap to set
     */
    public void setDistributionMap(Map<NamedArea, Set<DescriptionElementBase>> distributionMap) {
        this.distributionMap = distributionMap;
    }

}
