/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * @author a.mueller
 * @since 20.11.2018
 *
 */
public interface IWithAuthor {

    /**
     * Returns the references author(s)
     */
    public TeamOrPersonBase getAuthorship();

    /**
     * Sets the references author(s)
     */
    public void setAuthorship(TeamOrPersonBase authorship);
}
