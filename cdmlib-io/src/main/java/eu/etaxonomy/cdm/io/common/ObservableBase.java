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
 * @since 30.03.2012
 *
 */
public class ObservableBase implements IIoObservable {

    private static final long serialVersionUID = -8417951583494704537L;
    private Set<IIoObserver> observers = new HashSet<IIoObserver>();

	@Override
	public Set<IIoObserver> getObservers() {
		return observers;
	}

	@Override
	public int countObservers(){
		return observers.size();
	}

	public void setObservers(Set<IIoObserver> observers) {
		this.observers = observers;
	}

	@Override
	public boolean addObserver(IIoObserver observer){
		return this.observers.add(observer);
	}


	@Override
    public void removeObservers(){
		observers.removeAll(observers);
	}

	@Override
	public void addObservers(Set<IIoObserver> newObservers) {
		for (IIoObserver observer : newObservers){
			this.observers.add(observer);
		}
	}

	@Override
	public boolean removeObserver(IIoObserver observer){
		return this.observers.remove(observer);
	}

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
		String className = stackTrace[stackDepth].getClassName();
		Class<?> declaringClass;
		try {
			declaringClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			declaringClass = this.getClass();
		}

		IoProblemEvent event = IoProblemEvent.NewInstance(declaringClass, message, dataLocation,
				lineNumber, severity, methodName);

		//for performance improvement one may read:
		//http://stackoverflow.com/questions/421280/in-java-how-do-i-find-the-caller-of-a-method-using-stacktrace-or-reflection
//		Object o = new SecurityManager().getSecurityContext();


		fire(event);
	}

}
