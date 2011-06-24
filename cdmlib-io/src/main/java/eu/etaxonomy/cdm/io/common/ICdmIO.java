/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;


/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */

public interface ICdmIO<STATE extends IoStateBase> {
	
	final String USER_STORE = "person";
	final String PERSON_STORE = "person";
	final String TEAM_STORE = "team";
	final String REFERENCE_STORE = "reference";
	final String NOMREF_STORE = "nomRef";
	final String REF_DETAIL_STORE = "refDetail";
	final String NOMREF_DETAIL_STORE = "nomRefDetail";
	final String TAXONNAME_STORE = "taxonName";
	final String TAXON_STORE = "taxon";
	final String FEATURE_STORE = "feature";
	final String SPECIMEN_STORE = "specimen";
	
	public boolean check(STATE state);
	
//	public boolean invoke(T config, Map<String, MapWrapper<? extends CdmBase>> stores);
	
	public abstract boolean invoke(STATE state);
	
//	public boolean invoke(IoState<T> state);
	
	public void updateProgress(STATE state, String message);
	
	public void updateProgress(STATE state, String message, int worked);
	
	public void warnProgress(STATE state, String message, Throwable e);

//******************** Observers *********************************************************	

	/**
	 * Adds an observer to the set of observers for this object, provided that it is not the same as some observer already in the set.
	 * @param observer
	 */
	public void addObserver(IIoObserver observer);

	/**
	 * Returns the number of observers of this Observable object.
	 * @return number of observers
	 */
	public int countObservers();
    /**
     * Deletes an observer from the set of observers of this object.
     * @param observer
     */
	public void deleteObserver(IIoObserver observer);

	/**
	 *  Clears the observer list so that this object no longer has any observers.
	 */
	public void deleteObservers();
	
	/**
	 * If this object fires an event then notify all of its observers.
	 */
	public void fire(IIoEvent event);
	
//******************** End Observers *********************************************************	
	
}