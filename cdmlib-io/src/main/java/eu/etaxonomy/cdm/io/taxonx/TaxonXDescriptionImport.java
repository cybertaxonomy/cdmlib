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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
@Component
public class TaxonXDescriptionImport extends CdmIoBase<TaxonXImportState> implements ICdmIO<TaxonXImportState> {
	private static final Logger logger = Logger.getLogger(TaxonXDescriptionImport.class);

	public TaxonXDescriptionImport(){
		super();
	}

	@Override
    public boolean doCheck(TaxonXImportState state){
		boolean result = true;
		logger.warn("Checking for Facts not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);

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

	private String getDescriptionTitle(TaxonXImportState state){
		String result = "Untitled";
		Reference ref = state.getModsReference();
		if (ref != null){
			result = ref.getTitle();
			if ( CdmUtils.isEmpty(result)){
				result = ref.getTitleCache();
			}
		}
		return result;
	}

	@Override
    public void doInvoke(TaxonXImportState state){
		logger.debug("not yet fully implemented");

		TaxonXImportConfigurator txConfig = state.getConfig();
		Element root = txConfig.getSourceRoot();
		Namespace nsTaxonx = root.getNamespace();

		//Object source = config.getSource();

		logger.info("start make Descriptions ...");


		//for testing only
		Taxon taxon = getTaxon(txConfig);
		if (taxon == null){
			logger.warn("Taxon could not be found");
			state.setUnsuccessfull();
		}

		Reference modsReference = state.getModsReference();
		if (modsReference == null){
			modsReference = state.getConfig().getSourceReference();
		}

		//unlazyDescription(txConfig, taxon);
		TaxonDescription description = TaxonDescription.NewInstance();
		description.setTitleCache(getDescriptionTitle(state), true);
		if (modsReference != null){
			description.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, modsReference, null);
		}

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
				feature = handleFeatureException(strType, e, txConfig);
			}
			String text = getText(div);
			if (!"".equals(CdmUtils.Nz(text).trim())){
				// hibernate throws an exception when a string is longer than approx. 65500 chars.
				// for now we truncate any description text to 65500 characters.
				if(text.length() > 65500){
					text = text.substring(0, 65500) + "... [text truncated]";
					logger.warn("Truncation of text: description for taxon " + taxon.getTitleCache() + " was longer than 65500 characters.");
				}

				DescriptionElementBase descriptionElement = TextData.NewInstance(text, Language.ENGLISH(), null);
				descriptionElement.setFeature(feature);
				description.addElement(descriptionElement);

				//add reference
				if (modsReference != null){
					descriptionElement.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, modsReference, null, null, null);
				}
			}

		}
		if (description.size() >0){
			taxon.addDescription(description);
			getTaxonService().save(taxon);
		}
		return;
	}

	private Feature handleFeatureException(String strType, UnknownCdmTypeException e, TaxonXImportConfigurator txConfig){
		Feature feature = null;
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
		return feature;
	}


	private String getText(Element div){
		String result = "";
		Iterator<Content> it =div.getDescendants();
		while (it.hasNext()){
			Content next = it.next();
			if (next instanceof Text){
				result += ((Text)next).getText();
			}
		}
		return result;
	}

	private Taxon getTaxon(TaxonXImportConfigurator config){
		Taxon result;
//		result =  Taxon.NewInstance(BotanicalName.NewInstance(null), null);
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
	@Override
    protected boolean isIgnore(TaxonXImportState state){
		return ! state.getConfig().isDoFacts();
	}

	private String getBracketSourceName(TaxonXImportConfigurator config){
		return "(" + config.getSourceNameString() + ")";
	}

}
