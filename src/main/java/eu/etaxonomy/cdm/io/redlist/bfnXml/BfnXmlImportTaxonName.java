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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
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
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
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
	private static int i = 0;
	
	public BfnXmlImportTaxonName(){
		super();
	}

	@Override
	public boolean doCheck(BfnXmlImportState state){
		boolean result = true;
		return result;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doInvoke(BfnXmlImportState state){
		ITaxonService taxonService = getTaxonService();

		logger.info("start make TaxonNames...");
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		
		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "TaxonName";

		BfnXmlImportConfigurator config = state.getConfig();
		Element elDataSet = getDataSetElement(config);
		Namespace bfnNamespace = config.getBfnXmlNamespace();
		
		
		childName = "TAXONYME";
		obligatory = false;
		Element elTaxonNames = XmlHelp.getSingleChildElement(success, elDataSet, childName, bfnNamespace, obligatory);

		String bfnElementName = "TAXONYM";
		List<Element> elTaxonList = (List<Element>)elTaxonNames.getChildren(bfnElementName, bfnNamespace);
		
		//for each taxonName
		for (Element elTaxon : elTaxonList){

			String taxonId = elTaxon.getAttributeValue("reihenfolge");
			childName = "WISSNAME";
			Element elWissName = XmlHelp.getSingleChildElement(success, elTaxon, childName, bfnNamespace, obligatory);
			String childElementName = "NANTEIL";
			Taxon taxon = createOrUpdateTaxon(taxonMap, success, idNamespace, config, bfnNamespace, elWissName, childElementName, taxonId);
			
			//for each synonym
			childName = "SYNONYME";
			Element elSynonyms = XmlHelp.getSingleChildElement(success, elTaxon, childName, bfnNamespace, obligatory);
			if(elSynonyms != null){
				childElementName = "SYNONYM";
				createOrUpdateSynonym(taxon, success, obligatory, bfnNamespace, childElementName,elSynonyms, taxonId, config);
			}
			
			//for each information concerning the taxon element
			//TODO Information block
			childName = "INFORMATIONEN";
			Element elInformations = XmlHelp.getSingleChildElement(success, elTaxon, childName, bfnNamespace, obligatory);
			if(elInformations != null){
				childElementName = "BEZUGSRAUM";
				createOrUpdateInformation(taxon, success, obligatory, bfnNamespace, childElementName,elInformations, taxonId, config, state);
			}
			taxonMap.put(Integer.parseInt(taxonId), taxon);

		}
		taxonService.save(taxonMap.objects());
		createOrUdateClassification(config, taxonService);
		
		logger.info("end makeTaxonNames ...");
		if (!success.getValue()){
			state.setUnsuccessfull();
		}

		return;

	}


	/**
	 * @param config 
	 * @param taxonService 
	 * @param config 
	 * @return 
	 */
	@SuppressWarnings("rawtypes")
	private boolean createOrUdateClassification(BfnXmlImportConfigurator config, ITaxonService taxonService) {
		boolean isNewClassification = true;
		Classification classification = Classification.NewInstance(config.getClassificationName(), config.getSourceReference());
		
		List<Classification> classificationList = getClassificationService().list(Classification.class, null, null, null, VOC_CLASSIFICATION_INIT_STRATEGY);
		for(Classification c : classificationList){
			if(c.getTitleCache().equalsIgnoreCase(classification.getTitleCache())){
				classification = c;
				isNewClassification = false;
			}
		}
		ArrayList<TaxonBase> taxonBaseList = (ArrayList<TaxonBase>) taxonService.list(TaxonBase.class, null, null, null, VOC_CLASSIFICATION_INIT_STRATEGY);
		for(TaxonBase tb:taxonBaseList){
			if(tb instanceof Taxon){
				Taxon taxon = (Taxon) tb;
				classification.addChildTaxon(taxon, null, null);
			}
		}
		IClassificationService classificationService = getClassificationService();
		classificationService.saveOrUpdate(classification);
		return isNewClassification;
	}

	

	/**
	 * @param taxonNameMap
	 * @param success
	 * @param idNamespace
	 * @param config
	 * @param bfnNamespace
	 * @param elTaxonName
	 * @param childElementName
	 * @param taxonId 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Taxon createOrUpdateTaxon(MapWrapper<TaxonBase> taxonMap,
			ResultWrapper<Boolean> success, String idNamespace,
			BfnXmlImportConfigurator config, Namespace bfnNamespace,
			Element elTaxonName, String childElementName, String taxonId) {
		
		List<Element> elWissNameList = (List<Element>)elTaxonName.getChildren(childElementName, bfnNamespace);
		Rank rank = null;
		String strAuthor = null;
		String strSupplement = null;
		Taxon taxon = null;
		for(Element elWissName:elWissNameList){

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
					if(strSupplement.equalsIgnoreCase("nom. illeg.")){
						nameBase.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
					}

					//					nameBase.setId(Integer.parseInt(strId));
					
//					ImportHelper.setOriginalSource(nameBase, config.getSourceReference(), strId, idNamespace);
					//TaxonBase<?> taxonBase = null;
					//TODO find best matching Taxa
//					taxonBase = getTaxonService().findBestMatchingTaxon(titlecache);
					//getTaxonService().findTitleCache(null, titlecache, null, null, null, null);
//					if(taxonBase != null){
//						logger.info("Found Taxon in Database and updated it..." + titlecache);
//					}else{
					//taxonBase = Taxon.NewInstance(nameBase, config.getSourceReference());
//					}
//					taxonMap.put(taxonId, taxonBase);

					taxon = Taxon.NewInstance(nameBase, config.getSourceReference());
				} catch (UnknownCdmTypeException e) {
					success.setValue(false); 
				}
			}
		}
		return taxon;
	}
	
	/**
	 * @param taxonMap 
	 * @param success
	 * @param obligatory
	 * @param bfnNamespace
	 * @param childElementName
	 * @param elSynonyms
	 * @param taxon 
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
	 * @param taxonMap
	 * @param success
	 * @param obligatory
	 * @param bfnNamespace
	 * @param childElementName
	 * @param elInformations
	 * @param taxonId
	 * @param config
	 */
	@SuppressWarnings("unchecked")
	private void createOrUpdateInformation(Taxon taxon,
			ResultWrapper<Boolean> success, boolean obligatory,
			Namespace bfnNamespace, String childElementName,
			Element elInformations, String taxonId,
			BfnXmlImportConfigurator config, 
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
	 * @param descriptionElementList
	 * @param elInfoDetail
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
			result = null;
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
		if(!strSupplement.isEmpty() && strSupplement != null){
			strScientificName = StringUtils.remove(strScientificName, strSupplement);
		}

		NonViralName<?> nonViralName = null;
		NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
		nonViralName = parser.parseFullName(strScientificName, nomCode, rank);
		if(nonViralName.hasProblem()){
//			logger.info("Problems: "+nonViralName.hasProblem());
			//TODO handle parsing Problems
			
			for(ParserProblem p:nonViralName.getParsingProblems()){
				
				logger.info(++i + " " +nonViralName.toString() +" "+p.toString());
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
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BfnXmlImportState state){
		return ! state.getConfig().isDoTaxonNames();
	}
    /** Hibernate classification vocabulary initialisation strategy */
    private static final List<String> VOC_CLASSIFICATION_INIT_STRATEGY = Arrays.asList(new String[] {		
            "classification.$",
    		"classification.rootNodes",
    		"childNodes",
    		"childNodes.taxon",
            "childNodes.taxon.name",
            "taxonNodes",
            "taxonNodes.taxon",
            "synonymRelations",
            "taxon.*",
            "taxon.descriptions",
            "taxon.sec",
            "taxon.name.*",
            "taxon.synonymRelations",
            "termVocabulary",
            "terms"
    });


}
