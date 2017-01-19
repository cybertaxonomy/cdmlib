/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.XmlImportState;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @created 11.05.2009
 */
public class MarkupImportState extends XmlImportState<MarkupImportConfigurator, MarkupDocumentImport>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupImportState.class);


	private UnmatchedLeads unmatchedLeads;
	private boolean onlyNumberedTaxaExist; //attribute in <key>

	private Set<FeatureNode> featureNodesToSave = new HashSet<FeatureNode>();

	private Set<PolytomousKeyNode> polytomousKeyNodesToSave = new HashSet<PolytomousKeyNode>();

	private PolytomousKey currentKey;

	private TeamOrPersonBase<?> currentCollector;

	private Set<NamedArea> currentAreas = new HashSet<NamedArea>();

	private Language defaultLanguage;

	private Taxon currentTaxon;
	private String currentTaxonNum;

	private boolean taxonInClassification = true;

	private String latestGenusEpithet = null;

	private TeamOrPersonBase<?> latestAuthorInHomotype = null;
	private Reference latestReferenceInHomotype = null;

	private boolean isCitation = false;
	private boolean isNameType = false;
	private boolean isProParte = false;
	private boolean currentTaxonExcluded = false;

	private boolean isSpecimenType = false;


	private String baseMediaUrl = null;

	private Map<String, FootnoteDataHolder> footnoteRegister = new HashMap<String, FootnoteDataHolder>();

	private Map<String, Media> figureRegister = new HashMap<String, Media>();

	private Map<String, Set<AnnotatableEntity>> footnoteRefRegister = new HashMap<String, Set<AnnotatableEntity>>();
	private Map<String, Set<AnnotatableEntity>> figureRefRegister = new HashMap<String, Set<AnnotatableEntity>>();

	private Map<String, UUID> areaMap = new HashMap<String, UUID>();

	private Map<String,UUID> unknownFeaturesUuids = new HashMap<String, UUID>();

	private List<FeatureSorterInfo> currentGeneralFeatureSorterList;  //keep in multiple imports
	private List<FeatureSorterInfo> currentCharFeatureSorterList; //keep in multiple imports
	private Map<String,List<FeatureSorterInfo>> generalFeatureSorterListMap = new HashMap<String, List<FeatureSorterInfo>>();  //keep in multiple imports
	private Map<String,List<FeatureSorterInfo>> charFeatureSorterListMap = new HashMap<String, List<FeatureSorterInfo>>(); //keep in multiple imports


	/**
	 * This method resets all those variables that should not be reused from one import to another.
	 * @see MarkupImportConfigurator#isReuseExistingState()
	 * @see MarkupImportConfigurator#getNewState()
	 */
	protected void reset(){
		featureNodesToSave = new HashSet<FeatureNode>();
		polytomousKeyNodesToSave = new HashSet<PolytomousKeyNode>();
		currentKey = null;
		defaultLanguage = null;
		currentTaxon = null;
		currentCollector = null;
		footnoteRegister = new HashMap<String, FootnoteDataHolder>();
		figureRegister = new HashMap<String, Media>();
		footnoteRefRegister = new HashMap<String, Set<AnnotatableEntity>>();
		figureRefRegister = new HashMap<String, Set<AnnotatableEntity>>();
		currentAreas = new HashSet<NamedArea>();

		this.resetUuidTermMaps();
	}


//**************************** CONSTRUCTOR ******************************************/

	public MarkupImportState(MarkupImportConfigurator config) {
		super(config);
		if (getTransformer() == null){
			IInputTransformer newTransformer = config.getTransformer();
			if (newTransformer == null){
				newTransformer = new MarkupTransformer();
			}
			setTransformer(newTransformer);
		}
	}

// ********************************** GETTER / SETTER *************************************/

	public UnmatchedLeads getUnmatchedLeads() {
		return unmatchedLeads;
	}

	public void setUnmatchedLeads(UnmatchedLeads unmatchedKeys) {
		this.unmatchedLeads = unmatchedKeys;
	}

	public void setFeatureNodesToSave(Set<FeatureNode> featureNodesToSave) {
		this.featureNodesToSave = featureNodesToSave;
	}

	public Set<FeatureNode> getFeatureNodesToSave() {
		return featureNodesToSave;
	}

	public Set<PolytomousKeyNode> getPolytomousKeyNodesToSave() {
		return polytomousKeyNodesToSave;
	}

	public void setPolytomousKeyNodesToSave(Set<PolytomousKeyNode> polytomousKeyNodesToSave) {
		this.polytomousKeyNodesToSave = polytomousKeyNodesToSave;
	}

	public Language getDefaultLanguage() {
		return this.defaultLanguage;
	}

	public void setDefaultLanguage(Language defaultLanguage){
		this.defaultLanguage = defaultLanguage;
	}


	public void setCurrentTaxon(Taxon currentTaxon) {
		this.currentTaxon = currentTaxon;
	}

	public Taxon getCurrentTaxon() {
		return currentTaxon;
	}

	public void setCurrentTaxonNum(String currentTaxonNum) {
		this.currentTaxonNum = currentTaxonNum;
	}

	public String getCurrentTaxonNum() {
		return currentTaxonNum;
	}



	/**
	 * Is the import currently handling a citation?
	 * @return
	 */
	public boolean isCitation() {
		return isCitation;
	}

	public void setCitation(boolean isCitation) {
		this.isCitation = isCitation;
	}


	public boolean isNameType() {
		return isNameType;
	}

	public void setNameType(boolean isNameType) {
		this.isNameType = isNameType;
	}

	public void setProParte(boolean isProParte) {
		this.isProParte = isProParte;
	}

	public boolean isProParte() {
		return isProParte;
	}

	public void setBaseMediaUrl(String baseMediaUrl) {
		this.baseMediaUrl = baseMediaUrl;
	}

	public String getBaseMediaUrl() {
		return baseMediaUrl;
	}



	public void registerFootnote(FootnoteDataHolder footnote) {
		footnoteRegister.put(footnote.id, footnote);
	}

	public FootnoteDataHolder getFootnote(String key) {
		return footnoteRegister.get(key);
	}


	public void registerFigure(String key, Media figure) {
		figureRegister.put(key, figure);
	}

	public Media getFigure(String key) {
		return figureRegister.get(key);
	}

	public Set<AnnotatableEntity> getFootnoteDemands(String footnoteId){
		return footnoteRefRegister.get(footnoteId);
	}

	public void putFootnoteDemands(String footnoteId, Set<AnnotatableEntity> demands){
		footnoteRefRegister.put(footnoteId, demands);
	}


	public Set<AnnotatableEntity> getFigureDemands(String figureId){
		return figureRefRegister.get(figureId);
	}

	public void putFigureDemands(String figureId, Set<AnnotatableEntity> demands){
		figureRefRegister.put(figureId, demands);
	}

	/**
	 * @param key
	 */
	public void setCurrentKey(PolytomousKey key) {
		this.currentKey = key;
	}

	/**
	 * @return the currentKey
	 */
	public PolytomousKey getCurrentKey() {
		return currentKey;
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public UUID getAreaUuid(Object key) {
		return areaMap.get(key);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public UUID putAreaUuid(String key, UUID value) {
		return areaMap.put(key, value);
	}

	public void putUnknownFeatureUuid(String featureLabel, UUID featureUuid) {
		this.unknownFeaturesUuids.put(featureLabel, featureUuid);
	}

	public boolean isOnlyNumberedTaxaExist() {
		return onlyNumberedTaxaExist;
	}

	public void setOnlyNumberedTaxaExist(boolean onlyNumberedTaxaExist) {
		this.onlyNumberedTaxaExist = onlyNumberedTaxaExist;
	}

	public Map<String,List<FeatureSorterInfo>> getGeneralFeatureSorterListMap() {
		return generalFeatureSorterListMap;
	}
	public Map<String,List<FeatureSorterInfo>> getCharFeatureSorterListMap() {
		return charFeatureSorterListMap;
	}

	public UUID getUnknownFeatureUuid(String featureLabel){
		return this.unknownFeaturesUuids.get(featureLabel);
	}


	/**
	 * Adds new lists to the feature sorter list maps using the given key.
	 * If at least 1 list already existed for the given key, true is returned. False
	 * @param key Key that identifies the feature sorter list.
	 * @return <code>true</code> if at least 1 list already exited for the given key. <code>false</code> otherwise.
	 */
	public boolean addNewFeatureSorterLists(String key) {
		//general feature sorter list
		List<FeatureSorterInfo> generalList = new ArrayList<FeatureSorterInfo>();
		List<FeatureSorterInfo> previous1 = this.generalFeatureSorterListMap.put(key, generalList);
		currentGeneralFeatureSorterList = generalList;

		//character feature sorter list
		List<FeatureSorterInfo> charList = new ArrayList<FeatureSorterInfo>();
		List<FeatureSorterInfo> previous2 = this.charFeatureSorterListMap.put(key, charList);
		currentCharFeatureSorterList = charList;

		return (previous1 != null || previous2 != null);
	}

	/**
	 *
	 * @param feature
	 */
	public FeatureSorterInfo putFeatureToCharSorterList(Feature feature) {
		FeatureSorterInfo featureSorterInfo = new FeatureSorterInfo(feature);
		currentCharFeatureSorterList.add(featureSorterInfo);
		return featureSorterInfo;
	}

	public FeatureSorterInfo getLatestCharFeatureSorterInfo() {
		return currentCharFeatureSorterList.get(currentCharFeatureSorterList.size() - 1);
	}


	/**
	 *
	 * @param feature
	 */
	public void putFeatureToGeneralSorterList(Feature feature) {
		currentGeneralFeatureSorterList.add(new FeatureSorterInfo(feature));

	}

	public String getLatestGenusEpithet() {
		return latestGenusEpithet;
	}

	public void setLatestGenusEpithet(String latestGenusEpithet) {
		this.latestGenusEpithet = latestGenusEpithet;
	}


	public boolean isTaxonInClassification() {
		return taxonInClassification;
	}


	public void setTaxonInClassification(boolean taxonInClassification) {
		this.taxonInClassification = taxonInClassification;
	}


	public TeamOrPersonBase<?> getCurrentCollector() {
		return currentCollector;
	}


	public void setCurrentCollector(TeamOrPersonBase<?> currentCollector) {
		this.currentCollector = currentCollector;
	}


	public void addCurrentArea(NamedArea area) {
		currentAreas.add(area);
	}


	public Set<NamedArea> getCurrentAreas() {
		return currentAreas;
	}

	public void removeCurrentAreas(){
		currentAreas.clear();
	}


	public TeamOrPersonBase<?> getLatestAuthorInHomotype() {
		return latestAuthorInHomotype;
	}


	public void setLatestAuthorInHomotype(TeamOrPersonBase<?> latestAuthorInHomotype) {
		this.latestAuthorInHomotype = latestAuthorInHomotype;
	}


	public Reference getLatestReferenceInHomotype() {
		return latestReferenceInHomotype;
	}


	public void setLatestReferenceInHomotype(Reference latestReferenceInHomotype) {
		this.latestReferenceInHomotype = latestReferenceInHomotype;
	}

	public void setSpecimenType(boolean isSpecimenType) {
		this.isSpecimenType = isSpecimenType;
	}

	public boolean isSpecimenType() {
		return isSpecimenType;
	}


	//or do we need to make this a uuid?
	private Map<String, Collection> collectionMap = new HashMap<String, Collection>();
	public Collection getCollectionByCode(String code) {
		return collectionMap.get(code);
	}

	public void putCollectionByCode(String code, Collection collection) {
		collectionMap.put(code, collection);
	}


	String collectionAndType = "";
	public void addCollectionAndType(String txt) {
		collectionAndType = CdmUtils.concat("@", collectionAndType, txt);
	}
	public String getCollectionAndType() {
		return collectionAndType;
	}
	public void resetCollectionAndType() {
		collectionAndType = "";
	}


    public boolean isCurrentTaxonExcluded() {
        return currentTaxonExcluded;
    }
    public void setCurrentTaxonExcluded(boolean currentTaxonExcluded) {
        this.currentTaxonExcluded = currentTaxonExcluded;
    }



}
