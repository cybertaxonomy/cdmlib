/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.media.Media;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:36
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "NamedArea", propOrder = {
	"kindOf",
	"generalizationOf",
	"partOf",
	"includes",
    "validPeriod",
    "shape",
    "pointApproximation",
    "waterbodiesOrCountries",
    "type",
    "level"
})
@XmlRootElement(name = "NamedArea")
@XmlSeeAlso({
	TdwgArea.class,
	Continent.class,
	WaterbodyOrCountry.class
})
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class NamedArea extends OrderedTermBase<NamedArea> implements Cloneable {
	private static final long serialVersionUID = 6248434369557403036L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NamedArea.class);
	
	private static Map<UUID, NamedArea> termMap = null;		

//************************* FACTORY METHODS ****************************************/
	
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
	
//**************************** VARIABLES *******************************/
	
	//description of time valid context of this area. e.g. year range
	private TimePeriod validPeriod = TimePeriod.NewInstance();
	
	//Binary shape definition for user's defined area as polygon
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private Media shape;
	
	private Point pointApproximation;
	
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DefinedTermBase_WaterbodyOrCountry")
	private Set<WaterbodyOrCountry> waterbodiesOrCountries = new HashSet<WaterbodyOrCountry>();
	
	@ManyToOne(fetch = FetchType.LAZY)
	private NamedAreaType type;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private NamedAreaLevel level;

//*************************** CONSTRUCTOR ******************************************/
	
	/**
	 * Constructor
	 */
	public NamedArea() {
	}
	
	public NamedArea(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	
//********************************* GETTER /SETTER *********************************************/	

	@XmlElement(name = "NamedAreaType")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	public NamedAreaType getType(){
		return this.type;
	}
	
	public void setType(NamedAreaType type){
		this.type = type;
	}

	@XmlElement(name = "NamedAreaLevel")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	public NamedAreaLevel getLevel(){
		return this.level;
	}
	
	public void setLevel(NamedAreaLevel level){
		this.level = level;
	}

	@XmlElement(name = "ValidPeriod")
	public TimePeriod getValidPeriod(){
		return this.validPeriod;
	}
	
	public void setValidPeriod(TimePeriod validPeriod){
		this.validPeriod = validPeriod;
	}

	@XmlElement(name = "Shape")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	public Media getShape(){
		return this.shape;
	}
	public void setShape(Media shape){
		this.shape = shape;
	}

	@XmlElementWrapper(name = "WaterbodiesOrCountries")
	@XmlElement(name = "WaterbodiesOrCountry")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	public Set<WaterbodyOrCountry> getWaterbodiesOrCountries() {
		return waterbodiesOrCountries;
	}
	
	public void addWaterbodyOrCountry(WaterbodyOrCountry waterbodyOrCountry) {
		this.waterbodiesOrCountries.add(waterbodyOrCountry);
	}
	
	public void removeWaterbodyOrCountry(WaterbodyOrCountry waterbodyOrCountry) {
		this.waterbodiesOrCountries.remove(waterbodyOrCountry);
	}
	
	@XmlElement(name = "PointApproximation")
	public Point getPointApproximation() {
		return pointApproximation;
	}
	public void setPointApproximation(Point pointApproximation) {
		this.pointApproximation = pointApproximation;
	}
	
	@XmlElement(name = "KindOf", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	public NamedArea getKindOf(){
		return super.getKindOf();
	}

	public void setKindOf(NamedArea kindOf){
		super.setKindOf(kindOf);
	}
	
	@XmlElement(name = "PartOf", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
	@XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @Override
	public NamedArea getPartOf(){
		return super.getPartOf();
	}
	
	/**
	 * FIXME this method is a workaround for a casting problem in the getPartOf implementation
	 * 
	 * the partOf instance variable is typically a proxy object of type DefinedTermBase, thus
	 * does not coincide with the return value of NamedArea and a ClassCastException is thrown.
	 * 
	 * It is not clear why this only occurs in the editor and not in the webservice where the same
	 * method gets called and should lead to the same results.
	 * 
	 * Seems to be a bigger problem although its origin is buggy behaviour of the javassist implementation.
	 */
	@Deprecated
	@Transient
	public NamedArea getPartOfWorkaround(){
		Object area = super.getPartOf();
		
		if(!(area instanceof NamedArea)){
			area = HibernateProxyHelper.deproxy(area, NamedArea.class);
		}
		
		return (NamedArea) area;
	}
	
	public void setPartOf(NamedArea partOf){
		this.partOf = partOf;
	}
	
	@XmlElementWrapper(name = "Generalizations", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
	@XmlElement(name = "GeneralizationOf", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	public Set<NamedArea> getGeneralizationOf(){
		return super.getGeneralizationOf();
	}
	
	protected void setGeneralizationOf(Set<NamedArea> value){
		super.setGeneralizationOf(value);
	}
	
	@XmlElementWrapper(name = "Includes", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
	@XmlElement(name = "Include", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
	@XmlIDREF
    @XmlSchemaType(name = "IDREF")
	public Set<NamedArea> getIncludes(){
		return super.getIncludes();
	}
	
	protected void setIncludes(Set<NamedArea> includes) {
		super.setIncludes(includes);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#readCsvLine(java.util.List)
	 */
	@Override
	public NamedArea readCsvLine(Class<NamedArea> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		NamedArea newInstance = super.readCsvLine(termClass, csvLine, terms);
		
		String levelString = (String)csvLine.get(6);
		
		if(levelString != null && levelString.length() != 0) {
			UUID levelUuid = UUID.fromString(levelString);
			NamedAreaLevel level = (NamedAreaLevel)terms.get(levelUuid);
			newInstance.setLevel(level);
		}
		
		String partOfString = (String)csvLine.get(7);
	
		if(partOfString != null && partOfString.length() != 0) {
			UUID partOfUuid = UUID.fromString(partOfString);
			NamedArea partOf = (NamedArea)terms.get(partOfUuid);
			partOf.addIncludes(newInstance);
		} 
		return newInstance;
	}
	

	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}

	
	@Override
	protected void setDefaultTerms(TermVocabulary<NamedArea> termVocabulary) {
		termMap = new HashMap<UUID, NamedArea>();
		for (NamedArea term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}
	
// ************** Hierarchie List ****************************	
	
/*	public boolean equals(NamedArea area){
		boolean result = false;
		if (this.getLabel().toString().compareTo(area.getLabel().toString()) == 0){
			result = true;
		}
		return result;
	}*/
	
//	public int compareTo(NamedArea area){
//		return getLabel().compareTo(area.getLabel());
//	}
	
	
	
	/**
	 * This method returns a sorted tree structure which sorts areas by it's level and within the same level
	 * alphabetically (TODO to be tested).
	 * The structure returned is a tree with alternating nodes that represent an area and an areaLevel.
	 * This way also area the have children belonging to different levels can be handled.<BR>
	 * The root node is always an empty area node which holds the list of top level areaLevels.
	 * AreaLevels with no level defined are handled as if they have a separate level (level="null").
	 * 
	 * @param areaList
	 * @return
	 */
	public static NamedAreaNode getHiearchieList(List<NamedArea> areaList){
		NamedAreaNode result = new NamedAreaNode();
		for (NamedArea area : areaList){
			List<NamedArea> areaHierarchie  = area.getAllLevelList();
			mergeIntoResult(result, areaHierarchie);
		}
		return result;
	}
	
	
	public static class LevelNode {
		NamedAreaLevel level;
		List<NamedAreaNode> areaList = new ArrayList<NamedAreaNode>();

		public NamedAreaNode add(NamedArea area) {
			NamedAreaNode node = new NamedAreaNode();
			node.area = area;
			areaList.add(node);
			return node;

		}

		public NamedAreaNode getNamedAreaNode(NamedArea area) {
			for (NamedAreaNode node : areaList) {
				if (node.area.equals(area)) {
					return node;
				}
			}
			return null;
		}

		public String toString() {
			return toString(false, 0);
		}
		public String toString(boolean recursive, int identation) {
			String result = level == null? "" :level.getTitleCache();
			if (recursive == false){
				return result;
			}else{
				int areaSize = this.areaList.size();
				if (areaSize > 0){
					result = "\n" + StringUtils.leftPad("", identation) + result  + "[";
				}
				boolean isFirst = true;
				for (NamedAreaNode level: this.areaList){
					if (isFirst){
						isFirst = false;
					}else{
						result += ",";
					}
					result += level.toString(recursive, identation+1);
				}
				if (areaSize > 0){
					result += "]";
					
				}
				return result;
			}
		}

	}

	public static class NamedAreaNode {
		NamedArea area;
		List<LevelNode> levelList = new ArrayList<LevelNode>();
		
		public LevelNode getLevelNode(NamedAreaLevel level) {
			for (LevelNode node : levelList) {
				if (node.level != null &&  node.level.equals(level)) {
					return node;
				}
			}
			return null;
		}

		public List<NamedAreaNode> getList(NamedAreaLevel level) {
			LevelNode node = getLevelNode(level);
			if (node == null) {
				return new ArrayList<NamedAreaNode>();
			} else {
				return node.areaList;
			}
		};

		public boolean contains(NamedAreaLevel level) {
			if (getList(level).size() > 0) {
				return true;
			} else {
				return false;
			}
		}

		public LevelNode add(NamedAreaLevel level) {
			LevelNode node = new LevelNode();
			node.level = level;
			levelList.add(node);
			return node;
		}

		public String toString() {
			return toString(false, 0);
		}
		
		public String toString(boolean recursive, int identation) {
			String result = "";
			if (area != null) {
				result = area.getTitleCache();
			}
			if (recursive){
				int levelSize = this.levelList.size();
				if (levelSize > 0){
					result = "\n" + StringUtils.leftPad("", identation) + result  + "[";
				}
				boolean isFirst = true;
				for (LevelNode level: this.levelList){
					if (isFirst){
						isFirst = false;
					}else{
						result += ";";
					}
					result += level.toString(recursive, identation+1);
				}
				if (levelSize > 0){
					result += "]";
					
				}
				return result;
			}else{
				int levelSize = this.levelList.size();
				return result + "[" + levelSize + " sublevel(s)]";
			}
		}
	}

	private static void mergeIntoResult(NamedAreaNode root, List<NamedArea> areaHierarchie) {
		if (areaHierarchie.isEmpty()) {
			return;
		}
		NamedArea highestArea = areaHierarchie.get(0);
		NamedAreaLevel level = highestArea.getLevel();
		NamedAreaNode namedAreaNode;
		if (! root.contains(level)) {
			LevelNode node = root.add(level);
			namedAreaNode = node.add(highestArea);
			//NEW
//			root.area = highestArea;
		} else {
			LevelNode levelNode = root.getLevelNode(level);
			namedAreaNode = levelNode.getNamedAreaNode(highestArea);
			if (namedAreaNode == null) {
				namedAreaNode = levelNode.add(highestArea);
			}
		}
		List<NamedArea> newList = areaHierarchie.subList(1, areaHierarchie.size());
		mergeIntoResult(namedAreaNode, newList);

	}

	@Transient
	public List<NamedArea> getAllLevelList() {
		List<NamedArea> result = new ArrayList<NamedArea>();
		NamedArea copyArea = this;
		result.add(copyArea);		
		while (copyArea.getPartOf() != null) {
			copyArea = copyArea.getPartOf();
			result.add(0, copyArea);
		}
		return result;
	}
	
// ******************* toString **********************************/	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.TermBase#toString()
	 */
	public String toString(){
		String result, label, level = "";
		
		if (this.level != null){
			level = this.level.getLabel();
		}else{
			level = "no level";
		}
		label = this.getLabel();
		result = "[" + level + ", " + label + "]";
		
		return result;
	}
	
	

	/**
	 * Returns the label of the named area together with the area level label and the abbreviated label.
	 * This is kind of a formatter method which may be moved to a better place in future. 
	 * @param namedArea the area
	 * @param language the preferred language
	 * @return null if namedArea == null, the labelWithLevel otherwise
	 */
	public static String labelWithLevel(NamedArea namedArea, Language language) {
		if (namedArea == null){
			return null;
		}
		NamedArea area = (NamedArea) HibernateProxyHelper.deproxy(namedArea);
		
		StringBuilder title = new StringBuilder();
		Representation representation = area.getPreferredRepresentation(language);
		if (representation != null){
			String areaString = getPreferredAreaLabel(namedArea, representation);
			
			title.append(areaString);
			if (area.getLevel() == null){
				title.append(" - ");
				title.append(area.getClass().getSimpleName());
			}else{
				title.append(" - ");
				Representation levelRepresentation = area.getLevel().getPreferredRepresentation(language);
				String levelString = getPreferredAreaLabel(area.getLevel(), levelRepresentation);
				title.append(levelString);
			}
		}
		return title.toString();
	}

	/**
	 * @param definedTerm
	 * @param representation
	 * @return
	 */
	private static String getPreferredAreaLabel(DefinedTermBase<?> definedTerm, Representation representation) {
		String areaString = null;
		if (representation != null){
			areaString = representation.getLabel();
			if (StringUtils.isBlank(areaString)){
				areaString = representation.getAbbreviatedLabel();
			}
			if (StringUtils.isBlank(areaString)){
				areaString = representation.getText();
			}
		}
		if (StringUtils.isBlank(areaString)){
			areaString = definedTerm.getTitleCache();
		}
		if (StringUtils.isBlank(areaString)){
			areaString = "no title";
		}
		return areaString;
	}
	
	//*********************************** CLONE *****************************************/

	/** 
	 * Clones <i>this</i> NamedArea. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> NamedArea by
	 * modifying only some of the attributes.
	 * 
	 * @see eu.etaxonomy.cdm.model.common.OrderedTermBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		NamedArea result;
		
			result = (NamedArea)super.clone();
			//no changes to level, pointApproximation, shape, type, validPeriod and waterbodiesOrCountries
			return result;
		
	}
		
	
}