/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence.bioCase;

import java.net.URI;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.ext.occurrence.DataResponse;

/**
 * Wrapper class which holds a {@link DerivedUnitFacade} which was parsed from a GBIF JSON response.
 * Additionally it holds the {@link URI} to query the Biocase data set web service which
 * holds the endpoint URL of the original record and the {@link BiocaseDataSetProtocol}
 * @author k.luther
 * @since 21.02.2017
 *
 */

public class BioCaseResponse extends DataResponse{




        /**
         * @param derivedUnitFacade
         * @param dataSetUrl
         */
        public BioCaseResponse(Object abcdDataHolder, URI dataSetUrl,  String [] tripleID) {
            super(abcdDataHolder, dataSetUrl, tripleID);
        }

        public Object getAbcdDataHolder() {
            return dataHolder;
        }

        public URI getDataSetUri() {
            return dataSetUri;
        }


        public String[] getTripleID(){
            return tripleID;
        }




}
