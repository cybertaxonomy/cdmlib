// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.kohlbecker
 *
 */
public class TaxonBaseBeanProcessor extends AbstractCdmBeanProcessor<TaxonBase> {

	public static final Logger logger = Logger.getLogger(TaxonBaseBeanProcessor.class);

   private boolean skipTaggedTitle = false;

    /**
     * @return the skipTaggedName
     */
    public boolean isSkipTaggedTitle() {
        return skipTaggedTitle;
    }

    /**
     * @param skipTaggedName the skipTaggedName to set
     */
    public void setSkipTaggedTitle(boolean skipTaggedTitle) {
        this.skipTaggedTitle = skipTaggedTitle;
    }

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#getIgnorePropNames()
	 */
	@Override
	public List<String> getIgnorePropNames() {
		return Arrays.asList(new String[]{
				"taxonomicParent", //TODO put in json-config ignore list ?
		});
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#processBeanSecondStage(java.lang.Object, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(TaxonBase bean, JSONObject json, JsonConfig jsonConfig) {
	    if(!skipTaggedTitle) {
	        json.element("taggedTitle", bean.getTaggedTitle());
	    }
		return json;
	}

}
