// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public abstract class ImportStateBase<CONFIG extends ImportConfiguratorBase, IO extends CdmImportBase> extends IoStateBase<CONFIG, IO> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ImportStateBase.class);

	//States
	private boolean isCheck;

	private Map<Object,Classification> treeMap = new HashMap<Object,Classification>();

	private Map<Reference<?>,UUID> treeUuidMap = new HashMap<Reference<?>,UUID>();

	private Map<String,UUID> classificationKeyUuidMap = new HashMap<String,UUID>();

	private IInputTransformer inputTransformer;


	private Map<UUID, ExtensionType> extensionTypeMap = new HashMap<UUID, ExtensionType>();
	private Map<UUID, MarkerType> markerTypeMap = new HashMap<UUID, MarkerType>();
	private Map<UUID, AnnotationType> annotationTypeMap = new HashMap<UUID, AnnotationType>();
	private Map<UUID, DefinedTerm> identifierTypeMap = new HashMap<UUID, DefinedTerm>();

	private Map<UUID, NamedArea> namedAreaMap = new HashMap<UUID, NamedArea>();
	private Map<UUID, NamedAreaLevel> namedAreaLevelMap = new HashMap<UUID, NamedAreaLevel>();
	private Map<UUID, Feature> featureMap = new HashMap<UUID, Feature>();
	private Map<UUID, State> stateTermMap = new HashMap<UUID, State>();
	private Map<UUID, MeasurementUnit> measurementUnitMap = new HashMap<UUID, MeasurementUnit>();

	private Map<UUID, StatisticalMeasure> statisticalMeasureMap = new HashMap<UUID, StatisticalMeasure>();
	private Map<UUID, DefinedTerm> modifierMap = new HashMap<UUID, DefinedTerm>();

	private Map<UUID, PresenceAbsenceTerm> presenceTermMap = new HashMap<UUID, PresenceAbsenceTerm>();
	private Map<UUID, Language> languageMap = new HashMap<UUID, Language>();
	private Map<UUID, TaxonRelationshipType> taxonRelationshipTypeMap = new HashMap<UUID, TaxonRelationshipType>();

	private Map<UUID, ReferenceSystem> referenceSystemMap = new HashMap<UUID, ReferenceSystem>();
	private Map<UUID, Rank> rankMap = new HashMap<UUID, Rank>();
	private Map<UUID, DefinedTerm> kindOfUnitMap = new HashMap<UUID, DefinedTerm>();



	protected IService<CdmBase> service = null;

	protected ImportStateBase(CONFIG config){
		this.config = config;
		stores.put(ICdmIO.TEAM_STORE, new MapWrapper<TeamOrPersonBase<?>>(service));
		stores.put(ICdmIO.REFERENCE_STORE, new MapWrapper<Reference>(service));
		stores.put(ICdmIO.NOMREF_STORE, new MapWrapper<Reference>(service));
		stores.put(ICdmIO.TAXONNAME_STORE, new MapWrapper<TaxonNameBase<?,?>>(service));
		stores.put(ICdmIO.TAXON_STORE, new MapWrapper<TaxonBase>(service));
		stores.put(ICdmIO.SPECIMEN_STORE, new MapWrapper<DerivedUnit>(service));

		if (getTransformer() == null){
			IInputTransformer newTransformer = config.getTransformer();
//			if (newTransformer == null){
//				newTransformer = new DefaultTransf();
//			}
			setTransformer(newTransformer);
		}

	}



	/**
	 * Resets (empties) all maps which map a uuid to a {@link DefinedTermBase term}.
	 * This is usually needed when a a new transaction is opened and user defined terms are reused.
	 */
	public void resetUuidTermMaps(){
		extensionTypeMap = new HashMap<UUID, ExtensionType>();
		markerTypeMap = new HashMap<UUID, MarkerType>();
		annotationTypeMap = new HashMap<UUID, AnnotationType>();

		namedAreaMap = new HashMap<UUID, NamedArea>();
		namedAreaLevelMap = new HashMap<UUID, NamedAreaLevel>();
		featureMap = new HashMap<UUID, Feature>();
		stateTermMap = new HashMap<UUID, State>();
		measurementUnitMap = new HashMap<UUID, MeasurementUnit>();
		statisticalMeasureMap = new HashMap<UUID, StatisticalMeasure>();
		modifierMap = new HashMap<UUID, DefinedTerm>();

		presenceTermMap = new HashMap<UUID, PresenceAbsenceTerm>();;
		languageMap = new HashMap<UUID, Language>();
		taxonRelationshipTypeMap = new HashMap<UUID, TaxonRelationshipType>();

		referenceSystemMap = new HashMap<UUID, ReferenceSystem>();
		rankMap = new HashMap<UUID, Rank>();
	}


	//different type of stores that are used by the known imports
	protected Map<String, MapWrapper<? extends CdmBase>> stores = new HashMap<String, MapWrapper<? extends CdmBase>>();


	/**
	 * @return the stores
	 */
	public Map<String, MapWrapper<? extends CdmBase>> getStores() {
		return stores;
	}

	/**
	 * @param stores the stores to set
	 */
	public void setStores(Map<String, MapWrapper<? extends CdmBase>> stores) {
		this.stores = stores;
	}


 	public MapWrapper<? extends CdmBase> getStore(String storeLabel){
 		return stores.get(storeLabel);
 	}


	/**
	 * @return the treeMap
	 */
	public Classification getTree(Object ref) {
		return treeMap.get(ref);
	}

	/**
	 * @param treeMap the treeMap to set
	 */
	public void putTree(Object ref, Classification tree) {
		if (tree != null){
			this.treeMap.put(ref, tree);
		}
	}

	public int countTrees(){
		return treeUuidMap.size();
	}

	/**
	 * @return the treeUuid
	 */
	public UUID getTreeUuid(Reference ref) {
		return treeUuidMap.get(ref);
	}

	public void putTreeUuid(Reference ref, Classification tree) {
		if (tree != null &&  tree.getUuid() != null){
			this.treeUuidMap.put(ref, tree.getUuid());
		}
	}

	public int countTreeUuids(){
		return treeUuidMap.size();
	}


	/**
	 * Adds a classification uuid to the classification uuid map,
	 * which maps a key for the classification to its UUID in the CDM
	 * @param classificationKeyId
	 * @param classification
	 */
	public void putClassificationUuidInt(int classificationKeyId, Classification classification) {
		putClassificationUuid(String.valueOf(classificationKeyId), classification);
	}

	public void putClassificationUuid(String treeKey, Classification tree) {
		if (tree != null &&  tree.getUuid() != null){
			this.classificationKeyUuidMap.put(treeKey, tree.getUuid());
		}
	}

	public UUID getTreeUuidByIntTreeKey(int treeKey) {
		return classificationKeyUuidMap.get(String.valueOf(treeKey));
	}

	public UUID getTreeUuidByTreeKey(String treeKey) {
		return classificationKeyUuidMap.get(treeKey);
	}


	public DefinedTerm getIdentifierType(UUID uuid){
		return identifierTypeMap.get(uuid);
	}

	public void putIdentifierType(DefinedTerm identifierType){
		identifierTypeMap.put(identifierType.getUuid(), identifierType);
	}

	public ExtensionType getExtensionType(UUID uuid){
		return extensionTypeMap.get(uuid);
	}

	public void putExtensionType(ExtensionType extensionType){
		extensionTypeMap.put(extensionType.getUuid(), extensionType);
	}

	public MarkerType getMarkerType(UUID uuid){
		return markerTypeMap.get(uuid);
	}

	public void putMarkerType(MarkerType markerType){
		markerTypeMap.put(markerType.getUuid(), markerType);
	}

	public AnnotationType getAnnotationType(UUID uuid){
		return annotationTypeMap.get(uuid);
	}

	public void putAnnotationType(AnnotationType annotationType){
		annotationTypeMap.put(annotationType.getUuid(), annotationType);
	}

	public NamedArea getNamedArea(UUID uuid){
		return namedAreaMap.get(uuid);
	}

	public void putNamedArea(NamedArea namedArea){
		namedAreaMap.put(namedArea.getUuid(), namedArea);
	}

	public NamedAreaLevel getNamedAreaLevel(UUID uuid){
		return namedAreaLevelMap.get(uuid);
	}


	public void putNamedAreaLevel(NamedAreaLevel namedAreaLevel){
		namedAreaLevelMap.put(namedAreaLevel.getUuid(), namedAreaLevel);
	}

	public Rank getRank(UUID uuid){
		return rankMap.get(uuid);
	}


	public void putRank(Rank rank){
		rankMap.put(rank.getUuid(), rank);
	}

	public State getStateTerm(UUID uuid){
		return stateTermMap.get(uuid);
	}

	public void putStateTerm(State stateTerm){
		stateTermMap.put(stateTerm.getUuid(), stateTerm);
	}

	public Feature getFeature(UUID uuid){
		return featureMap.get(uuid);
	}

	public void putFeature(Feature feature){
		featureMap.put(feature.getUuid(), feature);
	}

	public DefinedTerm getKindOfUnit(UUID uuid){
		return kindOfUnitMap.get(uuid);
	}

	public void putKindOfUnit(DefinedTerm unit){
		kindOfUnitMap.put(unit.getUuid(), unit);
	}

	public MeasurementUnit getMeasurementUnit(UUID uuid){
		return measurementUnitMap.get(uuid);
	}

	public void putMeasurementUnit(MeasurementUnit unit){
		measurementUnitMap.put(unit.getUuid(), unit);
	}

	public void putStatisticalMeasure(StatisticalMeasure unit){
		statisticalMeasureMap.put(unit.getUuid(), unit);
	}

	public StatisticalMeasure getStatisticalMeasure(UUID uuid){
		return statisticalMeasureMap.get(uuid);
	}


	public DefinedTerm getModifier(UUID uuid){
		return modifierMap.get(uuid);
	}

	public void putModifier(DefinedTerm unit){
		modifierMap.put(unit.getUuid(), unit);
	}

	public TaxonRelationshipType getTaxonRelationshipType(UUID uuid){
		return taxonRelationshipTypeMap.get(uuid);
	}

	public void putTaxonRelationshipType(TaxonRelationshipType relType){
		taxonRelationshipTypeMap.put(relType.getUuid(), relType);
	}


	public PresenceAbsenceTerm getPresenceAbsenceTerm(UUID uuid){
		return presenceTermMap.get(uuid);
	}

	public void putPresenceAbsenceTerm(PresenceAbsenceTerm presenceTerm){
		presenceTermMap.put(presenceTerm.getUuid(), presenceTerm);
	}

	public Language getLanguage(UUID uuid){
		return languageMap.get(uuid);
	}

	public void putLanguage(Language language){
		languageMap.put(language.getUuid(), language);
	}


	public ReferenceSystem getReferenceSystem(UUID uuid){
		return referenceSystemMap.get(uuid);
	}

	public void putReferenceSystem(ReferenceSystem referenceSystem){
		referenceSystemMap.put(referenceSystem.getUuid(), referenceSystem);
	}


	//TODO make this abstract or find another way to force that the
	//transformer exists
	public IInputTransformer getTransformer(){
		return inputTransformer;
	}

	public void setTransformer(IInputTransformer transformer){
		this.inputTransformer = transformer;
	}

	/**
	 * Returns true, if this import is in validation state. Flase otherwise
	 * @return
	 */
	public boolean isCheck() {
		return isCheck;
	}

	/**
	 * @see #isCheck
	 * @param isCheck
	 */
	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}

    /**
     * Returns the import report as a byte array
     * @return
     */
    public byte[] getReportAsByteArray() {
        return null;
    }


}
