/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.redlist.bfnXml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.hibernate.Session;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.google.common.base.CharMatcher;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;
/**
 * 
 * @author a.oppermann
 * @date 04.07.2013
 *
 */
//@Component("bfnXmlTaxonNameIO")
@Component
public class BfnXmlImportTaxonName extends BfnXmlImportBase implements ICdmIO<BfnXmlImportState> {

	
	private static final Logger logger = Logger.getLogger(BfnXmlImportTaxonName.class);

	private static final String strNomenclaturalCode = "Zoological";//"Botanical";
	private static int parsingProblemCounter = 0;
	private Map<Integer, Taxon> firstList;
	private Map<Integer, Taxon> secondList;
	
	
	public BfnXmlImportTaxonName(){
		super();
	}

	@Override
	public boolean doCheck(BfnXmlImportState state){
		boolean result = true;
		return result;
	}

	@Override
	@SuppressWarnings({"rawtypes" })
	public void doInvoke(BfnXmlImportState state){
		ITaxonService taxonService = getTaxonService();

		BfnXmlImportConfigurator config = state.getConfig();
		Element elDataSet = getDataSetElement(config);
		Namespace bfnNamespace = config.getBfnXmlNamespace();
		
		List<?> contentXML = elDataSet.getContent();
		Element currentElement = null;
		for(Object object:contentXML){
		
			if(object instanceof Element){
				currentElement = (Element)object;
				//import taxon lists
				if(currentElement.getName().equalsIgnoreCase("ROTELISTEDATEN")){
					TransactionStatus tx = startTransaction();
					Map<UUID, TaxonBase> savedTaxonMap = extractTaxonNames(state, taxonService, config, currentElement, bfnNamespace);
					createOrUdateClassification(config, taxonService, savedTaxonMap, currentElement);
					commitTransaction(tx);
				}//import concept relations of taxon lists
				else if(currentElement.getName().equalsIgnoreCase("KONZEPTBEZIEHUNGEN")){
					TransactionStatus tx = startTransaction();
					extractTaxonConceptRelationShips(bfnNamespace,currentElement);
					commitTransaction(tx);
				}
			}
		}
		return;
	}

	/**
	 * This method will parse the XML concept relationships and tries to map them into cdm relationship types.
	 * 
	 * @param bfnNamespace
	 * @param currentElement
	 */
	private void extractTaxonConceptRelationShips(Namespace bfnNamespace,
			Element currentElement) {
		String childName;
		String bfnElementName = "KONZEPTBEZIEHUNG";
		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		List<Element> elConceptList = (List<Element>)currentElement.getChildren(bfnElementName, bfnNamespace);
		List<TaxonBase> updatedTaxonList = new ArrayList<TaxonBase>();
		for(Element element:elConceptList){
			
			childName = "TAXONYM1";
			Element elTaxon1 = XmlHelp.getSingleChildElement(success, element, childName, bfnNamespace, false);
			String taxNr1 = elTaxon1.getAttributeValue("taxNr");
			int int1 = Integer.parseInt(taxNr1);
			Taxon taxon1 = firstList.get(int1);
			TaxonBase<?> taxonBase1 = getTaxonService().load(taxon1.getUuid());
			taxon1 = (Taxon)taxonBase1;

			childName = "TAXONYM2";
			Element elTaxon2 = XmlHelp.getSingleChildElement(success, element, childName, bfnNamespace, false);
			String taxNr2 = elTaxon2.getAttributeValue("taxNr");
			int int2 = Integer.parseInt(taxNr2);
			Taxon taxon2 = secondList.get(int2);
			TaxonBase<?> taxonBase2 = getTaxonService().load(taxon2.getUuid());
			taxon2 = (Taxon) taxonBase2;
			
			childName = "STATUS";
			Element elConceptStatus = XmlHelp.getSingleChildElement(success, element, childName, bfnNamespace, false);
			String conceptStatusValue = elConceptStatus.getValue();
			conceptStatusValue = conceptStatusValue.replaceAll("\u00A0", "").trim();
			TaxonRelationshipType taxonRelationType = null;
			/**
			 * This if case only exists because it was decided not to have a included_in relationship type.
			 */
			if(conceptStatusValue.equalsIgnoreCase("<")){
				taxon2.addTaxonRelation(taxon1, TaxonRelationshipType.INCLUDES(), null, null);
			}else{
				try {
					taxonRelationType = BfnXmlTransformer.concept2TaxonRelation(conceptStatusValue);
				} catch (UnknownCdmTypeException e) {
					e.printStackTrace();
				}
				taxon1.addTaxonRelation(taxon2, taxonRelationType , null, null);
			}
			if(taxonRelationType != null && taxonRelationType.equals(TaxonRelationshipType.ALL_RELATIONSHIPS())){
				List<TaxonRelationship> relationsFromThisTaxon = (List<TaxonRelationship>) taxon1.getRelationsFromThisTaxon();
				TaxonRelationship taxonRelationship = relationsFromThisTaxon.get(0);
				taxonRelationship.setDoubtful(true);
			}
			updatedTaxonList.add(taxon2);
			updatedTaxonList.add(taxon1);
		}
		getTaxonService().saveOrUpdate(updatedTaxonList);
		logger.info("taxon relationships imported...");
	}

	/**
	 * This method stores the current imported maps in global variables to make
	 * them later available for matching the taxon relationships between these 
	 * imported lists.
	 *  
	 * @param config
	 * @param taxonMap
	 */
	private void prepareListforConceptImport(BfnXmlImportConfigurator config,Map<Integer, Taxon> taxonMap) {
		if(config.isFillSecondList()){
			secondList = taxonMap;
		}else{
			firstList = taxonMap;
		}
	}

	/**
	 * 
	 * @param state
	 * @param taxonService
	 * @param config
	 * @param elDataSet
	 * @param bfnNamespace
	 * @return
	 */
	private Map<UUID, TaxonBase> extractTaxonNames(BfnXmlImportState state,
			ITaxonService taxonService, BfnXmlImportConfigurator config,
			Element elDataSet, Namespace bfnNamespace) {
		logger.info("start make TaxonNames...");
		Map<Integer, Taxon> taxonMap = new LinkedHashMap<Integer, Taxon>();
		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "TaxonName";
		
		childName = "TAXONYME";
		obligatory = false;
		Element elTaxonNames = XmlHelp.getSingleChildElement(success, elDataSet, childName, bfnNamespace, obligatory);

		String bfnElementName = "TAXONYM";
		List<Element> elTaxonList = (List<Element>)elTaxonNames.getChildren(bfnElementName, bfnNamespace);
		
		//for each taxonName
		for (Element elTaxon : elTaxonList){
			//create Taxon
			String taxonId = elTaxon.getAttributeValue("taxNr");
			childName = "WISSNAME";
			Element elWissName = XmlHelp.getSingleChildElement(success, elTaxon, childName, bfnNamespace, obligatory);
			String childElementName = "NANTEIL";
			Taxon taxon = createOrUpdateTaxon(success, idNamespace, config, bfnNamespace, elWissName, childElementName);
			
			//for each synonym
			childName = "SYNONYME";
			Element elSynonyms = XmlHelp.getSingleChildElement(success, elTaxon, childName, bfnNamespace, obligatory);
			if(elSynonyms != null){
				childElementName = "SYNONYM";
				createOrUpdateSynonym(taxon, success, obligatory, bfnNamespace, childElementName,elSynonyms, taxonId, config);
			}
			//for each information concerning the taxon element
			//TODO Information block
			if(config.isDoInformationImport()){
				childName = "INFORMATIONEN";
				Element elInformations = XmlHelp.getSingleChildElement(success, elTaxon, childName, bfnNamespace, obligatory);
				if(elInformations != null){
					childElementName = "BEZUGSRAUM";
					createOrUpdateInformation(taxon, bfnNamespace, childElementName,elInformations, state);
				}
			}
			taxonMap.put(Integer.parseInt(taxonId), taxon);
		}
		
		//Quick'n'dirty to set concept relationships between two imported list
		prepareListforConceptImport(config, taxonMap);
		
		Map<UUID, TaxonBase> savedTaxonMap = taxonService.saveOrUpdate((Collection)taxonMap.values());
		//FIXME: after first list don't import metadata yet
		//TODO: import information for second taxon list.
		config.setDoInformationImport(false);
		config.setFillSecondList(true);
		logger.info("end makeTaxonNames ...");
		if (!success.getValue()){
			state.setUnsuccessfull();
		}
		return savedTaxonMap;
	}


	/**
	 * This will put the prior imported list into a classification
	 * 
	 * @param config 
	 * @param taxonService 
	 * @param config 
	 * @param savedTaxonMap 
	 * @param currentElement 
	 * @return 
	 */
	@SuppressWarnings("rawtypes")
	private boolean createOrUdateClassification(BfnXmlImportConfigurator config, ITaxonService taxonService, Map<UUID, TaxonBase> savedTaxonMap, Element currentElement) {
		boolean isNewClassification = true;
		//TODO make classification name dynamically depending on its value in the XML.
		Classification classification = Classification.NewInstance(config.getClassificationName()+" "+currentElement.getAttributeValue("inhalt"), config.getSourceReference());
//		List<Classification> classificationList = getClassificationService().list(Classification.class, null, null, null, VOC_CLASSIFICATION_INIT_STRATEGY);
//		for(Classification c : classificationList){
//			if(c.getTitleCache().equalsIgnoreCase(classification.getTitleCache())){
//				classification = c;
//				isNewClassification = false;
//			}
//		}
		
//		ArrayList<TaxonBase> taxonBaseList = (ArrayList<TaxonBase>) taxonService.list(TaxonBase.class, null, null, null, VOC_CLASSIFICATION_INIT_STRATEGY);
		for(TaxonBase tb:savedTaxonMap.values()){
			if(tb instanceof Taxon){
				TaxonBase tbase = CdmBase.deproxy(tb, TaxonBase.class);
				Taxon taxon = (Taxon)tbase;
				taxon = CdmBase.deproxy(taxon, Taxon.class);
				classification.addChildTaxon(taxon, null, null);
			}
		}
		IClassificationService classificationService = getClassificationService();
		classificationService.saveOrUpdate(classification);
		return isNewClassification;
	}

	

	/**
	 * Matches the XML attributes against CDM entities.<BR> 
	 * Imports Scientific Name, Rank, etc. and creates a taxon.<br>
	 * <b>Existing taxon names won't be matched yet</b>
	 * 
	 * @param success
	 * @param idNamespace
	 * @param config
	 * @param bfnNamespace
	 * @param elTaxonName
	 * @param childElementName
	 * @return
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Taxon createOrUpdateTaxon(
			ResultWrapper<Boolean> success, String idNamespace,
			BfnXmlImportConfigurator config, Namespace bfnNamespace,
			Element elTaxonName, String childElementName) {
		
		List<Element> elWissNameList = (List<Element>)elTaxonName.getChildren(childElementName, bfnNamespace);
		Rank rank = null;
		String strAuthor = null;
		String strSupplement = null;
		Taxon taxon = null;
		Integer uniqueID = null;
		for(Element elWissName:elWissNameList){

			if(elWissName.getAttributeValue("bereich", bfnNamespace).equalsIgnoreCase("Eindeutiger Code")){
				String textNormalize = elWissName.getTextNormalize();
				uniqueID = Integer.valueOf(textNormalize);
			}
			if(elWissName.getAttributeValue("bereich", bfnNamespace).equalsIgnoreCase("Autoren")){
				strAuthor = elWissName.getTextNormalize();
			}
			if(elWissName.getAttributeValue("bereich", bfnNamespace).equalsIgnoreCase("Rang")){
				String strRank = elWissName.getTextNormalize();
				rank = makeRank(strRank);
			}
			if(elWissName.getAttributeValue("bereich", bfnNamespace).equalsIgnoreCase("Zusätze")){
				strSupplement = elWissName.getTextNormalize();
			}
			if(elWissName.getAttributeValue("bereich", bfnNamespace).equalsIgnoreCase("wissName")){
				try{
					TaxonNameBase<?, ?> nameBase = parseNonviralNames(rank,strAuthor,strSupplement,elWissName);
					//TODO  extract to method?
					if(strSupplement != null && strSupplement.equalsIgnoreCase("nom. illeg.")){
						nameBase.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
					}

					//					nameBase.setId(Integer.parseInt(strId));
					//ImportHelper.setOriginalSource(nameBase, config.getSourceReference(), strId, idNamespace);

					
					/**
					 *  BFN does not want any name matching yet
					 */
//					TaxonBase<?> taxonBase = null;
//					//TODO find best matching Taxa
//					Pager<TaxonNameBase> names = getNameService().findByTitle(null, nameBase.getTitleCache(), null, null, null, null, null, null);
//					//TODO  correct handling for pager
//					List<TaxonNameBase> nameList = names.getRecords();
//					if (nameList.isEmpty()){
//						taxonBase = Taxon.NewInstance(nameBase, config.getSourceReference());	
//					}else{
//						taxonBase = Taxon.NewInstance(nameList.get(0), config.getSourceReference());
//						if (nameList.size()>1){
//							logger.warn("More than 1 matching taxon name found for " + nameBase.getTitleCache());
//						}
//					}

//					taxon = (Taxon) taxonBase;
					taxon = Taxon.NewInstance(nameBase, config.getSourceReference());
					taxon.addImportSource(uniqueID.toString(), config.getBfnXmlNamespace().toString(), null, null);
				} catch (UnknownCdmTypeException e) {
					success.setValue(false); 
				}
			}
		}
		return taxon;
	}
	
	/**
	 * Matches the XML attributes against CDM entities.<BR>
	 * Imports Scientific Name, Rank etc. and create a synonym.<br>
	 * <b>Existing synonym names won't be matched yet</b>
	 * 
	 * @param taxon
	 * @param success
	 * @param obligatory
	 * @param bfnNamespace
	 * @param childElementName
	 * @param elSynonyms
	 * @param taxonId
	 * @param config
	 */

	@SuppressWarnings({ "unchecked" })
	private void createOrUpdateSynonym(Taxon taxon, ResultWrapper<Boolean> success, boolean obligatory, Namespace bfnNamespace, 
			     String childElementName, Element elSynonyms, String taxonId, BfnXmlImportConfigurator config) {
		
		String childName;
		List<Element> elSynonymList = (List<Element>)elSynonyms.getChildren(childElementName, bfnNamespace);

		for(Element elSyn:elSynonymList){
			Rank rank = null;
			String strAuthor = null;
			String strSupplement = null;
			childName = "WISSNAME";
			Element elSynScientificName = XmlHelp.getSingleChildElement(success, elSyn, childName, bfnNamespace, obligatory);

			childElementName = "NANTEIL";
			List<Element> elSynDetails = (List<Element>)elSynScientificName.getChildren(childElementName, bfnNamespace);

			for(Element elSynDetail:elSynDetails){
				if(elSynDetail.getAttributeValue("bereich").equalsIgnoreCase("Rang")){
					String strRank = elSynDetail.getTextNormalize();
					rank = makeRank(strRank);
				}
				if(elSynDetail.getAttributeValue("bereich").equalsIgnoreCase("Autoren")){
					strAuthor = elSynDetail.getTextNormalize();
				}	
				if(elSynDetail.getAttributeValue("bereich", bfnNamespace).equalsIgnoreCase("Zusätze")){
					strSupplement = elSynDetail.getTextNormalize();
				}
				if(elSynDetail.getAttributeValue("bereich").equalsIgnoreCase("wissName")){
					try{
						TaxonNameBase<?, ?> nameBase = parseNonviralNames(rank,strAuthor,strSupplement,elSynDetail);

						//TODO find best matching Taxa
						Synonym synonym = Synonym.NewInstance(nameBase, config.getSourceReference());
						taxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
						
					} catch (UnknownCdmTypeException e) {
						logger.warn("Name with id " + taxonId + " has unknown nomenclatural code.");
						success.setValue(false); 
					}
				
				}
				
			}
		}
	}

	/**
	 * 
	 * @param taxon
	 * @param bfnNamespace
	 * @param childElementName
	 * @param elInformations
	 * @param state
	 */

	@SuppressWarnings("unchecked")
	private void createOrUpdateInformation(Taxon taxon,
			Namespace bfnNamespace, String childElementName,
			Element elInformations, 
			BfnXmlImportState state) {

		List<Element> elInformationList = (List<Element>)elInformations.getChildren(childElementName, bfnNamespace);
        
		//TODO
		TaxonDescription taxonDescription = getTaxonDescription(taxon, false, true);
		for(Element elInfo:elInformationList){

			childElementName = "IWERT";
			List<Element> elInfoDetailList = (List<Element>)elInfo.getChildren(childElementName, bfnNamespace);

			for(Element elInfoDetail : elInfoDetailList){
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("RL Kat.")){
					makeFeatures(taxonDescription, elInfoDetail, state, false);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("Kat. +/-")){
					makeFeatures(taxonDescription, elInfoDetail, state, false);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("aktuelle Bestandsstituation")){
					makeFeatures(taxonDescription, elInfoDetail, state, false);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("langfristiger Bestandstrend")){
					makeFeatures(taxonDescription, elInfoDetail, state, false);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("kurzfristiger Bestandstrend")){
					makeFeatures(taxonDescription, elInfoDetail, state, false);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("Risikofaktoren")){
					makeFeatures(taxonDescription, elInfoDetail, state, false);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("Verantwortlichkeit")){
					makeFeatures(taxonDescription, elInfoDetail, state, false);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("alte RL- Kat.")){
					makeFeatures(taxonDescription, elInfoDetail, state, false);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("Neobiota")){
					makeFeatures(taxonDescription, elInfoDetail, state, false);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("Eindeutiger Code")){
					makeFeatures(taxonDescription, elInfoDetail, state, false);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("Kommentar zur Taxonomie")){
					makeFeatures(taxonDescription, elInfoDetail, state, true);
				}
				if(elInfoDetail.getAttributeValue("standardname").equalsIgnoreCase("Kommentar zur Gefährdung")){
					makeFeatures(taxonDescription, elInfoDetail, state, true);
				}
			}
		}
	}

	/**
	 * 
	 * @param taxonDescription
	 * @param elInfoDetail
	 * @param state
	 * @param isTextData
	 */
	private void makeFeatures(
			TaxonDescription taxonDescription,
			Element elInfoDetail,
			BfnXmlImportState state,
			boolean isTextData) {
		String transformedRlKatValue = null;
		UUID featureUUID = null;
		UUID stateTermUUID = null;
		String strRlKatValue = elInfoDetail.getChild("WERT").getValue();
		String strRlKat = elInfoDetail.getAttributeValue("standardname");
		boolean randomStateUUID = false;
		try {
			featureUUID = BfnXmlTransformer.getRedlistFeatureUUID(strRlKat);
			transformedRlKatValue = BfnXmlTransformer.redListString2RedListCode(strRlKatValue);
		} catch (UnknownCdmTypeException e) {
			transformedRlKatValue = strRlKatValue;
		}
		Feature redListFeature = getFeature(state, featureUUID);
		State rlState = null;
		//if is text data a state is not needed
		if(!isTextData){
			try {
				stateTermUUID = BfnXmlTransformer.getRedlistStateTermUUID(transformedRlKatValue, strRlKat);
			} catch (UnknownCdmTypeException e) {
				stateTermUUID = UUID.randomUUID();
				randomStateUUID = true;
			}
			if(randomStateUUID || stateTermUUID == BfnXmlTransformer.stateTermEmpty){
				if(stateTermUUID == BfnXmlTransformer.stateTermEmpty)
					transformedRlKatValue = "keine Angabe";
				rlState = getStateTerm(state, stateTermUUID, transformedRlKatValue, transformedRlKatValue, transformedRlKatValue, null);
			}else{
				rlState = getStateTerm(state, stateTermUUID);
			}
		}
		if(isTextData){
			TextData textData = TextData.NewInstance(redListFeature);
			textData.putText(Language.GERMAN(), strRlKatValue);
			DescriptionElementBase descriptionElement = textData;
			taxonDescription.addElement(descriptionElement);
		}else{
			CategoricalData catData = CategoricalData.NewInstance(rlState, redListFeature);
			DescriptionElementBase descriptionElement = catData;
			taxonDescription.addElement(descriptionElement);
		}
	}
	
	/**
	 * Returns the rank represented by the rank element.<br>
	 * Returns <code>null</code> if the element is null.<br>
	 * Returns <code>null</code> if the code and the text are both either empty or do not exists.<br>
	 * Returns the rank represented by the code attribute, if the code attribute is not empty and could be resolved.<br>
	 * If the code could not be resolved it returns the rank represented most likely by the elements text.<br>
	 * Returns UNKNOWN_RANK if code attribute and element text could not be resolved.
	 * @param strRank bfn rank element
	 * @return 
	 */
	protected static Rank makeRank(String strRank){
		Rank result;
 		if (strRank == null){
			return null;
		}	
		Rank codeRank = null;
		try {
			codeRank = BfnXmlTransformer.rankCode2Rank(strRank);
		} catch (UnknownCdmTypeException e1) {
			codeRank = Rank.UNKNOWN_RANK();
		}
		//codeRank exists
		if ( (codeRank != null) && !codeRank.equals(Rank.UNKNOWN_RANK())){
			result = codeRank;
		}
		//codeRank does not exist
		else{
			result = codeRank;
			logger.warn("string rank used, because code rank does not exist or was not recognized: " + codeRank.toString() +" "+strRank);
		}
		return result;
	}

	/**
	 * @param rank
	 * @param strAuthor
	 * @param strSupplement 
	 * @param elWissName
	 * @return
	 * @throws UnknownCdmTypeException
	 */
	private TaxonNameBase<?, ?> parseNonviralNames(Rank rank, String strAuthor, String strSupplement, Element elWissName)
			throws UnknownCdmTypeException {
		TaxonNameBase<?,?> taxonNameBase = null;

		NomenclaturalCode nomCode = BfnXmlTransformer.nomCodeString2NomCode(strNomenclaturalCode);
		
		String strScientificName = elWissName.getTextNormalize();
		if(strSupplement != null && !strSupplement.isEmpty()){
			strScientificName = StringUtils.remove(strScientificName, strSupplement);
		}

		NonViralName<?> nonViralName = null;
		NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
		nonViralName = parser.parseFullName(strScientificName, nomCode, rank);
		if(nonViralName.hasProblem()){
//			logger.info("Problems: "+nonViralName.hasProblem());
			//TODO handle parsing Problems
			
			for(ParserProblem p:nonViralName.getParsingProblems()){
				
				logger.info(++parsingProblemCounter + " " +nonViralName.toString() +" "+p.toString());
			}
		}
		Rank parsedRank = nonViralName.getRank();
		if(parsedRank != rank){
			nonViralName.setRank(rank);
		}
			
//		nonViralName.setNameCache(strScientificName);
		taxonNameBase = nonViralName;
		return taxonNameBase;
	}
	
	@Override
	protected boolean isIgnore(BfnXmlImportState state){
		return ! state.getConfig().isDoTaxonNames();
	}
}
