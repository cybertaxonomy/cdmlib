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
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.remote.dto.TaggedText;

/**
 * @author a.kohlbecker
 * @date 29.07.2010
 *
 */
public class TaxonNodeDaoBeanProcessor extends AbstractCdmBeanProcessor<TaxonNode> {


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractCdmBeanProcessor#getIgnorePropNames()
	 */
	@Override
	public List<String> getIgnorePropNames() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractCdmBeanProcessor#processBeanSecondStep(eu.etaxonomy.cdm.model.common.CdmBase, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(TaxonNode bean, JSONObject json,
			JsonConfig jsonConfig) {
		
		json.element("class", "TaxonNodeDao");
		json.element("titleCache", bean.getTaxon().getName().getTitleCache(), jsonConfig);
		List<TaggedText> taggedTitle = TaxonNameBaseBeanProcessor.getTaggedName(bean.getTaxon().getName());
		json.element("taggedTitle", taggedTitle, jsonConfig);
		json.element("taxonUuid", bean.getTaxon().getUuid(), jsonConfig);
		json.element("secUuid", bean.getTaxon().getSec().getUuid(), jsonConfig);
		json.element("taxonomicChildrenCount", bean.getCountChildren(), jsonConfig);
		String ranklabel = null;
		if(bean.getTaxon().getName().getRank() != null){
			ranklabel = bean.getTaxon().getName().getRank().getLabel();
		}
		json.element("rankLabel", ranklabel, jsonConfig);
		//json.element("treeUuid", node.getTaxonomicTree().getUuid(), jsonConfig);
		
		return json;
	}

}
