/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 * @since 28.07.2010
 *
 */
public class TaxonNameDescriptionBeanProcessor extends AbstractCdmBeanProcessor<TaxonNameDescription> {

	@Override
	public List<String> getIgnorePropNames() {
		return null;
	}

	@Override
	public JSONObject processBeanSecondStep(TaxonNameDescription bean,
			JSONObject json, JsonConfig jsonConfig) {
		return json;
	}

}
