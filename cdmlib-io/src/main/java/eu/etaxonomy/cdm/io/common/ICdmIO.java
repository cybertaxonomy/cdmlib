/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.Map;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */

public interface ICdmIO<T extends IIoConfigurator> {
//public interface ICdmIO<T extends IImportConfigurator> {

	
	final String AUTHOR_STORE = "author";
	final String REFERENCE_STORE = "reference";
	final String NOMREF_STORE = "nomRef";
	final String REF_DETAIL_STORE = "refDetail";
	final String NOMREF_DETAIL_STORE = "nomRefDetail";
	final String TAXONNAME_STORE = "taxonName";
	final String TAXON_STORE = "taxon";
	final String FEATURE_STORE = "feature";
	final String SPECIMEN_STORE = "specimen";
	
	public boolean check(T config);
	
	public boolean invoke(T config, Map<String, MapWrapper<? extends CdmBase>> stores);
	
}