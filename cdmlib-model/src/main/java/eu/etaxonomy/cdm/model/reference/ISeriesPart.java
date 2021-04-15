/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

/**
 * @author a.mueller
 * @since 24.03.2021
 */
public interface ISeriesPart {

    /**
     * Returns the series part for this printed unit
     */
    public String getSeriesPart();

    /**
     * Sets the series part for this printed unit
     * @param seriesPart
     */
    public void setSeriesPart(String seriesPart);
}
