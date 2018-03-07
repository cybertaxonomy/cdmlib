/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * This class represents the result of a delete action.
 *
 * @author a.mueller
 * @date 04.01.2012
 *
 */
public class DeleteResult extends UpdateResult{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeleteResult.class);



	private final List<Exception> exceptions = new ArrayList<Exception>();

	private final Set<CdmBase> relatedObjects = new HashSet<CdmBase>();

//	private Set<PersistPair> objectsToDelete = new HashSet<PersistPair>();
//
//	private Set<PersistPair> objectsToSave = new HashSet<DeleteResult.PersistPair>();

//	protected class PersistPair{
//		protected CdmBase objectToPersist;
//		protected ICdmEntityDao<CdmBase> dao;
//	}



//***************************** GETTER /SETTER /ADDER *************************/



	/**
	 * Related objects that prevent the delete action to take place.
	 * @return
	 */
	public Set<CdmBase> getRelatedObjects() {
		return relatedObjects;
	}
	public void addRelatedObject(CdmBase relatedObject) {
		this.relatedObjects.add(relatedObject);
	}
	public void addRelatedObjects(Set<? extends CdmBase> relatedObjects) {
		this.relatedObjects.addAll(relatedObjects);
	}


	@Override
	public void includeResult(UpdateResult includedResult){

        this.setMaxStatus(includedResult.getStatus());
        this.addExceptions(includedResult.getExceptions());
        this.addUpdatedObjects(includedResult.getUpdatedObjects());

    }










}
