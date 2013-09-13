// $Id$
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
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;

/**
 * This class represents the result of a delete action.
 * 
 * @author a.mueller
 * @date 04.01.2012
 *
 */
public class DeleteResult {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeleteResult.class);
	
	private DeleteStatus status = DeleteStatus.OK;
	
	private List<Exception> exceptions = new ArrayList<Exception>();
	
	private Set<CdmBase> relatedObjects = new HashSet<CdmBase>();
	
	private Set<PersistPair> objectsToDelete = new HashSet<PersistPair>();
	
	private Set<PersistPair> objectsToSave = new HashSet<DeleteResult.PersistPair>();
	
	protected class PersistPair{
		protected CdmBase objectToPersist;
		protected ICdmEntityDao<CdmBase> dao;	
	}
	
	public enum DeleteStatus {
		OK(0),
		ABORT(1),
		ERROR(3),
		;
		
		protected Integer severity;
		private DeleteStatus(int severity){
			this.severity = severity;
		}
		
		public int compareSeverity(DeleteStatus other){
			return this.severity.compareTo(other.severity);
		}
	}

//***************************** GETTER /SETTER /ADDER *************************/	
	/**
	 * The resuting status of a delete action.
	 * 
	 * @see DeleteStatus
	 * @return
	 */
	public DeleteStatus getStatus() {
		return status;
	}
	public void setStatus(DeleteStatus status) {
		this.status = status;
	}

	/**
	 * The highest exception that occurred during delete (if any).
	 * @return
	 */
	public List<Exception> getExceptions() {
		return exceptions;
	}
	public void addException(Exception exception) {
		this.exceptions.add(exception);
	}
	public void addExceptions(List<Exception> exceptions) {
		this.exceptions.addAll(exceptions);
	}
	
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
	
	
	/**
	 * @return
	 */
	public Set<PersistPair> getObjectsToDelete() {
		return objectsToDelete;
	}
	public void setObjectsToDelete(Set<PersistPair> objectsToDelete) {
		this.objectsToDelete = objectsToDelete;
	}
	
	/**
	 * @return
	 */
	public Set<PersistPair> getObjectsToSave() {
		return objectsToSave;
	}
	public void setObjectsToSave(Set<PersistPair> objectsToSave) {
		this.objectsToSave = objectsToSave;
	}

	
//****************** CONVENIENCE *********************************************/
	
	/**
	 * Sets the status to {@link DeleteStatus#ERROR} if not yet set to a more serious
	 * status.
	 */
	public void setError(){
		setMaxStatus(DeleteStatus.ERROR);
	}

	/**
	 * Sets the status to {@link DeleteStatus#ABORT} if not yet set to a more serious
	 * status.
	 */
	public void setAbort(){
		setMaxStatus(DeleteStatus.ABORT);
	}
	
	/**
	 * Sets status to most severe status. If maxStatus is more severe then existing status
	 * existing status is set to maxStatus. Otherwise nothing changes. 
	 * If minStatus is more severe then given status minStatus will be the new status.
	 * @param maxStatus
	 */
	public void setMaxStatus(DeleteStatus maxStatus) {
		if (this.status.compareSeverity(maxStatus) < 0){
			this.status = maxStatus;
		}
	}
	
	public void includeResult(DeleteResult includedResult){
		this.setMaxStatus(includedResult.getStatus());
		this.addExceptions(includedResult.getExceptions());
		this.addRelatedObjects(includedResult.getRelatedObjects());
	}
	
	public boolean isOk(){
		return this.status == DeleteStatus.OK;
	}
	
	public boolean isAbort(){
		return this.status == DeleteStatus.ABORT;
	}

	public boolean isError(){
		return this.status == DeleteStatus.ERROR;
	}

	
	
	@Override
	public String toString(){
		return status.toString();
	}

	
}
