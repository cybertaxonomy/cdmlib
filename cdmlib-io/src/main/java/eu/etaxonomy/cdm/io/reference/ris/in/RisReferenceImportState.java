/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.reference.ris.in;

import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.mueller
 * @since 11.05.2017
 *
 */
public class RisReferenceImportState
        extends ImportStateBase<RisReferenceImportConfigurator, RisReferenceImport>{

    private Map<RisReferenceTag, Integer> unhandled = new HashMap<>();


    /**
     * @param config
     */
    protected RisReferenceImportState(RisReferenceImportConfigurator config) {
        super(config);
    }

    public void addUnhandled(RisReferenceTag tag) {
        Integer x = unhandled.get(tag);
        unhandled.put(tag, x == null ? 1 : ++x);
    }

    public Map<RisReferenceTag, Integer> getUnhandled() {
        return unhandled;
    }

}
