/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tcsrdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 29.05.2008
 * @version 1.0
 */
@Component
public class TcsRdfTaxonImport  extends TcsRdfImportBase implements ICdmIO<TcsRdfImportState> {
	private static final Logger logger = Logger.getLogger(TcsRdfTaxonImport.class);

	private static int modCount = 30000;
	
	public TcsRdfTaxonImport(){
		super();
	}
	
	
	@Override
	public boolean doCheck(TcsRdfImportState state){
		boolean result = true;
		logger.warn("Checking for Taxa not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	protected static CdmSingleAttributeXmlMapperBase[] standardMappers = new CdmSingleAttributeXmlMapperBase[]{
//		new CdmTextElementMapper("genusPart", "genusOrUninomial")
	
	};

	
	protected static CdmSingleAttributeXmlMapperBase[] operationalMappers = new CdmSingleAttributeXmlMapperBase[]{
		 new CdmUnclearMapper("hasName")
		,new CdmUnclearMapper("hasName")
		, new CdmUnclearMapper("accordingTo")
		, new CdmUnclearMapper("hasRelationship")
		, new CdmUnclearMapper("code", nsTgeo)	
	};
	
	protected static CdmSingleAttributeXmlMapperBase[] unclearMappers = new CdmSingleAttributeXmlMapperBase[]{
		new CdmUnclearMapper("primary")
		, new CdmUnclearMapper("note", nsTcom)	
		, new CdmUnclearMapper("taxonStatus", nsTpalm)
		
		, new CdmUnclearMapper("TaxonName", nsTn)	
		, new CdmUnclearMapper("dateOfEntry", nsTpalm)	
	};
	
	
	
	@Override
	protected void doInvoke(TcsRdfImportState state){
		
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<Reference> nomRefMap = (MapWrapper<Reference>)state.getStore(ICdmIO.NOMREF_STORE);
		
		String xmlElementName;
		String xmlAttributeName;
		Namespace elementNamespace;
		Namespace attributeNamespace;
		
		logger.info("start makeTaxa ...");
		
		TcsRdfImportConfigurator config = state.getConfig();
		Element root = config.getSourceRoot();
		
		Namespace rdfNamespace = config.getRdfNamespace();
		
		String idNamespace = "TaxonConcept";
		xmlElementName = "TaxonConcept";
		elementNamespace = config.getTcNamespace();
		List<Element> elTaxonConcepts = root.getChildren(xmlElementName, elementNamespace);

		ITaxonService taxonService = getTaxonService();
		
		//debug names
		if (false){
			for (Object nameResource : taxonNameMap.keySet()){
				System.out.println(nameResource);
			}
		}
		
		int i = 0;
		//for each taxonConcept
		for (Element elTaxonConcept : elTaxonConcepts){
			if ((i++ % modCount) == 0 && i > 1){ logger.info("Taxa handled: " + (i-1));}
			
			//
			String taxonAbout = elTaxonConcept.getAttributeValue("about", rdfNamespace);
			
			//hasName
			xmlElementName = "hasName";
			elementNamespace = config.getTcNamespace();
			xmlAttributeName = "resource";
			attributeNamespace = rdfNamespace;
			String strNameResource= XmlHelp.getChildAttributeValue(elTaxonConcept, xmlElementName, elementNamespace, xmlAttributeName, attributeNamespace);
			TaxonNameBase<?,?> taxonNameBase = taxonNameMap.get(strNameResource);
			if (taxonNameBase == null){
				logger.warn("Taxon has no name: " + taxonAbout + "; Resource: " + strNameResource);
			}
			
			
			//accordingTo
			xmlElementName = "accordingTo";
			elementNamespace = config.getTcNamespace();
			xmlAttributeName = "resource";
			attributeNamespace = rdfNamespace;
			//String strAccordingTo = elTaxonConcept.getChildTextTrim(xmlElementName, elementNamespace);
			String strAccordingTo = XmlHelp.getChildAttributeValue(elTaxonConcept, xmlElementName, elementNamespace, xmlAttributeName, attributeNamespace);
			
			
//			//FIXME
//			String secId = "pub_999999";
			Reference<?> sec = referenceMap.get(strAccordingTo);
			if (sec == null){
				sec = nomRefMap.get(strAccordingTo);
			}
			if (sec == null){
				logger.warn("sec could not be found in referenceMap or nomRefMap for secId: " + strAccordingTo);
			}
			
			TaxonBase<?> taxonBase;
			Namespace geoNamespace = config.getGeoNamespace();
			if (hasIsSynonymRelation(elTaxonConcept, rdfNamespace) || isSynonym(elTaxonConcept, config.getPalmNamespace())){
				//Synonym
				taxonBase = Synonym.NewInstance(taxonNameBase, sec);
				List<DescriptionElementBase> geo = makeGeo(elTaxonConcept, geoNamespace, rdfNamespace);
				if (geo.size() > 0){
					logger.warn("Synonym (" + taxonAbout + ") has geo description!");
				}
			}else{
				//Taxon
				Taxon taxon = Taxon.NewInstance(taxonNameBase, sec);
				List<DescriptionElementBase> geoList = makeGeo(elTaxonConcept, geoNamespace, rdfNamespace);
				TaxonDescription description = TaxonDescription.NewInstance(taxon);
				//TODO type
				description.addSource(OriginalSourceType.Unknown, null, null, taxon.getSec(), null);
				for (DescriptionElementBase geo: geoList){
					description.addElement(geo);
					//TODO type
					DescriptionElementSource source = DescriptionElementSource.NewInstance(OriginalSourceType.Unknown, null, null, taxon.getSec(), null);
					geo.addSource(source);
				}
				taxon.addDescription(description);
				taxonBase = taxon;
			}
			
			Set<String> omitAttributes = null;
			makeStandardMapper(elTaxonConcept, taxonBase, omitAttributes, standardMappers);

			ImportHelper.setOriginalSource(taxonBase, config.getSourceReference(), taxonAbout, idNamespace);
			checkAdditionalContents(elTaxonConcept, standardMappers, operationalMappers, unclearMappers);
			
			taxonMap.put(taxonAbout, taxonBase);
			
		}
		//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
		logger.info("saving " + taxonMap.size()+ " taxa ...");
		taxonService.save(taxonMap.objects());
		logger.info("end makeTaxa ...");
		return;
	}
	
	
	/**
	 * @param rdfNamespace 
	 * @param elTaxonConcept 
	 * @return
	 */
	private boolean isSynonym(Element elTaxonConcept, Namespace tpalmNamespace) {
		if (elTaxonConcept == null || ! "TaxonConcept".equalsIgnoreCase(elTaxonConcept.getName()) ){
			return false;
		}
		Element status = elTaxonConcept.getChild("taxonStatus", tpalmNamespace);
		if (status == null){
			return false;
		}else{
			String statusText = status.getTextNormalize();
			if ("S".equalsIgnoreCase(statusText)){
				return true;
			}else if ("A".equalsIgnoreCase(statusText)){
				return false;
			}else if ("C".equalsIgnoreCase(statusText)){
				return false;
			}else if ("V".equalsIgnoreCase(statusText)){
				return false;
			}else if ("O".equalsIgnoreCase(statusText)){
				return false;
			}else if ("U".equalsIgnoreCase(statusText)){
				return false;
			}else{
				logger.warn("Unknown taxon status: " +  statusText);
				return false;
			}
		}
	}


	private boolean hasIsSynonymRelation(Element elTaxonConcept, Namespace rdfNamespace){
		boolean result = false;
		if (elTaxonConcept == null || ! "TaxonConcept".equalsIgnoreCase(elTaxonConcept.getName()) ){
			return false;
		}
		
		String elName = "relationshipCategory";
		Filter filter = new ElementFilter(elName, elTaxonConcept.getNamespace());
		Iterator<Element> relationshipCategories = elTaxonConcept.getDescendants(filter);
		while (relationshipCategories.hasNext()){
			Element relationshipCategory = relationshipCategories.next();
			Attribute resource = relationshipCategory.getAttribute("resource", rdfNamespace);
			String isSynonymFor = "http://rs.tdwg.org/ontology/voc/TaxonConcept#IsSynonymFor";
			if (resource != null && isSynonymFor.equalsIgnoreCase(resource.getValue()) ){
				return true;
			}
		}
		return result;
	}
	
	private List<DescriptionElementBase> makeGeo(Element elConcept, Namespace geoNamespace, Namespace rdfNamespace){
		List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();
		String xmlElementName = "code";
		List<Element> elGeos = elConcept.getChildren(xmlElementName, geoNamespace);

		int i = 0;
		//for each geoTag
		for (Element elGeo : elGeos){
			//if ((i++ % modCount) == 0){ logger.info("Geocodes handled: " + (i-1));}
			
			String strGeoRegion = elGeo.getAttributeValue("resource", rdfNamespace);
			strGeoRegion = strGeoRegion.replace("http://rs.tdwg.org/ontology/voc/GeographicRegion#", "");
			NamedArea namedArea = TdwgAreaProvider.getAreaByTdwgAbbreviation(strGeoRegion);
			PresenceAbsenceTermBase<?> status = PresenceTerm.PRESENT();
			DescriptionElementBase distribution = Distribution.NewInstance(namedArea, status);
			distribution.setFeature(Feature.DISTRIBUTION());
			//System.out.println(namedArea);
			
			result.add(distribution);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(TcsRdfImportState state){
		return ! state.getConfig().isDoTaxa();
	}


}
