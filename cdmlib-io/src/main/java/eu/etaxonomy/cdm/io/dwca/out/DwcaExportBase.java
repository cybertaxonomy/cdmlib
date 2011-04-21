/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public abstract class DwcaExportBase extends CdmExportBase<DwcaTaxExportConfigurator, DwcaTaxExportState> implements ICdmExport<DwcaTaxExportConfigurator, DwcaTaxExportState>{
	private static final Logger logger = Logger.getLogger(DwcaExportBase.class);


	/**
	 * Returns the list of taxon nodes that are part in one of the given classifications 
	 * and do have a taxon attached (empty taxon nodes should not but do exist in CDM databases).
	 * Preliminary implementation. Better implement API method for this.
	 * @return
	 */
	protected List<TaxonNode> getAllNodes(Set<Classification> classificationList) {
		List<TaxonNode> allNodes =  getClassificationService().getAllNodes();
		List<TaxonNode> result = new ArrayList<TaxonNode>();
		for (TaxonNode node : allNodes){
			if (node.getClassification() == null ){
				continue;
			}else if (classificationList != null && classificationList.contains(node.getClassification())){
				continue;
			}
			Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
			if (taxon == null){
				String message = "There is a taxon node without taxon: " + node.getId();
				logger.warn(message);
				continue;
			}
			result.add(node);
		}
		return result;
	}
	
	
	/**
	 * Creates the locationId, locality, countryCode triple
	 * @param record
	 * @param area
	 */
	protected void handleArea(IDwcaAreaRecord record, NamedArea area) {
		if (area != null){
			record.setLocationId(area.getId());
			record.setLocality(area.getLabel());
			if (area.isInstanceOf(WaterbodyOrCountry.class)){
				WaterbodyOrCountry country = CdmBase.deproxy(area, WaterbodyOrCountry.class);
				record.setCountryCode(country.getIso3166_A2());
			}
		}
	}
}
