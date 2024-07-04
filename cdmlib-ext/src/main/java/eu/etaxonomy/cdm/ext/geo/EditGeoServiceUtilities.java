/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.ext.geo;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * Class implementing the business logic for creating the map service string for
 * a given set of distributions. See {@link EditGeoService} as API for the given functionality.
 *
 * @see EditGeoService
 *
 * @author a.mueller
 * @since 17.11.2008
 */
public class EditGeoServiceUtilities {

    private static final Logger logger = LogManager.getLogger();

    //TODO see #6835
    private static HashMap<SpecimenOrObservationType, Color> defaultSpecimenOrObservationTypeColors = new HashMap<>();
    static {
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.FieldUnit, Color.ORANGE);
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.DerivedUnit, Color.RED);
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.LivingSpecimen, Color.GREEN);
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.Observation, Color.ORANGE);
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.PreservedSpecimen, Color.GRAY);
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.Media, Color.BLUE);
    }

    private static HashMap<SpecimenOrObservationType, Color> getDefaultSpecimenOrObservationTypeColors() {
        return defaultSpecimenOrObservationTypeColors;
    }

    /**
     * URI encode the given String
     */
    private static String encode(String string) {
        String encoded = string;
        try {
            encoded = URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        }
        return encoded;
    }

    /**
     * combine parameter into a URI query string fragment. The values will be
     * escaped correctly.
     *
     * @param parameters
     * @return a URI query string fragment
     */
    private static String makeQueryString(Map<String, String> parameters){
        StringBuilder queryString = new StringBuilder();
        for (String key : parameters.keySet()) {
            if(queryString.length() > 0){
                queryString.append('&');
            }
            if(key.equals("od") || key.equals("os") || key.equals("ms") || key.equals("ad") || key.equals("as") || key.equals("title") || key.equals("bbox")){
                queryString.append(key).append('=').append(parameters.get(key));
            } else {
                queryString.append(key).append('=').append(encode(parameters.get(key)));
            }
        }
        return queryString.toString();
    }

    /**
     * @param fieldUnitPoints
     * @param derivedUnitPoints
     * @param specimenOrObservationTypeColors
     * @param width
     * @param height
     * @param bbox
     * @param backLayer
     * @return
     * e.g.:
     * 	l=v%3Aatbi%2Ce_w_0
     *  &legend=0
     *  &image=false
     *  &recalculate=false
     *  &ms=400%2C350

     *  &od=1%3A44.29481%2C6.82161|44.29252%2C6.822873|44.29247%2C6.82346|44.29279%2C6.823678|44.29269%2C6.82394|44.28482%2C6.887252|44.11469%2C7.287144|44.11468%2C7.289168
     *  &os=1%3Ac%2FFFD700%2F10%2FAporrectodea caliginosa
     */
    public static OccurrenceServiceRequestParameterDto getOccurrenceServiceRequestParameterString(
            List<Point> fieldUnitPoints,
            List<Point> derivedUnitPoints,
            Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors) {

        OccurrenceServiceRequestParameterDto dto = new OccurrenceServiceRequestParameterDto();

        specimenOrObservationTypeColors = mergeMaps(getDefaultSpecimenOrObservationTypeColors(), specimenOrObservationTypeColors);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("legend", "0");

        Map<String, String> styleAndData = new HashMap<>();

        addToStyleAndData(fieldUnitPoints, SpecimenOrObservationType.FieldUnit, specimenOrObservationTypeColors, styleAndData);
        addToStyleAndData(derivedUnitPoints, SpecimenOrObservationType.DerivedUnit, specimenOrObservationTypeColors, styleAndData);

        parameters.put("os", StringUtils.join(styleAndData.keySet().iterator(), "||"));
        parameters.put("od", StringUtils.join(styleAndData.values().iterator(), "||"));

        String queryString = makeQueryString(parameters);

        dto.setFieldUnitPoints(fieldUnitPoints);
        dto.setDerivedUnitPoints(derivedUnitPoints);
        dto.setOccurrenceQuery(queryString);

        logger.info(queryString);

        return dto;
    }

    private static <T, S> Map<T, S> mergeMaps(Map<T, S> defaultMap, Map<T, S> overrideMap) {
        Map<T, S> tmpMap = new HashMap<T, S>();
        tmpMap.putAll(defaultMap);
        if(overrideMap != null){
            tmpMap.putAll(overrideMap);
        }
        return tmpMap;
    }

    private static void addToStyleAndData(
            List<Point> points,
            SpecimenOrObservationType specimenOrObservationType,
            Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors, Map<String, String> styleAndData) {

        //TODO add markerShape and size and Label to specimenOrObservationTypeColors -> Map<Class<SpecimenOrObservationBase<?>>, MapStyle>

        if(points != null && points.size()>0){
            String style =  "c/" + Integer.toHexString(specimenOrObservationTypeColors.get(specimenOrObservationType).getRGB()).substring(2) + "/10/noLabel";
            StringBuilder data = new StringBuilder();
            for(Point point : points){
                if (point != null){  //should not be null, but just in case
                    if(data.length() > 0){
                        data.append('|');
                    }
                    data.append(point.getLatitude() + "," + point.getLongitude());
                }
            }
            int index = styleAndData.size() + 1;
            styleAndData.put(index + ":" +style, index + ":" +data.toString());
        }
    }
}
