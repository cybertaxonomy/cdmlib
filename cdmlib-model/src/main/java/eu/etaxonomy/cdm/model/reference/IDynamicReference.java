/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * Interface representing dynamic {@link Reference references} which allow
 * an "accessed" field and sources based on these references should also
 * support {@link OriginalSourceBase#accessed}.
 *
 * @author a.mueller
 * @date 26.07.2022
 */
//Note: the accessed field will be moved to OriginalSource in future #10057, #10145
public interface IDynamicReference {

    /**
     * Date (and time) when an {@link IDynamicReference dynamic reference} was accessed.<BR>
     * Will be changed to {@link TimePeriod} in future.
     *
     * @return the accessed date
     */
    //#5258
    public DateTime getAccessed();

// removed to start moving accessed from Reference to OriginalSourceBase (#10145)
//    /**
//     * @see #getAccessed()
//     */
//    public void setAccessed(DateTime accessed);

}
