/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.remote.l10n.KeyTerm_L10n;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

/**
 * @author a.kohlbecker
 * @since 29.07.2010
 *
 * @deprecated use the eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto instead for all classification web service endpoints
 */
@Deprecated
public class TaxonNodeDtoBeanProcessor implements JsonBeanProcessor {

    @Override
    public JSONObject processBean(Object bean, JsonConfig jsonConfig) {

        TaxonNode node = (TaxonNode)bean;
        JSONObject json = new JSONObject();
        json.element("class", "TaxonNodeDto");
        json.element("uuid", node.getUuid(), jsonConfig);
        json.element("taxonomicChildrenCount", node.getCountChildren(), jsonConfig);
        //json.element("classificationUuid", node.getClassification().getUuid(), jsonConfig);
        json.element("doubtful", node.isDoubtful());
        json.element("unplaced", node.isUnplaced());
        json.element("excluded", node.isExcluded());
        if(node.getStatus() != null) {
            json.element("status", node.getStatus());
            json.element("status_symbol", node.getStatus().getSymbol());
            KeyTerm_L10n<TaxonNodeStatus> status_L10n = new KeyTerm_L10n<TaxonNodeStatus>(node.getStatus());
            json.element("status_message_L10n", status_L10n.localizedMessage());
        }

        Taxon taxon = node.getTaxon();
        if(taxon != null){
            json.element("titleCache", taxon.getName().getTitleCache(), jsonConfig);
            List<TaggedText> taggedTitle = taxon.getName().getTaggedName();
            json.element("taggedTitle", taggedTitle, jsonConfig);
            json.element("taxonUuid", taxon.getUuid(), jsonConfig);
            //Sec can be null (web services can return null for sec)
            //comparation made for avoiding view exceptions
            if (taxon.getSec() == null){
                json.element("secUuid", "null");
            }else{
                json.element("secUuid", taxon.getSec().getUuid(), jsonConfig);
            }
            String ranklabel = null;
            if(taxon.getName().getRank() != null){
                ranklabel = taxon.getName().getRank().getLabel();
            }
            json.element("rankLabel", ranklabel, jsonConfig);
        }
        return json;
    }

}
