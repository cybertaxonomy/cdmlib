// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.ext;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * @author a.mueller
 * @created 17.11.2008
 * @version 1.0
 */
public class EditGeoServiceUtilities {
	private static final Logger logger = Logger.getLogger(EditGeoServiceUtilities.class);

	private static PresenceAbsenceTermBase<?> defaultStatus = PresenceTerm.PRESENT();

	//preliminary implementation for TDWG areas
	/**
	 * Returns the parameter String for the EDIT geo webservice to create a map.
	 * @param distributions A set of distributions that should be shown on the map
	 * @param presenceAbsenceTermColors A map that defines the colors of PresenceAbsenceTerms. 
	 * The PresenceAbsenceTerms are defined by their uuid. If a PresenceAbsenceTerm is not included in
	 * this map, it's default color is taken instead. If the map == null all terms are colored by their default color. 
	 * @param width The maps width
	 * @param height The maps height
	 * @param bbox The maps bounding box (e.g. "-180,-90,180,90" for the whole world)
	 * @param layer The layer that is responsible for background borders and colors. Use the name for the layer.
	 * If null 'earth' is taken as default. 
	 * @return the parameter string or an empty string if the <code>distributions</code> set was null or empty.
	 */
	@Transient
	public static String getEditGeoServiceUrlParameterString(
			Set<Distribution> distributions, 
			Map<PresenceAbsenceTermBase<?>,Color> presenceAbsenceTermColors, 
			int width, 
			int height, 
			String bbox, 
			String backLayer,
			List<Language> languages){
		
		String result = "";
		String layer = ""; 
		String areaData = "";
		String areaStyle = "";
		String areaTitle = "";
		String widthStr = "";
		String heightStr = "";
		String adLayerSeparator = ":";
		String styleInAreaDataSeparator = "|";
		String msSeparator = ","; //Separator for the ms parameter values , e.g. 'x' => ms=600x400
		//double borderWidth = 0.1;
		double borderWidth = 0.1;
		
		
		if(distributions == null || distributions.size() == 0){
			return "";
		}
		if (presenceAbsenceTermColors == null) {
			//presenceAbsenceTermColors = new HashMap<PresenceAbsenceTermBase<?>, Color>();
			presenceAbsenceTermColors = makeDefaultColorMap();
		}

		//List<String> layerStrings = new ArrayList<String>(); 
		Map<String, Map<Integer, Set<Distribution>>> layerMap = new HashMap<String, Map<Integer, Set<Distribution>>>(); 
		List<PresenceAbsenceTermBase<?>> statusList = new ArrayList<PresenceAbsenceTermBase<?>>();
		
		//bbox, width, hight
		if (bbox == null){
			// FIXME uncommented this as it can not be desirable to have default values in this method
			// we need a parameterString that consists of essential parameters only 
			// defaults should be implemented in the geoservice itself.
			//bbox ="bbox=-180,-90,180,90";  //earth is default
		}else{
			bbox = "bbox=" + bbox;
		}
		if (width > 0){
			widthStr = "" + width;
		}
		if (height > 0){
			heightStr = msSeparator + height;
		}
		String ms = "ms=" + widthStr + heightStr;
		if (ms.equals("ms=")){
			ms = null;
		}
		
		//iterate through distributions and group styles and layers
		//and collect necessary information
		for (Distribution distribution:distributions){
			//collect status
			PresenceAbsenceTermBase<?> status = distribution.getStatus();
			if(status == null){
				status = defaultStatus;
			}
			if (! statusList.contains(status)){
				statusList.add(status);
			}
			//group by layers and styles
			NamedArea area = distribution.getArea();
			if (area != null){
				NamedAreaLevel level = area.getLevel();
				String geoLayerString = getGeoServiceLayer(level);
				//Set<Distribution> layerDistributionSet;
				//int index = layerStrings.indexOf(geoLayerString);
				Map<Integer, Set<Distribution>> styleMap = layerMap.get(geoLayerString);
				if (styleMap == null){
					styleMap = new HashMap<Integer, Set<Distribution>>();
					layerMap.put(geoLayerString, styleMap);
				}
				addDistributionToMap(distribution, styleMap, statusList);
			}
		}
		
		//layer
		if (backLayer == null || "".equals(layer.trim())){
			backLayer = "earth"; 
		}
		layer = "l="+backLayer;
//		for (String layerString : layerMap.keySet()){
//			layer += "," + layerString;
//		}
		//layer = "l=" + layer.substring(1); //remove first |
		
		
		//style
		areaStyle = "";
		int i = 0;
		for (PresenceAbsenceTermBase<?> status: statusList){
			
			char style = getStyleAbbrev(i);
			
			//getting the area title
			if (languages == null){
				languages = new ArrayList<Language>();
			}
			if (languages.size() == 0){
				languages.add(Language.DEFAULT());
			}
			Representation representation = status.getPreferredRepresentation(languages);
			String statusLabel = representation.getLabel();
			logger.warn(statusLabel);
			//statusLabel.replace('introduced: ', '');
			statusLabel = statusLabel.replace("introduced: ", "introduced, ");
			statusLabel = statusLabel.replace("native: ", "native,  ");
			areaTitle += "|" + style + ":" + statusLabel;
			
			//getting the area color
			Color statusColor = presenceAbsenceTermColors.get(status);
			String rgb;
			if (statusColor != null){
				rgb = Integer.toHexString(statusColor.getRGB()).substring(2);
			}else{
				if(status != null){
					rgb = status.getDefaultColor(); //TODO
				} else {
					rgb = defaultStatus.getDefaultColor();
				}
			}
			areaStyle += "|" + style + ":" + rgb;

			if (borderWidth >0){
				areaStyle += ",," + borderWidth;
			}

			i++;			
		}
		
		if(areaStyle.length() > 0){
			areaStyle = "as=" + encode(areaStyle); //remove first |
		}
		if(areaTitle.length() > 0){
			areaTitle = "title=" + encode(areaTitle); //remove first |
		}
		
		boolean separateLevels = false; //FIXME as parameter
		//areaData
		areaData = "";
		boolean isFirstLayer = true;
		Map<String, String> resultMap = new HashMap<String, String>();
		
		for (String layerString : layerMap.keySet()){
			//Set<Distribution> layerDistributions = layerData.get(layerIndex);
			//int distributionListIndex = 1;
			areaData += (isFirstLayer? "" : "||") + layerString + adLayerSeparator;
			Map<Integer, Set<Distribution>> styleMap = layerMap.get(layerString);
			boolean isFirstStyle = true;
			for (int style: styleMap.keySet()){
				char styleChar = getStyleAbbrev(style);
				areaData += (isFirstStyle? "" : styleInAreaDataSeparator) + styleChar + ":";
				Set<Distribution> distributionSet = styleMap.get(style);
				boolean isFirstDistribution = true;
				for (Distribution distribution: distributionSet){
					String areaAbbrev = getAreaAbbrev(distribution);
					areaData += (isFirstDistribution ? "" : ",") + areaAbbrev;
					isFirstDistribution = false;
				}
				isFirstStyle = false;
			}
			isFirstLayer = separateLevels;
			if(separateLevels){
				//result per level
				result = CdmUtils.concat("&", new String[] {layer, "ad=" + areaData.substring(0), areaStyle, areaTitle, bbox, ms});
				//result = CdmUtils.concat("&", new String[] {layer, "ad=" + areaData.substring(0), areaStyle, bbox, ms});
				resultMap.put(layerString, result);
			}
		}
		
		areaData = "ad=" + areaData.substring(0); //remove first |
		
		//result
		result = CdmUtils.concat("&", new String[] {layer, areaData, areaStyle, areaTitle, bbox, ms});
		//result = CdmUtils.concat("&", new String[] {layer, areaData, areaStyle, bbox, ms});
		if (logger.isDebugEnabled()){logger.debug("getEditGeoServiceUrlParameterString end");}
		
		return result;
	}

	private static String encode(String areaStyle) {
		String encoded = areaStyle;
		try {
			encoded = URLEncoder.encode(areaStyle.substring(1), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		return encoded;
	}
	
	private static Map<PresenceAbsenceTermBase<?>,Color> makeDefaultColorMap(){
		Map<PresenceAbsenceTermBase<?>,Color> result = new HashMap<PresenceAbsenceTermBase<?>, Color>();
		try {
			result.put(PresenceTerm.PRESENT(), Color.decode("0x4daf4a"));
			result.put(PresenceTerm.NATIVE(), Color.decode("0x4daf4a"));
			result.put(PresenceTerm.NATIVE_DOUBTFULLY_NATIVE(), Color.decode("0x377eb8"));
			result.put(PresenceTerm.CULTIVATED(), Color.decode("0x984ea3"));
			result.put(PresenceTerm.INTRODUCED(), Color.decode("0xff7f00"));
			result.put(PresenceTerm.INTRODUCED_ADVENTITIOUS(), Color.decode("0xffff33"));
			result.put(PresenceTerm.INTRODUCED_CULTIVATED(), Color.decode("0xa65628"));
			result.put(PresenceTerm.INTRODUCED_NATURALIZED(), Color.decode("0xf781bf"));
		} catch (NumberFormatException nfe) {
			logger.error(nfe);
		}
		return result;
	}
	
	
	private static String getAreaAbbrev(Distribution distribution){
		NamedArea area = distribution.getArea();
		Representation representation = area.getRepresentation(Language.DEFAULT());
		String areaAbbrev = representation.getAbbreviatedLabel();
		if (area.getLevel() != null && area.getLevel().equals(NamedAreaLevel.TDWG_LEVEL4())){
			areaAbbrev = areaAbbrev.replace("-", "");
		}
		return areaAbbrev;
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
	
	private static String getGeoServiceLayer(NamedAreaLevel level){
		//TODO integrate into CDM 
		if (level.equals(NamedAreaLevel.TDWG_LEVEL1())){
			return "tdwg1";
		}else if (level.equals(NamedAreaLevel.TDWG_LEVEL2())){
			return "tdwg2";
		}if (level.equals(NamedAreaLevel.TDWG_LEVEL3())){
			return "tdwg3";
		}if (level.equals(NamedAreaLevel.TDWG_LEVEL4())){
			return "tdwg4";
		}
		return "unknown";
	}
	
	private static void addDistributionToMap(Distribution distribution, Map<Integer, Set<Distribution>> styleMap,
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
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
