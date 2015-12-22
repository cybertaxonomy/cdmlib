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

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 11.05.2009
 */
public abstract class DbImportStateBase<CONFIG extends DbImportConfiguratorBase, STATE extends DbImportStateBase> extends ImportStateBase<CONFIG, CdmImportBase> implements IPartitionedState {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportStateBase.class);

	public static final String CURRENT_OBJECT_NAMESPACE = "CurrentObjectNamespace";
	public static final String CURRENT_OBJECT_ID = "CurrentObjectId";

	private final Map<String, User> usernameMap = new HashMap<String, User>();


	private Reference<?> partitionSourceReference;

	private final RelatedObjectsHelper relatedObjectsHelper = new RelatedObjectsHelper();
	//holds the classifications needed for this partition, the key is a value that differentiate classifications
	//like the taxons reference (secundum)


	/**
	 * @param config
	 */
	protected DbImportStateBase(CONFIG config) {
		super(config);
	}

	@Override
	public void addRelatedObject(Object namespace, String id, CdmBase relatedObject) {
		this.relatedObjectsHelper.addRelatedObjet(namespace, id, relatedObject);
	}

	@Override
	public CdmBase getRelatedObject(Object namespace, String id) {
		return relatedObjectsHelper.getRelatedObject(namespace, id);
	}

	public<T extends CdmBase> T getRelatedObject(Object namespace, String id, Class<T> clazz) {
		CdmBase cdmBase = relatedObjectsHelper.getRelatedObject(namespace, id);
		T result = CdmBase.deproxy(cdmBase, clazz);
		return result;
	}

	@Override
    public void setRelatedObjects(Map<Object, Map<String, ? extends CdmBase>> relatedObjects) {
		relatedObjectsHelper.setRelatedObjects(relatedObjects);
	}


	/**
	 * Stores the source reference for the time of a transaction. Needs to be
	 * deleted manually after transaction finishes to avoid memory leaks.
	 * @return
	 */
	@Override
    public Reference<?> getTransactionalSourceReference() {
		return partitionSourceReference;
	}


	@Override
    public void resetTransactionalSourceReference(){
		this.partitionSourceReference = null;
	}

	@Override
    public void makeTransactionalSourceReference(IReferenceService service){
		//TODO handle undefined sourceRefUuid
		this.partitionSourceReference = service.find(this.getConfig().getSourceRefUuid());
		if (this.partitionSourceReference == null){
			this.partitionSourceReference = this.getConfig().getSourceReference();
			service.saveOrUpdate(this.partitionSourceReference);
		}
	}

	public User getUser(String username){
		return usernameMap.get(username);
	}

	public void putUser(String username, User user){
		usernameMap.put(username, user);
	}


}
