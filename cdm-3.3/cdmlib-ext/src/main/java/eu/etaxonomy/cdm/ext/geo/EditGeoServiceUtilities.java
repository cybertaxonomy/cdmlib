// $Id$
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.utility.DescriptionUtility;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
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

    private static PresenceAbsenceTermBase<?> defaultStatus = PresenceTerm.PRESENT();

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
            defaultSpecimenOrObservationTypeColors = new HashMap<SpecimenOrObservationType, Color>();
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.FieldUnit, Color.ORANGE);
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.DerivedUnit, Color.RED);
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.LivingSpecimen, Color.GREEN);
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.Observation, Color.ORANGE);
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.PreservedSpecimen, Color.GRAY);
            defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.Media, Color.BLUE);
        }
        return defaultSpecimenOrObservationTypeColors;
    }


    private static HashMap<PresenceAbsenceTermBase<?>, Color> defaultPresenceAbsenceTermBaseColors = null;

    private static HashMap<PresenceAbsenceTermBase<?>, Color> getDefaultPresenceAbsenceTermBaseColors() {
        if(defaultPresenceAbsenceTermBaseColors == null){
            defaultPresenceAbsenceTermBaseColors = new HashMap<PresenceAbsenceTermBase<?>, Color>();
            defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.PRESENT(), Color.decode("0x4daf4a"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.NATIVE(), Color.decode("0x4daf4a"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.NATIVE_DOUBTFULLY_NATIVE(), Color.decode("0x377eb8"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.CULTIVATED(), Color.decode("0x984ea3"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.INTRODUCED(), Color.decode("0xff7f00"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.INTRODUCED_ADVENTITIOUS(), Color.decode("0xffff33"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.INTRODUCED_CULTIVATED(), Color.decode("0xa65628"));
            defaultPresenceAbsenceTermBaseColors.put(PresenceTerm.INTRODUCED_NATURALIZED(), Color.decode("0xf781bf"));

            /*
             * and now something very hacky ...
             * ONLY-A-TEST is set by the Test class EditGeoServiceTest
             *
             * TODO remove according line from
             * EditGeoServiceTest.setUp() when the hardcoded colors for flora of
             * cyprus are no further needed !!
             */
            String onlyTest = System.getProperty("ONLY-A-TEST"); //
            if(onlyTest != null && onlyTest.equals("TRUE")){
                return defaultPresenceAbsenceTermBaseColors;
            }
            //special colors for flora of cyprus !!! see HACK above !!!
            UUID indigenousUuid = UUID.fromString("b325859b-504b-45e0-9ef0-d5c1602fcc0f");
            UUID indigenousQUuid = UUID.fromString("17bc601f-53eb-4997-a4bc-c03ce5bfd1d3");

            UUID cultivatedQUuid = UUID.fromString("4f31bfc8-3058-4d83-aea5-3a1fe9773f9f");

            UUID casualUuid = UUID.fromString("5e81353c-38a3-4ca6-b979-0d9abc93b877");
            UUID casualQUuid = UUID.fromString("73f75493-1185-4a3e-af1e-9a1f2e8dadb7");

            UUID naturalizedNonInvasiveUuid = UUID.fromString("1b025e8b-901a-42e8-9739-119b410c6f03");
            UUID naturalizedNonInvasiveQUuid = UUID.fromString("11f56e2f-c16c-4b3d-a870-bb5d3b20e624");

            UUID naturalizedInvasiveUuid = UUID.fromString("faf2d271-868a-4bf7-b0b8-a1c5ab309de2");
            UUID naturalizedInvasiveQUuid = UUID.fromString("ac429d5f-e8ad-49ae-a41c-e4779b58b96a");

            UUID questionablelUuid = UUID.fromString("4b48f675-a6cf-49f3-a5ba-77e2c2979eb3");
            UUID questionableQUuid = UUID.fromString("914e7393-1314-4632-bc45-5eff3dc1e424");

            UUID reportedInErrorUuid = UUID.fromString("38604788-cf05-4607-b155-86db456f7680");

            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(indigenousUuid), Color.decode("0x339966"));
            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(indigenousQUuid), Color.decode("0x339966"));

            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(cultivatedQUuid), Color.decode("0xbdb76b"));

            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(casualUuid), Color.decode("0xffff00"));
            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(casualQUuid), Color.decode("0xffff00"));

            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(naturalizedNonInvasiveUuid), Color.decode("0xff9900"));
            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(naturalizedNonInvasiveQUuid), Color.decode("0xff9900"));

            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(naturalizedInvasiveUuid), Color.decode("0xff0000"));
            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(naturalizedInvasiveQUuid), Color.decode("0xff0000"));

            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(questionablelUuid), Color.decode("0x00ccff"));
            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(questionableQUuid), Color.decode("0x00ccff"));

            defaultPresenceAbsenceTermBaseColors.put((PresenceAbsenceTermBase<?>) termDao.load(reportedInErrorUuid), Color.decode("0xcccccc"));

        }
        return defaultPresenceAbsenceTermBaseColors;
    }



    private static final String SUBENTRY_DELIMITER = ",";
    private static final String ENTRY_DELIMITER = ";";
    static final String ID_FROM_VALUES_SEPARATOR = ":";
    static final String VALUE_LIST_ENTRY_SEPARATOR = "|";
    static final String VALUE_SUPER_LIST_ENTRY_SEPARATOR = "||";



    //preliminary implementation for TDWG areas
    /**
     * Returns the parameter String for the EDIT geo webservice to create a
     * dsitribution map.
     *
     * @param distributions
     *            A set of distributions that should be shown on the map
     * @param presenceAbsenceTermColors
     *            A map that defines the colors of PresenceAbsenceTerms. The
     *            PresenceAbsenceTerms are defined by their uuid. If a
     *            PresenceAbsenceTerm is not included in this map, it's default
     *            color is taken instead. If the map == null all terms are
     *            colored by their default color.
     * @param width
     *            The maps width
     * @param height
     *            The maps height
     * @param bbox
     *            The maps bounding box (e.g. "-180,-90,180,90" for the whole
     *            world)
     * @param projectToLayer
     *            name of a layer which is representing a specific
     *            {@link NamedAreaLevel} Supply this parameter if you to project
     *            all other distribution area levels to this layer.
     * @param layer
     *            The layer that is responsible for background borders and
     *            colors. Use the name for the layer. If null 'earth' is taken
     *            as default.
     * @return the parameter string or an empty string if the
     *         <code>distributions</code> set was null or empty.
     */
    @Transient
    public static String getDistributionServiceRequestParameterString(
            Set<Distribution> distributions,
            IGeoServiceAreaMapping mapping,
            Map<PresenceAbsenceTermBase<?>,Color> presenceAbsenceTermColors,
            int width,
            int height,
            String bbox,
            String baseLayerName,
            String projectToLayer,
            List<Language> languages){


        /**
         * generateMultipleAreaDataParameters switches between the two possible styles:
         * 1. ad=layername1:area-data||layername2:area-data
         * 2. ad=layername1:area-data&ad=layername2:area-data
         */
        boolean generateMultipleAreaDataParameters = false;

        List<String>  perLayerAreaData = new ArrayList<String>();
        List<String> areaStyles = new ArrayList<String>();
        List<String> legendLabels = new ArrayList<String>();


        String borderWidth = "0.1";
        String borderColorRgb = "";
        String borderDashingPattern = "";


		//handle empty set
		if(distributions == null || distributions.size() == 0){
			return "";
		}

		Collection<Distribution> filteredDistributions = DescriptionUtility.filterDistributions(distributions);

		Map<String, Map<Integer, Set<Distribution>>> layerMap = new HashMap<String, Map<Integer, Set<Distribution>>>();
		List<PresenceAbsenceTermBase<?>> statusList = new ArrayList<PresenceAbsenceTermBase<?>>();

		groupStylesAndLayers(filteredDistributions, layerMap, statusList, mapping);

        presenceAbsenceTermColors = mergeMaps(getDefaultPresenceAbsenceTermBaseColors(), presenceAbsenceTermColors);

        Map<String, String> parameters = new HashMap<String, String>();

        //bbox
        if (bbox != null){
            parameters.put("bbox", bbox);
        }
        // map size
        String ms = compileMapSizeParameterValue(width, height);
        if(ms != null){
            parameters.put("ms", ms);
        }
        //layer
        if (StringUtils.isBlank(baseLayerName)){
            baseLayerName = "earth";
        }
        parameters.put("l", baseLayerName);

        //style
        int i = 0;
        for (PresenceAbsenceTermBase<?> status: statusList){

            char styleId = getStyleAbbrev(i);

            //getting the area title
            if (languages == null){
                languages = new ArrayList<Language>();
            }
            if (languages.size() == 0){
                languages.add(Language.DEFAULT());
            }
            Representation representation = status.getPreferredRepresentation(languages);
            String statusLabel = representation.getLabel();
            //statusLabel.replace('introduced: ', '');
            statusLabel = statusLabel.replace("introduced: ", "introduced, ");
            statusLabel = statusLabel.replace("native: ", "native,  ");

            //getting the area color
            Color statusColor = presenceAbsenceTermColors.get(status);
            String fillColorRgb;
            if (statusColor != null){
                fillColorRgb = Integer.toHexString(statusColor.getRGB()).substring(2);
            }else{
                if(status != null){
                    fillColorRgb = status.getDefaultColor(); //TODO
                } else {
                    fillColorRgb = defaultStatus.getDefaultColor();
                }
            }
            String styleValues = StringUtils.join(new String[]{fillColorRgb, borderColorRgb, borderWidth, borderDashingPattern}, ',');

            areaStyles.add(styleId + ID_FROM_VALUES_SEPARATOR + styleValues);
            legendLabels.add(styleId + ID_FROM_VALUES_SEPARATOR + encode(statusLabel));
            i++;
        }

        if(areaStyles.size() > 0){
            parameters.put("as", StringUtils.join(areaStyles.iterator(), VALUE_LIST_ENTRY_SEPARATOR));
        }
        if(legendLabels.size() > 0){
            parameters.put("title", StringUtils.join(legendLabels.iterator(), VALUE_LIST_ENTRY_SEPARATOR));
        }

		// area data
		List<String> stylesPerLayer;
		List<String> areasPerStyle;
		for (String layerString : layerMap.keySet()){
			// each layer
			stylesPerLayer = new ArrayList<String>();
			Map<Integer, Set<Distribution>> styleMap = layerMap.get(layerString);
			for (int style: styleMap.keySet()){
				// stylesPerLayer
				char styleChar = getStyleAbbrev(style);
				Set<Distribution> distributionSet = styleMap.get(style);
				areasPerStyle = new ArrayList<String>();
				for (Distribution distribution: distributionSet){
					// areasPerStyle
					areasPerStyle.add(encode(getAreaAbbrev(distribution, mapping)));
				}
				stylesPerLayer.add(styleChar + ID_FROM_VALUES_SEPARATOR + StringUtils.join(areasPerStyle.iterator(), SUBENTRY_DELIMITER));
			}
			perLayerAreaData.add(encode(layerString) + ID_FROM_VALUES_SEPARATOR + StringUtils.join(stylesPerLayer.iterator(), VALUE_LIST_ENTRY_SEPARATOR));
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
			List<PresenceAbsenceTermBase<?>> statusList,
			IGeoServiceAreaMapping mapping) {


		//iterate through distributions and group styles and layers
		//and collect necessary information
		for (Distribution distribution : distributions){
			//collect status
			PresenceAbsenceTermBase<?> status = distribution.getStatus();
			if(status == null){
				status = defaultStatus;
			}
			if (! statusList.contains(status)){
				statusList.add(status);
			}
			//group areas by layers and styles
			NamedArea area = distribution.getArea();

            addAreaToLayerMap(layerMap, statusList, distribution, area, mapping);
        }
    }

	/**
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
			List<PresenceAbsenceTermBase<?>> statusList,
			Distribution distribution,
			NamedArea area,
			IGeoServiceAreaMapping mapping) {

        if (area != null){
            String geoLayerString = getWMSLayerName(area, mapping);

            if(geoLayerString == null){

                // if no layer is mapped this area descend into sub areas in order to project
                // the distribution to those
                for(NamedArea subArea : area.getIncludes()){
                    addAreaToLayerMap(layerMap, statusList, distribution, subArea, mapping);
                }

            } else {

                Map<Integer, Set<Distribution>> styleMap = layerMap.get(geoLayerString);
                if (styleMap == null) {
                    styleMap = new HashMap<Integer, Set<Distribution>>();
                    layerMap.put(geoLayerString, styleMap);
                }
                addDistributionToStyleMap(distribution, styleMap, statusList);

            }
        }
    }


    private static String compileMapSizeParameterValue(int width, int height) {

        String widthStr = "";
        String heightStr = "";

        if (width > 0) {
            widthStr = "" + width;
        }
        if (height > 0) {
            heightStr = SUBENTRY_DELIMITER + height;
        }
        String ms = widthStr + heightStr;
        if(ms.length() == 0){
            ms = null;
        }
        return ms;
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

	private static String getAreaAbbrev(Distribution distribution, IGeoServiceAreaMapping mapping){
		NamedArea area = distribution.getArea();
		TermVocabulary<NamedArea> voc = area.getVocabulary();
		String result = null;
		if (voc !=  null && voc.getUuid().equals(NamedArea.uuidTdwgAreaVocabulary) || voc.getUuid().equals(uuidCyprusDivisionsVocabulary) ){
			Representation representation = area.getRepresentation(Language.DEFAULT());
			result = representation.getAbbreviatedLabel();
			if (area.getLevel() != null && area.getLevel().equals(NamedAreaLevel.TDWG_LEVEL4())){
				result = result.replace("-", "");
			}

		}else{
			GeoServiceArea areas =mapping.valueOf(area);
			if ((areas != null) && areas.size()>0){
				//FIXME multiple layers
				List<String> values= areas.getAreasMap().values().iterator().next().values().iterator().next();
				for (String value : values){
					result = CdmUtils.concat(SUBENTRY_DELIMITER, result, value);
				}
			}

		}
		return CdmUtils.Nz(result, "-");

	}



    //Preliminary as long as user defined areas are not fully implemented
    public static final UUID uuidCyprusDivisionsVocabulary = UUID.fromString("2119f610-1f93-4d87-af28-40aeefaca100");

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
        //TODO hardcoded for cyprus (as long as user defined areas are not fully implemented). Remove afterwards.
        if (voc.getUuid().equals(uuidCyprusDivisionsVocabulary)){
            matchedLayerName = "cyprusdivs:bdcode";
        }

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

		}
		//hardcoded for cyprus (as long as user defined areas are not fully implemented). Remove afterwards.
		if (voc.getUuid().equals(uuidCyprusDivisionsVocabulary)){
			return "cyprusdivs:bdcode";
		}

		GeoServiceArea areas = mapping.valueOf(area);
		if (areas != null && areas.getAreasMap().size() > 0){
			//FIXME multiple layers
			String layer = areas.getAreasMap().keySet().iterator().next();
			Map<String, List<String>> fields = areas.getAreasMap().get(layer);
			String field = fields.keySet().iterator().next();
			return layer + ":" + field;
		}

		return null;
	}


    private static void addDistributionToStyleMap(Distribution distribution, Map<Integer, Set<Distribution>> styleMap,
            List<PresenceAbsenceTermBase<?>> statusList) {
        PresenceAbsenceTermBase<?> status = distribution.getStatus();
        if (status == null) {
            status = defaultStatus;
        }
        int style = statusList.indexOf(status);
        Set<Distribution> distributionSet = styleMap.get(style);
        if (distributionSet == null) {
            distributionSet = new HashSet<Distribution>();
            styleMap.put(style, distributionSet);
        }
        distributionSet.add(distribution);
    }

    /**
     * @param fieldUnitPoints
     * @param derivedUnitPoints
     * @param specimenOrObservationTypeColors
     * @param doReturnImage TODO
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
    public static String getOccurrenceServiceRequestParameterString(
            	List<Point> fieldUnitPoints,
            	List<Point> derivedUnitPoints,
            	Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors,
            	Boolean doReturnImage, Integer width, Integer height, String bbox, String backLayer) {

            specimenOrObservationTypeColors = mergeMaps(getDefaultSpecimenOrObservationTypeColors(), specimenOrObservationTypeColors);

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("legend", "0");
            parameters.put("image", doReturnImage != null && doReturnImage ? "true" : "false");
            parameters.put("recalculate", "false"); // TODO add parameter to method
            if(bbox != null){
                parameters.put("bbox", bbox);
            }
            if(width != null || height != null){
                parameters.put("ms", compileMapSizeParameterValue(width, height));
            }

            Map<String, String> styleAndData = new HashMap<String, String>();

            addToStyleAndData(fieldUnitPoints, SpecimenOrObservationType.FieldUnit, specimenOrObservationTypeColors, styleAndData);
            addToStyleAndData(derivedUnitPoints, SpecimenOrObservationType.DerivedUnit, specimenOrObservationTypeColors, styleAndData);

            parameters.put("os", StringUtils.join(styleAndData.keySet().iterator(), "||"));
            parameters.put("od", StringUtils.join(styleAndData.values().iterator(), "||"));

            String queryString = makeQueryString(parameters);

            logger.info(queryString);

        return queryString;
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

}
