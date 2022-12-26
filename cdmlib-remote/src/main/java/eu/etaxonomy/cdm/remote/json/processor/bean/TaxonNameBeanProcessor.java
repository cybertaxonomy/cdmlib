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

import eu.etaxonomy.cdm.model.name.TaxonName;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 */
public class TaxonNameBeanProcessor extends AbstractCdmBeanProcessor<TaxonName> {

    private static final Logger logger = LogManager.getLogger();

    private boolean skipTaggedName = false;

    public boolean isSkipTaggedName() {
        return skipTaggedName;
    }
    public void setSkipTaggedName(boolean skipTaggedName) {
        this.skipTaggedName = skipTaggedName;
    }

    @Override
    public List<String> getIgnorePropNames() {
        return Arrays.asList(new String[]{
                // ignore nameRelations to avoid LazyLoadingExceptions coming
                // from NameRelationshipBeanProcessor.secondStep() in which
                // the transient field fromName is added to the serialization
                "relationsFromThisName",
                "relationsToThisName",
                "combinationAuthorship",
                "basionymAuthorship",
                "exCombinationAuthorship",
                "exBasionymAuthorship"
        });
    }

    @Override
    public JSONObject processBeanSecondStep(TaxonName bean, JSONObject json, JsonConfig jsonConfig) {
        if(logger.isDebugEnabled()){
            logger.debug("processing second step" + bean);
        }
        if(!skipTaggedName){
            json.element("taggedName", bean.getTaggedName(), jsonConfig);
        }
        json.element("nameCache", bean.getNameCache(), jsonConfig);
        return json;
    }
}