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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author k.luther
 * @since 27.11.2018
 */
public class TaxonDistributionDTO implements Serializable{

    private static final long serialVersionUID = -6565463192135410612L;
    private UUID taxonUUID;
    private String nameCache;
    private Integer rankOrderIndex = null;
    private String rankLabel = null;
    private TaxonDescriptionDTO descriptionsWrapper;
    private String concatenatedSynonyms = null;
    private String parentNameCache = null;

    public TaxonDistributionDTO(TaxonNode node){
        this.taxonUUID = node.getTaxon().getUuid();
        Taxon taxon = HibernateProxyHelper.deproxy(node.getTaxon());

        if (taxon.getName() != null){
            this.nameCache = taxon.getName().getNameCache();
        }
        if (nameCache == null){
            nameCache = taxon.getTitleCache();
        }
        TaxonName name = CdmBase.deproxy(taxon.getName());
        if (name != null && name.getRank() != null){
            this.rankOrderIndex = name.getRank().getOrderIndex();
            this.rankLabel = name.getRank().getLabel();
        }else{
            this.rankOrderIndex = null;
            this.rankLabel = null;
        }
        Taxon parentTaxon = HibernateProxyHelper.deproxy(node.getParent().getTaxon());

        setParentNameCache(parentTaxon.getName().getNameCache());
        this.descriptionsWrapper = new TaxonDescriptionDTO(taxon);
        concatenateSynonyms(taxon);
    }

   /* ------ Getter / Setter -----------*/
    public UUID getTaxonUuid() {
        return taxonUUID;
    }

    public String getNameCache() {
        return nameCache;
    }
    public void setNameCache(String nameCache) {
        this.nameCache = nameCache;
    }

    public String getRankString() {
        return rankLabel;
    }

    public Integer getRankOrderIndex() {
        return rankOrderIndex;
    }

    public void setRank(Rank rank) {
        if (rank != null){
            this.rankOrderIndex = rank.getOrderIndex();
            this.rankLabel = rank.getLabel();
        }
    }

    public TaxonDescriptionDTO getDescriptionsWrapper() {
        return descriptionsWrapper;
    }
    public void setDescriptionsWrapper(TaxonDescriptionDTO descriptionsWrapper) {
        this.descriptionsWrapper = descriptionsWrapper;
    }

    public String getConcatenatedSynonyms(){
        return concatenatedSynonyms;
    }

    private void concatenateSynonyms(Taxon taxon) {
       concatenatedSynonyms = null;
       for (TaxonName synName: taxon.getSynonymNames()){
           concatenatedSynonyms = CdmUtils.concat("; ", concatenatedSynonyms, synName.getNameCache());
       }
    }

    public String getParentNameCache() {
        return parentNameCache;
    }

    public void setParentNameCache(String parentNameCache) {
        this.parentNameCache = parentNameCache;
    }
}
