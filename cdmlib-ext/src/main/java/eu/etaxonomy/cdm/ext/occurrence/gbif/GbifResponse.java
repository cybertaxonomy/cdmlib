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
import eu.etaxonomy.cdm.ext.occurrence.DataResponse;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * Wrapper class which holds a {@link DerivedUnitFacade} which was parsed from a GBIF JSON response.
 * Additionally it holds the {@link URI} to query the GBIF data set web service which
 * holds the endpoint URL of the original record and the {@link GbifDataSetProtocol}
 * @author pplitzner
 * @date 27.05.2014
 *
 */
public class GbifResponse extends DataResponse {



    private final GbifDataSetProtocol dataSetProtocol;

    private final TaxonNameBase scientificName;
    /**
     * @param derivedUnitFacade
     * @param dataSetUrl
     */
    public GbifResponse(DerivedUnitFacade derivedUnitFacade, URI dataSetUrl, GbifDataSetProtocol dataSetProtocol, String [] tripleID, TaxonNameBase scientificName) {
        super(derivedUnitFacade,dataSetUrl, tripleID);
       this.dataSetProtocol = dataSetProtocol;

        this.scientificName = scientificName;

    }

    public DerivedUnitFacade getDerivedUnitFacade() {
        return (DerivedUnitFacade)dataHolder;
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

    public String[] getTripleID(){
        return tripleID;
    }

    /**
     * @return the scientificName
     */
    public TaxonNameBase getScientificName() {
        return scientificName;
    }

}
