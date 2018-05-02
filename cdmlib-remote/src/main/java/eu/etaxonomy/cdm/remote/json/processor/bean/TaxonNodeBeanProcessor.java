/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author n.hoffmann
 * @since Apr 9, 2010
 * @version 1.0
 */
public class TaxonNodeBeanProcessor extends AbstractCdmBeanProcessor<TaxonNode> {

	private static final Logger logger = Logger.getLogger(TaxonNodeBeanProcessor.class);

	@Override
	public List<String> getIgnorePropNames() {
		return Arrays.asList(new String[]{
				"parent", //TODO put in json-config ignore list ?
		});
	}

	@Override
	public JSONObject processBeanSecondStep(TaxonNode bean, JSONObject json,
			JsonConfig jsonConfig) {
		return json;
	}
}
