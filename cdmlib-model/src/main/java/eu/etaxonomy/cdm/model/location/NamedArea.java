/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import eu.etaxonomy.cdm.model.common.ILoadableTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.media.Media;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:36
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamedArea", propOrder = {
    "validPeriod",
    "shape",
    "pointApproximation",
    "waterbodiesOrCountries",
    "type",
    "level"
})
@XmlRootElement(name = "NamedArea")
@Entity
public class NamedArea extends OrderedTermBase<NamedArea> {
	static Logger logger = Logger.getLogger(NamedArea.class);
	
	//description of time valid context of this area. e.g. year range
    @XmlElement(name = "ValidPeriod")
	private TimePeriod validPeriod;
	
	//Binary shape definition for user's defined area as polygon
	@XmlElement(name = "Shape")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Media shape;
	
    @XmlElement(name = "PointApproximation")
	private Point pointApproximation;
	
	@XmlElementWrapper(name = "WaterbodiesOrCountries")
	@XmlElement(name = "WaterbodiesOrCountry")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Set<WaterbodyOrCountry> waterbodiesOrCountries = new HashSet<WaterbodyOrCountry>();
	
	@XmlElement(name = "NamedAreaType")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private NamedAreaType type;
	
	@XmlElement(name = "NamedAreaLevel")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private NamedAreaLevel level;
	
	/**
	 * Factory method
	 * @return
	 */
	public static NamedArea NewInstance(){
		return new NamedArea();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static NamedArea NewInstance(String term, String label, String labelAbbrev){
		return new NamedArea(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	public NamedArea() {
		super();
	}
	public NamedArea(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public NamedAreaType getType(){
		return this.type;
	}
	public void setType(NamedAreaType type){
		this.type = type;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public NamedAreaLevel getLevel(){
		return this.level;
	}
	public void setLevel(NamedAreaLevel level){
		this.level = level;
	}

	public TimePeriod getValidPeriod(){
		return this.validPeriod;
	}
	public void setValidPeriod(TimePeriod validPeriod){
		this.validPeriod = validPeriod;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Media getShape(){
		return this.shape;
	}
	public void setShape(Media shape){
		this.shape = shape;
	}

    @ManyToMany
    @JoinTable(
        name="DefinedTermBase_WaterbodyOrCountry"
    )
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<WaterbodyOrCountry> getWaterbodiesOrCountries() {
		return waterbodiesOrCountries;
	}
	protected void setWaterbodiesOrCountries(
			Set<WaterbodyOrCountry> waterbodiesOrCountries) {
		this.waterbodiesOrCountries = waterbodiesOrCountries;
	}
	public void addWaterbodyOrCountry(
			WaterbodyOrCountry waterbodyOrCountry) {
		this.waterbodiesOrCountries.add(waterbodyOrCountry);
	}
	public void removeWaterbodyOrCountry(
			WaterbodyOrCountry waterbodyOrCountry) {
		this.waterbodiesOrCountries.remove(waterbodyOrCountry);
	}
	
	public Point getPointApproximation() {
		return pointApproximation;
	}
	public void setPointApproximation(Point pointApproximation) {
		this.pointApproximation = pointApproximation;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#readCsvLine(java.util.List)
	 */
	@Override
	public ILoadableTerm readCsvLine(List csvLine) {
		Language lang = Language.DEFAULT();
		super.readCsvLine(csvLine, lang);
		String abbreviatedLabel = (String)csvLine.get(4);
		//TODO if TDWG
		if (true){
			TdwgArea.addTdwgArea(this,abbreviatedLabel);
		}
		this.getRepresentation(lang).setAbbreviatedLabel(abbreviatedLabel);
		return this;
	}

}