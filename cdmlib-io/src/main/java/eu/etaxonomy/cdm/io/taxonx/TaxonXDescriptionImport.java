/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.taxonx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.TermServiceImpl;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.tcsrdf.TcsRdfImportConfigurator;
import eu.etaxonomy.cdm.io.tcsrdf.TcsRdfImportState;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
@Component
public class TaxonXDescriptionImport extends CdmIoBase<TaxonXImportState> implements ICdmIO<TaxonXImportState> {
	private static final Logger logger = Logger.getLogger(TaxonXDescriptionImport.class);

	private static int modCount = 10000;

	public TaxonXDescriptionImport(){
		super();
	}
	
	public boolean doCheck(TaxonXImportState state){
		boolean result = true;
		logger.warn("Checking for Facts not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	public Map<Integer, Feature> fillFactCategories(IImportConfigurator config, CdmApplicationController cdmApp){
		logger.warn("not yet implemented");
		Map<Integer, Feature> featureMap = new HashMap<Integer, Feature>();
//		IDescriptionService descriptionService = cdmApp.getDescriptionService();
//		ITermService termService = cdmApp.getTermService();
//
//		Object source = config.getSource();
		return featureMap;
	}
	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
//	 */
//	@Override
//	protected boolean doInvoke(IImportConfigurator config, 
//			Map<String, MapWrapper<? extends CdmBase>> stores){ 
//		TaxonXImportState state = ((TaxonXImportConfigurator)config).getState();
//		state.setConfig((TaxonXImportConfigurator)config);
//		return doInvoke(state);
//	}
	
	public boolean doInvoke(TaxonXImportState state){
		logger.debug("not yet fully implemented");
		
//		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);//   (MapWrapper<TaxonBase>)(storeArray[0]);
//		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
//		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
//		MapWrapper<Feature> featureMap = (MapWrapper<Feature>)stores.get(ICdmIO.FEATURE_STORE);
			
		//	invokeFactCategories(config, cdmApp);
		
		//make features
		//Map<Integer, Feature> featureMap = 
		//fillFactCategories(config, cdmApp);
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		
		TaxonXImportConfigurator txConfig = state.getConfig();
		Element root = txConfig.getSourceRoot();
		Namespace nsTaxonx = root.getNamespace();
		
		//Object source = config.getSource();
		
		logger.info("start make Descriptions ...");
		
		
		//for testing only
		Taxon taxon = getTaxon(txConfig);
		if (taxon == null){
			logger.warn("Taxon could not be found");
			return false;
		}
		unlazyDescription(txConfig, taxon);
		TaxonDescription description = TaxonDescription.NewInstance();
		
		Element elTaxonBody = root.getChild("taxonxBody", nsTaxonx);
		Element elTreatment = elTaxonBody.getChild("treatment", nsTaxonx);
		List<Element> elDivs = elTreatment.getChildren("div", nsTaxonx);
		for (Element div : elDivs){
			Attribute attrType = div.getAttribute("type", nsTaxonx);
			String strType = attrType.getValue();
			Feature feature = null;
			try {
				feature = TaxonXTransformer.descriptionType2feature(strType);
			} catch (UnknownCdmTypeException e) {
				TermVocabulary<Feature> featureVoc = Feature.BIOLOGY_ECOLOGY().getVocabulary();
				for (Feature oneFeature : featureVoc.getTerms()){
					if (strType.equals(oneFeature.getLabel()) || strType.equals(oneFeature.getRepresentation(Language.DEFAULT() ).getText() )){
						feature = oneFeature;
					}
				}
				
				if (feature == null){
					feature = Feature.NewInstance(strType, strType, null);
					featureVoc.addTerm(feature);
					getTermService().save(feature);
					logger.warn(e.getMessage() + ". Feature was added to the feature vocabulary. " + getBracketSourceName(txConfig));
				}
			}
			String text = getText(div);
			if (!"".equals(CdmUtils.Nz(text).trim())){
				// FIXME hibernate throws an exception when a string is longer than approx. 4000 chars.
				// for now we truncate any description text to 4000 characters.
				if(text.length() > 4000){
					text = text.substring(0, 3900) + "... [text truncated]";
					logger.warn("FIXME - Truncation of text occurred.");
				}
				
				DescriptionElementBase descriptionElement = TextData.NewInstance(text, Language.ENGLISH(), null);
				descriptionElement.setFeature(feature);
				description.addElement(descriptionElement);
			}

		}
		if (description.size() >0){
			taxon.addDescription(description);
			getTaxonService().saveTaxon(taxon);
		}
		return true;
	}
	
	private String getText(Element div){
		String result = "";
		Iterator<Content> it =div.getDescendants(); 
		while (it.hasNext()){
			Content next = (Content)it.next();
			if (next instanceof Text){
				result += ((Text)next).getText();
			}
		}
		return result;
	}
	
	private Taxon getTaxon(TaxonXImportConfigurator config){
		Taxon result;
//		result =  Taxon.NewInstance(BotanicalName.NewInstance(null), null);
		//ICommonService commonService = config.getCdmAppController().getCommonService();
		ICommonService commonService = getCommonService();
		
		String originalSourceId = config.getOriginalSourceId();
		String namespace = config.getOriginalSourceTaxonNamespace();
		result = (Taxon)commonService.getSourcedObjectByIdInSource(Taxon.class, originalSourceId , namespace);
		if (result == null){
			logger.warn("Taxon (id: " + originalSourceId + ", namespace: " + namespace + ") could not be found");
		}
		return result;
	}
	
	private void unlazyDescription(TaxonXImportConfigurator config, Taxon taxon){
//		logger.warn("Preliminary commented");  //used single Transaction for all import instead !
//		TransactionStatus txStatus = config.getCdmAppController().startTransaction();
//		ITaxonService taxonService = config.getCdmAppController().getTaxonService();
//		taxonService.saveTaxon(taxon);
//		taxon.getDescriptions().size();
//		config.getCdmAppController().commitTransaction(txStatus);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(TaxonXImportState state){
		return ! state.getConfig().isDoFacts();
	}
	
	private String getBracketSourceName(TaxonXImportConfigurator config){
		return "(" + config.getSourceNameString() + ")";
	}

}
