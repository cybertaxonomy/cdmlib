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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 *
 */
public class TaxonBaseBeanProcessor extends AbstractCdmBeanProcessor<TaxonBase> {

    private static final Logger logger = LogManager.getLogger();

    private boolean skipTaggedTitle = false;

    public boolean isSkipTaggedTitle() {
        return skipTaggedTitle;
    }
    public void setSkipTaggedTitle(boolean skipTaggedTitle) {
        this.skipTaggedTitle = skipTaggedTitle;
    }

	@Override
	public List<String> getIgnorePropNames() {
		return Arrays.asList(new String[]{
		});
	}

	@Override
	public JSONObject processBeanSecondStep(TaxonBase bean, JSONObject json, JsonConfig jsonConfig) {
	    if(!skipTaggedTitle) {
	        json.element("taggedTitle", bean.getTaggedTitle(), jsonConfig);
	    }
		return json;
	}
}