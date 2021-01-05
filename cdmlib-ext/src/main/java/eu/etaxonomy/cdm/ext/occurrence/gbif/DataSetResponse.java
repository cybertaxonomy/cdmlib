/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence.gbif;

import eu.etaxonomy.cdm.common.URI;

/**
 * Wrapper object for the parameters that are parsed from a GBIF dataset query
 * @author pplitzner
 * @since 02.06.2014
 */
public class DataSetResponse {

    private GbifDataSetProtocol protocol;
    private URI endpoint;
    private String unitId;
    private String abcdSchema;

    public GbifDataSetProtocol getProtocol() {
        return protocol;
    }
    public void setProtocol(GbifDataSetProtocol protocol) {
        this.protocol = protocol;
    }

    public URI getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

    public String getUnitId() {
        return unitId;
    }
    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getAbcdSchema() {
        return abcdSchema;
    }
    public void setAbcdSchema(String abcdSchema) {
        this.abcdSchema = abcdSchema;
    }

}
