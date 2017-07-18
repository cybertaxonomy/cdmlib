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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.api.utility.DescriptionUtility;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;

/**
 * Class implementing the business logic for creating the map service string for
 * a given set of distributions. See {@link EditGeoService} as API for the given functionality.
 *
 * @see EditGeoService
 *
 * @author a.mueller
 * @created 17.11.2008
 */
public class EditGeoServiceUtilities {
    private static final Logger logger = Logger.getLogger(EditGeoServiceUtilities.class);

    private static final int INT_MAX_LENGTH = String.valueOf(Integer.MAX_VALUE).length();

    private static IDefinedTermDao termDao;

    /**
     * @param termDao
     */
    public static void setTermDao(IDefinedTermDao termDao) {
        EditGeoServiceUtilities.termDao= termDao;
    }


    private static HashMap<SpecimenOrObservationType, Color> defaultSpecimenOrObservationTypeColors = null;

    private static HashMap<SpecimenOrObservationType, Color> getDefaultSpecimenOrObservationTypeColors() {
        if(defaultSpecimenOrObservationTypeColors == null){
            defaultSpecimenOrObservationTypeColors = new HashMap<>();
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.FieldUnit, Color.ORANGE);
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.DerivedUnit, Color.RED);
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.LivingSpecimen, Color.GREEN);
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.Observation, Color.ORANGE);
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.PreservedSpecimen, Color.GRAY);
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.Media, Color.BLUE);
        }
        return defaultSpecimenOrObservationTypeColors;
    }


    private static HashMap<PresenceAbsenceTerm, Color> defaultPresenceAbsenceTermBaseColors = null;

    private static List<UUID>  presenceAbsenceTermVocabularyUuids = null;

    private static HashMap<PresenceAbsenceTerm, Color> getDefaultPresenceAbsenceTermBaseColors() {
        if(defaultPresenceAbsenceTermBaseColors == null){
            defaultPresenceAbsenceTermBaseColors = new HashMap<PresenceAbsenceTerm, Color>();
            defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.PRESENT(), Color.decode("0x4daf4a"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.NATIVE(), Color.decode("0x4daf4a"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE(), Color.decode("0x377eb8"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.CULTIVATED(), Color.decode("0x984ea3"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.INTRODUCED(), Color.decode("0xff7f00"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.CASUAL(), Color.decode("0xffff33"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.INTRODUCED_CULTIVATED(), Color.decode("0xa65628"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.NATURALISED(), Color.decode("0xf781bf"));
        }
        return defaultPresenceAbsenceTermBaseColors;
    }



    private static final String SUBENTRY_DELIMITER = ",";
    private static final String ENTRY_DELIMITER = ";";
    static final String ID_FROM_VALUES_SEPARATOR = ":";
    static final String VALUE_LIST_ENTRY_SEPARATOR = "|";
    static final String VALUE_SUPER_LIST_ENTRY_SEPARATOR = "||";


    /**
     * Returns the parameter String for the EDIT geo webservice to create a
     * distribution map.
     *
     * @param distributions
     *            A set of distributions that should be shown on the map
     *            The {@link DescriptionUtility} class provides a method for
     *            filtering a set of Distributions :
     *
     *            {@code
     *            Collection<Distribution> filteredDistributions =
     *            DescriptionUtility.filterDistributions(distributions,
     *            subAreaPreference, statusOrderPreference, hideMarkedAreas);
     *            }
     * @param mapping
     *            Data regarding the mapping of NamedAreas to shape file
     *            attribute tables
     * @param presenceAbsenceTermColors
     *            A map that defines the colors of PresenceAbsenceTerms. The
     *            PresenceAbsenceTerms are defined by their uuid. If a
     *            PresenceAbsenceTerm is not included in this map, it's default
     *            color is taken instead. If the map == null all terms are
     *            colored by their default color.
     * @param projectToLayer
     *            name of a layer which is representing a specific
     *            {@link NamedAreaLevel} Supply this parameter if you to project
     *            all other distribution area levels to this layer.
     * @param languages
     *
     * @return the parameter string or an empty string if the
     *         <code>distributions</code> set was null or empty.
     */
    @Transient
    public static String getDistributionServiceRequestParameterString(
            Collection<Distribution> filteredDistributions,
            IGeoServiceAreaMapping mapping,
            Map<PresenceAbsenceTerm,Color> presenceAbsenceTermColors,
            String projectToLayer,
            List<Language> languages){


        /*
         * generateMultipleAreaDataParameters switches between the two possible styles:
         * 1. ad=layername1:area-data||layername2:area-data
         * 2. ad=layername1:area-data&ad=layername2:area-data
         */
        boolean generateMultipleAreaDataParameters = false;

        List<String>  perLayerAreaData = new ArrayList<String>();
        Map<Integer, String> areaStyles = new HashMap<Integer, String>();
        List<String> legendSortList = new ArrayList<String>();

        String borderWidth = "0.1";
        String borderColorRgb = "";
        String borderDashingPattern = "";


        //handle empty set
        if(filteredDistributions == null || filteredDistributions.size() == 0){
            return "";
        }

        presenceAbsenceTermColors = mergeMaps(getDefaultPresenceAbsenceTermBaseColors(), presenceAbsenceTermColors);

        Map<String, Map<Integer, Set<Distribution>>> layerMap = new HashMap<String, Map<Integer, Set<Distribution>>>();
        List<PresenceAbsenceTerm> statusList = new ArrayList<PresenceAbsenceTerm>();

        groupStylesAndLayers(filteredDistributions, layerMap, statusList, mapping);

        Map<String, String> parameters = new HashMap<String, String>();

        //style
        int styleCounter = 0;
        for (PresenceAbsenceTerm status: statusList){

            char styleCode = getStyleAbbrev(styleCounter);

            //getting the area title
            if (languages == null){
                languages = new ArrayList<Language>();
            }
            if (languages.size() == 0){
                languages.add(Language.DEFAULT());
            }
            Representation statusRepresentation = status.getPreferredRepresentation(languages);

            //getting the area color
            Color statusColor = presenceAbsenceTermColors.get(status);
            String fillColorRgb;
            if (statusColor != null){
                fillColorRgb = Integer.toHexString(statusColor.getRGB()).substring(2);
            }else{
                fillColorRgb = status.getDefaultColor(); //TODO
            }
            String styleValues = StringUtils.join(new String[]{fillColorRgb, borderColorRgb, borderWidth, borderDashingPattern}, ',');

            areaStyles.put(styleCounter, styleValues);

            String legendEntry = styleCode + ID_FROM_VALUES_SEPARATOR + encode(statusRepresentation.getLabel());
            legendSortList.add(StringUtils.leftPad(String.valueOf(status.getOrderIndex()), INT_MAX_LENGTH, '0') + legendEntry );
            styleCounter++;
        }

        // area data
        List<String> styledAreasPerLayer;
        List<String> areasPerStyle;
        /**
         * Map<Integer, Integer> styleUsage
         *
         * Used to avoid reusing styles in multiple layers
         *
         * key: the style id
         * value: the count of how often the style has been used for different layers, starts with 0 for first time use
         */
        Map<Integer, Integer> styleUsage = new HashMap<Integer, Integer>();

        char styleChar;
        for (String layerString : layerMap.keySet()){
            // each layer
            styledAreasPerLayer = new ArrayList<String>();
            Map<Integer, Set<Distribution>> styleMap = layerMap.get(layerString);
            for (int style: styleMap.keySet()){
                // stylesPerLayer
                styleChar = getStyleAbbrev(style);
                Set<Distribution> distributionSet = styleMap.get(style);
                areasPerStyle = new ArrayList<String>();
                for (Distribution distribution: distributionSet){
                    // areasPerStyle
                    areasPerStyle.add(encode(getAreaCode(distribution, mapping)));
                }
                styledAreasPerLayer.add(styleChar + ID_FROM_VALUES_SEPARATOR + StringUtils.join(areasPerStyle.iterator(), SUBENTRY_DELIMITER));
            }
            perLayerAreaData.add(encode(layerString) + ID_FROM_VALUES_SEPARATOR + StringUtils.join(styledAreasPerLayer.iterator(), VALUE_LIST_ENTRY_SEPARATOR));
        }

        if(areaStyles.size() > 0){
            ArrayList<Integer> styleIds = new ArrayList<Integer>(areaStyles.size());
            styleIds.addAll(areaStyles.keySet());
            Collections.sort(styleIds); // why is it necessary to sort here?
            StringBuilder db = new StringBuilder();
            for(Integer sid : styleIds){
                if(db.length() > 0){
                    db.append(VALUE_LIST_ENTRY_SEPARATOR);
                }
                db.append( getStyleAbbrev(sid)).append(ID_FROM_VALUES_SEPARATOR).append(areaStyles.get(sid));
            }
            parameters.put("as", db.toString());
        }
        if(legendSortList.size() > 0){
            // sort the label entries after the status terms
            Collections.sort(legendSortList);
            // since the status terms are have an inverse natural order
            // (as all other ordered term, see OrderedTermBase.performCompareTo(T orderedTerm, boolean skipVocabularyCheck)
            // the sorted list must be reverted
//            Collections.reverse(legendSortList);
            // remove the prepended order index (like 000000000000001 ) from the legend entries
            @SuppressWarnings("unchecked")
            Collection<String> legendEntries = CollectionUtils.collect(legendSortList, new Transformer()
            {
                @Override
                public String transform(Object o)
                {
                  String s = ((String) o);
                  return s.substring(INT_MAX_LENGTH, s.length());
                }
              });

            parameters.put("title", StringUtils.join(legendEntries.iterator(), VALUE_LIST_ENTRY_SEPARATOR));
        }

        if(generateMultipleAreaDataParameters){
            // not generically possible since parameters can not contain duplicate keys with value "ad"
        } else {
            parameters.put("ad", StringUtils.join(perLayerAreaData.iterator(), VALUE_SUPER_LIST_ENTRY_SEPARATOR));
        }

        String queryString = makeQueryString(parameters);
        logger.debug("getDistributionServiceRequestParameterString(): " + queryString);

        return queryString;
    }


    /**
     * Fills the layerMap and the statusList
     *
     * @param distributions
     * @param layerMap see {@link #addAreaToLayerMap(Map, List, Distribution, NamedArea, IGeoServiceAreaMapping)}
     * @param statusList
     */
    private static void groupStylesAndLayers(Collection<Distribution> distributions,
            Map<String, Map<Integer,Set<Distribution>>> layerMap,
            List<PresenceAbsenceTerm> statusList,
            IGeoServiceAreaMapping mapping) {


        //iterate through distributions and group styles and layers
        //and collect necessary information
        for (Distribution distribution : distributions){
            //collect status
            PresenceAbsenceTerm status = distribution.getStatus();
            if(status == null){
                continue;
            }
            status = HibernateProxyHelper.deproxy(status, PresenceAbsenceTerm.class);
            if (! statusList.contains(status)){
                statusList.add(status);
            }
            //group areas by layers and styles
            NamedArea area = distribution.getArea();

            addAreaToLayerMap(layerMap, statusList, distribution, area, mapping);
        }
    }

    /**
     * Adds the areas to the layer map. Areas which do not have layer information
     * mapped to them are ignored.
     * <p>
     * A layer map holds the following information:
     *
     * <ul>
     *   <li><b>String</b>: the WMSLayerName which matches the level of the
     *   contained distributions areas</li>
     *   <li><b>StyleMap</b>:</li>
     *   <ul>
     *     <li><b>Integer</b>: the index of the status in the
     *     <code>statusList</code></li>
     *     <li><b>Set{@code<Distribution>}</b>: the set of distributions having the
     *     same Status, the status list is populated in {@link #groupStylesAndLayers(Set, Map, List, IGeoServiceAreaMapping)}</li>
     *   </ul>
     * </ul>
     *
     * @param layerMap
     * @param statusList
     * @param distribution
     * @param area
     */
    private static void addAreaToLayerMap(Map<String, Map<Integer,
            Set<Distribution>>> layerMap,
            List<PresenceAbsenceTerm> statusList,
            Distribution distribution,
            NamedArea area,
            IGeoServiceAreaMapping mapping) {

        if (area != null){
            String geoLayerName = getWMSLayerName(area, mapping);

            if(geoLayerName == null){
               logger.warn("no wms layer mapping defined for " + area.getLabel() + " [" + area.getIdInVocabulary() + "]");
            } else {
                Map<Integer, Set<Distribution>> styleMap = layerMap.get(geoLayerName);
                if (styleMap == null) {
                    styleMap = new HashMap<Integer, Set<Distribution>>();
                    layerMap.put(geoLayerName, styleMap);
                }
                addDistributionToStyleMap(distribution, styleMap, statusList);
            }
        }
    }



    /**
     * URI encode the given String
     * @param string
     * @return
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

    private static String getAreaCode(Distribution distribution, IGeoServiceAreaMapping mapping){
        NamedArea area = distribution.getArea();
        TermVocabulary<NamedArea> voc = area.getVocabulary();
        String result = null;

        if (voc != null && voc.getUuid().equals(NamedArea.uuidTdwgAreaVocabulary) ||  voc.getUuid().equals(Country.uuidCountryVocabulary)) {
            // TDWG or Country
            result = area.getIdInVocabulary();
            if (area.getLevel() != null && area.getLevel().equals(NamedAreaLevel.TDWG_LEVEL4())) {
                result = result.replace("-", "");
            }
        } else {
            // use generic GeoServiceArea data stored in technical annotations
            // of the
            // named area
            GeoServiceArea areas = mapping.valueOf(area);
            if ((areas != null) && areas.size() > 0) {
                // FIXME multiple layers
                List<String> values = areas.getAreasMap().values().iterator().next().values().iterator().next();
                for (String value : values) {
                    result = CdmUtils.concat(SUBENTRY_DELIMITER, result, value);
                }
            }

        }
        return CdmUtils.Nz(result, "-");

    }

    private static List<String> projectToWMSSubLayer(NamedArea area){

        List<String> layerNames = new ArrayList<String>();
        String matchedLayerName = null;
        TermVocabulary<NamedArea> voc = area.getVocabulary();
        //TDWG areas
        if (voc.getUuid().equals(NamedArea.uuidTdwgAreaVocabulary)){
            NamedAreaLevel level = area.getLevel();
            if (level != null) {
                //TODO integrate into CDM
                if (level.equals(NamedAreaLevel.TDWG_LEVEL1())) {
                    matchedLayerName = "tdwg1" ;
                } else if (level.equals(NamedAreaLevel.TDWG_LEVEL2())) {
                    matchedLayerName = "tdwg2";
                }else if (level.equals(NamedAreaLevel.TDWG_LEVEL3())) {
                    matchedLayerName = "tdwg3";
                }else if (level.equals(NamedAreaLevel.TDWG_LEVEL4())) {
                    matchedLayerName = "tdwg4";
                }
            }
            //unrecognized tdwg area

        }
        //TODO countries

        // check if the matched layer equals the layer to project to
        // if not: recurse into the sub-level in order to find the specified one.
        String[] matchedLayerNameTokens = StringUtils.split(matchedLayerName, ':');
//		if(matchedLayerNameTokens.length > 0 &&  matchedLayerNameTokens[0] != projectToLayer){
//			for (NamedArea subArea : area.getIncludes()){
//
//			}
            //
            // add all sub areas
//		}

        return null;
    }

    private static String getWMSLayerName(NamedArea area, IGeoServiceAreaMapping mapping){
        TermVocabulary<NamedArea> voc = area.getVocabulary();
        //TDWG areas
        if (voc.getUuid().equals(NamedArea.uuidTdwgAreaVocabulary)){
            NamedAreaLevel level = area.getLevel();
            if (level != null) {
                //TODO integrate into CDM
                if (level.equals(NamedAreaLevel.TDWG_LEVEL1())) {
                    return "tdwg1";
                } else if (level.equals(NamedAreaLevel.TDWG_LEVEL2())) {
                    return "tdwg2";
                }else if (level.equals(NamedAreaLevel.TDWG_LEVEL3())) {
                    return "tdwg3";
                }else if (level.equals(NamedAreaLevel.TDWG_LEVEL4())) {
                    return "tdwg4";
                }
            }
            //unrecognized tdwg area
            return null;

        }else if (voc.getUuid().equals(Country.uuidCountryVocabulary)){
            return "country_earth:gmi_cntry";
        }

        GeoServiceArea areas = mapping.valueOf(area);
        if (areas != null && areas.getAreasMap().size() > 0){
            //FIXME multiple layers
            String layer = areas.getAreasMap().keySet().iterator().next();
            Map<String, List<String>> fields = areas.getAreasMap().get(layer);
            String field = fields.keySet().iterator().next();
            String layerString = layer + ":" + field;
            return layerString.toLowerCase();
        }

        return null;
    }


    private static void addDistributionToStyleMap(Distribution distribution, Map<Integer, Set<Distribution>> styleMap,
            List<PresenceAbsenceTerm> statusList) {
        PresenceAbsenceTerm status = distribution.getStatus();
        if (status != null) {
            int style = statusList.indexOf(status);
            Set<Distribution> distributionSet = styleMap.get(style);
            if (distributionSet == null) {
                distributionSet = new HashSet<Distribution>();
                styleMap.put(style, distributionSet);
            }
            distributionSet.add(distribution);
        }
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

    /**
     * @param <T>
     * @param <S>
     * @param defaultMap
     * @param overrideMap
     * @return
     */
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
                if(data.length() > 0){
                    data.append('|');
                }
                data.append(point.getLatitude() + "," + point.getLongitude());
            }
            int index = styleAndData.size() + 1;
            styleAndData.put(index + ":" +style, index + ":" +data.toString());
        }
    }


    /**
     * transform an integer (style counter) into a valid character representing a style.
     * 0-25 => a-z<br>
     * 26-51 => A-Z<br>
     * i not in {0,...,51} is undefined
     * @param i
     * @return
     */
    private static char getStyleAbbrev(int i){
        i++;
        int ascii = 96 + i;
        if (i >26){
            ascii = 64 + i;
        }
        return (char)ascii;
    }

    /**
     * @param statusColorJson for example: {@code {"n":"#ff0000","p":"#ffff00"}}
     * @param vocabularyService TODO
     * @return
     * @throws IOException
     * @throws JsonParseException
     * @throws JsonMappingException
     */
    public static Map<PresenceAbsenceTerm, Color> buildStatusColorMap(String statusColorJson, ITermService termService, IVocabularyService vocabularyService) throws IOException, JsonParseException,
            JsonMappingException {

        Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors = null;
        if(StringUtils.isNotEmpty(statusColorJson)){

            ObjectMapper mapper = new ObjectMapper();
            // TODO cache the color maps to speed this up?

            TypeFactory typeFactory = mapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);

            Map<String,String> statusColorMap = mapper.readValue(statusColorJson, mapType);
            presenceAbsenceTermColors = new HashMap<PresenceAbsenceTerm, Color>();
            PresenceAbsenceTerm paTerm = null;
            for(String statusId : statusColorMap.keySet()){
                try {
                    Color color = Color.decode(statusColorMap.get(statusId));
                    // the below loop is  a hack for #4522 (custom status colors not working in cyprus portal)
                    // remove it once the ticket is solved
                    for(UUID vocabUuid : presenceAbsenceTermVocabularyUuids(vocabularyService)) {
                        paTerm = termService.findByIdInVocabulary(statusId, vocabUuid, PresenceAbsenceTerm.class);
                        if(paTerm != null) {
                            break;
                        }
                    }
                    if(paTerm != null){
                        presenceAbsenceTermColors.put(paTerm, color);
                    }
                } catch (NumberFormatException e){
                    logger.error("Cannot decode color", e);
                }
            }
        }
        return presenceAbsenceTermColors;
    }

    /**
     * this is a hack for #4522 (custom status colors not working in cyprus portal)
     * remove this method once the ticket is solved
     *
     * @param vocabularyService
     * @return
     */
    private static List<UUID> presenceAbsenceTermVocabularyUuids(IVocabularyService vocabularyService) {

        if(EditGeoServiceUtilities.presenceAbsenceTermVocabularyUuids == null) {

            List<UUID> uuids = new ArrayList<UUID>();
            // the default as first entry
            UUID presenceTermVocabUuid = PresenceAbsenceTerm.NATIVE().getVocabulary().getUuid();
            uuids.add(presenceTermVocabUuid);


            for(TermVocabulary vocab : vocabularyService.findByTermType(TermType.PresenceAbsenceTerm, null)) {
                if(!uuids.contains(vocab.getUuid())) {
                    uuids.add(vocab.getUuid());
                }
            }

            EditGeoServiceUtilities.presenceAbsenceTermVocabularyUuids = uuids;
        }

        return EditGeoServiceUtilities.presenceAbsenceTermVocabularyUuids;
    }


    /**
     * @param filteredDistributions
     * @param recipe
     * @param hideMarkedAreas
     * @param langs
     * @return
     */
    public static CondensedDistribution getCondensedDistribution(Collection<Distribution> filteredDistributions,
            CondensedDistributionRecipe recipe, List<Language> langs) {
        ICondensedDistributionComposer composer;
        if(recipe == null) {
            throw new NullPointerException("parameter recipe must not be null");
        }
        try {
            composer = recipe.newCondensedDistributionComposerInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                filteredDistributions,  langs);
        return condensedDistribution;
    }

}
