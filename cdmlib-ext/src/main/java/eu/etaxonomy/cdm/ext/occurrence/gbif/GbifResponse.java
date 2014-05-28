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

import java.net.URI;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;

/**
 * Wrapper class which holds a {@link DerivedUnitFacade} which was parsed from a GBIF JSON response.
 * Additionally it holds the {@link URI} to query the GBIF data set web service which
 * holds the endpoint URL of the original record and the {@link GbifDataSetProtocol}
 * @author pplitzner
 * @date 27.05.2014
 *
 */
public class GbifResponse {

    private final DerivedUnitFacade derivedUnitFacade;
    private final URI dataSetUri;
    private final GbifDataSetProtocol dataSetProtocol;
    /**
     * @param derivedUnitFacade
     * @param dataSetUrl
     */
    public GbifResponse(DerivedUnitFacade derivedUnitFacade, URI dataSetUrl, GbifDataSetProtocol dataSetProtocol) {
        super();
        this.derivedUnitFacade = derivedUnitFacade;
        this.dataSetUri = dataSetUrl;
        this.dataSetProtocol = dataSetProtocol;
    }

    public DerivedUnitFacade getDerivedUnitFacade() {
        return derivedUnitFacade;
    }

    public URI getDataSetUri() {
        return dataSetUri;
    }

    /**
     * @return the dataSetProtocol
     */
    public GbifDataSetProtocol getDataSetProtocol() {
        return dataSetProtocol;
    }

}
