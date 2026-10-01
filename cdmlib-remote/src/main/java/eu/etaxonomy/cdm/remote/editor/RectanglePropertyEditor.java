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

import org.springframework.util.Assert;

import eu.etaxonomy.cdm.api.service.dto.RectangleDTO;

/**
 * BBOX=minx(minlongitude),miny(minlatitude),maxx(maxlongitude),maxy(maxlatitude):
 * Bounding box corners (lower left, upper right)
 *
 * @author a.kohlbecker
 * @since Apr 26, 2013
 */
public class RectanglePropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        String[] values = text.split(",");
        Assert.isTrue(values.length == 4, "A rectangle string must contain four values");
        final double lowerLeftLongitude = Double.parseDouble(values[0]);
        final double lowerLeftLatitude = Double.parseDouble(values[1]);
        final double upperRightLongitude = Double.parseDouble(values[2]);
        final double upperRightLatitude = Double.parseDouble(values[3]);
        setValue(new RectangleDTO(
                // RectangleDTO expects: latitude, longitude
                normalizeLatitude(lowerLeftLatitude),
                normalizeLongitudeInclusive(lowerLeftLongitude),
                normalizeLatitude(upperRightLatitude),
                normalizeLongitudeInclusive(upperRightLongitude)
            ));
    }

    /**
     * Former Hibernate Search 5 {@code Point.normalizeLatitude}: clamp/wrap to [-90;+90].
     */
    static double normalizeLatitude(double latitude) {
        if (latitude > 90.0 || latitude < -90.0) {
            double wrapped = Math.abs((latitude + 90.0) % 360.0);
            if (wrapped > 180.0) {
                wrapped = 360.0 - wrapped;
            }
            return wrapped - 90.0;
        }
        return latitude;
    }

    /**
     * Former Hibernate Search 5 {@code Point.normalizeLongitudeInclusive}: wrap to [-180;+180].
     */
    static double normalizeLongitudeInclusive(double longitude) {
        if (longitude < -180.0 || longitude > 180.0) {
            double wrapped = (longitude + 180.0) % 360.0;
            if (wrapped < 0) {
                wrapped = wrapped + 180.0;
            } else {
                wrapped = wrapped - 180.0;
            }
            return wrapped;
        }
        return longitude;
    }

}
