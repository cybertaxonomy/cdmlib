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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 *
 */
public class TaxonNameBaseBeanProcessor extends AbstractCdmBeanProcessor<TaxonNameBase> {

    public static final Logger logger = Logger.getLogger(TaxonNameBaseBeanProcessor.class);

    private boolean skipTaggedName = false;

    /**
     * @return the skipTaggedName
     */
    public boolean isSkipTaggedName() {
        return skipTaggedName;
    }

    /**
     * @param skipTaggedName the skipTaggedName to set
     */
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
    public JSONObject processBeanSecondStep(TaxonNameBase bean, JSONObject json, JsonConfig jsonConfig) {
        if(logger.isDebugEnabled()){
            logger.debug("processing second step" + bean);
        }
        if(!skipTaggedName){
            json.element("taggedName", bean.getTaggedName(), jsonConfig);
        }
        if(bean instanceof NonViralName){
            json.element("nameCache", ((NonViralName) bean).getNameCache(), jsonConfig);
        }
        return json;
    }

}
