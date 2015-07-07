/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 *
 */
public interface IImportMapping {


	public enum MappingType{
		InMemoryMapping(0),
		DatabaseMapping(1),
//		InMemoryFileMapping(2) //idea: inMemory mapping which is written to a file at the end and can be loaded as such again.
		;

		private final int index;

		private MappingType(int index){
			this.index = index;
		}

		public IImportMapping getMappingInstance(String mappingId){
		    return getMappingInstance(mappingId, null);
		}

		public IImportMapping getMappingInstance(String mappingId, String file){
			if (this.equals(MappingType.InMemoryMapping)){
				return new InMemoryMapping();
			}else if (this.equals(MappingType.DatabaseMapping)){
				return new DatabaseMapping(mappingId, file);
			}else{
				throw new RuntimeException("Unknown MappingType: " + this.toString());
			}
		}

	}

	/**
	 * Put the destination object with for the given namespaced identifier into the mapping.
	 * @param namespace
	 * @param sourceKey
	 * @param destinationObject
	 */
	public void putMapping(String namespace, String sourceKey, IdentifiableEntity destinationObject);

	/**
	 * Put the destination object with for the given namespaced identifier into the mapping.
	 * @param namespace
	 * @param sourceKey
	 * @param destinationObject
	 * @see #putMapping(String, String, IdentifiableEntity)
	 */
	public void putMapping(String namespace, Integer sourceKey, IdentifiableEntity destinationObject);


	/**
	 * Retrieve the CdmKey set for the given namespaced identifier.
	 * @param namespace
	 * @param sourceKey
	 * @return
	 */
	public Set<CdmKey> get(String namespace, String sourceKey);

	/**
	 * Checks if a mapping exists for a given namespaced identifier.
	 * If destinationClass is not <code>null</code> the mapping is limited to
	 * result objects of the given class or a subclass of the given class.
	 * @param namespace
	 * @param sourceKey
	 * @param destinationClass
	 * @return
	 */
	public boolean exists(String namespace, String sourceKey,Class<?> destinationClass);

	/**
	 * Returns the mapping for only those obejcts addressed by the namespacedSourceKeys parameter
	 * @param namespacedKeys
	 * @return
	 */
	public InMemoryMapping getPartialMapping(Map<String, Set<String>> namespacedSourceKeys);

//	/**
//	 * Returns a list for all mapping entries.
//	 * @return
//	 */
//	//ONLY for InMemoryMapping, remove here
//	public List<MappingEntry<String, String, Class, Integer>> getEntryList();
//

	public class CdmKey<CLASS extends IdentifiableEntity>{
		Class<CLASS> clazz;
		int id;

		public CdmKey(Class clazz, int id){
			this.clazz = clazz;
			this.id = id;
		}

		public CdmKey(IdentifiableEntity<?> object){
			this.clazz = (Class)object.getClass();
			this.id = object.getId();
		}

		public Class<CLASS> getClazz(){
			return clazz;
		}
		public int getId(){
			return id;
		}


		@Override
		public String toString(){
			return id + "@" + clazz.getSimpleName();
		}
	}

	public void finish();

}
