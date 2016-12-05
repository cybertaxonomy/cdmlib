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

import java.util.List;

import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 * @date 09.06.2009
 *
 */
public class TaxonRelationshipBeanProcessor extends AbstractCdmBeanProcessor<TaxonRelationship> {

    @Override
    public List<String> getIgnorePropNames() {
        return null;
    }

    @Override
    public JSONObject processBeanSecondStep(TaxonRelationship bean, JSONObject json, JsonConfig jsonConfig) {

        json.element("fromTaxon", bean.getFromTaxon(), jsonConfig);
        json.element("toTaxon", bean.getToTaxon(), jsonConfig);

        return json;
    }

}
