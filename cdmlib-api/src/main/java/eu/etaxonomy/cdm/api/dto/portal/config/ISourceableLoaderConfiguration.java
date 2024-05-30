/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal.config;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.reference.OriginalSourceType;

/**
 * Interface for loader configurations that do load supplemental
 * data like markers, annotations and sources.
 *
 * @author muellera
 * @since 16.04.2024
 */
public interface ISourceableLoaderConfiguration {

    public Set<UUID> getMarkerTypes();

    public Set<UUID> getAnnotationTypes();

    public EnumSet<OriginalSourceType> getSourceTypes();
}
