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

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
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
	private static final String ONLY_NUMBERED_TAXA_EXIST = "onlyNumberedTaxaExist";
	private static final String EXISTS = "exists";
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
		boolean onlyNumberedTaxaExist = checkAndRemoveAttributeValue(attributes, ONLY_NUMBERED_TAXA_EXIST, "true");
		state.setOnlyNumberedTaxaExist(onlyNumberedTaxaExist);
		
		PolytomousKey key = PolytomousKey.NewInstance();
		key.addTaxonomicScope(state.getCurrentTaxon());
		state.setCurrentKey(key);
		
		boolean isFirstCouplet = true;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				save(key, state);
				//reset state
				state.setCurrentKey(null);
				state.setOnlyNumberedTaxaExist(false);
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
				//just to be on the save side
				parentNode.refreshNodeNumbering();
			}
		}else if (isNotBlank(num)){
			UnmatchedLeadsKey unmatchedKey = UnmatchedLeadsKey.NewInstance(state.getCurrentKey(), num);
			Set<PolytomousKeyNode> nodes = state.getUnmatchedLeads().getNodes(unmatchedKey);
			for(PolytomousKeyNode nodeToMatch: nodes){
				for (PolytomousKeyNode childNode : childList){
					nodeToMatch.addChild(childNode);
					//just to be on the save side
					nodeToMatch.refreshNodeNumbering();
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
		//TODO needed only for data lineage
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
			String message = "CData ('%s') not be handled in <toCouplet>";
			message = String.format(message, cData);
			fireWarningEvent(message, next, 4);
		}
		UnmatchedLeadsKey unmatched = UnmatchedLeadsKey.NewInstance(state.getCurrentKey(), num);
		state.getUnmatchedLeads().addKey(unmatched, node);
	}

	private void handleToTaxon(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, PolytomousKeyNode node) throws XMLStreamException {
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String num = getAndRemoveAttributeValue(attributes, NUM);
		boolean taxonNotExists = checkAndRemoveAttributeValue(attributes, EXISTS, "false");
		
		String taxonCData = handleInnerToTaxon(state, reader, parentEvent, node).trim();
		
		String taxonKeyStr = makeTaxonKey(taxonCData, state.getCurrentTaxon(), parentEvent.getLocation());
		taxonNotExists = taxonNotExists || (isBlank(num) && state.isOnlyNumberedTaxaExist());
		if (taxonNotExists){
			NonViralName<?> name = createNameByCode(state, Rank.UNKNOWN_RANK());
			Taxon taxon = Taxon.NewInstance(name, null);
			taxon.getName().setTitleCache(taxonKeyStr, true);
			node.setTaxon(taxon);
		}else{
			UnmatchedLeadsKey unmatched = UnmatchedLeadsKey.NewInstance(num, taxonKeyStr);
			state.getUnmatchedLeads().addKey(unmatched, node);
//			String message = "The following key leads are unmatched: %s";
//			message = String.format(message, state.getUnmatchedLeads().toString());
//			fireWarningEvent(message, parentEvent, 6);
		}
		return;
	}
	
	
	/**
	 * Returns the taxon text of the toTaxon element and handles all annotations as ';'-concatenated modifying text.
	 * Footnote refs are not yet handled.
	 * @param state
	 * @param reader
	 * @param parentEvent
	 * @param node
	 * @return
	 * @throws XMLStreamException
	 */
	private String handleInnerToTaxon(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, PolytomousKeyNode node) throws XMLStreamException {
		String taxonText = "";
		String modifyingText = null;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (isNotBlank(modifyingText)){
					node.putModifyingText(getDefaultLanguage(state), modifyingText);
				}
				return taxonText;
			} else if (next.isCharacters()) {
				taxonText += next.asCharacters().getData();
			} else if (isStartingElement(next, ANNOTATION)) {
				String annotation = handleSimpleAnnotation(state, reader, next);
				modifyingText = CdmUtils.concat("; ", modifyingText, annotation);
			} else if (isStartingElement(next, FOOTNOTE_REF)) {
				handleNotYetImplementedElement(next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("Event has no closing tag");

	}

	/**
	 * Creates a string that represents the given taxon. The string will try to replace e.g.
	 * abbreviated genus epithets by its full name etc.
	 * @param strGoto
	 * @param taxon
	 * @param location 
	 * @return
	 */
	private String makeTaxonKey(String strGoto, Taxon taxon, Location location) {
		String result = "";
		if (strGoto == null){
			return "";
		}
		
		NonViralName<?> name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
		String strGenusName = name.getGenusOrUninomial();
		
		final String bracketPattern = "\\([^\\(\\)]*\\)";
		final String bracketPatternSomewhere = String.format(".*%s.*", bracketPattern);
		if (strGoto.matches(bracketPatternSomewhere)){
			fireWarningEvent("toTaxon has bracket: " + strGoto, makeLocationStr(location), 4);
			strGoto = strGoto.replaceAll(bracketPattern, "");  //replace all brackets
		}
		strGoto = strGoto.replaceAll("\\s+", " "); //replace multiple whitespaces by exactly one whitespace
		
		strGoto = strGoto.trim();
		strGoto = strGoto.replaceAll("\\s+\\.", "\\.");   // " ." may be created by bracket replacement
		strGoto = strGoto.replaceAll("\\.\\.", "\\.");   //replace
		
		String[] split = strGoto.split("\\s");
		//handle single epithets and markers
		for (int i = 0; i<split.length; i++){
			String single = split[i];
			if (isGenusAbbrev(single, strGenusName)){
				split[i] = strGenusName;
			}
			if (isInfraSpecificMarker(single)){
				String strSpeciesEpi = name.getSpecificEpithet();
				if (isBlank(result) && isNotBlank(strSpeciesEpi)){
					result += strGenusName + " " + strSpeciesEpi;
				}
			}
			result = (result + " " + split[i]).trim();
		}
		//remove trailing "." except for "sp."
		while (result.matches(".*(?<!sp)\\.$")){
			result = result.substring(0, result.length()-1).trim();
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
	
	/**
	 * Checks if <code>abbrev</code> is the short form for the genus name (strGenusName).
	 * Usually this is the case if <code>abbrev</code> is the first letter (optional with ".") 
	 * of strGenusName. But in older floras it may also be the first 2 or 3 letters (optional with dot).
	 * However, we allow only a maximum of 2 letters to be anambigous. In cases with 3 letters better 
	 * change the original markup data.
	 * @param single
	 * @param strGenusName
	 * @return
	 */
	private boolean isGenusAbbrev(String abbrev, String strGenusName) {
		if (! abbrev.matches("[A-Z][a-z]?\\.?")) {
			return false;
		}else if (abbrev.length() == 0 || strGenusName == null || strGenusName.length() == 0){
			return false; 
		}else{
			abbrev = abbrev.replace(".", "");
			return strGenusName.startsWith(abbrev);
//			boolean result = true;
//			for (int i = 0 ; i < abbrev.length(); i++){
//				result &= ( abbrev.charAt(i) == strGenusName.charAt(i));
//			}
//			return result;
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
			//just to be on the save side
			matchingNode.refreshNodeNumbering();
			state.getPolytomousKeyNodesToSave().add(matchingNode);
		}
		return matchingNodes;
	}

}
