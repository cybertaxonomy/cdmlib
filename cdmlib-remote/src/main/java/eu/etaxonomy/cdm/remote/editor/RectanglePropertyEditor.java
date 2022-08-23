/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import org.hibernate.search.spatial.impl.Point;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.api.service.dto.RectangleDTO;

/**
 * BBOX=minx(minlongitute),miny(minlatitute),maxx(maxlongitute),max(maxlatitute): Bounding box corners (lower left, upper right)
 *
 * @author a.kohlbecker
 * @since Apr 26, 2013
 */
public class RectanglePropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        String[] values = text.split(",");
        Assert.isTrue(values.length == 4, "A rectangle string must contain four values");
        final Double lowerLeftLatitude = Double.parseDouble(values[1]);
        final Double lowerLeftLongitude = Double.parseDouble(values[2]);
        final Double upperRightLatitude = Double.parseDouble(values[3]);
        final Double upperRightLongitude = Double.parseDouble(values[4]);
        setValue(new RectangleDTO(
                // Points are constructed as : latitude, longitude
                Point.normalizeLatitude(lowerLeftLatitude),
                Point.normalizeLongitudeInclusive(lowerLeftLongitude),
                Point.normalizeLatitude(upperRightLatitude),
                Point.normalizeLongitudeInclusive(upperRightLongitude)
            ));
    }

}
