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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CategoricalDataTest;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureDao;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;
/**
 * 
 * @author a.oppermann
 * @date 04.07.2013
 *
 */
@Component
public class BfnXmlImportFeature extends BfnXmlImportBase implements ICdmIO<BfnXmlImportState> {
	private static final Logger logger = Logger.getLogger(BfnXmlImportFeature.class);

	private static final String strNomenclaturalCode = "Botanical";
	private static int i = 0;
	
	public BfnXmlImportFeature(){
		super();
	}

	@Override
	public boolean doCheck(BfnXmlImportState state){
		boolean result = true;
		//TODO needs to be implemented
		return result;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doInvoke(BfnXmlImportState state){

		ITermService termService = getTermService();
		IVocabularyService vocabularyService = getVocabularyService();
		
		
		logger.info("start create Features in CDM...");
		MapWrapper<DefinedTermBase> featureMap = (MapWrapper<DefinedTermBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<StateData> stateMap = (MapWrapper<StateData>)state.getStore(ICdmIO.FEATURE_STORE);

		
		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "Features";

		BfnXmlImportConfigurator config = state.getConfig();
		Element elDataSet = getDataSetElement(config);
		Namespace bfnNamespace = config.getBfnXmlNamespace();
		
		
		childName = "EIGENSCHAFTEN";
		obligatory = false;
		Element elFeatureNames = XmlHelp.getSingleChildElement(success, elDataSet, childName, bfnNamespace, obligatory);

		String bfnElementName = "EIGENSCHAFT";
		List<Element> elFeatureList = (List<Element>)elFeatureNames.getChildren(bfnElementName, bfnNamespace);
		List<Feature> featureList = new ArrayList<>();
		//for each taxonName
		for (Element elFeature : elFeatureList){
			
			if(elFeature.getAttributeValue("standardname", bfnNamespace).equalsIgnoreCase("RL Kat.")){
				makeFeature(termService, vocabularyService, featureList,success, obligatory, idNamespace, config, bfnNamespace,elFeature);
			}
			if(elFeature.getAttributeValue("standardname").equalsIgnoreCase("Kat. +/-")){
				makeFeature(termService, vocabularyService, featureList,success, obligatory, idNamespace, config, bfnNamespace,elFeature);
			}
			if(elFeature.getAttributeValue("standardname").equalsIgnoreCase("aktuelle Bestandsstituation")){
				makeFeature(termService, vocabularyService, featureList,success, obligatory, idNamespace, config, bfnNamespace,elFeature);
			}
			if(elFeature.getAttributeValue("standardname").equalsIgnoreCase("langfristiger Bestandstrend")){
				makeFeature(termService, vocabularyService, featureList,success, obligatory, idNamespace, config, bfnNamespace,elFeature);
			}
			if(elFeature.getAttributeValue("standardname").equalsIgnoreCase("kurzfristiger Bestandstrend")){
				makeFeature(termService, vocabularyService, featureList,success, obligatory, idNamespace, config, bfnNamespace,elFeature);
			}
			if(elFeature.getAttributeValue("standardname").equalsIgnoreCase("Risikofaktoren")){
				makeFeature(termService, vocabularyService, featureList,success, obligatory, idNamespace, config, bfnNamespace,elFeature);
			}
			if(elFeature.getAttributeValue("standardname").equalsIgnoreCase("Verantwortlichkeit")){
				makeFeature(termService, vocabularyService, featureList,success, obligatory, idNamespace, config, bfnNamespace,elFeature);
			}
			if(elFeature.getAttributeValue("standardname").equalsIgnoreCase("alte RL- Kat.")){
				makeFeature(termService, vocabularyService, featureList,success, obligatory, idNamespace, config, bfnNamespace,elFeature);
			}
			if(elFeature.getAttributeValue("standardname").equalsIgnoreCase("Neobiota")){
				makeFeature(termService, vocabularyService, featureList,success, obligatory, idNamespace, config, bfnNamespace,elFeature);
			}

		}
		createFeatureTree(featureList);
//		termService.save(featureMap.objects());
		logger.info("end create features ...");
		if (!success.getValue()){
			state.setUnsuccessfull();
		}

		return;

	}

	/**
	 * @param featureList
	 */
	private void createFeatureTree(List<Feature> featureList) {
		FeatureTree featureTree = FeatureTree.NewInstance(featureList);
		String featureTreeName = "RedListFeatureTree";
		featureTree.setTitleCache(featureTreeName);
		featureTree.setLabel("RedListFeatureTree", Language.GERMAN());
		getFeatureTreeService().save(featureTree);
	}

	/**
	 * @param termService
	 * @param vocabularyService
	 * @param featureMap
	 * @param success
	 * @param obligatory
	 * @param idNamespace
	 * @param config
	 * @param bfnNamespace
	 * @param elFeature
	 */
	private void makeFeature(ITermService termService,
			IVocabularyService vocabularyService,
			List<Feature> featureList,
			ResultWrapper<Boolean> success, boolean obligatory,
			String idNamespace, BfnXmlImportConfigurator config,
			Namespace bfnNamespace, Element elFeature) {
		String childName;
		String strRlKat = elFeature.getAttributeValue("standardname");
		Feature redListCat = Feature.NewInstance(strRlKat, strRlKat, strRlKat);
		featureList.add(redListCat);
		termService.saveOrUpdate(redListCat);
		childName = "LISTENWERTE";
		Element elListValues = XmlHelp.getSingleChildElement(success, elFeature, childName, bfnNamespace, obligatory);
		String childElementName = "LWERT";
		createOrUpdateStates(success, idNamespace, config, bfnNamespace, elListValues, childElementName, redListCat);
		createOrUpdateTermVocabulary(vocabularyService, redListCat, "Feature");
	}

	/**
	 * @param vocabularyService
	 * @param term
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private TermVocabulary createOrUpdateTermVocabulary(IVocabularyService vocabularyService, DefinedTermBase term, String strTermVocabulary) {
		TermVocabulary termVocabulary = null;
		List<TermVocabulary> vocList = vocabularyService.list(TermVocabulary.class, null, null, null, VOC_CLASSIFICATION_INIT_STRATEGY);
		for(TermVocabulary tv : vocList){
			if(tv.getTitleCache().equalsIgnoreCase(strTermVocabulary)){
				termVocabulary = tv;
			}
		}
		if(termVocabulary == null){
			termVocabulary = TermVocabulary.NewInstance(strTermVocabulary, strTermVocabulary, strTermVocabulary, null);
		}
		termVocabulary.addTerm(term);			
		vocabularyService.saveOrUpdate(termVocabulary);
		
		return termVocabulary;
	}


	/**
	 * @param success
	 * @param idNamespace
	 * @param config
	 * @param bfnNamespace
	 * @param elListValues
	 * @param childElementName
	 * @param redListCat 
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes"})
	private void createOrUpdateStates(ResultWrapper<Boolean> success, String idNamespace,
			BfnXmlImportConfigurator config, Namespace bfnNamespace,
			Element elListValues, String childElementName, Feature redListCat) {

		List<Element> elListValueList = (List<Element>)elListValues.getChildren(childElementName, bfnNamespace);
		List<StateData> stateList = new ArrayList<StateData>();
		
		TermVocabulary termVocabulary = null;
		for(Element elListValue:elListValueList){
			String listValue = elListValue.getTextNormalize();
			String matchedListValue;
			try {
				matchedListValue = BfnXmlTransformer.redListString2RedListCode(listValue);
			} catch (UnknownCdmTypeException e) {
				
				matchedListValue = listValue;
				logger.info("no matched red list code found. \n" + e);
				
			}
			
			State state = State.NewInstance(matchedListValue,matchedListValue, matchedListValue);
					
			getTermService().saveOrUpdate(state);
			termVocabulary = createOrUpdateTermVocabulary(getVocabularyService(), state, redListCat.toString()+"States");
			
			StateData stateData = StateData.NewInstance(state);
//			featureMap.put(i++, state);
			stateList.add(stateData);
		}
		if(termVocabulary != null){
			redListCat.addSupportedCategoricalEnumeration(termVocabulary);
			getTermService().saveOrUpdate(redListCat);
		}
		
		CategoricalData catData = CategoricalData.NewInstance();
		catData.setFeature(redListCat);
		for(StateData sd: stateList){
			catData.addState(sd);
		}
//		catData.setStatesOnly(stateList);
		DescriptionElementBase descriptionElementBase = catData;
		getDescriptionService().saveDescriptionElement(descriptionElementBase);
//		
//		DescriptionBase<?> descriptionBase = null; 
//		descriptionBase.addElement(descriptionElementBase);
//		
		//featureMap.put(i++, descriptionBase);
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
            "taxon.sec",
            "taxon.name.*",
            "taxon.synonymRelations",
            "termVocabulary.*",
            "terms"

    });


}
