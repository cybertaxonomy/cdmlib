/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import java.awt.Color;
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
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermDto;
import eu.etaxonomy.cdm.api.service.portal.DistributionDtoLoader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * @author muellera
 * @since 03.03.2024
 */
public class AreaMapServiceParameterBuilder {

    private static final Logger logger = LogManager.getLogger();

    private static final int INT_MAX_LENGTH = String.valueOf(Integer.MAX_VALUE).length();

    private static final String SUBENTRY_DELIMITER = ",";
    private static final String ID_FROM_VALUES_SEPARATOR = ":";
    private static final String VALUE_LIST_ENTRY_SEPARATOR = "|";
    private static final String VALUE_SUPER_LIST_ENTRY_SEPARATOR = "||";

    //TODO do we really want to store these colors here?
    //     if yes the list should be expanded to all entries
    //TODO 2 make it a Map<UUID,Color> map
    private static HashMap<UUID, Color> defaultDistributionStatusColors =  new HashMap<>();
    static {
        defaultDistributionStatusColors = new HashMap<>();
        defaultDistributionStatusColors.put(PresenceAbsenceTerm.uuidPresent, Color.decode("0x4daf4a"));
        defaultDistributionStatusColors.put(PresenceAbsenceTerm.uuidNative, Color.decode("0x4daf4a"));
        defaultDistributionStatusColors.put(PresenceAbsenceTerm.uuidNativeDoubtfullyNative, Color.decode("0x377eb8"));
        defaultDistributionStatusColors.put(PresenceAbsenceTerm.uuidCultivated, Color.decode("0x984ea3"));
        defaultDistributionStatusColors.put(PresenceAbsenceTerm.uuidIntroduced, Color.decode("0xff7f00"));
        defaultDistributionStatusColors.put(PresenceAbsenceTerm.uuidIntroducedAdventitious, Color.decode("0xffff33"));
        defaultDistributionStatusColors.put(PresenceAbsenceTerm.uuidIntroducedCultiated, Color.decode("0xa65628"));
        defaultDistributionStatusColors.put(PresenceAbsenceTerm.uuidNaturalised, Color.decode("0xf781bf"));
    }

    private static HashMap<UUID,Color> getDefaultDistributionStatusColors() {
        return defaultDistributionStatusColors;
    }

    public String buildFromEntities(
            Collection<Distribution> filteredDistributions,
            IGeoServiceAreaMapping mapping,
            Map<PresenceAbsenceTerm,Color> presenceAbsenceTermColors,
            String projectToLayer,
            List<Language> languages){

        Set<DistributionDto> filteredDistributionsDto = filteredDistributions.stream()
                .map(fd->distToDto(fd)).collect(Collectors.toSet());

        Map<UUID, Color> distributionStatusUuid2ColorsMap = transformDistributionStatusColorMap(
                presenceAbsenceTermColors);

        return build(
                filteredDistributionsDto, mapping, distributionStatusUuid2ColorsMap,
                projectToLayer, languages);
    }

    /**
     * Transforms a distribution status color map, which uses entities,
     * to a map that uses UUIDs.
     */
    public static Map<UUID, Color> transformDistributionStatusColorMap(
            Map<PresenceAbsenceTerm, Color> distributionStatusColorMap) {

        Map<UUID,Color> distributionStatusUuid2ColorsMap =
                distributionStatusColorMap == null? null :
                    distributionStatusColorMap
                        .entrySet().stream()
                        .collect(Collectors.toMap(e->e.getKey().getUuid(), e->e.getValue()));
        return distributionStatusUuid2ColorsMap;
    }

    private DistributionDto distToDto(Distribution dist) {
        DistributionDto dto = DistributionDtoLoader.INSTANCE().fromEntity(dist, null);
        return dto;
    }

    /**
     * Returns the parameter String for the EDIT geo webservice to create a
     * distribution map.
     *
     * @param filteredDistributions
     *            A set of distributions that should be shown on the map.
     *            The set should guarantee that for each area not more than
     *            1 status exists, otherwise the behavior is not deterministic.
     *            For filtering see {@link DescriptionUtility#filterDistributions(
     *            Collection, Set, boolean, boolean, boolean, boolean, boolean)}
     * @param mapping
     *            Data regarding the mapping of NamedAreas to shape file
     *            attribute tables
     * @param distributionStatusColors
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
    public String build(
            Collection<DistributionDto> filteredDistributions,
            IGeoServiceAreaMapping mapping,
            Map<UUID,Color> distributionStatusUuid2ColorsMap,
            String projectToLayer,
            List<Language> languages){

//        Collector<Map.Entry<PresenceAbsenceTerm,Color>,?,Map<UUID,Color>> collector
//            = Collectors.toMap(e->e.getKey().getUuid(), e->e.getValue());

//        Map<UUID,Color> distributionStatusUuid2ColorsMap = distributionStatusEntity2ColorsMap
//                .entrySet().stream()
//                .collect(Collectors.toMap(e->e.getKey().getUuid(), e->e.getValue()));

        /*
         * generateMultipleAreaDataParameters switches between the two possible styles:
         * 1. ad=layername1:area-data||layername2:area-data
         * 2. ad=layername1:area-data&ad=layername2:area-data
         */
        boolean generateMultipleAreaDataParameters = false;

        List<String>  perLayerAreaData = new ArrayList<>();
        Map<Integer, String> areaStyles = new HashMap<>();
        List<String> legendSortList = new ArrayList<>();

        String borderWidth = "0.1";
        String borderColorRgb = "";
        String borderDashingPattern = "";

        //handle empty set
        if(filteredDistributions == null || filteredDistributions.size() == 0){
            return "";
        }

        distributionStatusUuid2ColorsMap = mergeMaps(getDefaultDistributionStatusColors(),
                distributionStatusUuid2ColorsMap);

        Map<String, Map<Integer, Set<DistributionDto>>> layerMap = new HashMap<>();
        List<TermDto> statusList = new ArrayList<>();

        groupStylesAndLayers(filteredDistributions, layerMap, statusList, mapping);

        Map<String, String> parameters = new HashMap<>();

        //style
        int styleCounter = 0;
        for (TermDto status: statusList){

            char styleCode = getStyleAbbrev(styleCounter);

            //getting the area title
            if (languages == null){
                languages = new ArrayList<>();
            }
            if (languages.size() == 0){
                languages.add(Language.DEFAULT());
            }

            //getting the area color
            Color statusColor = distributionStatusUuid2ColorsMap.get(status);
            String fillColorRgb;
            if (statusColor != null){
                fillColorRgb = Integer.toHexString(statusColor.getRGB()).substring(2);
            }else{
                fillColorRgb = status.getDefaultColor(); //TODO
            }
            String styleValues = StringUtils.join(new String[]{fillColorRgb, borderColorRgb, borderWidth, borderDashingPattern}, ',');

            areaStyles.put(styleCounter, styleValues);

            String legendEntry = styleCode + ID_FROM_VALUES_SEPARATOR + encode(status.getLabel());
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
        Map<Integer, Integer> styleUsage = new HashMap<>();

        char styleChar;
        for (String layerString : layerMap.keySet()){
            // each layer
            styledAreasPerLayer = new ArrayList<>();
            Map<Integer, Set<DistributionDto>> styleMap = layerMap.get(layerString);
            for (int style: styleMap.keySet()){
                // stylesPerLayer
                styleChar = getStyleAbbrev(style);
                Set<DistributionDto> distributionSet = styleMap.get(style);
                areasPerStyle = new ArrayList<>();
                for (DistributionDto distribution: distributionSet){
                    // areasPerStyle
                    areasPerStyle.add(encode(getAreaCode(distribution, mapping)));
                }
                styledAreasPerLayer.add(styleChar + ID_FROM_VALUES_SEPARATOR + StringUtils.join(areasPerStyle.iterator(), SUBENTRY_DELIMITER));
            }
            perLayerAreaData.add(encode(layerString) + ID_FROM_VALUES_SEPARATOR + StringUtils.join(styledAreasPerLayer.iterator(), VALUE_LIST_ENTRY_SEPARATOR));
        }

        if(areaStyles.size() > 0){
            ArrayList<Integer> styleIds = new ArrayList<>(areaStyles.size());
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
            // (as all other ordered term, see DefinedTermBase.performCompareTo(T orderedTerm, boolean skipVocabularyCheck)
            // the sorted list must be reverted
//            Collections.reverse(legendSortList);
            // remove the prepended order index (like 000000000000001 ) from the legend entries
            @SuppressWarnings("unchecked")
            Collection<String> legendEntries = CollectionUtils.collect(legendSortList, (o)->{
                      String s = ((String) o);
                      return s.substring(INT_MAX_LENGTH, s.length());
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
    private void groupStylesAndLayers(Collection<DistributionDto> distributions,
            Map<String, Map<Integer,Set<DistributionDto>>> layerMap,
            List<TermDto> statusList,
            IGeoServiceAreaMapping mapping) {


        //iterate through distributions and group styles and layers
        //and collect necessary information
        for (DistributionDto distribution : distributions){
            //collect status
            TermDto status = distribution.getStatus();
            if(status == null){
                continue;
            }
            if (! statusList.contains(status)){
                statusList.add(status);
            }
            //group areas by layers and styles
            NamedAreaDto area = distribution.getArea();

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
    private void addAreaToLayerMap(Map<String,
            Map<Integer,Set<DistributionDto>>> layerMap,
            List<TermDto> statusList,
            DistributionDto distribution,
            NamedAreaDto area,
            IGeoServiceAreaMapping mapping) {

        if (area != null){
            String geoLayerName = getWMSLayerName(area, mapping);

            if(geoLayerName == null){
               logger.warn("no wms layer mapping defined for " + area.getLabel() + " [" + area.getIdInVocabulary() + "]");
            } else {
                Map<Integer, Set<DistributionDto>> styleMap = layerMap.get(geoLayerName);
                if (styleMap == null) {
                    styleMap = new HashMap<>();
                    layerMap.put(geoLayerName, styleMap);
                }
                addDistributionToStyleMap(distribution, styleMap, statusList);
            }
        }
    }

    private String getWMSLayerName(NamedAreaDto area, IGeoServiceAreaMapping mapping){
        UUID vocUuid = area.getVocabularyUuid();
        //TDWG areas
        if (NamedArea.uuidTdwgAreaVocabulary.equals(vocUuid)){
            UUID level = area.getLevelUuid();
            if (level != null) {
                //TODO integrate into CDM
                if (level.equals(NamedAreaLevel.TDWG_LEVEL1().getUuid())) {
                    return "tdwg1";
                } else if (level.equals(NamedAreaLevel.TDWG_LEVEL2().getUuid())) {
                    return "tdwg2";
                }else if (level.equals(NamedAreaLevel.TDWG_LEVEL3().getUuid())) {
                    return "tdwg3";
                }else if (level.equals(NamedAreaLevel.TDWG_LEVEL4().getUuid())) {
                    return "tdwg4";
                }
            }
            //unrecognized tdwg area
            return null;
        }else if (Country.uuidCountryVocabulary.equals(vocUuid)){
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

    private void addDistributionToStyleMap(DistributionDto distribution,
            Map<Integer,Set<DistributionDto>> styleMap,
            List<TermDto> statusList) {

        TermDto status = distribution.getStatus();
        if (status != null) {
            int style = statusList.indexOf(status);
            Set<DistributionDto> distributionSet = styleMap.get(style);
            if (distributionSet == null) {
                distributionSet = new HashSet<>();
                styleMap.put(style, distributionSet);
            }
            distributionSet.add(distribution);
        }
    }

    /**
     * combine parameter into a URI query string fragment. The values will be
     * escaped correctly.
     *
     * @param parameters
     * @return a URI query string fragment
     */
    private String makeQueryString(Map<String, String> parameters){
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

    private String getAreaCode(DistributionDto distribution, IGeoServiceAreaMapping mapping){

        NamedAreaDto area = distribution.getArea();
        UUID vocUuid = area.getVocabularyUuid();
        String result = null;

        if (NamedArea.uuidTdwgAreaVocabulary.equals(vocUuid) || Country.uuidCountryVocabulary.equals(vocUuid)) {
            // TDWG or Country
            result = area.getIdInVocabulary();
            if (NamedAreaLevel.TDWG_LEVEL4().getUuid().equals(area.getLevelUuid())) {
                result = result.replace("-", "");
            }
        } else {
            // use generic GeoServiceArea data stored in technical annotations
            // of the named area
            GeoServiceArea areas = mapping.valueOf(area);
            if (areas != null && areas.size() > 0) {
                // FIXME multiple layers
                List<String> values = areas.getAreasMap().values().iterator().next().values().iterator().next();
                for (String value : values) {
                    result = CdmUtils.concat(SUBENTRY_DELIMITER, result, value);
                }
            }
        }
        return CdmUtils.Nz(result, "-");
    }

    /**
     * URI encode the given String
     */
    private String encode(String string) {
        String encoded = string;
        try {
            encoded = URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        }
        return encoded;
    }

    /**
     * transform an integer (style counter) into a valid character representing a style.
     * 0-25 => a-z<br>
     * 26-51 => A-Z<br>
     * i not in {0,...,51} is undefined
     */
    private char getStyleAbbrev(int i){
        i++;
        int ascii = 96 + i;
        if (i >26){
            ascii = 64 + i;
        }
        return (char)ascii;
    }

    /**
     * Returns a merged new map which contains all values of the override map and
     * for those keys that do not exist in the override map it contains the default
     * map values.
     * TODO move to CdmUtils?
     */
    private <T, S> Map<T, S> mergeMaps(Map<T, S> defaultMap, Map<T, S> overrideMap) {
        Map<T, S> tmpMap = new HashMap<T, S>();
        tmpMap.putAll(defaultMap);
        if(overrideMap != null){
            tmpMap.putAll(overrideMap);
        }
        return tmpMap;
    }
}