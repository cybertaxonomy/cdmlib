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

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public abstract class DbImportStateBase<CONFIG extends ImportConfiguratorBase, STATE extends DbImportStateBase> extends ImportStateBase<CONFIG> implements IPartitionedState {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportStateBase.class);
	private RelatedObjectsHelper relatedObjectsHelper = new RelatedObjectsHelper();;
	private CdmImportBase<CONFIG, STATE> currentImport;
	//holds the taxonTrees needed for this partition, the key is a value that differentiate classifications
	//like the taxons reference (secundum)
	private Map<Integer, TaxonomicTree> curentTaxonTreeMap = new HashMap<Integer, TaxonomicTree>();
	
	
	/**
	 * @param config
	 */
	protected DbImportStateBase(CONFIG config) {
		super(config);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IPartitionedState#getRelatedObject(java.lang.Object, java.lang.String)
	 */
	public CdmBase getRelatedObject(Object namespace, String id) {
		return relatedObjectsHelper.getRelatedObject(namespace, id);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IPartitionedState#setRelatedObjects(java.util.Map)
	 */
	public void setRelatedObjects(Map<Object, Map<String, ? extends CdmBase>> relatedObjects) {
		relatedObjectsHelper.setRelatedObjects(relatedObjects);
	}

	/**
	 * @param currentImport the currentImport to set
	 */
	public void setCurrentImport(CdmImportBase<CONFIG, STATE> currentImport) {
		this.currentImport = currentImport;
	}

	/**
	 * @return the currentImport
	 */
	public CdmImportBase<CONFIG, STATE> getCurrentImport() {
		return currentImport;
	}

	/**
	 * @param curentTaxonTreeMap the curentTaxonTreeMap to set
	 */
	public void setCurentTaxonTreeMap(Map<Integer, TaxonomicTree> curentTaxonTreeMap) {
		this.curentTaxonTreeMap = curentTaxonTreeMap;
	}

	/**
	 * @return the curentTaxonTreeMap
	 */
	public Map<Integer, TaxonomicTree> getCurentTaxonTreeMap() {
		return curentTaxonTreeMap;
	}
	

	
}
