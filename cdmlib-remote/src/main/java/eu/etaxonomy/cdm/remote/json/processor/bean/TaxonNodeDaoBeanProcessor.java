// $Id$
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

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.TaggedText;
import eu.etaxonomy.cdm.strategy.TaggedTextGenerator;

/**
 * @author a.kohlbecker
 * @date 29.07.2010
 *
 */
public class TaxonNodeDaoBeanProcessor implements JsonBeanProcessor {


	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonBeanProcessor#processBean(java.lang.Object, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBean(Object bean, JsonConfig jsonConfig) {
		
		TaxonNode node = (TaxonNode)bean;
		JSONObject json = new JSONObject();
		json.element("class", "TaxonNodeDao");
		json.element("titleCache", node.getTaxon().getName().getTitleCache(), jsonConfig);
		List<TaggedText> taggedTitle = TaggedTextGenerator.getTaggedName(node.getTaxon().getName());
		json.element("taggedTitle", taggedTitle, jsonConfig);
		json.element("taxonUuid", node.getTaxon().getUuid(), jsonConfig);
		//Sec can be null (web services can return null for sec)
		//comparation made for avoding view exceptions
		if (node.getTaxon().getSec() == null){
			json.element("secUuid", "null");
		}else{
			json.element("secUuid", node.getTaxon().getSec().getUuid(), jsonConfig);
		}	
		json.element("taxonomicChildrenCount", node.getCountChildren(), jsonConfig);
		json.element("unplaced", node.getTaxon().isUnplaced());
		json.element("excluded", node.getTaxon().isExcluded());
		String ranklabel = null;
		if(node.getTaxon().getName().getRank() != null){
			ranklabel = node.getTaxon().getName().getRank().getLabel();
		}
		json.element("rankLabel", ranklabel, jsonConfig);
		//json.element("treeUuid", node.getClassification().getUuid(), jsonConfig);
		
		return json;
	}

}
