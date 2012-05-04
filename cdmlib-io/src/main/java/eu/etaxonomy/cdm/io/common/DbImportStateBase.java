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

import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public abstract class DbImportStateBase<CONFIG extends ImportConfiguratorBase, STATE extends DbImportStateBase> extends ImportStateBase<CONFIG, CdmImportBase> implements IPartitionedState {
	
	public static final String CURRENT_OBJECT_NAMESPACE = "CurrentObjectNamespace";
	public static final String CURRENT_OBJECT_ID = "CurrentObjectId";
	
	
	private Reference<?> partitionSourceReference;
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportStateBase.class);
	private RelatedObjectsHelper relatedObjectsHelper = new RelatedObjectsHelper();;
	//holds the classifications needed for this partition, the key is a value that differentiate classifications
	//like the taxons reference (secundum)
	
	
	/**
	 * @param config
	 */
	protected DbImportStateBase(CONFIG config) {
		super(config);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IPartitionedState#addRelatedObject(java.lang.Object, java.lang.String, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public void addRelatedObject(Object namespace, String id, CdmBase relatedObject) {
		this.relatedObjectsHelper.addRelatedObjet(namespace, id, relatedObject);
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IPartitionedState#getRelatedObject(java.lang.Object, java.lang.String)
	 */
	public CdmBase getRelatedObject(Object namespace, String id) {
		return relatedObjectsHelper.getRelatedObject(namespace, id);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IPartitionedState#getRelatedObject(java.lang.Object, java.lang.String)
	 */
	public<T extends CdmBase> T getRelatedObject(Object namespace, String id, Class<T> clazz) {
		CdmBase cdmBase = relatedObjectsHelper.getRelatedObject(namespace, id);
		T result = CdmBase.deproxy(cdmBase, clazz);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IPartitionedState#setRelatedObjects(java.util.Map)
	 */
	public void setRelatedObjects(Map<Object, Map<String, CdmBase>> relatedObjects) {
		relatedObjectsHelper.setRelatedObjects(relatedObjects);
	}
	

	/**
	 * Stores the source reference for the time of a transaction. Needs to be
	 * deleted manually after transaction finishes to avoid memory leaks.
	 * @return
	 */
	public Reference getTransactionalSourceReference() {
		return partitionSourceReference;
	}

	
	public void resetTransactionalSourceReference(){
		this.partitionSourceReference = null;
	}
	
	public void makeTransactionalSourceReference(IReferenceService service){
		//TODO handle undefined sourceRefUuid
		this.partitionSourceReference = service.find(this.getConfig().getSourceRefUuid());
		if (this.partitionSourceReference == null){
			this.partitionSourceReference = this.getConfig().getSourceReference();
			service.saveOrUpdate(this.partitionSourceReference);
		}
	}

	
}
