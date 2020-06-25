/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.common;

import org.joda.time.ReadablePartial;

/**
 * @author k.luther
 * @author a.mueller
 * @since Jun 15, 2020
 */
public class ExtendedTimePeriodPartialFormatter extends TimePeriodPartialFormatter {

    public static ExtendedTimePeriodPartialFormatter NewInstance(){
        return new ExtendedTimePeriodPartialFormatter();
    }

    private ExtendedTimePeriodPartialFormatter(){
        super();
    }

    @Override
    public String print(ReadablePartial partial){
        //for now we keep the same formatting as for TimePeriodPartialFormatter
        return super.print(partial);
     }
}
