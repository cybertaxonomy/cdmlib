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

    private static final long serialVersionUID = 5016285663369538997L;

    private Set<UUID> uuidsOfSecundumReferences = new HashSet<>();
    private Map<UUID, Boolean> publishFlagsForSynonyms = new HashMap<>();
    private Set<UUID> uuidsOfTaxonBases = new HashSet<>();

    public HomotypicGroupDto(HomotypicalGroup group, UUID nodeUuid) {
        super(group.getUuid(), group.getId(), group.getUserFriendlyDescription());
        for (TaxonName name: group.getTypifiedNames()){
            TaxonBase<?> tbInHomotypicGroup = null;
            for (TaxonBase<?> tb:name.getTaxonBases()){
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

    public Set<UUID> getUuidsOfSecundumReferences() {
        return uuidsOfSecundumReferences;
    }
    public void setUuidsOfSecundumReferences(Set<UUID> uuidsOfSecundumReferences) {
        this.uuidsOfSecundumReferences = uuidsOfSecundumReferences;
    }

    public Map<UUID, Boolean> getPublishFlagsForSynonyms() {
        return publishFlagsForSynonyms;
    }
    public void setPublishFlagsForSynonyms(Map<UUID, Boolean> publishFlagsForSynonyms) {
        this.publishFlagsForSynonyms = publishFlagsForSynonyms;
    }

    public Set<UUID> getUuidsOfTaxonBases() {
        return uuidsOfTaxonBases;
    }
    public void setUuidsOfTaxonBases(Set<UUID> uuidsOfTaxonBases) {
        this.uuidsOfTaxonBases = uuidsOfTaxonBases;
    }
}