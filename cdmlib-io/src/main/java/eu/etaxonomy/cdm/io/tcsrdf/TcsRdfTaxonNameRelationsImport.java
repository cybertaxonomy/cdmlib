/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 29.05.2008
 * @version 1.0
 */
@Component
public class TcsRdfTaxonNameRelationsImport extends TcsRdfImportBase implements ICdmIO<TcsRdfImportState> {
	private static final Logger logger = Logger.getLogger(TcsRdfTaxonNameRelationsImport.class);

	private static int modCount = 5000;
	
	public TcsRdfTaxonNameRelationsImport(){
		super();
	}
	
	@Override
	public boolean doCheck(TcsRdfImportState state){
		boolean result = true;
		logger.warn("Checking for TaxonNameRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);
		
		return result;
	}
	
	@Override
	public void doInvoke(TcsRdfImportState state){
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);
		
		String tcsElementName;
		Namespace tcsNamespace;
		String cdmAttrName;
		String value;

		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		TcsRdfImportConfigurator config = state.getConfig();
		//Model source = config.getSourceRoot();
		
		logger.info("start makeNameRelationships ...");
		INameService nameService = getNameService();

//		<tn:hasBasionym rdf:resource="palm_tn_14530"/>
		
		Model root = config.getSourceRoot();
		
		String rdfNamespace = config.getRdfNamespaceURIString();
		String taxonNameNamespace = config.getTnNamespaceURIString();
		/*
		List<Element> elTaxonNames = root.getChildren("TaxonName", taxonNameNamespace);
		
		int i = 0;
		int nameRelCount = 0;
		//for each taxonName
		for (Element elTaxonName : elTaxonNames){
			
			TaxonNameBase fromName = null;
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			
			//Basionyms
			tcsElementName = "hasBasionym";
			tcsNamespace = taxonNameNamespace;
			List<Element> elBasionymList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			
			for (Element elBasionym: elBasionymList){
				nameRelCount++;
				logger.debug("BASIONYM "+  nameRelCount);
				tcsElementName = "resource";
				tcsNamespace = rdfNamespace;
				Attribute attrResource = elBasionym.getAttribute(tcsElementName, tcsNamespace);
				if (attrResource == null){
					logger.warn("Basionym rdf:resource is missing ! Basionym not set!");
					continue;
				}
				String basionymId = attrResource.getValue();
				TaxonNameBase basionym = taxonNameMap.get(basionymId);
				if (basionym == null){
					logger.warn("Basionym name ("+basionymId+") not found in Map! Basionym not set!");
					continue;
				}
				if (fromName == null){
					Attribute about = elTaxonName.getAttribute("about", rdfNamespace);
					if (about != null){
						fromName = taxonNameMap.get(about.getValue() );
					}
					if (fromName == null){
						logger.warn("From name ("+about+") not found in Map! Basionym not set!");
						continue;
					}
				}
				String ruleConcidered = null; //TODO
				String microCitation = null; //TODO;
				Reference citation = null; //TODO;
				fromName.addBasionym(basionym, citation, microCitation, ruleConcidered);
				nameStore.add(fromName);

			}
		}// end Basionyms
		
		//Other Relations
		//TODO
		
		logger.info(nameRelCount + " nameRelations handled");
		nameService.save(nameStore);
		logger.info("end makeNameRelationships ...");
		*/
		return;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(TcsRdfImportState state){
		return ! state.getConfig().isDoRelNames();
	}

}
