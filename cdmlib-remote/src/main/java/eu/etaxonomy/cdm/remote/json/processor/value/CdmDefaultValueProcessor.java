/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.value;

import net.sf.json.JSONNull;
import net.sf.json.processors.DefaultDefaultValueProcessor;

/**
 * Overrides the default implementation in json-lib to preserve null values of all numbers.
 *
 * See https://dev.e-taxonomy.eu/redmine/issues/7578
 *
 * @author a.kohlbecker
 * @since Jul 23, 2018
 *
 */
public class CdmDefaultValueProcessor extends DefaultDefaultValueProcessor {

    @Override
    public Object getDefaultValue( Class type ) {
        if(Number.class.isAssignableFrom(type)){
           return JSONNull.getInstance();
        } else {
            return super.getDefaultValue(type);
        }
     }

}
