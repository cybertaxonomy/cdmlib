// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.markup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.XmlImportState;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class MarkupImportState extends XmlImportState<MarkupImportConfigurator, MarkupImportBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupImportState.class);
	

	private UnmatchedLeads unmatchedLeads;

	private Set<FeatureNode> featureNodesToSave = new HashSet<FeatureNode>();
	
	private Set<PolytomousKeyNode> polytomousKeyNodesToSave = new HashSet<PolytomousKeyNode>();
	
	private Language defaultLanguage;
	
	private Taxon currentTaxon;
	
	private boolean isCitation = false;
	private boolean isNameType = false;
	private boolean isProParte = false;
	
	private String baseMediaUrl = null;
	
	private Map<String, FootnoteDataHolder> footnoteRegister = new HashMap<String, FootnoteDataHolder>();
	
	private Map<String, Media> figureRegister = new HashMap<String, Media>();
	
	private Map<String, Set<AnnotatableEntity>> footnoteRefRegister = new HashMap<String, Set<AnnotatableEntity>>();
	private Map<String, Set<AnnotatableEntity>> figureRefRegister = new HashMap<String, Set<AnnotatableEntity>>();
	
	
		
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

}