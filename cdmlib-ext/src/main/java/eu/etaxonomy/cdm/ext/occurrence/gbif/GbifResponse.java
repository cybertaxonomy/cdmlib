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

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;

/**
 * @author pplitzner
 * @date 27.05.2014
 *
 */
public class GbifResponse {

    private final DerivedUnitFacade derivedUnitFacade;
    private final String endPoint;
    /**
     * @param derivedUnitFacade
     * @param endPoint
     */
    public GbifResponse(DerivedUnitFacade derivedUnitFacade, String endPoint) {
        super();
        this.derivedUnitFacade = derivedUnitFacade;
        this.endPoint = endPoint;
    }
    /**
     * @return the derivedUnitFacade
     */
    public DerivedUnitFacade getDerivedUnitFacade() {
        return derivedUnitFacade;
    }
    /**
     * @return the endPointQuery
     */
    public String getEndPoint() {
        return endPoint;
    }

}
