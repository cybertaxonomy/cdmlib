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

/**
 * Wrapper object for the parameters that are parsed from a GBIF dataset query
 * @author pplitzner
 * @date 02.06.2014
 *
 */
public class DataSetResponse {

    private GbifDataSetProtocol protocol;
    private URI endpoint;
    private String unitId;
    private String abcdSchema;

    /**
     * @return the protocol
     */
    public GbifDataSetProtocol getProtocol() {
        return protocol;
    }
    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(GbifDataSetProtocol protocol) {
        this.protocol = protocol;
    }
    /**
     * @return the endpoint
     */
    public URI getEndpoint() {
        return endpoint;
    }
    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }
    /**
     * @return the unitId
     */
    public String getUnitId() {
        return unitId;
    }
    /**
     * @param unitId the unitId to set
     */
    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }
    /**
     * @return the abcdSchema
     */
    public String getAbcdSchema() {
        return abcdSchema;
    }
    /**
     * @param abcdSchema the abcdSchema to set
     */
    public void setAbcdSchema(String abcdSchema) {
        this.abcdSchema = abcdSchema;
    }

}
