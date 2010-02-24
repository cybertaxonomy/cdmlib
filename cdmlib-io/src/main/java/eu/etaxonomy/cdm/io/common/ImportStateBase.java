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
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public abstract class ImportStateBase<CONFIG extends ImportConfiguratorBase> extends IoStateBase<CONFIG> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ImportStateBase.class);
	
	private Map<Object,TaxonomicTree> treeMap = new HashMap<Object,TaxonomicTree>();

	private Map<ReferenceBase,UUID> treeUuidMap = new HashMap<ReferenceBase,UUID>();

	private Map<Integer,UUID> refIdtreeUuidMap = new HashMap<Integer,UUID>();
	
	Map<UUID, ExtensionType> extensionTypeMap = new HashMap<UUID, ExtensionType>();
	Map<UUID, MarkerType> markerTypeMap = new HashMap<UUID, MarkerType>();

	
	protected ImportStateBase(CONFIG config){
		this.config = config;
		stores.put(ICdmIO.USER_STORE, new MapWrapper<User>(service));
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
	
	//different type of stores that are used by the known imports
	protected Map<String, MapWrapper<? extends CdmBase>> stores = new HashMap<String, MapWrapper<? extends CdmBase>>();
	
	protected IService<CdmBase> service = null;

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
	

	/**
	 * @return the treeMap
	 */
	public TaxonomicTree getTree(Object ref) {
		return treeMap.get(ref);
	}

	/**
	 * @param treeMap the treeMap to set
	 */
	public void putTree(Object ref, TaxonomicTree tree) {
		if (tree != null){
			this.treeMap.put(ref, tree);
		}
	}
	
	public int countTrees(){
		return treeUuidMap.size();
	}
	
	/**
	 * @return the treeUuid
	 */
	public UUID getTreeUuid(ReferenceBase ref) {
		return treeUuidMap.get(ref);
	}

	public void putTreeUuid(ReferenceBase ref, TaxonomicTree tree) {
		if (tree != null &&  tree.getUuid() != null){
			this.treeUuidMap.put(ref, tree.getUuid());
		}
	}

	public int countTreeUuids(){
		return treeUuidMap.size();
	}

	
	
	
	public void putTreeUuidByRefId(int referenceId, TaxonomicTree tree) {
		if (tree != null &&  tree.getUuid() != null){
			this.refIdtreeUuidMap.put(referenceId, tree.getUuid());
		}
	}
	
	public UUID getTreeUuidByRefId(Integer refId) {
		return refIdtreeUuidMap.get(refId);
	}
	
	
	public ExtensionType getExtensionType(UUID uuid){
		return extensionTypeMap.get(uuid);
	}
	
	public void putExtensionType(ExtensionType extensionType){
		extensionTypeMap.put(extensionType.getUuid(), extensionType);
	}

	public MarkerType getMarkerType(UUID uuid){
		return markerTypeMap.get(uuid);
	}
	
	public void putMarkerType(MarkerType markerType){
		markerTypeMap.put(markerType.getUuid(), markerType);
	}
	
}
