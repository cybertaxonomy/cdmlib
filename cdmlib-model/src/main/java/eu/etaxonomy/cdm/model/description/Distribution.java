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
	
	//preliminary implementation for TDWG areas
	@Transient
	//TODO move to an other place -> e.g. service layer
	public static String getWebServiceUrl(Set<Distribution> distributions, String webServiceUrl, String bbox, String mapSize){
		String result;
		String layer; 
		String areaData;
		String areaStyle;
		
		if (webServiceUrl == null){
			logger.warn("No WebServiceURL defined");
			return null;
		}
		List<String> layerStrings = new ArrayList<String>(); 
		List<Set<Distribution>> layerData = new ArrayList<Set<Distribution>>(); 
		List<PresenceAbsenceTermBase<?>> statusList = new ArrayList<PresenceAbsenceTermBase<?>>();
		
		//bbox, mapSize, format, ...
		//TODO
		if (CdmUtils.Nz(bbox).equals("")){
			bbox = "bbox=-20,40,40,40"; //TODO
		}
		if (CdmUtils.Nz(mapSize).equals("")){
			mapSize = "ms=400x300";
		}
		
		//iterate through distributions and group styles and layers
		for (Distribution distribution:distributions){
			//collect status
			PresenceAbsenceTermBase<?> status = distribution.getStatus();
			if (! statusList.contains(status)){
				statusList.add(status);
			}
			//group by layers
			NamedArea area = distribution.getArea();
			if (area != null){
				NamedAreaLevel level = area.getLevel();
				String geoLayerString = getGeoServiceLayer(level);
				Set<Distribution> layerDistributionSet;
				int index = layerStrings.indexOf(geoLayerString);
				if (index != -1){
					layerDistributionSet = layerData.get(index);
				}else{
					layerStrings.add(geoLayerString);
					layerDistributionSet = new HashSet<Distribution>();
					layerData.add(layerDistributionSet);
				}
				layerDistributionSet.add(distribution);
			}
		}
		
		//layer
		layer = ""; 
		for (String layerString : layerStrings){
			layer += "|" + layerString;
		}
		layer = "l=" + layer.substring(1); //remove first |
		
		
		//style
		areaStyle = "";
		Map<PresenceAbsenceTermBase<?>, Character> styleMap = new HashMap<PresenceAbsenceTermBase<?>, Character>();
		int i = 1;
		for (PresenceAbsenceTermBase<?> status: statusList){
			char style; 
			int ascii = 96 + i;
			if (i >26){
				ascii = 64 + i;
			}
			style = (char)ascii;
			String color = status.getDefaultColor();//"00FFAA"; //TODO
			areaStyle += "|" + style + ":" + color;
			styleMap.put(status, style);
			i++;
		}
		areaStyle = "as=" + areaStyle.substring(1); //remove first |
		
		//areaData
		areaData = "";
		i = 0;
		for (String layerString : layerStrings){
			//int index = layers.indexOf(layerString);
			Set<Distribution> layerDistributions = layerData.get(i);
			int distributionListIndex = 1;
			for (Distribution distribution: layerDistributions){
				//TODO null
				char style = styleMap.get(distribution.getStatus());
				//TODO
				
				NamedArea area = distribution.getArea();
				Representation representation = area.getRepresentation(Language.DEFAULT());
				String areaAbbrev = representation.getAbbreviatedLabel();
				if (distributionListIndex == 1){
					areaData += "|" + layerString + ":";
				}else{
					areaData += "|";	
				}
				areaData += style + ":" + areaAbbrev;
				distributionListIndex++;
			}
			i++;
		}
		areaData = "ad=" + areaData.substring(1); //remove first |
		
		//result
		result = webServiceUrl + "?";
		result += CdmUtils.concat("&", new String[] {layer, areaData, areaStyle, bbox, mapSize});
		return result;
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

}