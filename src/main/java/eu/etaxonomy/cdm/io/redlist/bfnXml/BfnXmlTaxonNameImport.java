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
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.lsid.impl.TaxonMetadataMapper;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.tcsxml.TcsXmlTransformer;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
/**
 * 
 * @author a.oppermann
 * @date 04.07.2013
 *
 */
@Component("bfnXmlTaxonNameIO")
public class BfnXmlTaxonNameImport extends BfnXmlImportBase implements ICdmIO<BfnXmlImportState> {
	private static final Logger logger = Logger.getLogger(BfnXmlTaxonNameImport.class);

	private static final String strNomenclaturalCode = "Botanical";//NomenclaturalCode.ICBN.toString();

	private static int modCount = 5000;
	
	public BfnXmlTaxonNameImport(){
		super();
	}

	@Override
	public boolean doCheck(BfnXmlImportState state){
		boolean result = true;
		logger.warn("BasionymRelations not yet implemented");
		logger.warn("Checking for TaxonNames not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);
		
		return result;
	}

	//@SuppressWarnings("unchecked")
	@SuppressWarnings("unchecked")
	@Override
	public void doInvoke(BfnXmlImportState state){
		ITaxonService taxonService = getTaxonService();


		logger.info("start make TaxonNames ...");
		MapWrapper<Person> authorMap = (MapWrapper<Person>)state.getStore(ICdmIO.TEAM_STORE);
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<Reference> referenceMap =  (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);
		
		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "TaxonName";

		BfnXmlImportConfigurator config = state.getConfig();
		Element elDataSet = getDataSetElement(config);
		Namespace bfnNamespace = config.getBfnXmlNamespace();
		Classification classification = Classification.NewInstance(config.getClassificationName(), config.getSourceReference());

		
		
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
			makeTaxon(taxonMap, success, idNamespace, config, bfnNamespace, elWissName, childElementName, taxonId);
			
			//for each synonym
			childName = "SYNONYME";
			Element elSynonyms = XmlHelp.getSingleChildElement(success, elTaxon, childName, bfnNamespace, obligatory);
			if(elSynonyms != null){
				childElementName = "SYNONYM";
				makeSynonym(taxonMap, success, obligatory, bfnNamespace, childElementName,elSynonyms, taxonId, config);
			}
			
			//for each information concerning the taxon element
			//TODO Information block

		}
//		Collection<? extends TaxonNameBase> col = taxonNameMap.objects();
//		getNameService().save((Collection)col);
		taxonService.save(taxonMap.objects());
		
		createClassification(classification, taxonService);
		
		logger.info("end makeTaxonNames ...");
		if (!success.getValue()){
			state.setUnsuccessfull();
		}

		return;

	}

	/**
	 * @param classification
	 * @param taxonService 
	 * @param config 
	 */
	private void createClassification(Classification classification, ITaxonService taxonService) {
		ArrayList<TaxonBase> taxonBaseList = (ArrayList<TaxonBase>) taxonService.list(TaxonBase.class, null, null, null, VOC_CLASSIFICATION_INIT_STRATEGY);
		for(TaxonBase tb:taxonBaseList){
			if(tb instanceof Taxon){
				Taxon taxon = (Taxon) tb;
//				TaxonNode tn = new TaxonNode(taxon);
				classification.addChildTaxon(taxon, null, null, null);
//				classification.addParentChild(taxon, taxon, null, null);
			}
		}
		IClassificationService classificationService = getClassificationService();
		classificationService.saveOrUpdate(classification);
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
	private void makeSynonym(MapWrapper<TaxonBase> taxonMap, ResultWrapper<Boolean> success, boolean obligatory, Namespace bfnNamespace, 
			     String childElementName, Element elSynonyms, String taxonId, BfnXmlImportConfigurator config) {
		
		
		TaxonNameBase<?, ?> taxonNameBase = null;
		String childName;
		List<Element> elSynonymList = (List<Element>)elSynonyms.getChildren(childElementName, bfnNamespace);

		//Element elSynonm = XmlHelp.getSingleChildElement(success, elSynonms, childName, bfnNamespace, obligatory);
		
		for(Element elSyn:elSynonymList){
			Rank rank = null;
			String strAuthor = null;
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
				//TODO save Synonym
				if(elSynDetail.getAttributeValue("bereich").equalsIgnoreCase("wissName")){
					try{
						TaxonNameBase<?, ?> nameBase = createTaxonOrSynonym(rank,strAuthor, elSynDetail);

						TaxonBase<?> taxonBase = null;
						String titlecache = elSynDetail.getTextNormalize();
						Taxon taxon = (Taxon)taxonMap.get(taxonId);
						
						taxonBase = getTaxonService().findBestMatchingSynonym(titlecache);
						//find best matching Synonym:
						if(taxonBase != null){
							taxon.addSynonym((Synonym)taxonBase, SynonymRelationshipType.SYNONYM_OF());
							taxonBase = taxon;
							logger.info("found existing Synonym and updated record" + titlecache);
						}else{
							Synonym synonym = Synonym.NewInstance(nameBase, config.getSourceReference());
							taxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
							taxonBase = taxon;
						}
						taxonMap.put(taxonId, taxonBase);
						
					} catch (UnknownCdmTypeException e) {
						logger.warn("Name with id " + taxonId + " has unknown nomenclatural code.");
						success.setValue(false); 
					}
				
				}
				
			}
		}
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
	private void makeTaxon(MapWrapper<TaxonBase> taxonMap,
			ResultWrapper<Boolean> success, String idNamespace,
			BfnXmlImportConfigurator config, Namespace bfnNamespace,
			Element elTaxonName, String childElementName, String taxonId) {
		
		List<Element> elWissNameList = (List<Element>)elTaxonName.getChildren(childElementName, bfnNamespace);
		Rank rank = null;
		String strId = null;
		String strAuthor = null;
		for(Element elWissName:elWissNameList){

			if(elWissName.getAttributeValue("bereich", bfnNamespace).equalsIgnoreCase("Autoren")){
				strAuthor = elWissName.getTextNormalize();
			}
			if(elWissName.getAttributeValue("bereich", bfnNamespace).equalsIgnoreCase("Eindeutiger Code")){
				strId = elWissName.getTextNormalize();
			}
			if(elWissName.getAttributeValue("bereich", bfnNamespace).equalsIgnoreCase("Rang")){
				String strRank = elWissName.getTextNormalize();
				rank = makeRank(strRank);
			}
			if(elWissName.getAttributeValue("bereich", bfnNamespace).equalsIgnoreCase("wissName")){
				try{
					TaxonNameBase<?, ?> nameBase = createTaxonOrSynonym(rank,strAuthor, elWissName);
					//nameBase.setTitleCache(strScientificName, false);

					ImportHelper.setOriginalSource(nameBase, config.getSourceReference(), strId, idNamespace);
					
					
					TaxonBase<?> taxonBase = null;
					//find best matching Taxa
					String titlecache = elWissName.getTextNormalize();
					taxonBase = getTaxonService().findBestMatchingTaxon(titlecache);
					
					if(taxonBase != null){
						logger.info("Found Taxon in Database and updated it..." + titlecache);
					}else{
						taxonBase = Taxon.NewInstance(nameBase, config.getSourceReference());
					}
					taxonMap.put(taxonId, taxonBase);
					//TODO Check if taxonNameMap is necessary
				//	taxonNameMap.put(strId, nameBase);
										
				} catch (UnknownCdmTypeException e) {
					logger.warn("Name with id " + strId + " has unknown nomenclatural code.");
					success.setValue(false); 
				}
			}
		}
		if(strId == null){
			logger.warn("TaxonID could not be retrieved...");
		}
	}

	/**
	 * @param rank
	 * @param strAuthor
	 * @param elWissName
	 * @return
	 * @throws UnknownCdmTypeException
	 */
	private TaxonNameBase<?, ?> createTaxonOrSynonym(Rank rank, String strAuthor, Element elWissName)
			throws UnknownCdmTypeException {
		TaxonNameBase<?,?> nameBase = null;

		NomenclaturalCode nomCode = BfnXmlTransformer.nomCodeString2NomCode(strNomenclaturalCode);
		if (nomCode != null){
			nameBase = nomCode.getNewTaxonNameInstance(rank);
		}else{
			nameBase = NonViralName.NewInstance(rank);
		}
		String strScientificName = elWissName.getTextNormalize();
		strScientificName = StringUtils.remove(strScientificName, strAuthor);					

		NonViralName<?> nonViralName = null;

		NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
		nonViralName = parser.parseFullName(strScientificName, nomCode, rank);
		try {
			parser.parseAuthors(nonViralName, strAuthor);
		} catch (StringNotParsableException e) {
			nonViralName.setAuthorshipCache(strAuthor); 
		}
		nonViralName.setNameCache(strScientificName);
		nameBase = nonViralName;

		return nameBase;
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
			logger.warn("string rank used, because code rank does not exist or was not recognized: " + codeRank.toString());
		}
		return result;
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
            "taxon.*",
            "taxon.sec",
            "taxon.name.*",
            "taxon.synonymRelations"
    });


}
