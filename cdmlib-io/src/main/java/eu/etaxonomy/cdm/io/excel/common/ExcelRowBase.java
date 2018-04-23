/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase.SourceType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Base class for data holder classes for excel or similar imports.
 *
 * @author a.mueller
 \* @since 13.07.2011
 */
public abstract class ExcelRowBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExcelRowBase.class);

	private UUID cdmUuid = null;

	private String ecology;
	private String plantDescription;


//	private String family;
//	private String genus;
//	private String specificEpithet;


	private TreeMap<Integer, IdentifiableSource> sources = new TreeMap<Integer, IdentifiableSource>();
	private TreeMap<Integer, SpecimenTypeDesignation> types = new TreeMap<Integer, SpecimenTypeDesignation>();
	private List<PostfixTerm> extensions  = new ArrayList<PostfixTerm>();

	//features
	private Map<UUID, TreeMap<Integer, String>> featureTexts = new HashMap<UUID, TreeMap<Integer, String>>();
	private Map<UUID, TreeMap<Integer, String>> featureLanguages = new HashMap<UUID, TreeMap<Integer, String>>();
	//feature sources
	private Map<UUID, TreeMap<Integer, SourceDataHolder>> textSources = new HashMap<UUID, TreeMap<Integer, SourceDataHolder>>();


	public ExcelRowBase() {
	}

	public class PostfixTerm{
		public PostfixTerm(){}
		public String term;
		public String postfix;
	}


	public class SourceDataHolder{
		private TreeMap<Integer, Map<SourceType, String>> sources = new TreeMap<Integer, Map<SourceType, String>>();

		public void putSource(int index, SourceType type, String value){
			Map<SourceType, String> map = sources.get(index);
			if (map == null){
				map = new HashMap<SourceType, String>();
				sources.put(index, map);
			}
			map.put(type, value);
		}

		public List<Map<SourceType, String>> getSources() {
			return getOrdered(sources);
		}
	}



// **************************** GETTER / SETTER *********************************/


	public void setCdmUuid(UUID cdmUuid) {
		this.cdmUuid = cdmUuid;
	}


	public UUID getCdmUuid() {
		return cdmUuid;
	}

//
//	/**
//	 * @return the author
//	 */
//	public String getAuthor() {
//		return author;
//	}
//
//
//	/**
//	 * @param author the author to set
//	 */
//	public void setAuthor(String author) {
//		this.author = author;
//	}



	/**
	 * @return the ecology
	 */
	public String getEcology() {
		return ecology;
	}


	/**
	 * @param ecology the ecology to set
	 */
	public void setEcology(String ecology) {
		this.ecology = ecology;
	}


	/**
	 * @return the plantDescription
	 */
	public String getPlantDescription() {
		return plantDescription;
	}


	/**
	 * @param plantDescription the plantDescription to set
	 */
	public void setPlantDescription(String plantDescription) {
		this.plantDescription = plantDescription;
	}

	public void putIdInSource(int key, String id){
		IdentifiableSource source = getOrMakeSource(key);
		source.setIdInSource(id);
	}
	public void putSourceReference(int key, Reference reference){
		IdentifiableSource source = getOrMakeSource(key);
		source.setCitation(reference);
	}

	public List<IdentifiableSource> getSources() {
		return getOrdered(sources);
	}


	/**
	 * @param key
	 * @return
	 */
	private IdentifiableSource getOrMakeSource(int key) {
		IdentifiableSource  source = sources.get(key);
		if (source == null){
			source = IdentifiableSource.NewInstance(OriginalSourceType.Unknown);
			sources.put(key, source);
		}
		return source;
	}


	public void putTypeCategory(int key, SpecimenTypeDesignationStatus status){
		SpecimenTypeDesignation designation = getOrMakeTypeDesignation(key);
		designation.setTypeStatus(status);
	}
	public void putTypifiedName(int key, TaxonName name){
		if (name != null){
			SpecimenTypeDesignation designation = getOrMakeTypeDesignation(key);
			name.addTypeDesignation(designation, false);
		}
	}

	public List<SpecimenTypeDesignation> getTypeDesignations() {
		return getOrdered(types);
	}


	private SpecimenTypeDesignation getOrMakeTypeDesignation(int key) {
		SpecimenTypeDesignation designation = types.get(key);
		if (designation == null){
			designation = SpecimenTypeDesignation.NewInstance();
			types.put(key, designation);
		}
		return designation;
	}

	private<T extends Object> List<T> getOrdered(TreeMap<?, T> tree) {
		List<T> result = new ArrayList<T>();
		for (T value : tree.values()){
			result.add(value);
		}
		return result;
	}

	public void addExtension(String levelPostfix, String value) {
		PostfixTerm term = new PostfixTerm();
		term.term = value;
		term.postfix = levelPostfix;
		this.extensions.add(term);
	}

	public List<PostfixTerm> getExtensions(){
		return extensions;
	}

//***************** FEATURES ***************************************************/

	public void putFeature(UUID featureUuid, int index, String value) {
		TreeMap<Integer, String> featureMap = featureTexts.get(featureUuid);
		if (featureMap == null){
			featureMap = new TreeMap<Integer, String>();
			featureTexts.put(featureUuid, featureMap);
		}
		featureMap.put(index, value);
	}

	public void putFeatureLanguage(UUID featureUuid, int index, String value) {
		TreeMap<Integer, String> featureLanguageMap = featureLanguages.get(featureUuid);
		if (featureLanguageMap == null){
			featureLanguageMap = new TreeMap<Integer, String>();
			featureLanguages.put(featureUuid, featureLanguageMap);
		}
		featureLanguageMap.put(index, value);
	}

	public Set<UUID> getFeatures() {
		return featureTexts.keySet();
	}

	public List<String> getFeatureTexts(UUID featureUuid) {
		TreeMap<Integer, String> map = featureTexts.get(featureUuid);
		if (map != null){
			return getOrdered(map);
		}else{
			return null;
		}
	}

	public List<String> getFeatureLanguages(UUID featureUuid) {
		TreeMap<Integer, String> map = featureLanguages.get(featureUuid);
		if (map != null){
			return getOrdered(map);
		}else{
			return null;
		}
	}


	public void putFeatureSource(UUID featureUuid,	int featureIndex, SourceType refType, String value, int refIndex) {
		//feature Map
		TreeMap<Integer, SourceDataHolder> featureMap = textSources.get(featureUuid);
		if (featureMap == null){
			featureMap = new TreeMap<Integer, SourceDataHolder>();
			textSources.put(featureUuid, featureMap);
		}
		//sourcedText
		SourceDataHolder sourceDataHolder = featureMap.get(featureIndex);
		if (sourceDataHolder == null){
			sourceDataHolder = new SourceDataHolder();
			featureMap.put(featureIndex, sourceDataHolder);
		}
		//
		sourceDataHolder.putSource(refIndex, refType, value);
	}


	public SourceDataHolder getFeatureTextReferences(UUID featureUuid, int index) {
		TreeMap<Integer, SourceDataHolder> textMap = textSources.get(featureUuid);
		if (textMap == null){
			return new SourceDataHolder();
		}else{
			SourceDataHolder sourceMap = textMap.get(index);
			return sourceMap;
		}

	}



}
