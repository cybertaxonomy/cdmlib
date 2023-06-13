/**
* Copyright (C) 2007 EDIT
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

import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionTreeDto;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionOrder;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.portal.DistributionTreeDtoLoader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.format.description.distribution.CondensedDistribution;
import eu.etaxonomy.cdm.format.description.distribution.CondensedDistributionComposer;
import eu.etaxonomy.cdm.format.description.distribution.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * Class implementing the business logic for creating the map service string for
 * a given set of distributions. See {@link EditGeoService} as API for the given functionality.
 *
 * @see EditGeoService
 * @see
 *
 * @author a.mueller
 * @since 17.11.2008 (as {@link DistributionServiceUtilities} in cdmlib-ext)
 */
public class DistributionServiceUtilities {

    private static final Logger logger = LogManager.getLogger();

    private static final int INT_MAX_LENGTH = String.valueOf(Integer.MAX_VALUE).length();

    private static final String SUBENTRY_DELIMITER = ",";
    private static final String ID_FROM_VALUES_SEPARATOR = ":";
    private static final String VALUE_LIST_ENTRY_SEPARATOR = "|";
    private static final String VALUE_SUPER_LIST_ENTRY_SEPARATOR = "||";

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

    private static HashMap<PresenceAbsenceTerm, Color> defaultPresenceAbsenceTermBaseColors =  new HashMap<>();
    static {
        defaultPresenceAbsenceTermBaseColors = new HashMap<>();
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.PRESENT(), Color.decode("0x4daf4a"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.NATIVE(), Color.decode("0x4daf4a"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE(), Color.decode("0x377eb8"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.CULTIVATED(), Color.decode("0x984ea3"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.INTRODUCED(), Color.decode("0xff7f00"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.CASUAL(), Color.decode("0xffff33"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.INTRODUCED_CULTIVATED(), Color.decode("0xa65628"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.NATURALISED(), Color.decode("0xf781bf"));
    }

    private static List<UUID>  presenceAbsenceTermVocabularyUuids = null;

    private static HashMap<PresenceAbsenceTerm, Color> getDefaultPresenceAbsenceTermBaseColors() {
        return defaultPresenceAbsenceTermBaseColors;
    }


    /**
     * @param filteredDistributions
     *            A set of distributions a condensed distribution string should
     *            be created for.
     *            The set should guarantee that for each area not more than
     *            1 status exists, otherwise the behavior is not deterministic.
     *            For filtering see {@link DescriptionUtility#filterDistributions(
     *            Collection, Set, boolean, boolean, boolean, boolean, boolean)}
     * @param parentAreaMap TODO should we have a separate tree for the condensed distribution string?
     *            Add the tree to the CondensedDistributionConfiguration?
     * @param config
     *            The configuration for the condensed distribution string creation.
     * @param languages
     *            A list of preferred languages in case the status or area symbols are
     *            to be taken from the language abbreviations (not really in use)
     *            TODO could be moved to configuration or fully removed
     * @return
     *            A CondensedDistribution object that contains a string representation
     *            and a {@link TaggedText} representation of the condensed distribution string.
     */
    public static CondensedDistribution getCondensedDistribution(Collection<Distribution> filteredDistributions,
            SetMap<NamedArea, NamedArea> parentAreaMap, CondensedDistributionConfiguration config, List<Language> languages) {

        CondensedDistributionComposer composer = new CondensedDistributionComposer();

        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                filteredDistributions, parentAreaMap, languages, config);
        return condensedDistribution;
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

        presenceAbsenceTermColors = mergeMaps(getDefaultPresenceAbsenceTermBaseColors(), presenceAbsenceTermColors);

        Map<String, Map<Integer, Set<Distribution>>> layerMap = new HashMap<>();
        List<PresenceAbsenceTerm> statusList = new ArrayList<>();

        groupStylesAndLayers(filteredDistributions, layerMap, statusList, mapping);

        Map<String, String> parameters = new HashMap<>();

        //style
        int styleCounter = 0;
        for (PresenceAbsenceTerm status: statusList){

            char styleCode = getStyleAbbrev(styleCounter);

            //getting the area title
            if (languages == null){
                languages = new ArrayList<>();
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
        Map<Integer, Integer> styleUsage = new HashMap<>();

        char styleChar;
        for (String layerString : layerMap.keySet()){
            // each layer
            styledAreasPerLayer = new ArrayList<>();
            Map<Integer, Set<Distribution>> styleMap = layerMap.get(layerString);
            for (int style: styleMap.keySet()){
                // stylesPerLayer
                styleChar = getStyleAbbrev(style);
                Set<Distribution> distributionSet = styleMap.get(style);
                areasPerStyle = new ArrayList<>();
                for (Distribution distribution: distributionSet){
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
            status = HibernateProxyHelper.deproxy(status);
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
                    styleMap = new HashMap<>();
                    layerMap.put(geoLayerName, styleMap);
                }
                addDistributionToStyleMap(distribution, styleMap, statusList);
            }
        }
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

    private static String getAreaCode(Distribution distribution, IGeoServiceAreaMapping mapping){

        NamedArea area = distribution.getArea();
        TermVocabulary<NamedArea> voc = area.getVocabulary();
        String result = null;

        if (voc != null && (voc.getUuid().equals(NamedArea.uuidTdwgAreaVocabulary) ||  voc.getUuid().equals(Country.uuidCountryVocabulary))) {
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

    private static List<String> projectToWMSSubLayer(NamedArea area){

        List<String> layerNames = new ArrayList<>();
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

    private static <T, S> Map<T, S> mergeMaps(Map<T, S> defaultMap, Map<T, S> overrideMap) {
        Map<T, S> tmpMap = new HashMap<T, S>();
        tmpMap.putAll(defaultMap);
        if(overrideMap != null){
            tmpMap.putAll(overrideMap);
        }
        return tmpMap;
    }

    /**
     * transform an integer (style counter) into a valid character representing a style.
     * 0-25 => a-z<br>
     * 26-51 => A-Z<br>
     * i not in {0,...,51} is undefined
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
     */
    public static Map<PresenceAbsenceTerm, Color> buildStatusColorMap(String statusColorJson,
            ITermService termService, IVocabularyService vocabularyService)
            throws JsonProcessingException {

        Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors = null;
        if(StringUtils.isNotEmpty(statusColorJson)){

            ObjectMapper mapper = new ObjectMapper();
            // TODO cache the color maps to speed this up?

            TypeFactory typeFactory = mapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);

            Map<String,String> statusColorMap = mapper.readValue(statusColorJson, mapType);
            presenceAbsenceTermColors = new HashMap<>();
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

        if(DistributionServiceUtilities.presenceAbsenceTermVocabularyUuids == null) {

            List<UUID> uuids = new ArrayList<>();
            // the default as first entry
            UUID presenceTermVocabUuid = PresenceAbsenceTerm.NATIVE().getVocabulary().getUuid();
            uuids.add(presenceTermVocabUuid);

            for(TermVocabulary<?> vocab : vocabularyService.findByTermType(TermType.PresenceAbsenceTerm, null)) {
                if(!uuids.contains(vocab.getUuid())) {
                    uuids.add(vocab.getUuid());
                }
            }
            DistributionServiceUtilities.presenceAbsenceTermVocabularyUuids = uuids;
        }
        return DistributionServiceUtilities.presenceAbsenceTermVocabularyUuids;
    }


    /**
     * <b>NOTE: To avoid LayzyLoadingExceptions this method must be used in a transactional context.</b>
     *
     * Filters the given set of {@link Distribution}s for publication purposes.
     * The following rules are respected during the filtering:
     * <ol>
     * <li><b>Marked area filter</b>: Skip distributions for areas having a {@code TRUE} {@link Marker}
     * with one of the specified {@link MarkerType}s. Existing sub-areas of a marked area must also be marked
     * with the same marker type, otherwise the marked area acts as a <b>fallback area</b> for the sub areas.
     * An area is a <b>fallback area</b> if it is marked to be hidden and if it has at least one
     * sub area which is not marked to be hidden. The fallback area will be shown if there is no {@link Distribution}
     * for any of the non hidden sub-areas. For more detailed discussion on fallback areas see
     * https://dev.e-taxonomy.eu/redmine/issues/4408</li>
     *
     * <li><b>Prefer aggregated rule</b>: if this flag is set to <code>true</code> aggregated
     * distributions are preferred over non-aggregated elements.
     * (Aggregated descriptions are identified by their description having type
     * {@link DescriptionType.AGGREGATED_DISTRIBUTION}). This means if a non-aggregated status
     * information exists for the same area for which aggregated data is available,
     * the aggregated data has to be given preference over other data.
     * See parameter <code>preferAggregated</code></li>
     *
     * <li><b>Status order preference rule</b>: In case of multiple distribution
     * status ({@link PresenceAbsenceTermBase}) for the same area the status
     * with the highest order is preferred, see
     * {@link DefinedTermBase#compareTo(DefinedTermBase)}. This rule is
     * optional, see parameter <code>statusOrderPreference</code></li>
     *
     * <li><b>Sub area preference rule</b>: If there is an area with a <i>direct
     * sub area</i> and both areas have the same status only the
     * information on the sub area should be reported, whereas the super area
     * should be ignored. This rule is optional, see parameter
     * <code>subAreaPreference</code>. Can be run separately from the other filters.
     * This rule affects any distribution,
     * that is to be computed and edited equally. For more details see
     * {@link https://dev.e-taxonomy.eu/redmine/issues/5050})</li>
     * </ol>
     *
     * @param distributions
     *            the distributions to filter
     * @param fallbackAreaMarkerTypes
     *            distributions where the area has a {@link Marker} with one of the specified {@link MarkerType}s will
     *            be skipped or acts as fall back area. For more details see <b>Marked area filter</b> above.
     * @param preferAggregated
     *            Computed distributions for the same area will be preferred over edited distributions.
     *            <b>This parameter should always be set to <code>true</code>.</b>
     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to true,
     *            This rule can be run separately from the other filters.
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true
     * @param ignoreDistributionStatusUndefined
     *            workaround until #9500 is implemented
     * @return the filtered collection of distribution elements.
     */
    public static Set<Distribution> filterDistributions(Collection<Distribution> distributions,
            TermTree<NamedArea> areaTree, Set<MarkerType> fallbackAreaMarkerTypes,
            boolean preferAggregated, boolean statusOrderPreference,
            boolean subAreaPreference, boolean keepFallBackOnlyIfNoSubareaDataExists,
            boolean ignoreDistributionStatusUndefined) {

        SetMap<NamedArea, Distribution> filteredDistributions = new SetMap<>(distributions.size());

        // assign distributions to the area and filter undefinedStatus
        for(Distribution distribution : distributions){
            NamedArea area = distribution.getArea();
            if(area == null) {
                logger.debug("skipping distribution with NULL area");
                continue;
            }
            boolean filterUndefined = ignoreDistributionStatusUndefined && distribution.getStatus() != null
                    && distribution.getStatus().getUuid().equals(PresenceAbsenceTerm.uuidUndefined);
            if (!filterUndefined){
                filteredDistributions.putItem(area, distribution);
            }
        }

        if (areaTree == null) {
            areaTree = getAreaTree(distributions, fallbackAreaMarkerTypes);
        }

        // -------------------------------------------------------------------
        // 1) skip distributions having an area with markers matching fallbackAreaMarkerTypes
        //    but keep distributions for fallback areas (areas with hidden marker, but with visible sub-areas)
        //TODO since using area tree this is only relevant if keepFallBackOnlyIfNoSubareaDataExists = true
        //     as the area tree should also exclude real hidden areas
//        if(!CdmUtils.isNullSafeEmpty(fallbackAreaMarkerTypes)) {
            removeHiddenAndKeepFallbackAreas(areaTree, fallbackAreaMarkerTypes, filteredDistributions, keepFallBackOnlyIfNoSubareaDataExists);
//        }

        // -------------------------------------------------------------------
        // 2) remove not computed distributions for areas for which computed
        //    distributions exists
        if(preferAggregated) {
            handlePreferAggregated(filteredDistributions);
        }

        // -------------------------------------------------------------------
        // 3) status order preference rule
        if (statusOrderPreference) {
            SetMap<NamedArea, Distribution> tmpMap = new SetMap<>(filteredDistributions.size());
            for(NamedArea key : filteredDistributions.keySet()){
                tmpMap.put(key, filterByHighestDistributionStatusForArea(filteredDistributions.get(key)));
            }
            filteredDistributions = tmpMap;
        }

        // -------------------------------------------------------------------
        // 4) Sub area preference rule
        if(subAreaPreference){
            handleSubAreaPreferenceRule(filteredDistributions, areaTree);
        }

        return valuesOfAllInnerSets(filteredDistributions.values());
    }

    static TermTree<NamedArea> getAreaTree(Collection<Distribution> distributions,
            Set<MarkerType> fallbackAreaMarkerTypes) {

        //TODO see comment in below method
//        List<TermVocabulary<NamedArea>> vocs = getVocabualries(distributions);
        TermTree<NamedArea> areaTree = createAreaTreeByDistributions(distributions, fallbackAreaMarkerTypes);
        return areaTree;
    }

    /**
     * Returns a list of all named area vocabularies for named areas used in the given
     */
    //TODO maybe needed by above call once vocabularies are term trees
    private static List<TermVocabulary<NamedArea>> getVocabualries(Collection<Distribution> distributions) {
        List<TermVocabulary<NamedArea>> result = new ArrayList<>();
        distributions.stream()
            .map(d->d.getArea())
            .filter(a->a != null)
            .map(a->a.getVocabulary())
            .filter(v->!result.contains(v))
            .forEach(v->result.add(v));
        return result;
    }

    /**
     * Creates a term tree from the part-of information of the distribution areas.
     * Removes hidden areas marked as such if they have no children.
     */
    private static TermTree<NamedArea> createAreaTreeByDistributions(Collection<Distribution> distributions,
            Set<MarkerType> fallbackAreaMarkerTypes) {

        fallbackAreaMarkerTypes = fallbackAreaMarkerTypes == null ? new HashSet<>() : fallbackAreaMarkerTypes;
        TermTree<NamedArea> result = TermTree.NewInstance(TermType.NamedArea, NamedArea.class);

        //create area list
        Set<NamedArea> relevantAreas = new HashSet<>();
        for (Distribution distribution : distributions) {
            NamedArea area = HibernateProxyHelper.deproxy(distribution.getArea(), NamedArea.class) ;
            if (area != null && !relevantAreas.contains(area)) {
                boolean isHidden = isMarkedAs(area, fallbackAreaMarkerTypes);
                if (isHidden) {
                    //if is hidden area either ignore (hidden area) or add unhidden children (fallback area)
                    Set<NamedArea> unhiddenChildren = getUnhiddenChildren(area, fallbackAreaMarkerTypes);
                    if (!unhiddenChildren.isEmpty()) {
                        relevantAreas.addAll(unhiddenChildren);
                    }
                }else {
                    relevantAreas.add(area);
                }
            }
        }

        //create area tree from area list using part-of info
        Map<NamedArea, TermNode<NamedArea>> map = new HashMap<>();

        for (NamedArea area : relevantAreas) {
            if (map.get(area)!=null) {
                continue;
            }
            NamedArea parent = area.getPartOf();
            TermNode<NamedArea> parentNode;

            if (parent == null) {
                parentNode = result.getRoot();
            }else {
                parentNode = getNodeRecursive(parent, map, result.getRoot());
            }

            TermNode<NamedArea> node = parentNode.addChild(area);
            map.put(area, node);
        }
        return result;
    }

    private static TermNode<NamedArea> getNodeRecursive(NamedArea area, Map<NamedArea, TermNode<NamedArea>> map,
            TermNode<NamedArea> root) {

            TermNode<NamedArea> areaNode = map.get(area);
            if (areaNode != null) {
                return areaNode;
            }
            //this usage is only needed as long as vocabularies are not term trees yet
            NamedArea parentArea = area.getPartOf();
            TermNode<NamedArea> parentNode;

            if (parentArea == null) {
                parentNode = root;
            }else {
                parentNode = map.get(parentArea);
                if (parentNode == null) {
                    parentNode = getNodeRecursive(parentArea, map, root);
                }
            }
            areaNode = parentNode.addChild(area);
            map.put(area, areaNode);
            return areaNode;
    }

    private static Set<NamedArea> getUnhiddenChildren(NamedArea area, Set<MarkerType> fallbackAreaMarkerTypes) {
        Set<NamedArea> result = new HashSet<>();
        for (NamedArea child : area.getIncludes()) {
            if (!isMarkedAs(child, fallbackAreaMarkerTypes)) {
                result.add(child);
            }
            result.addAll(getUnhiddenChildren(child, fallbackAreaMarkerTypes));
        }
        return result;
    }

    /**
     * Removes all distributions that have an area being a parent of
     * anothers distributions area. E.g. removes distribution for "Europe"
     * if a distribution for "France" exists in the list, where Europe
     * is a direct parent for France.
     */
    private static void handleSubAreaPreferenceRule(SetMap<NamedArea, Distribution> filteredDistributions,
            TermTree<NamedArea> areaTree) {

        SetMap<NamedArea, NamedArea> parentMap = areaTree.getParentMap();
        Set<NamedArea> removeCandidatesArea = new HashSet<>();
        for(NamedArea area : filteredDistributions.keySet()){
            if(removeCandidatesArea.contains(area)){
                continue;
            }
            parentMap.get(area).forEach(parent->{
                if(parent != null && filteredDistributions.containsKey(parent)){
                    removeCandidatesArea.add(parent);
                }
            });
        }
        for(NamedArea removeKey : removeCandidatesArea){
            filteredDistributions.remove(removeKey);
        }
    }

    /**
     * Remove areas not in area tree but keep fallback areas.
     */
    private static void removeHiddenAndKeepFallbackAreas(TermTree<NamedArea> areaTree, Set<MarkerType> fallbackAreaMarkerTypes,
            SetMap<NamedArea,Distribution> filteredDistributionsPerArea, boolean keepFallBackOnlyIfNoSubareaDataExists) {

        Set<NamedArea> areasHiddenByMarker = new HashSet<>();
        Set<NamedArea> availableAreas = areaTree.getDistinctTerms();

        for(NamedArea area : filteredDistributionsPerArea.keySet()) {
            if (! availableAreas.contains(area)) {
                areasHiddenByMarker.add(area);
            }else if(isMarkedAs(area, fallbackAreaMarkerTypes)) {
                Set<TermNode<NamedArea>> nodes = areaTree.getNodesForTerm(area);

                // if at least one sub area is not hidden by a marker
                // the given area is a fall-back area for this sub area
                SetMap<NamedArea, Distribution> distributionsForSubareaCheck = keepFallBackOnlyIfNoSubareaDataExists ? filteredDistributionsPerArea : null;
                boolean isFallBackArea = isRemainingFallBackArea(nodes, fallbackAreaMarkerTypes, distributionsForSubareaCheck);
                if (!isFallBackArea) {
                    // this area does not need to be shown as
                    // fall-back for another area so it will be hidden.
                    areasHiddenByMarker.add(area);
                }
            }
        }
        for(NamedArea area :areasHiddenByMarker) {
            filteredDistributionsPerArea.remove(area);
        }
    }

    //if filteredDistributions == null it can be ignored if data exists or not
    private static boolean isRemainingFallBackArea(Set<TermNode<NamedArea>> areaNode, Set<MarkerType> fallbackAreaMarkerTypes,
            SetMap<NamedArea, Distribution> filteredDistributions) {

        Set<TermNode<NamedArea>> childNodes = new HashSet<>();
        areaNode.stream().forEach(an->childNodes.addAll(an.getChildNodes()));
        for(TermNode<NamedArea> included : childNodes) {
            NamedArea subArea = included.getTerm();
            boolean noOrIgnoreData = filteredDistributions == null || !filteredDistributions.containsKey(subArea);

            Set<TermNode<NamedArea>> childNodeAsSet = new HashSet<>();
            childNodeAsSet.add(included);
            //if subarea is not hidden and data exists return true
            if (isMarkedAs(subArea, fallbackAreaMarkerTypes)){
                boolean subAreaIsFallback = isRemainingFallBackArea(childNodeAsSet, fallbackAreaMarkerTypes, filteredDistributions);
                if (subAreaIsFallback && noOrIgnoreData){
                    return true;
                }else{
                    continue;
                }
            }else{ //subarea not marked hidden
                if (noOrIgnoreData){
                    return true;
                }else{
                    continue;
                }
            }
//            boolean isNotHidden_AndHasNoData_OrDataCanBeIgnored =
//                    && noOrIgnoreData && subArea.getIncludes().isEmpty();
//            if (isNotHidden_AndHasNoData_OrDataCanBeIgnored) {
//                return true;
//            }
//            if (!isMarkedHidden(subArea, fallbackAreaMarkerTypes) ){
//
//            }
//
//            //do the same recursively
//            boolean hasVisibleSubSubarea = isRemainingFallBackArea(subArea, fallbackAreaMarkerTypes, filteredDistributions, areasHiddenByMarker);
//            if (hasVisibleSubSubarea){
//                return true;
//            }
        }
        return false;
    }

    private static void handlePreferAggregated(SetMap<NamedArea, Distribution> filteredDistributions) {
        SetMap<NamedArea, Distribution> computedDistributions = new SetMap<>(filteredDistributions.size());
        SetMap<NamedArea, Distribution> nonComputedDistributions = new SetMap<>(filteredDistributions.size());
        // separate computed and edited Distributions
        for (NamedArea area : filteredDistributions.keySet()) {
            for (Distribution distribution : filteredDistributions.get(area)) {
                // this is only required for rule 1
                if(isAggregated(distribution)){
                    computedDistributions.putItem(area, distribution);
                } else {
                    nonComputedDistributions.putItem(area,distribution);
                }
            }
        }
        //remove nonComputed distributions for which computed distributions exist in the same area
        for(NamedArea keyComputed : computedDistributions.keySet()){
            nonComputedDistributions.remove(keyComputed);
        }
        // combine computed and non computed Distributions again
        filteredDistributions.clear();
        for(NamedArea area : computedDistributions.keySet()){
            filteredDistributions.put(area, computedDistributions.get(area));  //is it a problem that we use the same interal Set here?
        }
        for(NamedArea area : nonComputedDistributions.keySet()){
            filteredDistributions.put(area, nonComputedDistributions.get(area));
        }
    }

    private static boolean isAggregated(Distribution distribution) {
        DescriptionBase<?> desc = distribution.getInDescription();
        if (desc != null && desc.isAggregatedDistribution()){
            return true;
        }
        return false;
    }

    public static boolean isMarkedAs(NamedArea area, Set<MarkerType> markerTypes) {
        if(markerTypes != null) {
            for(MarkerType markerType : markerTypes){
                if(area.hasMarker(markerType, true)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Orders the given Distribution elements in a hierarchical structure.
     * This method will not filter out any of the distribution elements.
     *
     * @param omitLevels
     * @param distributions
     * @param fallbackAreaMarkerTypes
     *      Areas are fallback areas if they have a {@link Marker} with one of the specified
     *      {@link MarkerType marker types}.
     *      Areas identified as such are omitted from the hierarchy and the sub areas are moving one level up.
     *      This may not be the case if the fallback area has a distribution record itself AND if
     *      neverUseFallbackAreasAsParents is <code>false</code>.
     *      For more details on fall back areas see <b>Marked area filter</b> of
     *      {@link DescriptionUtility#filterDistributions(Collection, Set, boolean, boolean, boolean)}.
     * @param distributionOrder
     * @param termDao
     *      Currently used from performance reasons (preloading of parent areas), may be removed in future
     * @return the {@link DistributionTree distribution tree}
     */
    public static DistributionTree buildOrderedTree(Set<NamedAreaLevel> omitLevels,
            Collection<Distribution> distributions,
            SetMap<NamedArea, NamedArea> parentAreaMap,
            Set<MarkerType> fallbackAreaMarkerTypes,
            Set<MarkerType> alternativeRootAreaMarkerTypes,
            boolean neverUseFallbackAreaAsParent,
            DistributionOrder distributionOrder,
            IDefinedTermDao termDao) {

        DistributionTree tree = new DistributionTree(termDao);

        if (logger.isDebugEnabled()){logger.debug("order tree ...");}
        //order by areas
        tree.orderAsTree(distributions, parentAreaMap, omitLevels, fallbackAreaMarkerTypes,
                neverUseFallbackAreaAsParent);
        tree.handleAlternativeRootArea(alternativeRootAreaMarkerTypes);
        tree.recursiveSortChildren(distributionOrder); // TODO respect current locale for sorting
        if (logger.isDebugEnabled()){logger.debug("create tree - DONE");}
        return tree;
    }

    public static DistributionTreeDto buildOrderedTreeDto(Set<NamedAreaLevel> omitLevels,
            Collection<DistributionDto> distributions,
            SetMap<NamedArea, NamedArea> parentAreaMap,
            TermTree<NamedArea> areaTree, Set<MarkerType> fallbackAreaMarkerTypes,
            Set<MarkerType> alternativeRootAreaMarkerType,
            boolean neverUseFallbackAreaAsParent,
            DistributionOrder distributionOrder,
            IDefinedTermDao termDao,
            boolean useSecondMethod) {

        //TODO loader needed?
        DistributionTreeDtoLoader loader = new DistributionTreeDtoLoader(termDao);
        DistributionTreeDto distributionTreeDto = loader.load();

        if (logger.isDebugEnabled()){logger.debug("order tree ...");}

        //order by areas
        if (!useSecondMethod) {
            loader.orderAsTree(distributionTreeDto, distributions, parentAreaMap, omitLevels,
                    fallbackAreaMarkerTypes, neverUseFallbackAreaAsParent);
        }else {
            loader.orderAsTree2(distributionTreeDto, distributions, areaTree, omitLevels,
                    fallbackAreaMarkerTypes, neverUseFallbackAreaAsParent);
        }
        loader.handleAlternativeRootArea(distributionTreeDto, alternativeRootAreaMarkerType);
        loader.recursiveSortChildren(distributionTreeDto, distributionOrder); // TODO respect current locale for sorting

        if (logger.isDebugEnabled()){logger.debug("create tree - DONE");}
        return distributionTreeDto;
    }

    /**
     * Implements the Status order preference filter for a given set to Distributions.
     * The distributions should all be for the same area.
     * The method returns a site of distributions since multiple Distributions
     * with the same status are possible. For example if the same status has been
     * published in more than one literature references.
     *
     * @param distributions
     *
     * @return the set of distributions with the highest status
     */
    private static Set<Distribution> filterByHighestDistributionStatusForArea(Set<Distribution> distributions){

        Set<Distribution> preferred = new HashSet<>();
        PresenceAbsenceTerm highestStatus = null;  //we need to leave generics here as for some reason highestStatus.compareTo later jumps into the wrong class for calling compareTo
        int compareResult;
        for (Distribution distribution : distributions) {
            if(highestStatus == null){
                highestStatus = distribution.getStatus();
                preferred.add(distribution);
            } else {
                if(distribution.getStatus() == null){
                    continue;
                } else {
                    compareResult = highestStatus.compareTo(distribution.getStatus());
                }
                if(compareResult < 0){
                    highestStatus = distribution.getStatus();
                    preferred.clear();
                    preferred.add(distribution);
                } else if(compareResult == 0) {
                    preferred.add(distribution);
                }
            }
        }

        return preferred;
    }

    private static <T extends CdmBase> Set<T> valuesOfAllInnerSets(Collection<Set<T>> collectionOfSets){
        Set<T> allValues = new HashSet<T>();
        for(Set<T> set : collectionOfSets){
            allValues.addAll(set);
        }
        return allValues;
    }

}
