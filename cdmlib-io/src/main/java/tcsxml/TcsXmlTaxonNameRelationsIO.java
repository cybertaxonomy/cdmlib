package tcsxml;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;


public class TcsXmlTaxonNameRelationsIO extends TcsXmlIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsXmlTaxonNameRelationsIO.class);

	private static int modCount = 5000;
	
	public TcsXmlTaxonNameRelationsIO(){
		super();
	}
	
	@Override
	public boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for TaxonNameRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);
		
		return result;
	}
	
	@Override
	public boolean doInvoke(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores){
		
		
		logger.info("start make taxon name relations ...");
		MapWrapper<TaxonNameBase<?,?>> taxonNameMap = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		INameService nameService = config.getCdmAppController().getNameService();

		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();

		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "TaxonName";

		TcsXmlImportConfigurator tcsConfig = (TcsXmlImportConfigurator)config;
		Element elDataSet = super. getDataSetElement(tcsConfig);
		Namespace tcsNamespace = tcsConfig.getTcsXmlNamespace();
		
		DoubleResult<Element, Boolean> doubleResult;
		childName = "TaxonNames";
		obligatory = false;
		Element elTaxonNames = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
		
		String tcsElementName = "TaxonName";
		List<Element> elTaxonNameList = elTaxonNames.getChildren(tcsElementName, tcsNamespace);

//		Element source = tcsConfig.getSourceRoot();
		
		int i = 0;
		int nameRelCount = 0;
		//for each taxonName
		for (Element elTaxonName : elTaxonNameList){
			
			
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			
			
			
			//Basionyms
			tcsElementName = "Basionym";
			List<Element> elBasionymList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			
			for (Element elBasionym: elBasionymList){
				nameRelCount++;
				logger.debug("BASIONYM "+  nameRelCount);
				
				NameRelationshipType relType = NameRelationshipType.BASIONYM();
				boolean inverse = false;
				
				String id = elTaxonName.getAttributeValue("id", elTaxonName.getNamespace());
//				TaxonNameBase<?,?> fromName = taxonNameMap.get(id);
				
				makeNomenclaturalNoteType(tcsConfig, elBasionym, relType, taxonNameMap, nameStore, id, inverse);
			}
		}// end Basionyms
		
		//Other Relations
		//TODO
		
		logger.info(nameRelCount + " nameRelations handled");
		nameService.saveTaxonNameAll(nameStore);
		logger.info("end make taxon name relationships ...");
		return success.getValue();
	}
	
	private  boolean makeNomenclaturalNoteType(TcsXmlImportConfigurator tcsConfig, Element elRelation, NameRelationshipType relType, MapWrapper<TaxonNameBase<?,?>> taxonNameMap, Set<TaxonNameBase> nameStore, String id, boolean inverse){
		if (elRelation == null){
			return false;
		}
		Namespace ns = elRelation.getNamespace();
		
		String ruleConsidered = elRelation.getChildText("RuleConsidered", ns);
		String note = elRelation.getChildText("Note", ns);
		String microReference = elRelation.getChildText("MicroReference", ns);
		Element elRelatedName = elRelation.getChild("RelatedName", ns);
		//TODO relType
		String relatedNameId = elRelatedName.getAttributeValue("ref", ns);
		
		TaxonNameBase<?,?> fromName = taxonNameMap.get(id);
		TaxonNameBase<?,?> toName = taxonNameMap.get(relatedNameId);
		if (fromName == null){
			logger.warn("fromName (" + id + ") not found in Map! Relationship not set!");
			return false;
		}
		if (toName == null){
			logger.warn("toName (" + id + ") not found in Map! Relationship not set!");
			return false;
		}

		
		//TODO note, microreference
		if (inverse == false){
			toName.addRelationshipToName(fromName, relType, ruleConsidered);
		}else{
			fromName.addRelationshipToName(toName, relType, ruleConsidered);
		}
		nameStore.add(fromName);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoRelNames();
	}

}
