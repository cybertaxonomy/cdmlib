/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author k.luther
 * @since Mar 19, 2021
 */
public class HomotypicGroupDto extends UuidAndTitleCache<HomotypicalGroup> {

    Set<UUID> uuidsOfSecundumReferences = new HashSet<>();
    Map<UUID, Boolean> publishFlagsForSynonyms = new HashMap<>();
    Set<UUID> uuidsOfTaxonBases = new HashSet<>();
    /**
     * @param uuid
     * @param id
     * @param titleCache
     */
    public HomotypicGroupDto(HomotypicalGroup group, UUID nodeUuid) {
        super(group.getUuid(), group.getId(), group.getUserFriendlyDescription());
        for (TaxonName name: group.getTypifiedNames()){
            TaxonBase tbInHomotypicGroup = null;
            for (TaxonBase tb:name.getTaxonBases()){
                if (tb instanceof Taxon){
                    for (TaxonNode node: ((Taxon)tb).getTaxonNodes()){
                        if (node.getUuid().equals(nodeUuid)){
                            tbInHomotypicGroup = tb;
                            break;
                        }
                    }
                }else {
                    for (TaxonNode node: ((Synonym)tb).getAcceptedTaxon().getTaxonNodes()){
                        if (node.getUuid().equals(nodeUuid)){
                            tbInHomotypicGroup = tb;
                            break;
                        }
                    }
                }
            }
            if (tbInHomotypicGroup != null){
                uuidsOfTaxonBases.add(tbInHomotypicGroup.getUuid());
                uuidsOfSecundumReferences.add(tbInHomotypicGroup.getSec()!= null?tbInHomotypicGroup.getSec().getUuid(): null);
                publishFlagsForSynonyms.put(tbInHomotypicGroup.getUuid(), tbInHomotypicGroup.isPublish());
            }
        }
    }
    /**
     * @return the uuidsOfSecundumReferences
     */
    public Set<UUID> getUuidsOfSecundumReferences() {
        return uuidsOfSecundumReferences;
    }
    /**
     * @param uuidsOfSecundumReferences the uuidsOfSecundumReferences to set
     */
    public void setUuidsOfSecundumReferences(Set<UUID> uuidsOfSecundumReferences) {
        this.uuidsOfSecundumReferences = uuidsOfSecundumReferences;
    }
    /**
     * @return the publishFlagsForSynonyms
     */
    public Map<UUID, Boolean> getPublishFlagsForSynonyms() {
        return publishFlagsForSynonyms;
    }
    /**
     * @param publishFlagsForSynonyms the publishFlagsForSynonyms to set
     */
    public void setPublishFlagsForSynonyms(Map<UUID, Boolean> publishFlagsForSynonyms) {
        this.publishFlagsForSynonyms = publishFlagsForSynonyms;
    }
    /**
     * @return the uuidsOfTaxonBases
     */
    public Set<UUID> getUuidsOfTaxonBases() {
        return uuidsOfTaxonBases;
    }
    /**
     * @param uuidsOfTaxonBases the uuidsOfTaxonBases to set
     */
    public void setUuidsOfTaxonBases(Set<UUID> uuidsOfTaxonBases) {
        this.uuidsOfTaxonBases = uuidsOfTaxonBases;
    }

}
