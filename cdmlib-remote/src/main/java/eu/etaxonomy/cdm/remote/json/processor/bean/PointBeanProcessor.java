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

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import eu.etaxonomy.cdm.model.location.Point;

/**
 * @author a.kohlbecker
 * @since 20.10.2010
 *
 */
public class PointBeanProcessor extends AbstractBeanProcessor<Point> {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractBeanProcessor#getIgnorePropNames()
     */
    @Override
    public List<String> getIgnorePropNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractBeanProcessor#processBeanSecondStep(java.lang.Object, net.sf.json.JSONObject, net.sf.json.JsonConfig)
     */
    @Override
    public JSONObject processBeanSecondStep(Point bean, JSONObject json,
            JsonConfig jsonConfig) {
        json.element("sexagesimalString", bean.toSexagesimalString(false, true));
        return json;
    }

}
