/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.common.DOI;

/**
 * Interface used by all reference type interfaces to define
 * if they support {@link DOI DOIs} or not.
 * @author a.mueller
 * @since 28.11.2018
 */
public interface IWithDoi {

    /**
     * Returns the references {@link DOI digital object identifier}.
     */
    public DOI getDoi();

    /**
     * @see #getDoi()
     * @param doi the DOI to set
     */
    public void setDoi(DOI doi);
}
