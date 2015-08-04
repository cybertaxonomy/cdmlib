package eu.etaxonomy.cdm.io.common;

import java.io.Serializable;
import java.util.Set;

import eu.etaxonomy.cdm.io.common.events.IIoObserver;

public interface IIoObservable extends Serializable {

	/**
	 * Sets the observers for this object
	 * @return
	 */
	public abstract Set<IIoObserver> getObservers();

	/**
	 * Adds a new observer for this object.
	 * @param observer
	 * @return
	 */
	public abstract boolean addObserver(IIoObserver observer);

	/**
	 * Adds a set of new observer for this object.
	 * @param observer
	 * @return
	 */
	public abstract void addObservers(Set<IIoObserver> observer);


	/**
	 * Removes an observer from this object
	 * @param observer
	 * @return
	 */
	public abstract boolean removeObserver(IIoObserver observer);

	/**
	 *  Clears the observer list so that this object no longer has any observers.
	 */
	public void removeObservers();


	/**
	 * Returns the number of observers of this Observable object.
	 * @return number of observers
	 */
	public int countObservers();

}