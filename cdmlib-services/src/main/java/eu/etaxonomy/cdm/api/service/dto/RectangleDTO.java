/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

/**
 * @author a.mueller
 * @date 23.08.2022
 */
public class RectangleDTO {

    private final double lowerLeftLatitude;
    private final double lowerLeftLongitude;
    private final double upperRightLatitude;
    private final double upperRightLongitude;

    public RectangleDTO(double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude,
            double upperRightLongitude) {

        this.lowerLeftLatitude = lowerLeftLatitude;
        this.lowerLeftLongitude = lowerLeftLongitude;
        this.upperRightLatitude = upperRightLatitude;
        this.upperRightLongitude = upperRightLongitude;
    }

    public Double getLowerLeftLatitude() {
        return lowerLeftLatitude;
    }

    public double getLowerLeftLongitude() {
        return lowerLeftLongitude;
    }

    public double getUpperRightLatitude() {
        return upperRightLatitude;
    }

    public double getUpperRightLongitude() {
        return upperRightLongitude;
    }
}