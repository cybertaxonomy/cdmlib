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
import java.util.UUID;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author k.luther
 * @since 27.11.2018
 *
 */
public class TaxonDistributionDTO implements Serializable{

    private static final long serialVersionUID = -6565463192135410612L;
    UUID taxonUUID;
    String nameCache;
    Integer rankOrderIndex = null;
    String rankLabel = null;
    TaxonDescriptionDTO descriptionsWrapper;
//    Map<NamedArea,Set<DescriptionElementBase>> distributionMap = new HashMap();



    public TaxonDistributionDTO(Taxon taxon){
        this.taxonUUID = taxon.getUuid();
        taxon = HibernateProxyHelper.deproxy(taxon);
        this.nameCache = taxon.getName().getNameCache() != null ? taxon.getName().getNameCache(): taxon.getName().getTitleCache();
        if (nameCache == null){
            nameCache = taxon.getTitleCache();
        }
        if (taxon.getName() != null){
            TaxonName name = HibernateProxyHelper.deproxy(taxon.getName(), TaxonName.class);
            this.rankOrderIndex = name != null ? name.getRank().getOrderIndex(): null;
            this.rankLabel = name != null ? name.getRank().getLabel(): null;
        }

        this.descriptionsWrapper = new TaxonDescriptionDTO(taxon);


    }

   /* ------ Getter / Setter -----------*/

    /**
     * @return the taxonUuid
     */
    public UUID getTaxonUuid() {
        return taxonUUID;
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
        return rankLabel;
    }

    /**
     * @return the rank
     */
    public Integer getRankOrderIndex() {
        return rankOrderIndex;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(Rank rank) {
        if (rank != null){
            this.rankOrderIndex = rank.getOrderIndex();
            this.rankLabel = rank.getLabel();
        }
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

//    /**
//     * @return the distributionMap
//     */
//    public Map<NamedArea, Set<DescriptionElementBase>> getDistributionMap() {
//        return distributionMap;
//    }
//
//    /**
//     * @param distributionMap the distributionMap to set
//     */
//    public void setDistributionMap(Map<NamedArea, Set<DescriptionElementBase>> distributionMap) {
//        this.distributionMap = distributionMap;
//    }

}
