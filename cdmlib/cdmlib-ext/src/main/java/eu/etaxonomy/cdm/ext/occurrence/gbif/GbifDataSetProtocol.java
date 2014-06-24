// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence.gbif;

/**
 * @author pplitzner
 * @date 28.05.2014
 *
 */
public enum GbifDataSetProtocol {
    BIOCASE,
    DWC_ARCHIVE,
    ;

    public static GbifDataSetProtocol parseProtocol(String protocolString) {
        if(protocolString.equals("BIOCASE")){
            return BIOCASE;
        }
        else if(protocolString.equals("DWC_ARCHIVE")){
            return DWC_ARCHIVE;
        }
        return null;
    }
}
