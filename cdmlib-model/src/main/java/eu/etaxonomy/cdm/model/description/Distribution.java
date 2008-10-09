/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents elementary distribution data for a {@link Taxon taxon}.
 * Only {@link TaxonDescription taxon descriptions} may contain distributions.
 * A distribution instance consist of a {@link NamedArea named area} and of a {@link PresenceAbsenceTermBase status}
 * describing the absence or the presence of a taxon (like "extinct"
 * or "introduced") in this named area.
 * <P>
 * This class corresponds partially to: <ul>
 * <li> CodedDescriptionType according to the the SDD schema
 * <li> Distribution according to the TDWG ontology
 * </ul>
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:21
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Distribution", propOrder = {
    "area",
    "status"
})
@XmlRootElement(name = "Distribution")
@Entity
public class Distribution extends DescriptionElementBase {
	static Logger logger = Logger.getLogger(Distribution.class);
	
	@XmlElement(name = "NamedArea")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private NamedArea area;
	
	@XmlElement(name = "PresenceAbsenceStatus")
	private PresenceAbsenceTermBase<?> status;

	
	/**
	 * Class constructor: creates a new empty distribution instance.
	 * The corresponding {@link Feature feature} is set to {@link Feature#DISTRIBUTION() DISTRIBUTION}.
	 */
	protected Distribution(){
		super(Feature.DISTRIBUTION());
	}
	
	
	/**
	 * Creates an empty distribution instance. The corresponding {@link Feature feature}
	 * is set to {@link Feature#DISTRIBUTION() DISTRIBUTION}.
	 *
	 * @see		#NewInstance(NamedArea, PresenceAbsenceTermBase)
	 */
	public static Distribution NewInstance(){
		Distribution result = new Distribution();
		return result;
	}

	/**
	 * Creates a distribution instance with the given {@link NamedArea named area} and {@link PresenceAbsenceTermBase status}.
	 * The corresponding {@link Feature feature} is set to {@link Feature#DISTRIBUTION() DISTRIBUTION}.
	 *
	 * @param	area	the named area for the new distribution 
	 * @param	status	the presence or absence term for the new distribution
	 * @see				#NewInstance()
	 */
	public static Distribution NewInstance(NamedArea area, PresenceAbsenceTermBase<?> status){
		Distribution result = new Distribution();
		result.setArea(area);
		result.setStatus(status);
		return result;
	}
	

		
	
	/** 
	 * Deprecated because {@link Feature feature} should always be {@link Feature#DISTRIBUTION() DISTRIBUTION}
	 * for all distribution instances.
	 */
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#setFeature(eu.etaxonomy.cdm.model.description.Feature)
	 */
	@Override
	@Deprecated
	public void setFeature(Feature feature) {
		super.setFeature(feature);
	}
	
	/** 
	 * Returns the {@link NamedArea named area} <i>this</i> distribution applies to.
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public NamedArea getArea(){
		return this.area;
	}
	/** 
	 * @see	#getArea()
	 */
	public void setArea(NamedArea area){
		this.area = area;
	}

	/** 
	 * Returns the {@link PresenceAbsenceTermBase presence or absence term} for <i>this</i> distribution.
	 */
	@ManyToOne
	public PresenceAbsenceTermBase<?> getStatus(){
		return this.status;
	}
	/** 
	 * @see	#getStatus()
	 */
	public void setStatus(PresenceAbsenceTermBase<?> status){
		this.status = status;
	}
	
	//preliminary implementation for TDWG areas
	@Transient
	//TODO move to an other place -> e.g. service layer
	public static String getWebServiceUrl(Set<Distribution> distributions, String webServiceUrl, int width, int hight, String bbox){
		String result = "";
		String layer = ""; 
		String areaData = "";
		String areaStyle = "";
		String widthStr = null;
		String hightStr = null;
		
		if (webServiceUrl == null){
			logger.warn("No WebServiceURL defined");
			return null;
		}
		//List<String> layerStrings = new ArrayList<String>(); 
		Map<String, Map<Integer, Set<Distribution>>> layerMap = new HashMap<String, Map<Integer, Set<Distribution>>>(); 
		List<PresenceAbsenceTermBase<?>> statusList = new ArrayList<PresenceAbsenceTermBase<?>>();
		
		//bbox, width, hight
		if (width > 0){
			widthStr = "w=" + width;
		}
		if (hight > 0){
			hightStr = "h=" + hight;
		}
		
		//iterate through distributions and group styles and layers
		for (Distribution distribution:distributions){
			//collect status
			PresenceAbsenceTermBase<?> status = distribution.getStatus();
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
		layer = ""; 
		for (String layerString : layerMap.keySet()){
			layer += "|" + layerString;
		}
		layer = "l=" + layer.substring(1); //remove first |
		
		
		//style
		areaStyle = "";
		Map<PresenceAbsenceTermBase<?>, Character> styleCharMap = new HashMap<PresenceAbsenceTermBase<?>, Character>();
		int i = 0;
		for (PresenceAbsenceTermBase<?> status: statusList){
			char style = getStyleAbbrev(i);
			String color = status.getDefaultColor();//"00FFAA"; //TODO
			areaStyle += "|" + style + ":" + color;
			styleCharMap.put(status, style);
			i++;
		}
		areaStyle = "as=" + areaStyle.substring(1); //remove first |
		
		//areaData
		areaData = "";
		boolean isFirstLayer = true;
		for (String layerString : layerMap.keySet()){
			//Set<Distribution> layerDistributions = layerData.get(layerIndex);
			//int distributionListIndex = 1;
			areaData += (isFirstLayer? "" : "||") + layerString + ":";
			Map<Integer, Set<Distribution>> styleMap = layerMap.get(layerString);
			boolean isFirstStyle = true;
			for (int style: styleMap.keySet()){
				char styleChar = getStyleAbbrev(style);
				areaData += (isFirstStyle? "" : "|") + styleChar + ":";
				Set<Distribution> distributionSet = styleMap.get(style);
				boolean isFirstDistribution = true;
				for (Distribution distribution: distributionSet){
					String areaAbbrev = getAreaAbbrev(distribution);
					areaData += (isFirstDistribution ? "" : ",") + areaAbbrev;
					isFirstDistribution = false;
				}
				isFirstStyle = false;
			}
			isFirstLayer = false;
		}
		areaData = "ad=" + areaData.substring(0); //remove first |
		
		//result
		result = webServiceUrl + "?";
		result += CdmUtils.concat("&", new String[] {layer, areaData, areaStyle, bbox, widthStr, hightStr});
		return result;
	}
	
	private static String getAreaAbbrev(Distribution distribution){
		NamedArea area = distribution.getArea();
		Representation representation = area.getRepresentation(Language.DEFAULT());
		String areaAbbrev = representation.getAbbreviatedLabel();
		return areaAbbrev;
	}
	
	private static void addDistributionToMap(Distribution distribution, Map<Integer, Set<Distribution>> styleMap, List<PresenceAbsenceTermBase<?>> statusList){
		int style = statusList.indexOf(distribution.getStatus());
		Set<Distribution> distributionSet = styleMap.get(style);
		if (distributionSet == null){
			distributionSet = new HashSet<Distribution>();
			styleMap.put(style, distributionSet);
		}
		distributionSet.add(distribution);
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

}