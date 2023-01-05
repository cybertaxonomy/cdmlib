/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * This class is meant to remove the pre v3.3 class TdwgArea in cdmlib-model.
 * It may be changed or fully removed in future when we implement a more
 * general solution for terms.
 *
 * @author a.mueller
 * @since 11.6.2013
 */
@Deprecated
public class TdwgAreaProvider {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

//************************** METHODS ********************************

    public static NamedArea getAreaByTdwgAbbreviation(String tdwgAbbreviation){
        return NamedArea.getAreaByTdwgAbbreviation(tdwgAbbreviation);
    }

    public static NamedArea getAreaByTdwgLabel(String tdwgLabel){
        return NamedArea.getAreaByTdwgLabel(tdwgLabel);
    }

	public static boolean isTdwgAreaLabel(String area) {
		return NamedArea.isTdwgAreaLabel(area);
	}

	public static boolean isTdwgAreaAbbreviation(String abbrev) {
		return NamedArea.isTdwgAreaAbbreviation(abbrev);
	}
}
