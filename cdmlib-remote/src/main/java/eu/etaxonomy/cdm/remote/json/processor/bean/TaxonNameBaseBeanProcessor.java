// $Id: TaxonBaseBeanProcessor.java 5473 2009-03-25 13:42:07Z a.kohlbecker $
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

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.TaggedTextGenerator;

/**
 * @author a.kohlbecker
 *
 */
public class TaxonNameBaseBeanProcessor extends AbstractCdmBeanProcessor<TaxonNameBase> {

	public static final Logger logger = Logger.getLogger(TaxonNameBaseBeanProcessor.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#getIgnorePropNames()
	 */
	@Override
	public List<String> getIgnorePropNames() {
		return Arrays.asList(new String[]{
				// ignore nameRelations to avoid LazyLoadingExceptions coming 
				// from NameRelationshipBeanProcessor.secondStep() in which 
				// the transient field fromName is added to the serialization
				"relationsFromThisName",
				"relationsToThisName",
				"combinationAuthorTeam",
				"basionymAuthorTeam",
				"exCombinationAuthorTeam",
				"exBasionymAuthorTeam"
		});
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#processBeanSecondStage(java.lang.Object, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(TaxonNameBase bean, JSONObject json, JsonConfig jsonConfig) {
		if(logger.isDebugEnabled()){
			logger.debug("processing second step" + bean);
		}
		json.element("taggedName", TaggedTextGenerator.getTaggedName(bean), jsonConfig);
		return json;
	}

	

}
