/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IIdentifiableEntityService;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.stream.mapping.IImportMapping;
import eu.etaxonomy.cdm.io.stream.mapping.InMemoryMapping;
import eu.etaxonomy.cdm.io.stream.mapping.MappingEntry;
import eu.etaxonomy.cdm.io.stream.mapping.IImportMapping.CdmKey;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 23.11.2011
 */
public abstract class StreamImportStateBase<CONFIG extends StreamImportConfiguratorBase, IO extends StreamImportBase>
            extends ImportStateBase<CONFIG, IO>{
	private static final Logger logger = Logger.getLogger(StreamImportStateBase.class);

	private UUID uuid = UUID.randomUUID();

	boolean taxaCreated;
	private Map<String, Map<String, IdentifiableEntity>> partitionStore;

	private final IImportMapping mapping;

	public StreamImportStateBase(CONFIG config) {
		super(config);
		if (config.getStateUuid()!= null){
		    uuid = config.getStateUuid();
		}else{
		    String message = "State uuid: " + uuid.toString();
		    logger.warn(message);
		    System.out.println(message);
		}
		mapping = getConfig().getMappingType().getMappingInstance(uuid.toString(), getConfig().getDatabaseMappingFile());
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

	public void finish(){
		this.mapping.finish();
	}

//********************* MAPPING ACCESS *********************************
	//TODO this may move to an external class soon

	public void putMapping(MappedCdmBase mappedCdmBase) throws IllegalArgumentException{
		if (! mappedCdmBase.getCdmBase().isInstanceOf(IdentifiableEntity.class)){
			throw new IllegalArgumentException("Mapped cdmBase does not map an identifiable entity");
		}
		putMapping(mappedCdmBase.getNamespace(), mappedCdmBase.getSourceId(), CdmBase.deproxy(mappedCdmBase.getCdmBase(), IdentifiableEntity.class));
	}


	public void putMapping(String namespace, Integer sourceKey, IdentifiableEntity<?> destinationObject){
	    putMapping(namespace, String.valueOf(sourceKey), destinationObject);
	}

	public void putMapping(String namespace, String sourceKey, IdentifiableEntity<?> destinationObject){
		if (destinationObject.isInstanceOf(DefinedTermBase.class)){
		    addRelatedObject(namespace, sourceKey, destinationObject);
		}
	    mapping.putMapping(namespace, sourceKey, destinationObject);
	}


	public List<IdentifiableEntity> get(String namespace, String sourceKey){
		return get(namespace, sourceKey, null);
	}

	public <CLASS extends IdentifiableEntity> List<CLASS> get(String namespace, String sourceKey, Class<CLASS> destinationClass){
		List<CLASS> result = new ArrayList<>();
		if (this.partitionStore != null){
			Map<String, IdentifiableEntity> namespaceMap = this.partitionStore.get(namespace);
			if (namespaceMap != null){
				IdentifiableEntity<?> cdmBase = namespaceMap.get(sourceKey);
				if (cdmBase == null){
					logger.info("CdmBase does not exist in mapping: " + sourceKey);
				}else if (cdmBase.isInstanceOf(destinationClass)){
					CLASS typedCdmBase = CdmBase.deproxy(cdmBase, destinationClass);
					result.add(typedCdmBase);
				}

			}
		}else{
			Set<CdmKey> keySet = mapping.get(namespace, sourceKey);
			for (CdmKey<CLASS> key: keySet){
				if (destinationClass == null || destinationClass.isAssignableFrom(key.getClazz())){
					IIdentifiableEntityService<CLASS> service = getCurrentIO().getServiceByClass(key.getClazz());
					CLASS entity = CdmBase.deproxy(service.find(key.getId()), key.getClazz());
					result.add(entity);
				}
			}
			return result;
		}
		return result;
	}

	public boolean exists(String namespace, String sourceKey,Class<?> destinationClass){
		return mapping.exists(namespace, sourceKey, destinationClass);
	}


	public  void loadRelatedObjects (InMemoryMapping mapping){
		Map<String, Map<String, IdentifiableEntity>> result = new HashMap<>();

		List<MappingEntry<String, String, Class, Integer>> mappingEntryList = mapping.getEntryList();

		//order ids by destination classes
		Map<Class, Set<Integer>> destinationNamespaceMap = new HashMap<>();
		for (MappingEntry<String, String, Class, Integer> entry : mappingEntryList){
			Set<Integer> idSet = destinationNamespaceMap.get(entry.getDestinationNamespace());
			if (idSet == null){
				idSet = new HashSet<>();
				destinationNamespaceMap.put(entry.getDestinationNamespace(), idSet);
			}
			idSet.add(entry.getDestinationId());
		}

		//retrieve cdm objects per class
		Map<Class, Map<Integer, IdentifiableEntity>> classMap = new HashMap<>();
		for (Class<?> cdmClass : destinationNamespaceMap.keySet()){
			IIdentifiableEntityService<?> classService = getCurrentIO().getServiceByClass(cdmClass);
			Set<Integer> idSet = destinationNamespaceMap.get(cdmClass);
			List<? extends IdentifiableEntity> relatedObjects = classService.findById(idSet);

			//put into id map
			Map<Integer, IdentifiableEntity> idMap = new HashMap<>();
			for (IdentifiableEntity<?> identEnt : relatedObjects){
				idMap.put(identEnt.getId(), identEnt);
			}

			//add to class map
			classMap.put(cdmClass, idMap);
		}

		//fill related object map
		for (MappingEntry<String, String, Class, Integer> entry : mappingEntryList){
			IdentifiableEntity<?> cdmBase = getCdmObject(classMap, entry);

			Map<String, IdentifiableEntity> namespaceMap = getOrMakeNamespaceMap(result, entry.getNamespace());
			if (cdmBase != null){
				namespaceMap.put(entry.getSourceKey(), cdmBase);
			}else{
				logger.info("CdmBase not found for mapping entry.");
			}
		}

		//store
		this.partitionStore = result;

	}

	public void addRelatedObject(String sourceNamespace, String sourceKey, IdentifiableEntity<?> cdmEntity){
		Map<String, IdentifiableEntity> namespaceMap = getOrMakeNamespaceMap(this.partitionStore, sourceNamespace);
		if (cdmEntity != null){
			namespaceMap.put(sourceKey, cdmEntity);
		}else{
			logger.info("CdmBase is null and will not be added to related objects.");
		}
	}

	public void unloadPartitionStore(Map<String, Map<String, IdentifiableEntity>> partitionStore) {
		this.partitionStore = new HashMap<>();
	}

	public IImportMapping getMapping() {
		return this.mapping;
	}


	private Map<String, IdentifiableEntity> getOrMakeNamespaceMap(Map<String, Map<String, IdentifiableEntity>> relatedObjectMap, String namespace) {
		Map<String, IdentifiableEntity> namespaceMap = relatedObjectMap.get(namespace);
		if (namespaceMap == null){
			namespaceMap = new HashMap<>();
			relatedObjectMap.put(namespace, namespaceMap);
		}
		return namespaceMap;
	}


	private IdentifiableEntity getCdmObject(Map<Class, Map<Integer, IdentifiableEntity>> classMap,
			MappingEntry<String, String, Class, Integer> entry) {
		Class<?> cdmClass = entry.getDestinationNamespace();
		Integer cdmKey = entry.getDestinationId();
		Map<Integer, IdentifiableEntity> idMap = classMap.get(cdmClass);
		if (idMap != null){
			return idMap.get(cdmKey);
		}else{
			return null;
		}
	}

	/**
	 * Returns the source reference object that is attached to the current transaction.
	 * @return
	 */
	public Reference getTransactionalSourceReference() {
		TermUri namespaceSourceReference = TermUri.CDM_SOURCE_REFERENCE;
		UUID sourceReferenceUuid = getConfig().getSourceRefUuid();
		List<Reference> references = this.get(namespaceSourceReference.toString(), sourceReferenceUuid.toString(), Reference.class);
		if (references.isEmpty()){
			//TODO better fire warning, but not yet available for state
			throw new RuntimeException("Source reference can not be found. This should not happen.");
		}else if (references.size() > 1){
			//TODO better fire warning, but not yet available for state
			throw new RuntimeException("More than 1 source reference found. This is not yet handled.");
		}else{
			return references.get(0);
		}

	}



}
