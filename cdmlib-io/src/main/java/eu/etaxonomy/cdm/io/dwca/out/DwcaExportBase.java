/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public abstract class DwcaExportBase extends CdmExportBase<DwcaTaxExportConfigurator, DwcaTaxExportState> implements ICdmExport<DwcaTaxExportConfigurator, DwcaTaxExportState>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaExportBase.class);


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
