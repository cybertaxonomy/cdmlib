/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import java.util.Map;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.babadshanjan
 * @created 17.11.2008
 */
public interface ICdmIoExport<T extends IExportConfigurator> {


//	final String AUTHOR_STORE = "author";
//	final String REFERENCE_STORE = "reference";
//	final String NOMREF_STORE = "nomRef";
//	final String REF_DETAIL_STORE = "refDetail";
//	final String NOMREF_DETAIL_STORE = "nomRefDetail";
//	final String TAXONNAME_STORE = "taxonName";
//	final String TAXON_STORE = "taxon";
//	final String FEATURE_STORE = "feature";
//	final String SPECIMEN_STORE = "specimen";

	public boolean check(T config);

	public boolean invoke(T config);
//	public boolean invoke(T config, Map<String, MapWrapper<? extends CdmBase>> stores);

}
