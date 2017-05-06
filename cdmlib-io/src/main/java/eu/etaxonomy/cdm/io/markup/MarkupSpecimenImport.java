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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.SpecimenTypeParser;
import eu.etaxonomy.cdm.strategy.parser.SpecimenTypeParser.TypeInfo;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @created 30.05.2012
 *
 */
public class MarkupSpecimenImport extends MarkupImportBase  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupSpecimenImport.class);

	private static final String ALTERNATIVE_COLLECTION_TYPE_STATUS = "alternativeCollectionTypeStatus";
	private static final String ALTERNATIVE_COLLECTOR = "alternativeCollector";
	private static final String ALTERNATIVE_FIELD_NUM = "alternativeFieldNum";
	private static final String COLLECTOR = "collector";
	private static final String COLLECTION = "collection";
	private static final String COLLECTION_AND_TYPE = "collectionAndType";
	private static final String COLLECTION_TYPE_STATUS = "collectionTypeStatus";
	private static final String DAY = "day";
	private static final String DESTROYED = "destroyed";
	private static final String FIELD_NUM = "fieldNum";
	private static final String FULL_TYPE = "fullType";
	private static final String FULL_DATE = "fullDate";
	private static final String GATHERING_NOTES = "gatheringNotes";
	private static final String LOST = "lost";
	private static final String MONTH = "month";
	private static final String SUB_GATHERING = "subGathering";
	private static final String NOT_FOUND = "notFound";
	private static final String NOT_SEEN = "notSeen";
	private static final String ORIGINAL_DETERMINATION = "originalDetermination";

	private static final String UNKNOWN = "unknown";
	private static final String YEAR = "year";



	public MarkupSpecimenImport(MarkupDocumentImport docImport) {
		super(docImport);
	}


	public void handleSpecimenType(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent,
				HomotypicalGroup homotypicalGroup) throws XMLStreamException {

		// attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String typeStatus = getAndRemoveAttributeValue(attributes, TYPE_STATUS);
		String notSeen = getAndRemoveAttributeValue(attributes, NOT_SEEN);
		String unknown = getAndRemoveAttributeValue(attributes, UNKNOWN);
		String notFound = getAndRemoveAttributeValue(attributes, NOT_FOUND);
		String destroyed = getAndRemoveAttributeValue(attributes, DESTROYED);
		String lost = getAndRemoveAttributeValue(attributes, LOST);
		checkNoAttributes(attributes, parentEvent);
		if (isNotBlank(typeStatus)) {
			// TODO
			// currently not needed
			fireWarningEvent("Type status not yet used", parentEvent, 4);
		}
		if (isNotBlank(notSeen)) {
			handleNotYetImplementedAttribute(attributes, NOT_SEEN, parentEvent);
		}
		if (isNotBlank(unknown)) {
			handleNotYetImplementedAttribute(attributes, UNKNOWN, parentEvent);
		}
		if (isNotBlank(notFound)) {
			handleNotYetImplementedAttribute(attributes, NOT_FOUND, parentEvent);
		}
		if (isNotBlank(destroyed)) {
			handleNotYetImplementedAttribute(attributes, DESTROYED, parentEvent);
		}
		if (isNotBlank(lost)) {
			handleNotYetImplementedAttribute(attributes, LOST, parentEvent);
		}

		INonViralName firstName = null;
		Set<TaxonNameBase> names = homotypicalGroup.getTypifiedNames();
		if (names.isEmpty()) {
			String message = "There is no name in a homotypical group. Can't create the specimen type";
			fireWarningEvent(message, parentEvent, 8);
		} else {
			firstName = CdmBase.deproxy(names.iterator().next());
		}

		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
		state.setFirstSpecimenInFacade(true);
		String text = "";
		state.resetCollectionAndType();
		state.setSpecimenType(true);
		boolean isFullType = false;
		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (! isFullType){
					makeSpecimenType(state, facade, text, state.getCollectionAndType(), firstName, parentEvent);
				}
				state.setSpecimenType(false);
				state.resetCollectionAndType();
				state.setFirstSpecimenInFacade(false);
				return;
			} else if (isStartingElement(next, FULL_TYPE)) {
				handleAmbigousManually(state, reader, next.asStartElement());
				isFullType = true;
			} else if (isStartingElement(next, TYPE_STATUS)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, GATHERING)) {
				handleGathering(state, reader, next, facade);
			} else if (isStartingElement(next, ORIGINAL_DETERMINATION)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, SPECIMEN_TYPE)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, COLLECTION_AND_TYPE)) {
				String colAndType = getCData(state, reader, next, true);
				state.addCollectionAndType(colAndType);
			} else if (isStartingElement(next, CITATION)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, NOTES)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, ANNOTATION)) {
				handleNotYetImplementedElement(next);
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("Specimen type has no closing tag");
	}



	private void makeSpecimenType(MarkupImportState state, DerivedUnitFacade facade, String text, String collectionAndType,
			INonViralName name, XMLEvent parentEvent) {
		text = text.trim();
		if (isBlank(text) || isPunctuation(text)){
			//do nothing
		}else{
			String message = "Text '%s' not handled for <SpecimenType>";
			this.fireWarningEvent(String.format(message, text), parentEvent, 4);
		}

		if (makeFotgSpecimenType(state, collectionAndType, facade, name, parentEvent) || state.getConfig().isUseFotGSpecimenTypeCollectionAndTypeOnly()){
			return;
		}else{
			// remove brackets
			if (collectionAndType.matches("^\\(.*\\)\\.?$")) {
				collectionAndType = collectionAndType.replaceAll("\\.$", "");
				collectionAndType = collectionAndType.substring(1, collectionAndType.length() - 1);
			}

			String[] splitsSemi = collectionAndType.split("[;]");
            for (String splitSemi : splitsSemi) {
                String[] splitKomma = splitSemi.split("[,]");
                TypeInfo lastTypeInfo = null;
                for (String str : splitKomma) {
                    str = str.trim();
        			boolean addToAllNamesInGroup = true;
        			TypeInfo typeInfo = makeSpecimenTypeTypeInfo(state, str, lastTypeInfo, parentEvent);
        			SpecimenTypeDesignationStatus typeStatus = typeInfo.status;
        			Collection collection = this.getCollection(state, typeInfo.collectionString);

        			// TODO improve cache strategy handling
        			DerivedUnit typeSpecimen;
        			if (state.isFirstSpecimenInFacade()){
        			    state.setFirstSpecimenInFacade(false);
        			    typeSpecimen = facade.innerDerivedUnit();
        			    typeSpecimen.setCollection(collection);
        			}else{
        			    typeSpecimen = facade.addDuplicate(collection, null, null, null, null);
        			}
        			typeSpecimen.setCacheStrategy(new DerivedUnitFacadeCacheStrategy());
        			name.addSpecimenTypeDesignation(typeSpecimen, typeStatus,
        			        null, null, null, typeInfo.notDesignated, addToAllNamesInGroup);
        			handleNotSeen(state, typeSpecimen, typeInfo);
        			lastTypeInfo = typeInfo;
                }
			}
		}
	}


	/**
     * @param state
     * @param typeSpecimen
     * @param typeInfo
     */
    private void handleNotSeen(MarkupImportState state, DerivedUnit typeSpecimen, TypeInfo typeInfo) {
        if (typeInfo.notSeen){
            String text = "n.v. for " + state.getConfig().getSourceReference().getAbbrevTitleCache();
            typeSpecimen.addAnnotation(Annotation.NewInstance(text, AnnotationType.EDITORIAL(), getDefaultLanguage(state)));
            if(state.getConfig().getSpecimenNotSeenMarkerTypeUuid() != null){
                UUID uuidNotSeenMarker = state.getConfig().getSpecimenNotSeenMarkerTypeUuid();
                String markerTypeNotSeenLabel = state.getConfig().getSpecimenNotSeenMarkerTypeLabel();
                markerTypeNotSeenLabel = markerTypeNotSeenLabel == null ? "Not seen" : markerTypeNotSeenLabel;
                MarkerType notSeenMarkerType = getMarkerType(state, uuidNotSeenMarker, markerTypeNotSeenLabel, markerTypeNotSeenLabel, null, null);
                Marker marker = Marker.NewInstance(notSeenMarkerType, true);
                typeSpecimen.addMarker(marker);
            }
        }
    }


    private Pattern fotgTypePattern = null;
	/**
	 * Implemented for Flora of the Guyanas this may include duplicated code from similar places
	 * @param state
	 * @param collectionAndTypeOrig
	 * @param facade
	 * @param name
	 * @param parentEvent
	 * @return
	 */
	private boolean makeFotgSpecimenType(MarkupImportState state, final String collectionAndTypeOrig, DerivedUnitFacade facade, INonViralName name, XMLEvent parentEvent) {
		String collectionAndType = collectionAndTypeOrig;

		String notDesignatedRE = "not\\s+designated";
		String designatedByRE = "\\s*\\(((designated\\s+by\\s+|according\\s+to\\s+)[^\\)]+|here\\s+designated)\\)";
		String typesRE = "(holotype|isotypes?|neotype|isoneotype|syntype|lectotype|isolectotypes?|typ\\.\\scons\\.,?)";
		String collectionRE = "[A-Z\\-]{1,5}!?";
		String collectionsRE = String.format("%s(,\\s+%s)*",collectionRE, collectionRE);
		String addInfoRE = "(not\\s+seen|(presumed\\s+)?destroyed)";
		String singleTypeTypeRE = String.format("(%s\\s)?%s(,\\s+%s)*", typesRE, collectionsRE, addInfoRE);
		String allTypesRE = String.format("(\\(not\\s+seen\\)|\\(%s([,;]\\s%s)?\\))", singleTypeTypeRE, singleTypeTypeRE);
		String designatedRE = String.format("%s(%s)?", allTypesRE, designatedByRE);
		if (fotgTypePattern == null){

			String pattern = String.format("(%s|%s)", notDesignatedRE, designatedRE );
			fotgTypePattern = Pattern.compile(pattern);
		}
		Matcher matcher = fotgTypePattern.matcher(collectionAndType);

		if (matcher.matches()){
		    fireWarningEvent("Try to synchronize type handling (at least creation) with standard type handling. E.g. use TypeInfo and according algorithms", parentEvent, 2);
			if (collectionAndType.matches(notDesignatedRE)){
				SpecimenTypeDesignation desig = SpecimenTypeDesignation.NewInstance();
				desig.setNotDesignated(true);
//				name.addSpecimenTypeDesignation(typeSpecimen, status, citation, citationMicroReference, originalNameString, isNotDesignated, addToAllHomotypicNames)
				name.addTypeDesignation(desig, true);
			}else if(collectionAndType.matches(designatedRE)){
				String designatedBy = null;
				Matcher desigMatcher = Pattern.compile(designatedByRE).matcher(collectionAndType);
				boolean hasDesignatedBy = desigMatcher.find();
				if (hasDesignatedBy){
					designatedBy = desigMatcher.group(0);
					collectionAndType = collectionAndType.replace(designatedBy, "");
				}

				//remove brackets
				collectionAndType = collectionAndType.substring(1, collectionAndType.length() -1);
				List<String> singleTypes = new ArrayList<String>();
				Pattern singleTypePattern = Pattern.compile("^" + singleTypeTypeRE);
				matcher = singleTypePattern.matcher(collectionAndType);
				while (matcher.find()){
					String match = matcher.group(0);
					singleTypes.add(match);
					collectionAndType = collectionAndType.substring(match.length());
					if (!collectionAndType.isEmpty()){
						collectionAndType = collectionAndType.substring(1).trim();
					}else{
						break;
					}
					matcher = singleTypePattern.matcher(collectionAndType);
				}

				List<SpecimenTypeDesignation> designations = new ArrayList<SpecimenTypeDesignation>();

				//single types
				for (String singleTypeOrig : singleTypes){
					String singleType = singleTypeOrig;
					//type
					Pattern typePattern = Pattern.compile("^" + typesRE);
					matcher = typePattern.matcher(singleType);
					SpecimenTypeDesignationStatus typeStatus = null;
					if (matcher.find()){
						String typeStr = matcher.group(0);
						singleType = singleType.substring(typeStr.length()).trim();
						try {
							typeStatus = SpecimenTypeParser.parseSpecimenTypeStatus(typeStr);
						} catch (UnknownCdmTypeException e) {
							fireWarningEvent("specimen type not recognized. Use generic type instead", parentEvent, 4);
							typeStatus = SpecimenTypeDesignationStatus.TYPE();
							//TODO use also type info from state
						}
					}else{
						typeStatus = SpecimenTypeDesignationStatus.TYPE();
						//TODO use also type info from state
					}


					//collection
					Pattern collectionPattern = Pattern.compile("^" + collectionsRE);
					matcher = collectionPattern.matcher(singleType);
					String[] collectionStrings = new String[0];
					if (matcher.find()){
						String collectionStr = matcher.group(0);
						singleType = singleType.substring(collectionStr.length());
						collectionStr = collectionStr.replace("(", "").replace(")", "").replaceAll("\\s", "");
						collectionStrings = collectionStr.split(",");
					}

					//addInfo
					if (!singleType.isEmpty() && singleType.startsWith(", ")){
						singleType = singleType.substring(2);
					}

					boolean notSeen = false;
					if (singleType.equals("not seen")){
						singleType = singleType.replace("not seen", "");
						notSeen = true;
					}
					if (singleType.startsWith("not seen, ")){
						singleType = singleType.replace("not seen, ", "");
						notSeen = true;
					}
					boolean destroyed = false;
					if (singleType.equals("destroyed")){
						destroyed = true;
						singleType = singleType.replace("destroyed", "");
					}
					boolean presumedDestroyed = false;
					if (singleType.equals("presumed destroyed")){
						presumedDestroyed = true;
						singleType = singleType.replace("presumed destroyed", "");
					}
					boolean hasAddInfo = notSeen || destroyed || presumedDestroyed;


					if (!singleType.isEmpty()){
						String message = "SingleType was not fully read. Remaining: " + singleType + ". Original singleType was: " + singleTypeOrig;
						fireWarningEvent(message, parentEvent, 6);
						System.out.println(message);
					}

					if (collectionStrings.length > 0){
						boolean isFirst = true;
						for (String collStr : collectionStrings){
							Collection collection = getCollection(state, collStr);
							DerivedUnit unit = isFirst ? facade.innerDerivedUnit()
									: facade.addDuplicate(collection, null, null, null, null);
							SpecimenTypeDesignation desig = SpecimenTypeDesignation.NewInstance();
							designations.add(desig);
							desig.setTypeSpecimen(unit);
							desig.setTypeStatus(typeStatus);
							handleSpecimenTypeAddInfo(state, notSeen, destroyed,
									presumedDestroyed, desig);
							name.addTypeDesignation(desig, true);
							isFirst = false;
						}
					}else if (hasAddInfo){  //handle addInfo if no collection data available
						SpecimenTypeDesignation desig = SpecimenTypeDesignation.NewInstance();
						designations.add(desig);
						desig.setTypeStatus(typeStatus);
						handleSpecimenTypeAddInfo(state, notSeen, destroyed,
								presumedDestroyed, desig);
						name.addTypeDesignation(desig, true);
					}else{
						fireWarningEvent("No type designation could be created as collection info was not recognized", parentEvent, 4);
					}
				}

				if (designatedBy != null){
					if (designations.size() != 1){
						fireWarningEvent("Size of type designations is not exactly 1, which is expected for 'designated by'", parentEvent, 2);
					}
					designatedBy = designatedBy.trim();
					if (designatedBy.startsWith("(") && designatedBy.endsWith(")") ){
						designatedBy = designatedBy.substring(1, designatedBy.length() - 1);
					}

					for (SpecimenTypeDesignation desig : designations){
						if (designatedBy.startsWith("designated by")){
							String titleCache = designatedBy.replace("designated by", "").trim();
							Reference reference = ReferenceFactory.newGeneric();
							reference.setTitleCache(titleCache, true);
							desig.setCitation(reference);
							//in future we could also try to parse it automatically
							fireWarningEvent("MANUALLY: Designated by should be parsed manually: " + titleCache, parentEvent, 1);
						}else if (designatedBy.equals("designated here")){
							Reference ref = state.getConfig().getSourceReference();
							desig.setCitation(ref);
							fireWarningEvent("MANUALLY: Microcitation should be added to 'designated here", parentEvent, 1);
						}else if (designatedBy.startsWith("according to")){
							String annotationStr = designatedBy.replace("according to", "").trim();
							Annotation annotation = Annotation.NewInstance(annotationStr, AnnotationType.EDITORIAL(), Language.ENGLISH());
							desig.addAnnotation(annotation);
						}else{
							fireWarningEvent("Designated by does not match known pattern: " + designatedBy, parentEvent, 6);
						}
					}
				}
			}else{
				fireWarningEvent("CollectionAndType unexpectedly not matching: " + collectionAndTypeOrig, parentEvent, 6);
			}
			return true;
		}else{
			if (state.getConfig().isUseFotGSpecimenTypeCollectionAndTypeOnly()){
				fireWarningEvent("NO MATCH: " + collectionAndTypeOrig, parentEvent, 4);
			}
			return false;
		}

//		// remove brackets
//		if (collectionAndType.matches("^\\(.*\\)\\.?$")) {
//			collectionAndType = collectionAndType.replaceAll("\\.$", "");
//			collectionAndType = collectionAndType.substring(1, collectionAndType.length() - 1);
//		}
//
//		String[] split = collectionAndType.split("[;,]");
//		for (String str : split) {
//			str = str.trim();
//			boolean addToAllNamesInGroup = true;
//			TypeInfo typeInfo = makeSpecimenTypeTypeInfo(str, parentEvent);
//			SpecimenTypeDesignationStatus typeStatus = typeInfo.status;
//			Collection collection = this.getCollection(state, typeInfo.collectionString);
//
//			// TODO improve cache strategy handling
//			DerivedUnit typeSpecimen = facade.addDuplicate(collection, null, null, null, null);
//			typeSpecimen.setCacheStrategy(new DerivedUnitFacadeCacheStrategy());
//			name.addSpecimenTypeDesignation(typeSpecimen, typeStatus, null, null, null, false, addToAllNamesInGroup);
//		}
	}


	/**
	 * @param notSeen
	 * @param destroyed
	 * @param presumedDestroyed
	 * @param desig
	 */
	private void handleSpecimenTypeAddInfo(MarkupImportState state, boolean notSeen, boolean destroyed,
			boolean presumedDestroyed, SpecimenTypeDesignation desig) {
		DerivedUnit specimen = desig.getTypeSpecimen();
		AnnotatableEntity annotEntity = specimen != null ? specimen : desig;

	    if (notSeen){
			UUID uuidNotSeenMarker = MarkupTransformer.uuidMarkerNotSeen;
			MarkerType notSeenMarkerType = getMarkerType(state, uuidNotSeenMarker, "Not seen", "Not seen", null, null);
			Marker marker = Marker.NewInstance(notSeenMarkerType, true);
			annotEntity.addMarker(marker);
			fireWarningEvent("not seen not yet implemented", "handleSpecimenTypeAddInfo", 4);
		}
		if (destroyed){
			UUID uuidDestroyedMarker = MarkupTransformer.uuidMarkerDestroyed;
			MarkerType destroyedMarkerType = getMarkerType(state, uuidDestroyedMarker, "Destroyed", "Destroyed", null, null);
			Marker marker = Marker.NewInstance(destroyedMarkerType, true);
			annotEntity.addMarker(marker);
			fireWarningEvent("'destroyed' not yet fully implemented", "handleSpecimenTypeAddInfo", 4);
		}
		if (presumedDestroyed){
			Annotation annotation = Annotation.NewInstance("presumably destroyed", Language.ENGLISH());
			annotation.setAnnotationType(AnnotationType.EDITORIAL());
			annotEntity.addAnnotation(annotation);
		}
	}


	private TypeInfo makeSpecimenTypeTypeInfo(MarkupImportState state, String originalString, TypeInfo lastTypeInfo, XMLEvent event) {
		TypeInfo result = new TypeInfo();
		if ("not designated".equals(originalString)){
			result.notDesignated = true;
			return result;
		}
		List<String> knownCollections = state.getConfig().getKnownCollections();
		for (String knownCollection:knownCollections){
		    if (originalString.contains(knownCollection)){
		        result.collectionString = knownCollection;
		        originalString = originalString.replace(knownCollection, "").trim();
		        break;
		    }
		}

		String[] split = originalString.split("(?<!not)\\s+");

		String unrecognizedTypeParts = null;
		for (String str : split) {
		    //holo/lecto/iso ...
			if (str.matches(SpecimenTypeParser.typeTypePattern)) {
				SpecimenTypeDesignationStatus status;
				try {
					status = SpecimenTypeParser.parseSpecimenTypeStatus(str);
				} catch (UnknownCdmTypeException e) {
					String message = "Specimen type status '%s' not recognized by parser";
					fireWarningEvent(String.format(message, str), event, 4);
					status = null;
				}
                if (result.status != null){
                    String message = "More than 1 status string found: " + originalString;
                    fireWarningEvent(message, event, 4);
                }
				result.status = status;
			} else if (str.matches(SpecimenTypeParser.collectionPattern)) {
				if (result.collectionString != null){
				    String message = "More than 1 collection string found: " + originalString;
                    fireWarningEvent(message, event, 4);
				}
			    result.collectionString = str;
			} else if (str.matches(SpecimenTypeParser.notSeen)) {
                if (result.notSeen){
                    String message = "More than 1 'not seen' string found: " + originalString;
                    fireWarningEvent(message, event, 4);
                }
                result.notSeen = true;
            } else {
                unrecognizedTypeParts = CdmUtils.concat(" ", unrecognizedTypeParts, str);
			}
			if (result.status == null && lastTypeInfo != null && lastTypeInfo.status != null){
			    result.status = lastTypeInfo.status;
			}
		}
		if(isNotBlank(unrecognizedTypeParts)){
		    String message = "Type parts '%s' could not be recognized";
            fireWarningEvent(String.format(message, unrecognizedTypeParts), event, 2);
		}
		return result;
	}


	private void handleGathering(MarkupImportState state, XMLEventReader readerOrig, XMLEvent parentEvent , DerivedUnitFacade facade) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		boolean hasCollector = false;
		boolean hasFieldNum = false;

		LookAheadEventReader reader = new LookAheadEventReader(parentEvent.asStartElement(), readerOrig);

		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (! hasCollector){
					if (state.getCurrentCollector() == null){
						checkMandatoryElement(hasCollector,parentEvent.asStartElement(), COLLECTOR);
					}else{
						facade.setCollector(state.getCurrentCollector());
					}
				}
				checkMandatoryElement(hasFieldNum,parentEvent.asStartElement(), FIELD_NUM);
				return;
			}else if (isStartingElement(next, COLLECTOR)) {
				hasCollector = true;
				String collectorStr = getCData(state, reader, next);
				TeamOrPersonBase<?> collector = createCollector(state, collectorStr);
				facade.setCollector(collector);
				state.setCurrentCollector(collector);
			} else if (isStartingElement(next, ALTERNATIVE_COLLECTOR)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, FIELD_NUM)) {
				hasFieldNum = true;
				String fieldNumStr = getCData(state, reader, next);
				facade.setFieldNumber(fieldNumStr);
			} else if (isStartingElement(next, ALTERNATIVE_FIELD_NUM)) {
				handleAlternativeFieldNumber(state, reader, next, facade.innerFieldUnit());
			} else if (isStartingElement(next, COLLECTION_TYPE_STATUS)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, COLLECTION_AND_TYPE)) {
				handleGatheringCollectionAndType(state, reader, next, facade);
			} else if (isStartingElement(next, ALTERNATIVE_COLLECTION_TYPE_STATUS)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, SUB_GATHERING)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, COLLECTION)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, LOCALITY)) {
				handleLocality(state, reader, next, facade);
			} else if (isStartingElement(next, FULL_NAME)) {
				Rank defaultRank = Rank.SPECIES(); // can be any
				INonViralName nvn = createNameByCode(state, defaultRank);
				handleFullName(state, reader, nvn, next);
				TaxonNameBase<?,?> name = TaxonNameBase.castAndDeproxy(nvn);
				DeterminationEvent.NewInstance(name, facade.innerDerivedUnit() != null ? facade.innerDerivedUnit() : facade.innerFieldUnit());
			} else if (isStartingElement(next, DATES)) {
				TimePeriod timePeriod = handleDates(state, reader, next);
				facade.setGatheringPeriod(timePeriod);
			} else if (isStartingElement(next, GATHERING_NOTES)) {
				handleAmbigousManually(state, reader, next.asStartElement());
			} else if (isStartingElement(next, NOTES)) {
				handleNotYetImplementedElement(next);
			}else if (next.isCharacters()) {
				String text = next.asCharacters().getData().trim();
				if (isPunctuation(text)){
					//do nothing
				}else if (state.isSpecimenType() && charIsSimpleType(text) ){
						//do nothing
				}else if ( (text.equals("=") || text.equals("(") ) && reader.nextIsStart(ALTERNATIVE_FIELD_NUM)){
					//do nothing
				}else if ( (text.equals(").") || text.equals(")")) && reader.previousWasEnd(ALTERNATIVE_FIELD_NUM)){
					//do nothing
				}else if ( charIsOpeningOrClosingBracket(text) ){
					//for now we don't do anything, however in future brackets may have semantics
				}else{
					//TODO
					String message = "Unrecognized text: %s";
					fireWarningEvent(String.format(message, text), next, 6);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("Collection has no closing tag.");

	}


	private final String fotgPattern = "^\\(([A-Z]{1,3})(?:,\\s?([A-Z]{1,3}))*\\)"; // eg. (US, B, CAN)
	private void handleGatheringCollectionAndType(MarkupImportState state, XMLEventReader reader, XMLEvent parent, DerivedUnitFacade facade) throws XMLStreamException {
		checkNoAttributes(parent);

		XMLEvent next = readNoWhitespace(reader);

		if (next.isCharacters()){
			String txt = next.asCharacters().getData().trim();
			if (state.isSpecimenType()){
				state.addCollectionAndType(txt);
			}else{

				Matcher fotgMatcher = Pattern.compile(fotgPattern).matcher(txt);

				if (fotgMatcher.matches()){
					txt = txt.substring(1, txt.length() - 1);  //remove bracket
					String[] splits = txt.split(",");
					for (String split : splits ){
						Collection collection = getCollection(state, split.trim());
						if (facade.innerDerivedUnit() == null){
						    String message = "Adding a duplicate to a non derived unit based facade is not possible. Please check why no derived unit exists yet in facade!";
						    this.fireWarningEvent(message, next, -6);
						}else{
						    facade.addDuplicate(collection, null, null, null, null);
						}
					}
					//FIXME 9
					//create derived units and and add collections

				}else{
					fireWarningEvent("Collection and type pattern for gathering not recognized: " + txt, next, 4);
				}
			}

		}else{
			fireUnexpectedEvent(next, 0);
		}

		if (isMyEndingElement(next, parent)){
			return;  //in case we have a completely empty element
		}
		next = readNoWhitespace(reader);
		if (isMyEndingElement(next, parent)){
			return;
		}else{
			fireUnexpectedEvent(next, 0);
			return;
		}
	}


	private Collection getCollection(MarkupImportState state, String code) {
		Collection collection = state.getCollectionByCode(code);
		if (collection == null){
			List<Collection> list = this.docImport.getCollectionService().searchByCode(code);
			if (list.size() == 1){
				collection = list.get(0);
			}else if (list.size() > 1){
				fireWarningEvent("More then one occurrence for collection " + code +  " in database. Collection not reused" , "", 1);
			}

			if (collection == null){
				collection = Collection.NewInstance();
				collection.setCode(code);
				this.docImport.getCollectionService().saveOrUpdate(collection);
			}
			state.putCollectionByCode(code, collection);
		}
		return collection;
	}


	private void handleAlternativeFieldNumber(MarkupImportState state, XMLEventReader reader, XMLEvent parent, FieldUnit fieldUnit) throws XMLStreamException {
		Map<String, Attribute> attrs = getAttributes(parent);
		Boolean doubtful = this.getAndRemoveBooleanAttributeValue(parent, attrs, "doubful", false);

		//for now we do not handle annotation and typeNotes
		String altFieldNum = getCData(state, reader, parent, false).trim();
		DefinedTerm type = this.getIdentifierType(state, MarkupTransformer.uuidIdentTypeAlternativeFieldNumber, "Alternative field number", "Alternative field number", "alt. field no.", null);
		fieldUnit.addIdentifier(altFieldNum, type);
		if (doubtful){
			fireWarningEvent("Marking alternative field numbers as doubtful not yet possible, see #4673", parent,4);
//			Marker.NewInstance(identifier, "true", MarkerType.IS_DOUBTFUL());
		}

	}


	private boolean charIsOpeningOrClosingBracket(String text) {
		return text.equals("(") || text.equals(")");
	}


	private TimePeriod handleDates(MarkupImportState state, XMLEventReader reader, XMLEvent parent) throws XMLStreamException {
		checkNoAttributes(parent);
		TimePeriod result = TimePeriod.NewInstance();
		String parseMessage = "%s can not be parsed: %s";
		boolean hasFullDate = false;
		boolean hasAtomised = false;
		boolean hasUnparsedAtomised = false;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parent)) {
				if (! isAlternative(hasFullDate, hasAtomised, hasUnparsedAtomised)){
					String message = "Some problems exist when defining the date";
					fireWarningEvent(message, parent, 4);
				}
				return result;
			} else if (isStartingElement(next, FULL_DATE)) {
				String fullDate = getCData(state, reader, next, true);
				if (fullDate.endsWith(".")){
				    fullDate = fullDate.substring(0, fullDate.length()-1);
				}
				result = TimePeriodParser.parseString(fullDate);
				if (result.getFreeText() != null){
					fireWarningEvent(String.format(parseMessage, FULL_DATE, fullDate), parent, 1);
				}
				hasFullDate = true;
			} else if (isStartingElement(next, DAY)) {
				String day = getCData(state, reader, next, true).trim();
				day = normalizeDate(day);
				if (CdmUtils.isNumeric(day)){
					result.setStartDay(Integer.valueOf(day));
					hasAtomised = true;
				}else{
					fireWarningEvent(String.format(parseMessage,"Day", day), parent, 2);
					hasUnparsedAtomised = true;
				}
			} else if (isStartingElement(next, MONTH)) {
				String month = getCData(state, reader, next, true).trim();
				month = normalizeDate(month);
				if (CdmUtils.isNumeric(month)){
					result.setStartMonth(Integer.valueOf(month));
					hasAtomised = true;
				}else{
					fireWarningEvent(String.format(parseMessage,"Month", month), parent, 2);
					hasUnparsedAtomised = true;
				}
			} else if (isStartingElement(next, YEAR)) {
				String year = getCData(state, reader, next, true).trim();
				year = normalizeDate(year);
				if (CdmUtils.isNumeric(year)){
					result.setStartYear(Integer.valueOf(year));
					hasAtomised = true;
				}else{
					fireWarningEvent(String.format(parseMessage,"Year", year), parent, 2);
					hasUnparsedAtomised = true;
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("Dates has no closing tag.");
	}


	private String normalizeDate(String partOfDate) {
		if (isBlank(partOfDate)){
			return null;
		}
		partOfDate = partOfDate.trim();
		while (partOfDate.startsWith("-")){
			partOfDate = partOfDate.substring(1);
		}
		return partOfDate;
	}


	private boolean isAlternative(boolean first, boolean second, boolean third) {
		return ( (first ^ second) && !third)  ||
				(! first && ! second && third) ;
	}


	private void handleLocality(MarkupImportState state, XMLEventReader reader,XMLEvent parentEvent, DerivedUnitFacade facade)throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);
		boolean isLocality = false;
		NamedAreaLevel areaLevel = null;
		if ("locality".equalsIgnoreCase(classValue)||state.getConfig().isIgnoreLocalityClass()) {
			isLocality = true;
		} else {
			areaLevel = makeNamedAreaLevel(state, classValue, parentEvent);
		}

		String text = "";
		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (StringUtils.isNotBlank(text)) {
					text = normalize(text);text = removeTrailingPunctuation(text);
					if (isLocality) {
						facade.setLocality(text, getDefaultLanguage(state));
					} else {
						text = CdmUtils.removeTrailingDot(text);
						NamedArea area = makeArea(state, text, areaLevel);
						facade.addCollectingArea(area);
					}
				}
				// TODO
				return;
			}else if (isStartingElement(next, ALTITUDE)) {
				handleNotYetImplementedElement(next);
				// homotypicalGroup = handleNom(state, reader, next, taxon,
				// homotypicalGroup);
			} else if (isStartingElement(next, COORDINATES)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, ANNOTATION)) {
				handleNotYetImplementedElement(next);
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<SpecimenType> has no closing tag");
	}



	/**
     * @param text
     * @return
     */
    private String removeTrailingPunctuation(String text) {
        while (isPunctuation(text.substring(text.length()-1))){
            text = text.substring(0, text.length()-1).trim();
        }
        return text;
    }


    private TeamOrPersonBase<?> createCollector(MarkupImportState state, String collectorStr) {
		return createAuthor(state, collectorStr);
	}


	public List<DescriptionElementBase> handleMaterialsExamined(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Feature feature, TaxonDescription defaultDescription) throws XMLStreamException {
		List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();
		//reset current areas
		state.removeCurrentAreas();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (result.isEmpty()){
					fireWarningEvent("Materials examined created empty Individual Associations list", parentEvent, 4);
				}
				state.removeCurrentAreas();
				return result;
			} else if (isStartingElement(next, SUB_HEADING)) {
//				Map<String, Object> inlineMarkup = new HashMap<String, Object>();
				String text = getCData(state, reader, next, true);
				if (isFeatureHeading(state, next, text)){
					feature = makeHeadingFeature(state, next, text, feature);
				}else{
					String message = "Unhandled subheading: %s";
					fireWarningEvent(String.format(message,  text), next, 4);
				}
//				for (String key : inlineMarkup.keySet()){
//					handleInlineMarkup(state, key, inlineMarkup);
//				}

			} else if (isStartingElement(next, BR) || isEndingElement(next, BR)) {
				//do nothing
			} else if (isStartingElement(next, GATHERING)) {
				DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.DerivedUnit);
				addCurrentAreas(state, next, facade);
				handleGathering(state, reader, next, facade);
				SpecimenOrObservationBase<?> specimen;
				if (facade.innerDerivedUnit() != null){
					specimen = facade.innerDerivedUnit();
				}else{
					specimen = facade.innerFieldUnit();
				}
				IndividualsAssociation individualsAssociation = IndividualsAssociation.NewInstance();
				individualsAssociation.addPrimaryTaxonomicSource(state.getConfig().getSourceReference());
				individualsAssociation.setAssociatedSpecimenOrObservation(specimen);
				result.add(individualsAssociation);
			} else if (isStartingElement(next, GATHERING_GROUP)) {
				List<DescriptionElementBase> list = getGatheringGroupDescription(state, reader, next);
				result.addAll(list);
			}else if (next.isCharacters()) {
				String text = next.asCharacters().getData().trim();
				if (isPunctuation(text)){
					//do nothing
				}else{
					String message = "Unrecognized text: %s";
					fireWarningEvent(String.format(message, text), next, 6);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<String> has no closing tag");

	}



	private List<DescriptionElementBase> getGatheringGroupDescription(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String geoScope = getAndRemoveAttributeValue(attributes, "geoscope");
		Boolean doubtful = getAndRemoveBooleanAttributeValue(parentEvent, attributes, DOUBTFUL, null);
		checkNoAttributes(attributes, parentEvent);

		List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();


		TaxonDescription td = null;

		if (isNotBlank(geoScope)){
			NamedArea area = Country.getCountryByLabel(geoScope);
			if (area == null){
				try {
					area = state.getTransformer().getNamedAreaByKey(geoScope);
				} catch (Exception e) {
					fireWarningEvent("getNamedArea not supported", parentEvent, 16);
				}
			}
			if (area == null){
				fireWarningEvent("Area for geoscope not found: " +  geoScope +"; add specimen group to ordinary description", parentEvent, 4);
			}else{
				state.addCurrentArea(area);
				Set<TaxonDescription> descs = state.getCurrentTaxon().getDescriptions();
				for (TaxonDescription desc : descs){
					Set<NamedArea> scopes = desc.getGeoScopes();
					if (scopes.size() == 1 && scopes.iterator().next().equals(area)){
						td = desc;
						break;
					}
				}
				if (td == null){
					TaxonDescription desc = TaxonDescription.NewInstance(state.getCurrentTaxon());
					desc.addPrimaryTaxonomicSource(state.getConfig().getSourceReference(), null);
					desc.addGeoScope(area);
					if (doubtful != null){
						desc.addMarker(Marker.NewInstance(MarkerType.IS_DOUBTFUL(), doubtful));
					}
					td = desc;
				}
			}
		}

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (result.isEmpty()){
					fireWarningEvent("Gathering group created empty Individual Associations list", parentEvent, 4);
				}
				state.removeCurrentAreas();
				return result;
			} else if (isStartingElement(next, GATHERING)) {
				DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.DerivedUnit);
				addCurrentAreas(state, next, facade);
				handleGathering(state, reader, next, facade);
				SpecimenOrObservationBase<?> specimen;
				if (facade.innerDerivedUnit() != null){
					specimen = facade.innerDerivedUnit();
				}else{
					specimen = facade.innerFieldUnit();
				}
				IndividualsAssociation individualsAssociation = IndividualsAssociation.NewInstance();
				individualsAssociation.addPrimaryTaxonomicSource(state.getConfig().getSourceReference());
				individualsAssociation.setAssociatedSpecimenOrObservation(specimen);
				result.add(individualsAssociation);

			}else if (next.isCharacters()) {
				String text = next.asCharacters().getData().trim();
				if (isPunctuation(text)){
					//do nothing
				}else{
					//TODO
					String message = "Unrecognized text: %s";
					fireWarningEvent(String.format(message, text), next, 6);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<Gathering group> has no closing tag");

	}

	private void addCurrentAreas(MarkupImportState state, XMLEvent event, DerivedUnitFacade facade) {
		for (NamedArea area : state.getCurrentAreas()){
			if (area == null){
				continue;
			}else if (area.isInstanceOf(Country.class)){
				facade.setCountry(area);
			}else{
				String message = "Current area %s is not country. This is not expected for currently known data.";
				fireWarningEvent(String.format(message, area.getTitleCache()), event, 2);
				facade.addCollectingArea(area);
			}
		}

	}


//	private void handleInlineMarkup(MarkupImportState state, String key, Map<String, Object> inlineMarkup) {
//		Object obj = inlineMarkup.get(key);
//		if (key.equals(LOCALITY)){
//			if (obj instanceof NamedArea){
//				NamedArea area = (NamedArea)obj;
//				state.addCurrentArea(area);
//			}
//		}
//
//	}


	/**
	 * Changes the feature if the (sub)-heading implies this. Also recognizes hidden country information
	 * @param state
	 * @param parent
	 * @param text
	 * @param feature
	 * @return
	 */
	private Feature makeHeadingFeature(MarkupImportState state, XMLEvent parent, String originalText, Feature feature) {
		//expand, provide by config or service
		String materialRegEx = "Mat[\u00E9\u00C9]riel";
		String examinedRegEx = "[\u00E9\u00C9]tudi[\u00E9\u00C9]";
		String countryRegEx = "(gabonais)";
		String postfixCountryRegEx = "\\s+(pour le Gabon)";

		String materialExaminedRegEx = "(?i)" + materialRegEx + "\\s+(" + countryRegEx +"\\s+)?" + examinedRegEx + "(" +postfixCountryRegEx + ")?:?";

		String text = originalText;

		if (isBlank(text)){
			return feature;
		}else{
			if (text.matches(materialExaminedRegEx)){
				//gabon specific
				if (text.contains("gabonais ")){
					text = text.replace("gabonais ", "");
					state.addCurrentArea(Country.GABONGABONESEREPUBLIC());
				}
				if (text.contains(" pour le Gabon")){
					text = text.replace(" pour le Gabon", "");
					state.addCurrentArea(Country.GABONGABONESEREPUBLIC());
				}

				//update feature
				feature = Feature.MATERIALS_EXAMINED();
				state.putFeatureToGeneralSorterList(feature);
				return feature;
			}else{
				String message = "Heading/Subheading not recognized: %s";
				fireWarningEvent(String.format(message, originalText), parent, 4);
				return feature;
			}
		}
	}


	/**
	 * True if heading or subheading represents feature information
	 * @param state
	 * @param parent
	 * @param text
	 * @return
	 */
	private boolean isFeatureHeading(MarkupImportState state, XMLEvent parent, String text) {
		return makeHeadingFeature(state, parent, text, null) != null;
	}


	public String handleInLineGathering(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.FieldUnit);
		handleGathering(state, reader, parentEvent, facade);
		SpecimenOrObservationBase<?> specimen  = facade.innerFieldUnit();
		if (specimen == null){
			specimen = facade.innerDerivedUnit();
			String message = "Inline gaterhing has no field unit";
			fireWarningEvent(message, parentEvent, 2);
		}

		String result = "<cdm:specimen uuid='%s'>%s</specimen>";
		if (specimen != null){
			result = String.format(result, specimen.getUuid(), specimen.getTitleCache());
		}else{
			String message = "Inline gathering has no specimen";
			fireWarningEvent(message, parentEvent, 4);
		}
		save(specimen, state);
		return result;
	}





}
