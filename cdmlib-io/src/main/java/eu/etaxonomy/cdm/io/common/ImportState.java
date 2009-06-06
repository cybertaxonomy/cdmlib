// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class ImportState<CONFIG extends ImportConfiguratorBase> extends IoState<CONFIG> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ImportState.class);
	
	//different type of stores that are used by the known imports
	protected Map<String, MapWrapper<? extends CdmBase>> stores = new HashMap<String, MapWrapper<? extends CdmBase>>();
	
	protected IService<CdmBase> service = null;

	public ImportState() {
		super();
		stores.put(ICdmIO.PERSON_STORE, new MapWrapper<Person>(service));
		stores.put(ICdmIO.TEAM_STORE, new MapWrapper<TeamOrPersonBase<?>>(service));
		stores.put(ICdmIO.REFERENCE_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.NOMREF_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.NOMREF_DETAIL_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.REF_DETAIL_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.TAXONNAME_STORE, new MapWrapper<TaxonNameBase<?,?>>(service));
		stores.put(ICdmIO.TAXON_STORE, new MapWrapper<TaxonBase>(service));
		stores.put(ICdmIO.SPECIMEN_STORE, new MapWrapper<Specimen>(service));
	}

	/**
	 * @return the stores
	 */
	public Map<String, MapWrapper<? extends CdmBase>> getStores() {
		return stores;
	}

	/**
	 * @param stores the stores to set
	 */
	public void setStores(Map<String, MapWrapper<? extends CdmBase>> stores) {
		this.stores = stores;
	}


 	public MapWrapper<? extends CdmBase> getStore(String storeLabel){
 		return (MapWrapper<? extends CdmBase>) stores.get(storeLabel);
 	}
	
	
	
}
