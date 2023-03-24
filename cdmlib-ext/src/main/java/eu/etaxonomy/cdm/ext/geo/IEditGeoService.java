/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * @author a.kohlbecker
 * @author n.hoffmann
 *
 * @since 18.06.2009
 */
public interface IEditGeoService {

    public OccurrenceServiceRequestParameterDto getOccurrenceServiceRequestParameters(
            List<SpecimenOrObservationBase> specimensOrObersvations,
            Map<SpecimenOrObservationType,Color> specimenOrObservationTypeColorss);

	public Kml occurrencesToKML(List<SpecimenOrObservationBase> specimensOrObersvations,
			Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors);

}
