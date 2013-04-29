/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.markup.UnmatchedLeads.UnmatchedLeadsKey;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 26.04.2013
 * 
 */
public class MarkupKeyImport  extends MarkupImportBase  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupKeyImport.class);
	
	private static final String COUPLET = "couplet";
	private static final String IS_SPOTCHARACTERS = "isSpotcharacters";
	private static final String KEYNOTES = "keynotes";
	private static final String KEY_TITLE = "keyTitle";
	private static final String QUESTION = "question";
	private static final String TEXT = "text";
	private static final String TO_COUPLET = "toCouplet";
	private static final String TO_KEY = "toKey";
	private static final String TO_TAXON = "toTaxon";

	
	public MarkupKeyImport(MarkupDocumentImport docImport) {
		super(docImport);
	}
	
	public void handleKey(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		// attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String isSpotcharacters = getAndRemoveAttributeValue(attributes, IS_SPOTCHARACTERS);
		if (isNotBlank(isSpotcharacters) ) {
			//TODO isSpotcharacters
			String message = "Attribute isSpotcharacters not yet implemented for <key>";
			fireWarningEvent(message, parentEvent, 4);
		}
		
		PolytomousKey key = PolytomousKey.NewInstance();
		key.addTaxonomicScope(state.getCurrentTaxon());
		state.setCurrentKey(key);
		
		boolean isFirstCouplet = true;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				save(key, state);
				state.setCurrentKey(null);
				return;
			} else if (isEndingElement(next, KEYNOTES)){
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, KEY_TITLE)) {
				handleKeyTitle(state, reader, next);
			} else if (isStartingElement(next, KEYNOTES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, COUPLET)) {
				PolytomousKeyNode node = null;
				if (isFirstCouplet){
					node = key.getRoot();
					isFirstCouplet = false;
				}
				handleCouplet(state, reader, next, node);
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<key> has no closing tag");
	}


	/**
	 * @param state
	 * @param reader
	 * @param key
	 * @param next
	 * @throws XMLStreamException
	 */
	private void handleKeyTitle(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		PolytomousKey key = state.getCurrentKey();
		String keyTitle = getCData(state, reader, parentEvent);
		String standardTitlesEngl = "(?i)(Key\\sto\\sthe\\s(genera|species|varieties|forms))";
		String standardTitlesFrench = "(?i)(Cl\u00e9\\sdes\\s(genres|esp\u00e8ces))";
		String standardTitles = standardTitlesEngl;
		if (state.getDefaultLanguage() != null && state.getDefaultLanguage().equals(Language.FRENCH())){
			standardTitles = standardTitlesFrench;
		}
		
		if (isNotBlank(keyTitle) ){
			if (!state.getConfig().isReplaceStandardKeyTitles() || ! keyTitle.matches(standardTitles)){
				key.setTitleCache(keyTitle, true);
			}
		}
	}
	

	private void handleCouplet(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, PolytomousKeyNode parentNode) throws XMLStreamException {
		String num = getOnlyAttribute(parentEvent, NUM, true);
		List<PolytomousKeyNode> childList = new ArrayList<PolytomousKeyNode>(); 
		
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				completeCouplet(state, parentEvent, parentNode, num, childList);
				return;
			} else if (isStartingElement(next, QUESTION)) {
				handleQuestion(state, reader, next, childList);
			} else if (isStartingElement(next, KEYNOTES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, KEYNOTES)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<couplet> has no closing tag");
	}
	

	/**
	 * @param state
	 * @param parentEvent
	 * @param parentNode
	 * @param num
	 * @param childList
	 */
	private void completeCouplet(MarkupImportState state, XMLEvent parentEvent,
			PolytomousKeyNode parentNode, String num, List<PolytomousKeyNode> childList) {
		if (parentNode != null){
			for (PolytomousKeyNode childNode : childList){
				parentNode.addChild(childNode);
			}
		}else if (isNotBlank(num)){
			UnmatchedLeadsKey unmatchedKey = UnmatchedLeadsKey.NewInstance(state.getCurrentKey(), num);
			Set<PolytomousKeyNode> nodes = state.getUnmatchedLeads().getNodes(unmatchedKey);
			for(PolytomousKeyNode nodeToMatch: nodes){
				for (PolytomousKeyNode childNode : childList){
					nodeToMatch.addChild(childNode);
				}
				state.getUnmatchedLeads().removeNode(unmatchedKey, nodeToMatch);
			}
		}else{
			String message = "Parent num could not be matched. Please check if num (%s) is correct";
			message = String.format(message, num);
			fireWarningEvent(message, parentEvent, 6);
		}
	}

	private void handleQuestion(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, List<PolytomousKeyNode> nodesList) throws XMLStreamException {
		// attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		//needed only for data lineage
		String questionNum = getAndRemoveRequiredAttributeValue(parentEvent, attributes, NUM);
		
		PolytomousKeyNode myNode = PolytomousKeyNode.NewInstance();
		myNode.setKey(state.getCurrentKey());  //to avoid NPE while computing num in PolytomousKeyNode in case this node is not matched correctly with a parent
		nodesList.add(myNode);
		
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return;
			} else if (isStartingElement(next, TEXT)) {
				String text = getCData(state, reader, next);
				Language language = state.getDefaultLanguage();
				if (language == null){
					language = Language.DEFAULT();
				}
				KeyStatement statement = KeyStatement.NewInstance(language, text);
				myNode.setStatement(statement);
			} else if (isStartingElement(next, COUPLET)) {
				//TODO test
				handleCouplet(state, reader, next, myNode);
			} else if (isStartingElement(next, TO_COUPLET)) {
				handleToCouplet(state, reader, next, myNode);
			} else if (isStartingElement(next, TO_TAXON)) {
				handleToTaxon(state, reader, next, myNode);
			} else if (isStartingElement(next, TO_KEY)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, TO_KEY)){
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, KEYNOTES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, KEYNOTES)){
				//TODO
				popUnimplemented(next.asEndElement());
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<question> has no closing tag");
	}

	private void handleToCouplet(MarkupImportState state, XMLEventReader reader, XMLEvent next, PolytomousKeyNode node) throws XMLStreamException {
		String num = getOnlyAttribute(next, NUM, true);
		String cData = getCData(state, reader, next, false);
		if (isNotBlank(cData) && ! cData.equals(num)){
			String message = "CData ('%s') not handled in <toCouplet>";
			message = String.format(message, cData);
			fireWarningEvent(message, next, 4);
		}
		UnmatchedLeadsKey unmatched = UnmatchedLeadsKey.NewInstance(state.getCurrentKey(), num);
		state.getUnmatchedLeads().addKey(unmatched, node);
	}

	private void handleToTaxon(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, PolytomousKeyNode node) throws XMLStreamException {
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String num = getAndRemoveAttributeValue(attributes, NUM);
		String taxonStr = getCData(state, reader, parentEvent, false).trim();
		if (taxonStr.endsWith(".")){
			taxonStr = taxonStr.substring(0, taxonStr.length()-1).trim();
		}
		//TODO ?
		taxonStr = makeTaxonKey(taxonStr, state.getCurrentTaxon());
		UnmatchedLeadsKey unmatched = UnmatchedLeadsKey.NewInstance(num, taxonStr);
		state.getUnmatchedLeads().addKey(unmatched, node);
		return;
	}
	
	
	private String makeTaxonKey(String strGoto, Taxon taxon) {
		String result = "";
		if (strGoto == null){
			return "";
		}
		
		NonViralName<?> name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
		String strGenusName = name.getGenusOrUninomial();
		
		
		strGoto = strGoto.replaceAll("\\([^\\(\\)]*\\)", "");  //replace all brackets
		strGoto = strGoto.replaceAll("\\s+", " "); //replace multiple whitespaces by exactly one whitespace
		
		strGoto = strGoto.trim();  
		String[] split = strGoto.split("\\s");
		for (int i = 0; i<split.length; i++){
			String single = split[i];
			if (isGenusAbbrev(single, strGenusName)){
				split[i] = strGenusName;
			}
			if (isInfraSpecificMarker(single)){
				String strSpeciesEpi = name.getSpecificEpithet();
				if (isBlank(result)){
					result += strGenusName + " " + strSpeciesEpi;
				}
			}
			result = (result + " " + split[i]).trim();
		}
		return result;
	}
	

	private boolean isInfraSpecificMarker(String single) {
		try {
			if (Rank.getRankByAbbreviation(single).isInfraSpecific()){
				return true;
			}else{
				return false;
			}
		} catch (UnknownCdmTypeException e) {
			return false;
		}
	}
	
	private boolean isGenusAbbrev(String single, String strGenusName) {
		if (! single.matches("[A-Z]\\.?")) {
			return false;
		}else if (single.length() == 0 || strGenusName == null || strGenusName.length() == 0){
			return false; 
		}else{
			return single.charAt(0) == strGenusName.charAt(0);
		}
	}
	
	
//******************************** recognize nodes ***********/

	public void makeKeyNodes(MarkupImportState state, XMLEvent event, String taxonTitle) {
		Taxon taxon = state.getCurrentTaxon();
		String num = state.getCurrentTaxonNum();
		
		String nameString = CdmBase.deproxy(taxon.getName(), NonViralName.class).getNameCache();
//		String nameString = taxonTitle;
		
		//try to find matching lead nodes 
		UnmatchedLeadsKey leadsKey = UnmatchedLeadsKey.NewInstance(num, nameString);
		Set<PolytomousKeyNode> matchingNodes = handleMatchingNodes(state, taxon, leadsKey);
		
		if (num != null){//same without using the num
			UnmatchedLeadsKey noNumLeadsKey = UnmatchedLeadsKey.NewInstance("", nameString);
			Set<PolytomousKeyNode> noNumMatchingNodes = handleMatchingNodes(state, taxon, noNumLeadsKey);
			if(noNumMatchingNodes.size() > 0){
				String message ="Taxon matches additional key node when not considering <num> attribute in taxontitle. This may be correct but may also indicate an error.";
				fireWarningEvent(message, event, 1);
			}
		}
		//report missing match, if num exists
		if (matchingNodes.isEmpty() && num != null){
			String message = "Taxon has <num> attribute in taxontitle but no matching key nodes exist: %s, Key: %s";
			message = String.format(message, num, leadsKey.toString());
			fireWarningEvent(message, event, 1);
		}
		
	}
	
	private Set<PolytomousKeyNode> handleMatchingNodes(MarkupImportState state, Taxon taxon, UnmatchedLeadsKey leadsKey) {
		Set<PolytomousKeyNode> matchingNodes = state.getUnmatchedLeads().getNodes(leadsKey);
		for (PolytomousKeyNode matchingNode : matchingNodes){
			state.getUnmatchedLeads().removeNode(leadsKey, matchingNode);
			matchingNode.setTaxon(taxon);
			state.getPolytomousKeyNodesToSave().add(matchingNode);
		}
		return matchingNodes;
	}

}
