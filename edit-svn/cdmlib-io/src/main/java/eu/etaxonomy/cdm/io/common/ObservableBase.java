/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.common.events.IoProblemEvent;

/**
 * @author a.mueller
 * @created 30.03.2012
 *
 */
public class ObservableBase implements IIoObservable {
	
	private Set<IIoObserver> observers = new HashSet<IIoObserver>();
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IObservable#getObservers()
	 */
	@Override
	public Set<IIoObserver> getObservers() {
		return observers;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IIoObservable#countObservers()
	 */
	@Override
	public int countObservers(){
		return observers.size();
	}
	
	public void setObservers(Set<IIoObserver> observers) {
		this.observers = observers;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IObservable#addObserver(eu.etaxonomy.cdm.io.common.events.IIoObserver)
	 */
	@Override
	public boolean addObserver(IIoObserver observer){
		return this.observers.add(observer);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IIoObservable#removeObservers()
	 */
	public void removeObservers(){
		observers.removeAll(observers);
	}
	
	@Override
	public void addObservers(Set<IIoObserver> newObservers) {
		for (IIoObserver observer : newObservers){
			this.observers.add(observer);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IObservable#removeObserver(eu.etaxonomy.cdm.io.common.events.IIoObserver)
	 */
	@Override
	public boolean removeObserver(IIoObserver observer){
		return this.observers.remove(observer);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmIO#fire(eu.etaxonomy.cdm.io.common.events.IIoEvent)
	 */
	protected void fire(IIoEvent event){
		for (IIoObserver observer: observers){
			observer.handleEvent(event);
		}
	}
	
	protected void fireWarningEvent(String message, String dataLocation, Integer severity) {
		fireWarningEvent(message, dataLocation, severity, 1);
	}
	
	protected void fireWarningEvent(String message, String dataLocation, Integer severity, int stackDepth) {
		stackDepth++;
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		int lineNumber = stackTrace[stackDepth].getLineNumber();
		String methodName = stackTrace[stackDepth].getMethodName();

		IoProblemEvent event = IoProblemEvent.NewInstance(this.getClass(), message, dataLocation, 
				lineNumber, severity, methodName);
		
		//for performance improvement one may read:
		//http://stackoverflow.com/questions/421280/in-java-how-do-i-find-the-caller-of-a-method-using-stacktrace-or-reflection
//		Object o = new SecurityManager().getSecurityContext();

		
		fire(event);
	}

}
