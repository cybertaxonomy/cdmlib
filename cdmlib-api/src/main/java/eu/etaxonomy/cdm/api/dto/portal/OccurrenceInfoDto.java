/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.List;

import eu.etaxonomy.cdm.api.dto.SpecimenOrObservationBaseDTO;

/**
 * DTO which holds all information required by the dataportal to display
 * occurrence data.<BR>
 * These are the<ol>
 *  <li>map params to show the occurrences on a map</li>
 *  <li>the root units/tree(TODO) for full hierarchical representation</li>
 *  <li>the summary/statistics for summaries e.g. in a table header (TODO is this needed?
 *       Can't it be handled within the tree (as it is currently implemented) </li>
 *  <li>TODO flag indicating if data exists at all?</li> (could be integrated in statistics)
 * </ol>
 *
 * @see DistributionInfoDto
 *
 * @author muellera
 * @since 17.02.2024
 */
public class OccurrenceInfoDto {

    private List<SpecimenOrObservationBaseDTO> rootSpecimens;


    public List<SpecimenOrObservationBaseDTO> getRootSpecimens() {
        return rootSpecimens;
    }
    public void setRootSpecimens(List<SpecimenOrObservationBaseDTO> rootSpecimens) {
        this.rootSpecimens = rootSpecimens;
    }
}
