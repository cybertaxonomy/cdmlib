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

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 29.05.2008
 */
@Component
public class TcsRdfTaxonImport  extends TcsRdfImportBase implements ICdmIO<TcsRdfImportState> {
    private static final long serialVersionUID = 4615869699069336295L;

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

	protected static CdmSingleAttributeRDFMapperBase[] standardMappers = new CdmSingleAttributeRDFMapperBase[]{
//		new CdmTextElementMapper("genusPart", "genusOrUninomial")

	};


	protected static CdmSingleAttributeRDFMapperBase[] operationalMappers = new CdmSingleAttributeRDFMapperBase[]{
		 new CdmUnclearMapper("hasName")
		,new CdmUnclearMapper("hasName")
		, new CdmUnclearMapper("accordingTo")
		, new CdmUnclearMapper("hasRelationship")
		, new CdmUnclearMapper("code", nsTgeo)
	};

	protected static CdmSingleAttributeRDFMapperBase[] unclearMappers = new CdmSingleAttributeRDFMapperBase[]{
		new CdmUnclearMapper("primary")
		, new CdmUnclearMapper("note", nsTcom)
		, new CdmUnclearMapper("taxonStatus", nsTpalm)

		, new CdmUnclearMapper("TaxonName", nsTn)
		, new CdmUnclearMapper("dateOfEntry", nsTpalm)
	};



	@Override
	protected void doInvoke(TcsRdfImportState state){

		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<TaxonName> taxonNameMap = (MapWrapper<TaxonName>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<Reference> nomRefMap = (MapWrapper<Reference>)state.getStore(ICdmIO.NOMREF_STORE);

		String xmlElementName;
		String xmlAttributeName;
		String elementNamespace;
		String attributeNamespace;

		logger.info("start makeTaxa ...");

		TcsRdfImportConfigurator config = state.getConfig();
		Model root = config.getSourceRoot();

		String rdfNamespace = config.getRdfNamespaceURIString();

		String idNamespace = "TaxonConcept";
		xmlElementName = "TaxonConcept";
		elementNamespace = config.getTcNamespaceURIString();

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
			PresenceAbsenceTerm status = PresenceAbsenceTerm.PRESENT();
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
	@Override
    protected boolean isIgnore(TcsRdfImportState state){
		return ! state.getConfig().isDoTaxa();
	}


}
