/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.media;

import java.util.Set;

/**
 * Interface indicating that an entity supports {@link Rights}
 *
 * @author muellera
 * @since 22.07.2025
 * @see https://dev.e-taxonomy.eu/redmine/issues/10772
 */
public interface IHasRights {

    public Set<Rights> getRights();

    public void addRights(Rights right);

    public void removeRights(Rights right);

}
