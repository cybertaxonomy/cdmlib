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
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author k.luther
 * @since 27.11.2018
 */
public class TaxonDistributionDTO implements Serializable{

    private static final long serialVersionUID = -6565463192135410612L;
    private TaxonNodeDto nodeDto;
    private TaxonDescriptionDTO descriptionsWrapper;
    private String concatenatedSynonyms = null;
    private TaxonNodeDto parentNodeDto = null;

    public TaxonDistributionDTO(TaxonNode node){
        nodeDto = new TaxonNodeDto(node);
        parentNodeDto = new TaxonNodeDto(node.getParent());

        Taxon taxon = HibernateProxyHelper.deproxy(node.getTaxon());


        this.descriptionsWrapper = new TaxonDescriptionDTO(taxon);
        concatenateSynonyms(taxon);
    }

   /* ------ Getter / Setter -----------*/
    public UUID getTaxonUuid() {
        if (nodeDto != null){
            return nodeDto.getTaxonUuid();
        }
        return null;
    }

    public String getNameCache() {
        if (nodeDto != null){
            return nodeDto.getNameCache();
        }
        return null;

    }


    public String getRankString() {
        if (nodeDto != null){
            return nodeDto.getRankLabel();
        }
        return null;
    }

    public Integer getRankOrderIndex() {
        if (nodeDto != null){
            return nodeDto.getRankOrderIndex();
        }
        return null;
    }

    public TaxonNodeDto getTaxonNodeDto(){
        return nodeDto;
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

    public TaxonNodeDto getParentDto() {
        return parentNodeDto;
    }


}
