/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.CdmLinkSource;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

/**
 * @author a.kohlbecker
 * @since 24.03.2011
 *
 * TODO this bean processor is unused but preserved for the time when the REST API will be revised
 *
 */
public class CdmLinkSourceBeanProcessor implements JsonBeanProcessor {

    public static final Logger logger = Logger.getLogger(CdmLinkSourceBeanProcessor.class);

    @Override
    public JSONObject processBean(Object bean, JsonConfig jsonConfig) {
        if(bean instanceof CdmLinkSource){
            CdmLinkSource cdmLinkSource = (CdmLinkSource)bean;
            if(Hibernate.isInitialized(cdmLinkSource.getTarget())){
                    if(cdmLinkSource .getTarget() != null){
                        TypedEntityReference<CdmLinkSource> entityReference = new TypedEntityReference<>(CdmLinkSource.class, ((CdmBase)cdmLinkSource .getTarget()).getUuid());
                        return JSONObject.fromObject(entityReference);
                    }
            } else {
            }
        } else {
            logger.error("Ivalid bean type " + bean.getClass() + " can not be processed in " + this.getClass());
        }
        return new JSONObject(true);

    }

}
