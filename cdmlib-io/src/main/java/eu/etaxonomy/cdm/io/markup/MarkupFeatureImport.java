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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @created 30.05.2012
 *
 */
public class MarkupFeatureImport extends MarkupImportBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupFeatureImport.class);

	protected static final String MODS_TITLEINFO = "titleInfo";

	private final MarkupSpecimenImport specimenImport;
	private final MarkupNomenclatureImport nomenclatureImport;

	public MarkupFeatureImport(MarkupDocumentImport docImport, MarkupSpecimenImport specimenImport,
			 MarkupNomenclatureImport nomenclatureImport) {
		super(docImport);
		this.specimenImport = specimenImport;
		this.nomenclatureImport = nomenclatureImport;
		this.featureImport = this;
	}

	public void handleFeature(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		Map<String, Attribute> attrs = getAttributes(parentEvent);
		Boolean isFreetext = getAndRemoveBooleanAttributeValue(parentEvent, attrs, IS_FREETEXT, false);
		String classValue =getAndRemoveRequiredAttributeValue(parentEvent, attrs, CLASS);
		checkNoAttributes(attrs, parentEvent);


		Feature feature = makeFeature(classValue, state, parentEvent, null);
		Taxon taxon = state.getCurrentTaxon();
		TaxonDescription taxonDescription = getTaxonDescription(taxon, state.getConfig().getSourceReference(), NO_IMAGE_GALLERY, CREATE_NEW);
		// TextData figureHolderTextData = null; //for use with one TextData for
		// all figure only

		boolean isDescription = feature.equals(Feature.DESCRIPTION());
		DescriptionElementBase lastDescriptionElement = null;

		CharOrder charOrder= new CharOrder();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				state.putFeatureToGeneralSorterList(feature);
				return;
			} else if (isEndingElement(next, DISTRIBUTION_LIST) || isEndingElement(next, HABITAT_LIST)) {
				// only handle list elements
			} else if (isStartingElement(next, HEADING)) {
				makeFeatureHeading(state, reader, classValue, feature, next);
			} else if (isStartingElement(next, WRITER)) {
				makeFeatureWriter(state, reader, feature, taxon, next);
//			} else if (isStartingElement(next, DISTRIBUTION_LOCALITY)) {
//				if (!feature.equals(Feature.DISTRIBUTION())) {
//					String message = "Distribution locality only allowed for feature of type 'distribution'";
//					fireWarningEvent(message, next, 4);
//				}
//				handleDistributionLocality(state, reader, next);
			} else if (isStartingElement(next, DISTRIBUTION_LIST) || isStartingElement(next, HABITAT_LIST)) {
				// only handle single list elements
			} else if (isStartingElement(next, HABITAT)) {
				if (!(feature.equals(Feature.HABITAT())
						|| feature.equals(Feature.HABITAT_ECOLOGY())
						|| feature.equals(Feature.ECOLOGY()))) {
					String message = "Habitat only allowed for feature of type 'habitat','habitat ecology' or 'ecology'";
					fireWarningEvent(message, next, 4);
				}
				handleHabitat(state, reader, next);
			} else if (isStartingElement(next, CHAR)) {
				List<TextData> textDataList = handleChar(state, reader, next, null, charOrder);
				charOrder = charOrder.next();
				for (TextData textData : textDataList){
					taxonDescription.addElement(textData);
				}
			} else if (isStartingElement(next, STRING)) {
				lastDescriptionElement = makeFeatureString(state, reader,feature, taxonDescription, lastDescriptionElement,next, isFreetext);
			} else if (isStartingElement(next, FIGURE_REF)) {
				lastDescriptionElement = makeFeatureFigureRef(state, reader, taxonDescription, isDescription, lastDescriptionElement, next);
			} else if (isStartingElement(next, REFERENCES)) {
				// TODO details/microcitation ??

				List<Reference> refs = handleReferences(state, reader, next);
				if (!refs.isEmpty()) {
					// TODO
					Reference descriptionRef = state.getConfig().getSourceReference();
					TaxonDescription description = getTaxonDescription(taxon, descriptionRef, false, true);
					TextData featurePlaceholder = docImport.getFeaturePlaceholder(state, description, feature, true);
					for (Reference citation : refs) {
						featurePlaceholder.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, citation, null);
					}
				} else {
					String message = "No reference found in references";
					fireWarningEvent(message, next, 6);
				}
			} else if (isStartingElement(next, NUM)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<Feature> has no closing tag");
	}


	/**
	 * @param state
	 * @param reader
	 * @param taxonDescription
	 * @param isDescription
	 * @param lastDescriptionElement
	 * @param next
	 * @return
	 * @throws XMLStreamException
	 */
	public DescriptionElementBase makeFeatureFigureRef(MarkupImportState state, XMLEventReader reader,TaxonDescription taxonDescription,
					boolean isDescription, DescriptionElementBase lastDescriptionElement, XMLEvent next) throws XMLStreamException {
		FigureDataHolder figureHolder = handleFigureRef(state, reader, next);
		Feature figureFeature = getFeature(state, MarkupTransformer.uuidFigures, "Figures", "Figures", "Fig.",null);
		if (isDescription) {
			TextData figureHolderTextData = null;
			// if (figureHolderTextData == null){
			figureHolderTextData = TextData.NewInstance(figureFeature);
			if (StringUtils.isNotBlank(figureHolder.num)) {
				String annotationText = "<num>" + figureHolder.num.trim() + "</num>";
				Annotation annotation = Annotation.NewInstance(annotationText, AnnotationType.TECHNICAL(), getDefaultLanguage(state));
				figureHolderTextData.addAnnotation(annotation);
			}
			if (StringUtils.isNotBlank(figureHolder.figurePart)) {
				String annotationText = "<figurePart>"+ figureHolder.figurePart.trim() + "</figurePart>";
				Annotation annotation = Annotation.NewInstance(annotationText,AnnotationType.EDITORIAL(), getDefaultLanguage(state));
				figureHolderTextData.addAnnotation(annotation);
			}
			// if (StringUtils.isNotBlank(figureText)){
			// figureHolderTextData.putText(language, figureText);
			// }
			taxonDescription.addElement(figureHolderTextData);
			// }
			registerFigureDemand(state, next, figureHolderTextData, figureHolder.ref);
		} else {
			if (lastDescriptionElement == null) {
				String message = "No description element created yet that can be referred by figure. Create new TextData instead";
				fireWarningEvent(message, next, 4);
				lastDescriptionElement = TextData.NewInstance(figureFeature);
				taxonDescription.addElement(lastDescriptionElement);
			}
			registerFigureDemand(state, next, lastDescriptionElement,	figureHolder.ref);
		}
		return lastDescriptionElement;
	}

	/**
	 * @param state
	 * @param reader
	 * @param feature
	 * @param taxonDescription
	 * @param lastDescriptionElement
	 * @param distributionList
	 * @param next
	 * @return
	 * @throws XMLStreamException
	 * @throws
	 */
	private DescriptionElementBase makeFeatureString(MarkupImportState state,XMLEventReader reader, Feature feature,
				TaxonDescription taxonDescription, DescriptionElementBase lastDescriptionElement, XMLEvent next, Boolean isFreetext) throws XMLStreamException {

		//for specimen only
		if (feature.equals(Feature.SPECIMEN()) || feature.equals(Feature.MATERIALS_EXAMINED())
				|| feature.getUuid().equals(MarkupTransformer.uuidWoodSpecimens)){

			List<DescriptionElementBase> specimens = specimenImport.handleMaterialsExamined(state, reader, next, feature, taxonDescription);
			for (DescriptionElementBase specimen : specimens){
				if (specimen.getInDescription() == null){
					taxonDescription.addElement(specimen);
				}
				lastDescriptionElement = specimen;
			}
			state.setCurrentCollector(null);

			return lastDescriptionElement;
		}else if (feature.equals(Feature.COMMON_NAME()) && (isFreetext == null || !isFreetext)){
			List<DescriptionElementBase> commonNames = makeCommonNameString(state, reader, next);
			//NODE: we do also have the old version makeVernacular, which was called from "others" below
			for (DescriptionElementBase commonName : commonNames){
				taxonDescription.addElement(commonName);
				lastDescriptionElement = commonName;
			}
			return lastDescriptionElement;
		}
		else{

			//others
			Map<String, String> subheadingMap = handleString(state, reader, next, feature);
			for (String subheading : subheadingMap.keySet()) {
				Feature subheadingFeature = feature;
				if (StringUtils.isNotBlank(subheading) && subheadingMap.size() > 1) {
					subheadingFeature = makeFeature(subheading, state, next, null);
				}
				if (feature.equals(Feature.COMMON_NAME()) && (isFreetext == null || !isFreetext)){
					//NOTE: see above
//					List<DescriptionElementBase> commonNames = makeVernacular(state, subheading, subheadingMap.get(subheading));
//					for (DescriptionElementBase commonName : commonNames){
//						taxonDescription.addElement(commonName);
//						lastDescriptionElement = commonName;
//					}
				}else {
					TextData textData = TextData.NewInstance(subheadingFeature);
					textData.putText(getDefaultLanguage(state), subheadingMap.get(subheading));
					taxonDescription.addElement(textData);
					lastDescriptionElement = textData;
					// TODO how to handle figures when these data are split in
					// subheadings
				}
			}
			return lastDescriptionElement;
		}
	}

	/**
	 * @param classValue
	 * @param state
	 * @param parentEvent
	 * @param parentFeature
	 * @return
	 * @throws UndefinedTransformerMethodException
	 */
	private Feature makeFeature(String classValue, MarkupImportState state, XMLEvent parentEvent, Feature parentFeature) {
		UUID uuid;
		try {
			String featureText = StringUtils.capitalize(classValue);
			if (parentFeature != null){
				featureText = "<%s>" + featureText;
				featureText = String.format(featureText, parentFeature.getTitleCache());
				classValue = "<%s>" + classValue;
				classValue = String.format(classValue, parentFeature.getTitleCache());
			}


			//get existing feature
			if (classValue.endsWith(".")){
			    classValue = classValue.substring(0, classValue.length() - 1);
			}
			Feature feature = state.getTransformer().getFeatureByKey(classValue);
			if (feature != null) {
				return feature;
			}
			uuid = state.getTransformer().getFeatureUuid(classValue);

			if (uuid == null){
				uuid = state.getUnknownFeatureUuid(classValue);
			}

			if (uuid == null) {
				// TODO
				String message = "Uuid is not defined for '%s'";
				message = String.format(message, classValue);
				if (! message.contains("<")){
					//log only top level features
					fireWarningEvent(message, parentEvent, 8);
				}
				uuid = UUID.randomUUID();
				state.putUnknownFeatureUuid(classValue, uuid);
			}

			// TODO eFlora vocabulary
			TermVocabulary<Feature> voc = null;
			feature = getFeature(state, uuid, featureText, featureText, classValue, voc);
			if (parentFeature != null){
				parentFeature.addIncludes(feature);
				save(parentFeature, state);
			}
			save(feature, state);

			if (feature == null) {
				throw new NullPointerException(classValue + " not recognized as a feature");
			}
//			state.putFeatureToCurrentList(feature);
			return feature;
		} catch (Exception e) {
			String message = "Could not create feature for %s: %s";
			message = String.format(message, classValue, e.getMessage());
			fireWarningEvent(message, parentEvent, 4);
			state.putUnknownFeatureUuid(classValue, null);
//			e.printStackTrace();
			return Feature.UNKNOWN();
		}
	}

	public class CharOrder{
		static final int strlength = 3;
		private int order = 1;
		private CharOrder parent;
		private final List<CharOrder> children = new ArrayList<CharOrder>();

		public CharOrder nextChild(){
			CharOrder result = new CharOrder();
			if (! children.isEmpty()) {
				result.order = children.get(children.size() - 1).order + 1;
			}
			result.parent = this;
			children.add(result);
			return result;
		}

		public CharOrder next(){
			CharOrder result = new CharOrder();
			result.order = order + 1;
			result.parent = parent;
			if (parent != null){
				parent.children.add(result);
			}
			return result;
		}

		public String orderString(){
			String parentString = parent == null ? "" : parent.orderString();
			String result = CdmUtils.concat("-", parentString, StringUtils.leftPad(String.valueOf(order), strlength, '0'));
			return result;
		}

		@Override
        public String toString(){
			return orderString();
		}
	}


	/**
	 * Handle the char or subchar element. As
	 * @param state the import state
	 * @param reader
	 * @param parentEvent
	 * @param parentFeature in case of subchars we need to attache the newly created feature to a parent feature, should be <code>null</code>
	 * for top level chars.
	 * @return List of TextData. Not a single one as the recursive TextData will also be returned
	 * @throws XMLStreamException
	 */
	private List<TextData> handleChar(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Feature parentFeature, CharOrder myCharOrder) throws XMLStreamException {
		List<TextData> result = new ArrayList<TextData>();
		String classValue = getClassOnlyAttribute(parentEvent);
		Feature feature = makeFeature(classValue, state, parentEvent, parentFeature);
		if(parentFeature == null){
			state.putFeatureToCharSorterList(feature);
		}else{
			FeatureSorterInfo parentInfo = state.getLatestCharFeatureSorterInfo();
//			if (! parentInfo.getUuid().equals(parentFeature.getUuid())){
//				String message = "The parent char feature is not the same as the latest feature. This is the case for char hierarchies with > 2 levels, which is not yet handled by the import";
//				fireWarningEvent(message, parentEvent, 6);
//			}else{
				state.getLatestCharFeatureSorterInfo().addSubFeature(new FeatureSorterInfo(feature));
//			}
		}

		TextData textData = TextData.NewInstance(feature);
		result.add(textData);

		AnnotationType annType = getAnnotationType(state, MarkupTransformer.uuidOriginalOrder, "Original order", "Order in original treatment", null, AnnotationType.TECHNICAL().getVocabulary());
		textData.addAnnotation(Annotation.NewInstance(myCharOrder.orderString(), annType, Language.ENGLISH()));

		boolean isTextMode = true;
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				text = text.trim();
				textData.putText(getDefaultLanguage(state), text);
				return result;
			} else if (isStartingElement(next, FIGURE_REF)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, FOOTNOTE_REF)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, BR)) {
				text += "<br/>";
				isTextMode = false;
			} else if (isEndingElement(next, BR)) {
				isTextMode = true;
			} else if (isHtml(next)) {
				text += getXmlTag(next);
			} else if (next.isStartElement()) {
				if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next); //TODO test handleSimpleAnnotation
				} else if (isStartingElement(next, ITALICS)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, BOLD)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, FIGURE)) {
					handleFigure(state, reader, next, specimenImport, nomenclatureImport);
				} else if (isStartingElement(next, SUB_CHAR)) {
					List<TextData> subTextData = handleChar(state, reader, next, feature, myCharOrder.nextChild());
					result.addAll(subTextData);
				} else if (isStartingElement(next, FOOTNOTE)) {
					FootnoteDataHolder footnote = handleFootnote(state, reader,	next, specimenImport, nomenclatureImport);
					if (footnote.isRef()) {
						String message = "Ref footnote not implemented here";
						fireWarningEvent(message, next, 4);
					} else {
						registerGivenFootnote(state, footnote);
					}
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()) {
				if (!isTextMode) {
					String message = "String is not in text mode";
					fireWarningEvent(message, next, 6);
				} else {
					text += next.asCharacters().getData();
				}
			} else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("RefPart has no closing tag");
	}


	/**
	 * @param state
	 * @param reader
	 * @param classValue
	 * @param feature
	 * @param next
	 * @throws XMLStreamException
	 */
	private void makeFeatureHeading(MarkupImportState state, XMLEventReader reader, String classValue, Feature feature, XMLEvent next) throws XMLStreamException {
		String heading = handleHeading(state, reader, next);
		if (StringUtils.isNotBlank(heading)) {
			if (!heading.equalsIgnoreCase(classValue)) {
				try {
					if (!feature.equals(state.getTransformer().getFeatureByKey(heading))) {
						UUID headerFeatureUuid = state.getTransformer().getFeatureUuid(heading);
						if (!feature.getUuid().equals(headerFeatureUuid)) {
							String message = "Feature heading '%s' differs from feature class '%s' and can not be transformed to feature";
							message = String.format(message, heading, classValue);
							fireWarningEvent(message, next, 1);
						}
					}
				} catch (UndefinedTransformerMethodException e) {
					throw new RuntimeException(e);
				}
			} else {
				// do nothing
			}
		}
	}


	/**
	 * @param state
	 * @param reader
	 * @param feature
	 * @param taxon
	 * @param next
	 * @throws XMLStreamException
	 */
	private void makeFeatureWriter(MarkupImportState state,XMLEventReader reader, Feature feature, Taxon taxon, XMLEvent next) throws XMLStreamException {
		WriterDataHolder writer = handleWriter(state, reader, next);
		if (isNotBlank(writer.writer)) {
			// TODO
			Reference ref = state.getConfig().getSourceReference();
			TaxonDescription description = getTaxonDescription(taxon, ref,
					false, true);
			TextData featurePlaceholder = docImport.getFeaturePlaceholder(state,
					description, feature, true);
			featurePlaceholder.addAnnotation(writer.annotation);
			registerFootnotes(state, featurePlaceholder, writer.footnotes);
		} else {
			String message = "Writer element is empty";
			fireWarningEvent(message, next, 4);
		}
	}


	protected void handleHabitat(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		Taxon taxon = state.getCurrentTaxon();
		// TODO which ref to take?
		Reference ref = state.getConfig().getSourceReference();


		boolean isTextMode = true;
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				TaxonDescription description = getTaxonDescription(taxon, ref,
						false, true);
				UUID uuidExtractedHabitat = MarkupTransformer.uuidExtractedHabitat;
				Feature feature = getFeature(
						state,
						uuidExtractedHabitat,
						"Extracted Habitat",
						"An structured habitat that was extracted from a habitat text",
						"extr. habit.", null);
				TextData habitat = TextData.NewInstance(feature);
				habitat.putText(getDefaultLanguage(state), text);
				description.addElement(habitat);

				return;
			} else if (isStartingElement(next, ALTITUDE)) {
				text = text.trim() + getTaggedCData(state, reader, next);
			} else if (isStartingElement(next, LIFE_CYCLE_PERIODS)) {
				handleNotYetImplementedElement(next);
			} else if (next.isCharacters()) {
			    if (! isTextMode) {
                    String message = "String is not in text mode";
                    fireWarningEvent(message, next, 6);
                } else {
                    text += next.asCharacters().getData();
                }
	         } else if (isStartingElement(next, BR)) {
	                text += "<br/>";
	                isTextMode = false;
	        } else if (isEndingElement(next, BR)) {
	                isTextMode = true;
	        } else if (isStartingElement(next, REFERENCES)) {
	            handleNotYetImplementedElement(next);
	        } else if (isStartingElement(next, FIGURE_REF)) {
                handleNotYetImplementedElement(next);
            } else {
                String type = next.toString();
                String location = String.valueOf(next.getLocation().getLineNumber());
                System.out.println("MarkupFeature.handleHabitat: Unexpected element in habitat: " + type + ":  " + location);
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<Habitat> has no closing tag");
	}


	private FigureDataHolder handleFigureRef(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent)
			throws XMLStreamException {
		FigureDataHolder result = new FigureDataHolder();
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		result.ref = getAndRemoveAttributeValue(attributes, REF);
		checkNoAttributes(attributes, parentEvent);

		// text is not handled, needed only for debugging purposes
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return result;
			} else if (isStartingElement(next, NUM)) {
				String num = getCData(state, reader, next);
				result.num = num; // num is not handled during import
			} else if (isStartingElement(next, FIGURE_PART)) {
				result.figurePart = getCData(state, reader, next);
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				fireUnexpectedEvent(next, 0);
			}
		}
		throw new IllegalStateException("<figureRef> has no end tag");
	}


	private void registerFigureDemand(MarkupImportState state, XMLEvent next, AnnotatableEntity entity, String figureRef) {
		Media existingFigure = state.getFigure(figureRef);
		if (existingFigure != null) {
			attachFigure(state, next, entity, existingFigure);
		} else {
			Set<AnnotatableEntity> demands = state.getFigureDemands(figureRef);
			if (demands == null) {
				demands = new HashSet<AnnotatableEntity>();
				state.putFigureDemands(figureRef, demands);
			}
			demands.add(entity);
		}
	}

	private List<DescriptionElementBase> makeCommonNameString(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException{

		List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();

		checkNoAttributes(parentEvent);

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (result.isEmpty()){
					fireWarningEvent("Common name was not created", next, 4);
				}
				return result;
			} else if (isStartingElement(next, VERNACULAR_NAMES)) {
				result = makeVernacularNames(state, reader, next);
			} else if (isStartingElement(next, SUB_HEADING)) {
				String subheading = getCData(state, reader, next);
				if (! subheading.matches("(Nom(s)? vernaculaire(s)?\\:|Vern.)")){
					fireWarningEvent("Subheading for vernacular name not recognized: " + subheading, next, 4);
				}
			} else if (next.isCharacters()) {
				String chars = next.asCharacters().toString().trim();
				if (chars.equals(".")){
					//do nothing
				}else{
					fireWarningEvent("Character not handled in vernacular name: " + chars, next, 4);
				}
			} else if (isStartingElement(next, REFERENCES)) {
			    handleNotYetImplementedElement(next);
            }else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("closing tag is missing");


	}

	private List<DescriptionElementBase> makeVernacularNames(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException{
		List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();
		checkNoAttributes(parentEvent);

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				state.removeCurrentAreas();
				return result;
			} else if (isStartingElement(next, VERNACULAR_NAME)) {
				List<CommonTaxonName> names = makeSingleVernacularName(state, reader, next);
				result.addAll(names);
			} else if (isStartingElement(next, SUB_HEADING)) {
				makeVernacularNamesSubHeading(state, reader, next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("closing tag is missing");

	}

	private void makeVernacularNamesSubHeading(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (StringUtils.isNotBlank(text)){
					NamedArea area = getCommonNameArea(text);
					if (area != null){
						state.removeCurrentAreas();
						state.addCurrentArea(area);
					}else{
						fireWarningEvent("Vernacular subheading not recognized", next, 8);
					}
				}

				return ;
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("closing tag is missing");

	}

	private NamedArea getCommonNameArea(String text) {
		if (text.endsWith(":")){
			text = text.substring(0, text.length()-1);
		}

		// for now we do it hardcoded
		if (text.equalsIgnoreCase("Guyana")){
			return Country.GUYANAREPUBLICOF();
		}else if (text.equalsIgnoreCase("Suriname")){
			return Country.SURINAMEREPUBLICOF();
		}else if (text.equalsIgnoreCase("French Guiana")){
			return Country.FRENCHGUIANA();
		}
		return null;
	}

	private List<CommonTaxonName> makeSingleVernacularName(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException{
		checkNoAttributes(parentEvent);
		List<CommonTaxonName> result = new ArrayList<CommonTaxonName>();

		Language language = state.getDefaultLanguage();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				for (CommonTaxonName commonName : result){
					commonName.setLanguage(language);
				}
//				if (isNotBlank(name)){
//					result.setName(name);
//				}else{
//					fireWarningEvent("No name string for common name", parentEvent, 4);
//				}

				return result;
			} else if (isStartingElement(next, NAME)) {
				//TODO test
				CommonTaxonName name = handleVernacularNameName(state, reader, next);
				if (name != null){
					result.add(name);
				}
			} else if (isStartingElement(next, LOCAL_LANGUAGE)) {
				Language localLanguage = handleLocalLanguage(state, reader, next);
				if (localLanguage != null){
					language = localLanguage;
				}
			} else if (isStartingElement(next, TRANSLATION)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, LOCALITY)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, ANNOTATION)){
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, FOOTNOTE_REF)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (next.isCharacters()) {
				String chars = next.asCharacters().toString().trim();
				if (chars.equals("(") || chars.equals(")") || chars.equals(",")){
					//do nothing
				}else{
					fireWarningEvent("Character not handled in vernacular name: " + chars, next, 4);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("closing tag is missing");
	}

	private CommonTaxonName handleVernacularNameName(MarkupImportState state, XMLEventReader reader,
				XMLEvent parentEvent) throws XMLStreamException {
		//attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		this.checkAndRemoveAttributeValue(attributes, CLASS, "vernacular");
		this.checkNoAttributes(attributes, parentEvent);

		//
		String text = getCData(state, reader, parentEvent, false);
		CommonTaxonName name = CommonTaxonName.NewInstance(text, null);
		if (! state.getCurrentAreas().isEmpty()){
			if (state.getCurrentAreas().size() > 1){
				fireWarningEvent("Multiple areas for common name not yet covered by CDM", parentEvent , 8);
			}else{
				name.setArea(state.getCurrentAreas().iterator().next());
			}
		}
		return name;
	}

	private Language handleLocalLanguage(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		//attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		boolean doubtful = getAndRemoveBooleanAttributeValue(parentEvent, attributes, DOUBTFUL, false);
		boolean unknown = getAndRemoveBooleanAttributeValue(parentEvent, attributes, UNKNOWN, false);
		this.checkNoAttributes(attributes, parentEvent);

		if (doubtful == true){
			fireWarningEvent("Doubtful not yet implemented for local language", parentEvent, 2);
		}
		if (unknown == true){
			fireWarningEvent("Unknown not yet implemented for local language ", parentEvent, 2);
		}

		//
		String text = getCData(state, reader, parentEvent);
		Language lang = makeLanguageByLangStr(state, text);
		return lang;

	}

	private List<DescriptionElementBase> makeVernacular(MarkupImportState state, String subheading, String commonNameString) throws XMLStreamException {
		List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();
		String[] splits = commonNameString.split(",");
		for (String split : splits){
			split = split.trim();
			if (! split.matches(".*\\(.*\\)\\.?")){
				fireWarningEvent("Common name string '"+split+"' does not match given pattern", state.getReader().peek(), 4);
			}

			String name = split.replaceAll("\\(.*\\)", "").replace(".", "").trim();
			String languageStr = split.replaceFirst(".*\\(", "").replaceAll("\\)\\.?", "").trim();

			Language language = null;
			if (StringUtils.isNotBlank(languageStr)){
				language = makeLanguageByLangStr(state, languageStr);
			}
			DescriptionElementBase commonName;
			if (name != null && name.length() < 255 ){
				NamedArea area = null;
				commonName = CommonTaxonName.NewInstance(name, language, area);
			}else{
				if (language == null){
					language = getDefaultLanguage(state);
				}
				commonName = TextData.NewInstance(Feature.COMMON_NAME(), name, language, null);
				String warning = "Vernacular feature is >255 size. Therefore it is handled as TextData, not CommonTaxonName: " + name;
				fireWarningEvent(warning, state.getReader().peek(), 1);
			}
			result.add(commonName);
		}

		return result;
	}

	private Language makeLanguageByLangStr(MarkupImportState state, String languageStr) throws XMLStreamException {
		try {
			Language language = state.getTransformer().getLanguageByKey(languageStr);
			if (language == null){
				UUID langUuid = state.getTransformer().getLanguageUuid(languageStr);
				TermVocabulary<?> voc = null;
				language = getLanguage(state, langUuid, languageStr, languageStr, null, voc);
			}
			if (language == null){
				String warning = "Language " + languageStr + " not recognized by transformer";
				fireWarningEvent(warning, state.getReader().peek(), 4);
			}
			return language;
		} catch (UndefinedTransformerMethodException e) {
			throw new RuntimeException(e);
		}
	}


	private String handleHeading(MarkupImportState state,XMLEventReader reader, XMLEvent parentEvent)throws XMLStreamException {
		checkNoAttributes(parentEvent);

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return text;
			} else if (next.isStartElement()) {
				if (isStartingElement(next, FOOTNOTE)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("<String> has no closing tag");

	}


	private List<Reference> handleReferences(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		// attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String bibliography = getAndRemoveAttributeValue(attributes,
				BIBLIOGRAPHY);
		String serialsAbbreviations = getAndRemoveAttributeValue(attributes,
				SERIALS_ABBREVIATIONS);
		if (isNotBlank(bibliography) || isNotBlank(serialsAbbreviations)) {
			String message = "Attributes not yet implemented for <references>";
			fireWarningEvent(message, parentEvent, 4);
		}

		List<Reference> result = new ArrayList<Reference>();

		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					return result;
				} else {
					if (isEndingElement(next, HEADING)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, WRITER)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, FOOTNOTE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, STRING)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, REF_NUM)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, HEADING)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, SUB_HEADING)) {
					String subheading = getCData(state, reader, next).trim();
					String excludePattern = "(i?)(References?|Literature):?";
					if (!subheading.matches(excludePattern)) {
						fireNotYetImplementedElement(next.getLocation(), next.asStartElement().getName(), 0);
					}
				} else if (isStartingElement(next, WRITER)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, FOOTNOTE)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, STRING)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, REF_NUM)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, REFERENCE)) {
					Reference ref = nomenclatureImport.handleReference(state, reader, next);
					result.add(ref);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<References> has no closing tag");
	}


	private String getTaggedCData(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);

		String text = getXmlTag(parentEvent);
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				text += getXmlTag(next);
				return text;
			} else if (next.isStartElement()) {
				text += getTaggedCData(state, reader, next);
			} else if (next.isEndElement()) {
				text += getTaggedCData(state, reader, next);
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("Some tag has no closing tag");
	}
}
