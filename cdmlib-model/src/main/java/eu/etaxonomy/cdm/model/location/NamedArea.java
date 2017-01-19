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
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Parameter;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.search.DefinedTermBaseClassBridge;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.media.Media;

/**
 * @author m.doering
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
    "countries",
    "type",
    "level"
})
@XmlRootElement(name = "NamedArea")
@XmlSeeAlso({
    Country.class
})
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
@ClassBridge(impl=DefinedTermBaseClassBridge.class, params={
    @Parameter(name="includeParentTerms", value="true")
})
public class NamedArea extends OrderedTermBase<NamedArea> implements Cloneable {
    private static final long serialVersionUID = 6248434369557403036L;
    private static final Logger logger = Logger.getLogger(NamedArea.class);


	//Continent UUIDs
    private static final UUID uuidEurope = UUID.fromString("3b69f979-408c-4080-b573-0ad78a315610");
	private static final UUID uuidAfrica = UUID.fromString("c204c529-d8d2-458f-b939-96f0ebd2cbe8");
	private static final UUID uuidAsiaTemperate = UUID.fromString("7f4f4f89-3b4c-475d-929f-144109bd8457");
	private static final UUID uuidAsiaTropical = UUID.fromString("f8039275-d2c0-4753-a1ab-0336642a1499");
	private static final UUID uuidNAmerica = UUID.fromString("81d8aca3-ddd7-4537-9f2b-5327c95b6e28");
	private static final UUID uuidSAmerica = UUID.fromString("12b861c9-c922-498c-8b1a-62afc26d19e3");
	private static final UUID uuidAustralasia = UUID.fromString("a2afdb9a-04a0-434c-9e75-d07dbeb86526");
	private static final UUID uuidPacific = UUID.fromString("c57adcff-5213-45f0-a5f0-97a9f5c0f1fe");
	private static final UUID uuidAntarctica = UUID.fromString("71fd9ab7-9b07-4eb6-8e54-c519aff56728");


    public static final UUID uuidTdwgAreaVocabulary = UUID.fromString("1fb40504-d1d7-44b0-9731-374fbe6cac77");
    public static final UUID uuidContinentVocabulary = UUID.fromString("e72cbcb6-58f8-4201-9774-15d0c6abc128");
    public static final UUID uuidWaterbodyVocabulary = UUID.fromString("35a62b25-f541-4f12-a7c7-17d90dec3e03");


	private static final UUID uuidArcticOcean = UUID.fromString("af4271e5-8897-4e6f-9db7-54ea4f28cfc0");
	private static final UUID uuidAtlanticOcean = UUID.fromString("77e79804-1b17-4c99-873b-933fe216e3da");
	private static final UUID uuidPacificOcean = UUID.fromString("3d68a327-104c-49d5-a2d8-c71c6600181b");
	private static final UUID uuidIndianOcean = UUID.fromString("ff744a37-5990-462c-9c20-1e85a9943851");
	private static final UUID uuidSouthernOcean = UUID.fromString("ef04f363-f67f-4a2c-8d98-110de4c5f654");
	private static final UUID uuidMediterraneanSea = UUID.fromString("8811a47e-29d6-4455-8f83-8916b78a692f");
	private static final UUID uuidBlackSea = UUID.fromString("4cb4bbae-9aab-426c-9025-e34f809165af");
	private static final UUID uuidCaspianSea = UUID.fromString("598fec0e-b93a-4947-a1f3-601e380797f7");
	private static final UUID uuidRedSea = UUID.fromString("ee69385e-6c80-405c-be6e-974e9fd1e297");
	private static final UUID uuidPersianGulf = UUID.fromString("8dc16e70-74b8-4143-95cf-a659a319a854");



    private static Map<String, UUID> tdwgAbbrevMap = null;
    private static Map<String, UUID> tdwglabelMap = null;

    private static Map<UUID, NamedArea> tdwgTermMap = null;
    private static Map<UUID, NamedArea> continentMap = null;
    private static Map<UUID, NamedArea> waterbodyMap = null;


    private static Map<UUID, NamedArea> termMap = null;

	public static final NamedArea ARCTICOCEAN () { return waterbodyMap.get(uuidArcticOcean );}
	public static final NamedArea ATLANTICOCEAN () { return waterbodyMap.get(uuidAtlanticOcean );}
	public static final NamedArea PACIFICOCEAN () { return waterbodyMap.get(uuidPacificOcean );}
	public static final NamedArea INDIANOCEAN () { return waterbodyMap.get(uuidIndianOcean );}
	public static final NamedArea SOUTHERNOCEAN () { return waterbodyMap.get(uuidSouthernOcean );}
	public static final NamedArea MEDITERRANEANSEA () { return waterbodyMap.get(uuidMediterraneanSea );}
	public static final NamedArea BLACKSEA () { return waterbodyMap.get(uuidBlackSea );}
	public static final NamedArea CASPIANSEA () { return waterbodyMap.get(uuidCaspianSea );}
	public static final NamedArea REDSEA () { return waterbodyMap.get(uuidRedSea );}
	public static final NamedArea PERSIANGULF () { return waterbodyMap.get(uuidPersianGulf );}


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
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Media shape;

    private Point pointApproximation;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DefinedTermBase_Country")
    private final Set<Country> countries = new HashSet<Country>();

    @ManyToOne(fetch = FetchType.LAZY)
    private NamedAreaType type;

    @ManyToOne(fetch = FetchType.LAZY)
    private NamedAreaLevel level;


//********************************** Constructor *******************************************************************/

  	//for hibernate use only
  	@Deprecated
  	protected NamedArea() {
    	super(TermType.NamedArea);
    }

    protected NamedArea(String term, String label, String labelAbbrev) {
        super(TermType.NamedArea, term, label, labelAbbrev);
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

    @XmlElementWrapper(name = "Countries")
    @XmlElement(name = "Country")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    public Set<Country> getCountries() {
        return countries;
    }

    public void addCountry(Country country) {
        this.countries.add(country);
    }

    public void removeCountry(Country country) {
        this.countries.remove(country);
    }

    @XmlElement(name = "PointApproximation")
    public Point getPointApproximation() {
        return pointApproximation;
    }
    public void setPointApproximation(Point pointApproximation) {
        this.pointApproximation = pointApproximation;
    }

    @Override
    @XmlElement(name = "KindOf", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    public NamedArea getKindOf(){
        return super.getKindOf();
    }

    @Override
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

    @Override
    public void setPartOf(NamedArea partOf){
        this.partOf = partOf;
    }

    @Override
    @XmlElementWrapper(name = "Generalizations", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    @XmlElement(name = "GeneralizationOf", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    public Set<NamedArea> getGeneralizationOf(){
        return super.getGeneralizationOf();
    }

    @Override
    protected void setGeneralizationOf(Set<NamedArea> value){
        super.setGeneralizationOf(value);
    }

    @Override
    @XmlElementWrapper(name = "Includes", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    @XmlElement(name = "Include", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    public Set<NamedArea> getIncludes(){
        return super.getIncludes();
    }

    @Override
    protected void setIncludes(Set<NamedArea> includes) {
        super.setIncludes(includes);
    }

    @Override
    public NamedArea readCsvLine(Class<NamedArea> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {
        NamedArea newInstance = super.readCsvLine(termClass, csvLine, terms, abbrevAsId);

        String levelString = csvLine.get(6);

        if(levelString != null && levelString.length() != 0) {
            UUID levelUuid = UUID.fromString(levelString);
            NamedAreaLevel level = (NamedAreaLevel)terms.get(levelUuid);
            newInstance.setLevel(level);
        }

//        String partOfString = csvLine.get(7);
//
//        if(partOfString != null && partOfString.length() != 0) {
//            UUID partOfUuid = UUID.fromString(partOfString);
//            NamedArea partOf = (NamedArea)terms.get(partOfUuid);
//            partOf.addIncludes(newInstance);
//        }
        return newInstance;
    }

	@Override
	protected int partOfCsvLineIndex(){
		return 7;
	}


    @Override
    public void resetTerms(){
        termMap = null;
        tdwgAbbrevMap = null;
   		tdwglabelMap = null;
   		tdwgTermMap = null;
   		continentMap = null;
   		waterbodyMap = null;
    }

	@Deprecated //preliminary, will be removed in future
    protected static NamedArea getContinentByUuid(UUID uuid){
		if (continentMap == null){
			return null;
		}else{
			return continentMap.get(uuid);
		}
	}

    @Deprecated //preliminary, will be removed in future
    protected static NamedArea getWaterbodyByUuid(UUID uuid){
		if (waterbodyMap == null){
			return null;
		}else{
			return waterbodyMap.get(uuid);
		}
    }

    @Deprecated //preliminary, will be removed in future
    protected static NamedArea getTdwgTermByUuid(UUID uuid){
        if (tdwgTermMap == null){
            DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
            vocabularyStore.initialize();
        }
        return tdwgTermMap.get(uuid);
    }

    @Deprecated //preliminary, will be removed in future
    public static NamedArea getAreaByTdwgAbbreviation(String tdwgAbbreviation){
        if (tdwgAbbrevMap == null){
        	initTdwgMaps();
        }
        UUID uuid = tdwgAbbrevMap.get(tdwgAbbreviation);
        if (uuid == null){
            logger.info("Unknown TDWG area: " + CdmUtils.Nz(tdwgAbbreviation));
            return null;
        }
        return NamedArea.getTdwgTermByUuid(uuid);
    }

    @Deprecated //preliminary, will be removed in future
    public static NamedArea getAreaByTdwgLabel(String tdwgLabel){
        if (tdwglabelMap == null){
            initTdwgMaps();
        }
        tdwgLabel = tdwgLabel.toLowerCase();
        UUID uuid = tdwglabelMap.get(tdwgLabel);
        if (uuid == null){
            logger.info("Unknown TDWG area: " + CdmUtils.Nz(tdwgLabel));
            return null;
        }
        return NamedArea.getTdwgTermByUuid(uuid);
    }

    @Deprecated //preliminary, will be removed in future
    public static boolean isTdwgAreaLabel(String label){
        label = (label == null? null : label.toLowerCase());
        if (tdwglabelMap.containsKey(label)){
            return true;
        }else{
            return false;
        }
    }

    @Deprecated //preliminary, will be removed in future
    public static boolean isTdwgAreaAbbreviation(String abbrev){
        if (tdwgAbbrevMap.containsKey(abbrev)){
            return true;
        }else{
            return false;
        }
    }

	public static final NamedArea EUROPE(){
		return getContinentByUuid(uuidEurope);
	}

	public static final NamedArea AFRICA(){
		return getContinentByUuid(uuidAfrica);
	}

	public static final NamedArea ASIA_TEMPERATE(){
		return getContinentByUuid(uuidAsiaTemperate);
	}

	public static final NamedArea ASIA_TROPICAL(){
		return getContinentByUuid(uuidAsiaTropical);
	}

	public static final NamedArea NORTH_AMERICA(){
		return getContinentByUuid(uuidNAmerica);
	}

	public static final NamedArea ANTARCTICA(){
		return getContinentByUuid(uuidAntarctica);
	}

	public static final NamedArea SOUTH_AMERICA(){
		return getContinentByUuid(uuidSAmerica);
	}

	public static final NamedArea AUSTRALASIA(){
		return getContinentByUuid(uuidAustralasia);
	}

	public static final NamedArea PACIFIC(){
		return getContinentByUuid(uuidPacific);
	}


	protected void setDefaultContinentTerms(TermVocabulary<NamedArea> termVocabulary) {
		continentMap = new HashMap<UUID, NamedArea>();
		for (NamedArea term : termVocabulary.getTerms()){
			continentMap.put(term.getUuid(), term);  //TODO casting
		}
	}

	protected void setDefaultWaterbodyTerms(TermVocabulary<NamedArea> termVocabulary) {
		waterbodyMap = new HashMap<UUID, NamedArea>();
		for (NamedArea term : termVocabulary.getTerms()){
			waterbodyMap.put(term.getUuid(), term);  //TODO casting
		}
	}

	protected void setTdwgDefaultTerms(TermVocabulary<NamedArea> tdwgTermVocabulary) {
        tdwgTermMap = new HashMap<UUID, NamedArea>();
        for (NamedArea term : tdwgTermVocabulary.getTerms()){
            tdwgTermMap.put(term.getUuid(), term);  //TODO casting
            addTdwgArea(term);
        }

	}

   protected static void addTdwgArea(NamedArea area){
        if (area == null){
            logger.warn("tdwg area is null");
            return;
        }
        Language lang = Language.DEFAULT();
        Representation representation = area.getRepresentation(lang);
        String tdwgAbbrevLabel = representation.getAbbreviatedLabel();
        String tdwgLabel = representation.getLabel().toLowerCase();
        if (tdwgAbbrevLabel == null){
            logger.warn("tdwgLabel = null");
            return;
        }
        //init map
        if (tdwgAbbrevMap == null){
        	tdwgAbbrevMap = new HashMap<String, UUID>();
        }
        if (tdwglabelMap == null){
        	tdwglabelMap = new HashMap<String, UUID>();
        }
        //add to map
        tdwgAbbrevMap.put(tdwgAbbrevLabel, area.getUuid());
        tdwglabelMap.put(tdwgLabel, area.getUuid());
        //add type
        area.setType(NamedAreaType.ADMINISTRATION_AREA());
        //add level
        if (tdwgAbbrevLabel.trim().length()== 1){
            area.setLevel(NamedAreaLevel.TDWG_LEVEL1());
        }else if (tdwgAbbrevLabel.trim().length()== 2){
            area.setLevel(NamedAreaLevel.TDWG_LEVEL2());
        }else if (tdwgAbbrevLabel.trim().length()== 3){
            area.setLevel(NamedAreaLevel.TDWG_LEVEL3());
        }else if (tdwgAbbrevLabel.trim().length()== 6){
            area.setLevel(NamedAreaLevel.TDWG_LEVEL4());
        }else {
            logger.warn("Unknown TDWG Level " + tdwgAbbrevLabel + "! Unvalid string length (" +  tdwgAbbrevLabel.length() +")");
        }
    }

    private static void initTdwgMaps(){
    	tdwglabelMap = new HashMap<String, UUID>();
    	tdwgAbbrevMap = new HashMap<String, UUID>();
    }



    @Override
    protected void setDefaultTerms(TermVocabulary<NamedArea> termVocabulary) {
        if (termVocabulary.getUuid().equals(NamedArea.uuidTdwgAreaVocabulary)){
        	this.setTdwgDefaultTerms(termVocabulary);
        }else if (termVocabulary.getUuid().equals(NamedArea.uuidContinentVocabulary)){
        	this.setDefaultContinentTerms(termVocabulary);
        }else if (termVocabulary.getUuid().equals(NamedArea.uuidWaterbodyVocabulary)){
        	this.setDefaultWaterbodyTerms(termVocabulary);
        }else{
	    	termMap = new HashMap<UUID, NamedArea>();
	        for (NamedArea term : termVocabulary.getTerms()){
	            termMap.put(term.getUuid(), term);
	        }
        }
    }

// ************** Hierarchie List ****************************

    /**
     * This method returns a sorted tree structure which sorts areas by it's level and within the same level
     * alphabetically (TODO to be tested).
     * The structure returned is a tree with alternating nodes that represent an area and an areaLevel.
     * This way also areas that have children belonging to different levels can be handled.<BR>
     * The root node is always an empty area node which holds the list of top level areaLevels.
     * AreaLevels with no level defined are handled as if they have a separate level (level="null").
     *
     * There is a somehow similar implementation in {@link eu.etaxonomy.cdm.api.service.DistributionTree}
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

///****************** toString ***********************************************/

        @Override
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

        @Override
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

    @Override
    public String toString(){
        String result, label, IdInVocabulary, level = "";

        if (this.level != null){
            level = this.level.getLabel();
        }else{
            level = "no level";
        }
        label = this.getLabel();
        IdInVocabulary = getIdInVocabulary();
        IdInVocabulary = IdInVocabulary != null ? '<' + IdInVocabulary + '>' : "";
        result = "[" + level + ", " +  IdInVocabulary + label + "]";

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
        NamedArea area = CdmBase.deproxy(namedArea, NamedArea.class);

        StringBuilder title = new StringBuilder();
        Representation representation = area.getPreferredRepresentation(language);
        if (representation != null){
            String areaString = getPreferredAreaLabel(namedArea, representation);

            title.append(areaString);
        }else if (area.isProtectedTitleCache()){
        	title.append(area.getTitleCache());
        }else if (StringUtils.isNotBlank(area.getIdInVocabulary())){
        	title.append(area.getIdInVocabulary());
        }
        if (area.getLevel() == null){
        	title.append(" - ");
        	title.append(area.getClass().getSimpleName());
        }else{
        	title.append(" - ");
        	Representation levelRepresentation = area.getLevel().getPreferredRepresentation(language);
        	String levelString = getPreferredAreaLabel(area.getLevel(), levelRepresentation);
        	title.append(levelString);
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
            areaString = definedTerm == null ? null : definedTerm.getTitleCache();
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
            //no changes to level, pointApproximation, shape, type, validPeriod and countries
            return result;

    }


}
