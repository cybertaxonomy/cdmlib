// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.in;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IIdentifiableEntityService;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.dwca.in.InMemoryMapping.CdmKey;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 23.11.2011
 */
public class DwcaImportState extends ImportStateBase<DwcaImportConfigurator, DwcaImport>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportState.class);

	boolean taxaCreated;
	
	private IImportMapping mapping = new InMemoryMapping();
	
	public DwcaImportState(DwcaImportConfigurator config) {
		super(config);
	}

	/**
	 * True, if taxa have been fully created.
	 * @return
	 */
	public boolean isTaxaCreated() {
		return taxaCreated;
		
	}

	/**
	 * @param taxaCreated the taxaCreated to set
	 */
	public void setTaxaCreated(boolean taxaCreated) {
		this.taxaCreated = taxaCreated;
	}
	
	public void putMapping(String namespace, Integer sourceKey, IdentifiableEntity<?> destinationObject){
		mapping.putMapping(namespace, sourceKey, destinationObject);
	}
		
	public void putMapping(String namespace, String sourceKey, IdentifiableEntity<?> destinationObject){
		mapping.putMapping(namespace, sourceKey, destinationObject);
	}
	
	
	public List<IdentifiableEntity> get(String namespace, String sourceKey){
		return get(namespace, sourceKey, null);
	}
	
	public <CLASS extends IdentifiableEntity> List<CLASS> get(String namespace, String sourceKey,Class<CLASS> destinationClass){
		List<CLASS> result = new ArrayList<CLASS>(); 
		Set<CdmKey> keySet = mapping.get(namespace, sourceKey);
		for (CdmKey<CLASS> key: keySet){
			if (destinationClass == null || destinationClass.isAssignableFrom(key.clazz)){
				IIdentifiableEntityService<CLASS> service = getCurrentIO().getServiceByClass(key.clazz);
				CLASS entity = CdmBase.deproxy(service.find(key.id), key.clazz);
				result.add(entity);
			}
		}
		return result;
	}

	public boolean exists(String namespace, String sourceKey,Class<?> destinationClass){
		return mapping.exists(namespace, sourceKey, destinationClass);
	}

	
	
	


}
