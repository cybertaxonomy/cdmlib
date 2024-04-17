/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal.config;

import java.util.Set;
import java.util.UUID;

/**
 * @author muellera
 * @since 16.04.2024
 */
public interface IAnnotatableLoaderConfiguration {

    public Set<UUID> getMarkerTypes();

    public Set<UUID> getAnnotationTypes();
}
