// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.in;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * @author a.mueller
 * @created 05.05.2011
 */
public final class DwcaImportTransformer extends InputTransformerBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportTransformer.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureByKey(java.lang.String)
	 */
	@Override
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		if (key == null){
			return null;
		}else if (key.equalsIgnoreCase("morphology")){
			return Feature.Morphology();
		}
		return super.getFeatureByKey(key);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureUuid(java.lang.String)
	 */
	@Override
	public UUID getFeatureUuid(String key) throws UndefinedTransformerMethodException {
		return super.getFeatureUuid(key);
	}
	
	

	
	
	
}
