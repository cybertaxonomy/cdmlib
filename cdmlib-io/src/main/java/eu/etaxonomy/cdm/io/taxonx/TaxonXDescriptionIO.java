/**
 * 
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
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 *
 */
public class TaxonXDescriptionIO extends CdmIoBase<IImportConfigurator> implements ICdmIO<IImportConfigurator> {
	private static final Logger logger = Logger.getLogger(TaxonXDescriptionIO.class);

	private static int modCount = 10000;

	public TaxonXDescriptionIO(){
		super();
	}
	
	public boolean doCheck(IImportConfigurator config){
		
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
	
	public boolean doInvoke(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores){
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
		
		TaxonXImportConfigurator txConfig = (TaxonXImportConfigurator)config;
		Element root = txConfig.getSourceRoot();
		Namespace nsTaxonx = root.getNamespace();
		
		//Object source = config.getSource();
		ITaxonService taxonService = config.getCdmAppController().getTaxonService();
		
		logger.info("start make Descriptions ...");
		
		
		//for testing only
		Taxon taxon = getTaxon(txConfig);
		unlazyDescription(txConfig, taxon);
		TaxonDescription description = TaxonDescription.NewInstance();
		
		Element elTaxonBody = root.getChild("taxonxBody", nsTaxonx);
		Element elTreatment = elTaxonBody.getChild("treatment", nsTaxonx);
		List<Element> elDivs = elTreatment.getChildren("div", nsTaxonx);
		for (Element div : elDivs){
			Attribute attrType = div.getAttribute("type", nsTaxonx);
			String strType = attrType.getValue();
			try {
				Feature feature = TaxonXTransformer.descriptionType2feature(strType);
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
			} catch (UnknownCdmTypeException e) {
				logger.warn(e.getMessage());
			}
		}
		if (description.size() >0){
			taxon.addDescription(description);
			taxonService.saveTaxon(taxon);
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
		ICommonService commonService = config.getCdmAppController().getCommonService();
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
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoFacts();
	}

}
