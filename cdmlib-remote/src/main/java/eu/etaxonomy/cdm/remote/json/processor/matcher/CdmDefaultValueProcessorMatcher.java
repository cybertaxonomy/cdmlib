/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.matcher;

import java.util.Set;

import eu.etaxonomy.cdm.remote.json.processor.value.CdmDefaultValueProcessor;
import net.sf.json.processors.DefaultValueProcessor;
import net.sf.json.processors.DefaultValueProcessorMatcher;

/**
 * @author a.kohlbecker
 * @since Jul 23, 2018
 *
 */
public class CdmDefaultValueProcessorMatcher extends DefaultValueProcessorMatcher {

    public static final DefaultValueProcessor DEFAULT = new CdmDefaultValueProcessor();

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getMatch(Class target, Set set) {
        if(Number.class.isAssignableFrom(Number.class)){
            return Number.class;
        } else {
            return DefaultValueProcessorMatcher.DEFAULT.getMatch(target, set);
        }
    }


}
