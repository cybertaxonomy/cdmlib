/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence;

import java.net.URI;

/**
 * @author k.luther
 * @date 21.02.2017
 *
 */
public class DataResponse {
    protected final URI dataSetUri;

    protected final String[] tripleID;

    protected final Object dataHolder;

    /**
     * @param derivedUnitFacade
     * @param dataSetUrl
     */
    public DataResponse(Object abcdDataHolder, URI dataSetUrl,  String [] tripleID) {

        this.dataHolder = abcdDataHolder;
        this.dataSetUri = dataSetUrl;

        this.tripleID = tripleID;


    }

}
