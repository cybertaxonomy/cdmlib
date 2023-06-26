/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

/**
 * Interface for entities allowing to be managed externally, e.g. in another database
 * and being synchronized from time to time.
 *
 * @author a.mueller
 * @date 26.06.2023
 */
public interface IExternallyManaged {


    /**
     * @return <code>true</code> if this entity is managed externally.
     */
    public boolean isManaged();
}
