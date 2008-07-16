/**
 * 
 */
package eu.etaxonomy.cdm.io.taxonx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
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
public class TaxonXDescriptionIO extends CdmIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TaxonXDescriptionIO.class);

	private static int modCount = 10000;

	private static final String ioNameLocal = "TaxonXDescriptionIO";
	
	public TaxonXDescriptionIO(boolean ignore){
		super(ioNameLocal, ignore);
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
		IDescriptionService descriptionService = cdmApp.getDescriptionService();
		ITermService termService = cdmApp.getTermService();

		Object source = config.getSource();
		return featureMap;
	}
	
	public boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp, Map<String, MapWrapper<? extends CdmBase>> stores){
		logger.warn("not yet implemented");
		
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);//   (MapWrapper<TaxonBase>)(storeArray[0]);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
		MapWrapper<Feature> featureMap = (MapWrapper<Feature>)stores.get(ICdmIO.FEATURE_STORE);
			
		//	invokeFactCategories(config, cdmApp);
		
		//make features
		//Map<Integer, Feature> featureMap = 
		//fillFactCategories(config, cdmApp);
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		
		TaxonXImportConfigurator txConfig = (TaxonXImportConfigurator)config;
		Element root = txConfig.getSourceRoot();
		Namespace nsTaxonx = root.getNamespace();
		
		//Object source = config.getSource();
		ITaxonService taxonService = cdmApp.getTaxonService();
		
		logger.info("start make Descriptions ...");
		
		
		//for testing only
		Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(null), null);
		TaxonDescription description = TaxonDescription.NewInstance();
		
		taxon.addDescription(description);
		
		Element elTaxonBody = root.getChild("taxonxBody", nsTaxonx);
		Element elTreatment = elTaxonBody.getChild("treatment", nsTaxonx);
		List<Element> elDivs = elTreatment.getChildren("div", nsTaxonx);
		for (Element div : elDivs){
			Attribute attrType = div.getAttribute("type", nsTaxonx);
			String strType = attrType.getValue();
			try {
				Feature feature = TaxonXTransformer.descriptionType2feature(strType);
				String value = div.getTextTrim();
				if (!"".equals(CdmUtils.Nz(value).trim())){
					DescriptionElementBase desciptionElement = TextData.NewInstance(value, Language.ENGLISH(), null);
					desciptionElement.setFeature(feature);
					description.addElement(desciptionElement);
				}
			} catch (UnknownCdmTypeException e) {
				logger.warn(e.getMessage());
			}
			
		}
		taxonService.saveTaxon(taxon);
		
		return true;
	}
	
	private Taxon getTaxon(){
		logger.warn("not yet implemented");
		return null;
	}

}
