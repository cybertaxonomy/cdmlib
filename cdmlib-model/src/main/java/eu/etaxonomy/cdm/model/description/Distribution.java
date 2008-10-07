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
import eu.etaxonomy.cdm.model.location.TdwgArea;
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
	private PresenceAbsenceTermBase status;

	
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
	public static Distribution NewInstance(NamedArea area, PresenceAbsenceTermBase status){
		Distribution result = new Distribution();
		result.setArea(area);
		result.setStatus(status);
		return result;
	}
	
	@Transient
	//TODO move to an other place -> e.g. service layer
	public static String getWebServiceUrl(Set<Distribution> distributions, String webServiceUrl){
		if (webServiceUrl == null){
			logger.warn("No WebServiceURL defined");
			return null;
		}
		String result = webServiceUrl + "?";
		//TODO
		String layer = "l=tdwg3"; 
		List<String> layers = new ArrayList(); 
		layers.add(layer);
		
		String areaData = "ad=";
		//TODO 
		areaData += "tdwg3:";
		String areaStyle = "as=";
		//TODO
		String bbox = "bbox=-20,40,40,40";
		String mapSize = "ms=400x300";
		List<PresenceAbsenceTermBase> statusList = new ArrayList<PresenceAbsenceTermBase>();
		for (Distribution distribution:distributions){
			//collect status
			PresenceAbsenceTermBase status = distribution.getStatus();
			if (! statusList.contains(status)){
				statusList.add(status);
			}
			//collect
		}
		
		//style
		Map<PresenceAbsenceTermBase, Character> styleMap = new HashMap<PresenceAbsenceTermBase, Character>();
		int i = 1;
		for (PresenceAbsenceTermBase status: statusList){
			char style; //TODO char
			//TODO ASCII Translation 64+i
			int ascii = 96 + i;
			if (i >26){
				ascii = 64 + i;
			}
			style = (char)ascii;
			String color = status.getDefaultColor();//"00FFAA"; //TODO
			if (i > 1){
				areaStyle += "|";
			}
			areaStyle += style + ":" + color;
			styleMap.put(status, style);
			i++;
		}
		
		//areaData
		i = 1;
		for (Distribution distribution: distributions){
			//TODO null
			char style = styleMap.get(distribution.getStatus());
			//TODO
			
			NamedArea area = distribution.getArea();
			Representation representation = area.getRepresentation(Language.DEFAULT());
			String areaAbbrev = representation.getAbbreviatedLabel();
			if (i > 1){
				areaData += "|";
			}
			areaData += style + ":" + areaAbbrev;
			i++;
		}
		
		result += CdmUtils.concat("&", new String[] {layer, areaData, areaStyle, bbox, mapSize});
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
	public PresenceAbsenceTermBase getStatus(){
		return this.status;
	}
	/** 
	 * @see	#getStatus()
	 */
	public void setStatus(PresenceAbsenceTermBase status){
		this.status = status;
	}

}