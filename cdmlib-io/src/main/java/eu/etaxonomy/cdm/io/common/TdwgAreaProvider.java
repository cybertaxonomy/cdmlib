/**
 * 
 */
package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * This class is meant to remove the pre v3.3 class TdwgArea in cdmlib-model.
 * It may be changed or fully removed in future when we implement a more 
 * general solution for terms.
 * 
 * @author a.mueller
 * @since 11.6.2013
 *
 */
@Deprecated
public class TdwgAreaProvider {
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TdwgAreaProvider.class);


//************************** METHODS ********************************


    /**
     */
    public static NamedArea getAreaByTdwgAbbreviation(String tdwgAbbreviation){
        return NamedArea.getAreaByTdwgAbbreviation(tdwgAbbreviation);
    }

    /**
     */
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
