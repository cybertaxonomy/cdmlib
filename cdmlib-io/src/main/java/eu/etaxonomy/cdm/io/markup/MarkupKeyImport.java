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
import java.util.UUID;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.io.markup.UnmatchedLeads.UnmatchedLeadsKey;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 26.04.2013
 */
public class MarkupKeyImport  extends MarkupImportBase  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupKeyImport.class);


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
		key.addPrimaryTaxonomicSource(state.getConfig().getSourceReference(), null);
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
		List<PolytomousKeyNode> childList = new ArrayList<>();

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				completeCouplet(state, parentEvent, parentNode, num, childList);
				return;
			} else if (next.isCharacters()){
				handleNotYetImplementedCharacters(next);
				//work in progress from pesiimport2, not sure if this works
//				String mainQuestion = next.asCharacters().getData();
//				mainQuestion = mainQuestion.replaceAll("\\s+", " ").trim();
//				KeyStatement question = KeyStatement.NewInstance(mainQuestion);
//				if (parentNode != null){ parentNode.setStatement(question);}  //work in progress
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
					try {
						nodeToMatch.addChild(childNode);
						//just to be on the save side
						nodeToMatch.refreshNodeNumbering();
					} catch (Exception e) {
						String message = "An exception occurred when trying to add a key node child or to referesh the node numbering: " + e.getMessage();
						fireWarningEvent(message, parentEvent, 6);
					}
				}
				state.getUnmatchedLeads().removeNode(unmatchedKey, nodeToMatch);
			}
		}else{
			String message = "Parent num could not be matched. Please check if num (%s) is correct";
			message = String.format(message, num);
			fireWarningEvent(message, parentEvent, 6);
		}
	}

	private void handleQuestion(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent,
	        List<PolytomousKeyNode> nodesList) throws XMLStreamException {
		// attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		//TODO needed only for data lineage
		String questionNum = getAndRemoveRequiredAttributeValue(parentEvent, attributes, NUM);

		PolytomousKeyNode myNode = PolytomousKeyNode.NewInstance();
		myNode.setKey(state.getCurrentKey());  //to avoid NPE while computing num in PolytomousKeyNode in case this node is not matched correctly with a parent
		nodesList.add(myNode);
		int countToTaxon = 0;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return;
			} else if (isStartingElement(next, TEXT)) {
				String text = getCData(state, reader, next);
				KeyStatement statement = KeyStatement.NewInstance(getDefaultLanguage(state), text);
				myNode.setStatement(statement);
			} else if (isStartingElement(next, COUPLET)) {
				fireWarningEvent("Check if toCouplet in question is implemented correctly", next, 4);
				handleCouplet(state, reader, next, myNode);
			} else if (isStartingElement(next, TO_COUPLET)) {
				handleToCouplet(state, reader, next, myNode);
			} else if (isStartingElement(next, TO_TAXON)) {
				if (countToTaxon == 0){
				    handleToTaxon(state, reader, next, myNode);
				}else{
				    if(countToTaxon == 1){
				        //rearrange first taxon  //similar to PKNode.setOrAddTaxon(taxon) but the later can't be used here
				        //TODO this does not yet move the unmatched taxon keys to the new node
				        fireWarningEvent("Multiple toTaxon requires manual adjustment", next, 6);
				        Taxon firstTaxon = myNode.removeTaxon();
				        PolytomousKeyNode firstChildNode = PolytomousKeyNode.NewInstance();
				        firstChildNode.setTaxon(firstTaxon);
	                    myNode.addChild(firstChildNode);
				    }
				    PolytomousKeyNode childNode = PolytomousKeyNode.NewInstance();
			        myNode.addChild(childNode);
				    handleToTaxon(state, reader, next, childNode);
				}
				countToTaxon++;
			} else if (isStartingElement(next, TO_KEY)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, KEYNOTES)) {
				handleAmbigousManually(state, reader, next.asStartElement());
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
		boolean taxonNotExists = checkAndRemoveAttributeValue(attributes, EXISTS, "false");

		String taxonCData = handleInnerToTaxon(state, reader, parentEvent, node).trim();

		String taxonKeyStr = makeTaxonKey(taxonCData, state.getCurrentTaxon(), parentEvent.getLocation());

		taxonNotExists = taxonNotExists || (isBlank(num) && state.isOnlyNumberedTaxaExist());
		if (taxonNotExists){
			INonViralName name = createNameByCode(state, Rank.UNKNOWN_RANK());
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
	private String handleInnerToTaxon(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent,
	        PolytomousKeyNode node) throws XMLStreamException {
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

		INonViralName name = taxon.getName();
		String strGenusName = name.getGenusOrUninomial();

		String normalized = normalizeKeyString(strGoto, location);

		String[] split = normalized.split("\\s");
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
		result = removeTrailingDot(result);
		return result;
	}


	final static String bracketPattern = "\\([^\\(\\)]*\\)";
    final static String bracketPatternSomewhere = String.format(".*%s.*", bracketPattern);

    /**
     * @param strGoto
     * @param location
     * @return
     */
    private String normalizeKeyString(String strGoto, Location location) {
        String result = strGoto;
		if (result.matches(bracketPatternSomewhere)){
			fireWarningEvent("keyString has bracket (uncritical for fullname matching): " + result, makeLocationStr(location), 4);
			result = result.replaceAll(bracketPattern, "");  //replace all brackets
		}
		result = result.replaceAll("\\s+", " "); //replace multiple whitespaces by exactly one whitespace

		result = result.trim();
		result = result.replaceAll("\\s+\\.", "\\.");   // " ." may be created by bracket replacement
		result = result.replaceAll("\\.\\.", "\\.");   //replace
        result = result.replace(UTF8.HYBRID.toString(), "x ");
		return result;
    }


	private boolean isInfraSpecificMarker(String single) {
		try {
			if (Rank.getRankByIdInVoc(single).isInfraSpecific()){
				return true;
			}else{
				return false;
			}
		} catch (UnknownCdmTypeException e) {
			return false;
		}
	}

//******************************** recognize nodes ***********/

	public void makeKeyNodes(MarkupImportState state, XMLEvent event, String taxonTitle) {
		Taxon taxon = state.getCurrentTaxon();
		String num = state.getCurrentTaxonNum();

		INonViralName nvn = taxon.getName();
		String nameString = nvn.getNameCache();
		nameString = normalizeKeyString(nameString, event.getLocation());
        nameString = removeTrailingDot(nameString);
        try{
            if (nameString.contains(":")){
                System.out.println(":");
                UUID.fromString(nameString);
                System.out.println("Here we have a uuid: " + nameString + "for" + nvn.getTitleCache());
            }
        }catch(Exception e){
        }

		//try to find matching lead nodes
		UnmatchedLeadsKey leadsKey = UnmatchedLeadsKey.NewInstance(num, nameString);
		Set<PolytomousKeyNode> matchingNodes = handleMatchingNodes(state, event, taxon, leadsKey);

		if (num != null){//same without using the num
			UnmatchedLeadsKey noNumLeadsKey = UnmatchedLeadsKey.NewInstance("", nameString);
			Set<PolytomousKeyNode> noNumMatchingNodes = handleMatchingNodes(state, event, taxon, noNumLeadsKey);
			if(noNumMatchingNodes.size() > 0){
				String message ="Taxon matches additional key node when not considering <num> attribute in taxontitle. This may be correct but may also indicate an error.";
				fireWarningEvent(message, event, 1);
			}
		}
		//report missing match, if num exists
		if (num != null && matchingNodes.isEmpty() /* TODO redo comment && num != null (later DONE) */){
			String message = "Taxon has <num> attribute in taxontitle but no matching key nodes exist: %s, Key: %s";
			message = String.format(message, num, leadsKey.toString());
			fireWarningEvent(message, event, 1);
		}
	}

	/**
     * remove trailing "." except for "sp."
     * @param str
     * @return
     */
    private String removeTrailingDot(String str) {
        while (str.matches(".*(?<!sp)\\.$")){
            str = str.substring(0, str.length()-1).trim();
        }
        return str;
    }

    private Set<PolytomousKeyNode> handleMatchingNodes(MarkupImportState state, XMLEvent event, Taxon taxon, UnmatchedLeadsKey leadsKey) {
		Set<PolytomousKeyNode> matchingNodes = state.getUnmatchedLeads().getNodes(leadsKey);
		for (PolytomousKeyNode matchingNode : matchingNodes){
			state.getUnmatchedLeads().removeNode(leadsKey, matchingNode);
			matchingNode.setTaxon(taxon);
			//just to be on the save side
			try{
				matchingNode.refreshNodeNumbering();
			} catch (Exception e) {
				String message = "An exception occurred when trying to referesh the node numbering: " + e.getMessage();
				fireWarningEvent(message, event, 6);
			}
			state.getPolytomousKeyNodesToSave().add(matchingNode);
		}
		return matchingNodes;
	}
}
