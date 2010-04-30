// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor;

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @date 29.04.2010
 * TODO introduced temporaryly 
 */
public class FeatureNodeBeanProcessor extends AbstractCdmBeanProcessor<CdmBase> {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#getIgnorePropNames()
	 */
	@Override
	public List<String> getIgnorePropNames() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#processBeanSecondStep(eu.etaxonomy.cdm.model.common.CdmBase, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(CdmBase bean, JSONObject json,	JsonConfig jsonConfig) {
		return json;
	}
	
	

}
