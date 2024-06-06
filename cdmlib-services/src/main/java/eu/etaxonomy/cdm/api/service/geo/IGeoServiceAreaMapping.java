/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import javax.xml.stream.XMLStreamException;

import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * Class that offers mapping functionality from a CDM {@link NamedArea named area}
 * to a {@link GeoServiceArea geo service data holder} class.
 * It also offers mapping definition functionality.<BR>
 * The mapping is unidirectional.
 *
 * @author a.mueller
 * @since 15.08.2011
 */
public interface IGeoServiceAreaMapping {

    /**
     * Transforms a CDM area to an geoservice area
     *
     * @param area the CDM NamedArea
     * @return GeoServiceArea the geoservice area representing the CDM area
     */
    public GeoServiceArea valueOf(NamedArea area);

    public GeoServiceArea valueOf(NamedAreaDto area);

    /**
     * Set the mapping. Usually the mapping should be set in a persistent way, so it is
     * available after restarting the application.
     * @param area
     * @param geoServiceArea
     * @throws XMLStreamException
     */
    public void set(NamedArea area, GeoServiceArea geoServiceArea);

    /**
     * Clear the mapping for the given NamedArea
     * @param area
     */
    public void clear(NamedArea area);

}
