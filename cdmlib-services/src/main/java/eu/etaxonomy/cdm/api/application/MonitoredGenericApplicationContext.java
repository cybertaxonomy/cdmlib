// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;

/**
 * {@link GenericApplicationContext Generic application context} which allows progress monitoring.
 * @author a.mueller
 * @date 29.09.2011
 *
 */
public class MonitoredGenericApplicationContext extends GenericApplicationContext{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmApplicationController.class);
	
	final int countInvokeBeanFactoryPostProcessors = 10;
	final int countFinishBeanFactoryInitialization = 90;
	private final int countTasks = countInvokeBeanFactoryPostProcessors + countFinishBeanFactoryInitialization;
	private IProgressMonitor currentMonitor;

	
	
	/**
	 * Constructor.
	 * @param progressMonitor
	 */
	public MonitoredGenericApplicationContext() {
//		MonitoredListableBeanFactory beanFactory = 
		super(new MonitoredListableBeanFactory());
		//taken from empty constructor of GenericApplicationContext
		((MonitoredListableBeanFactory)getBeanFactory()).setSerializationId(getId());
		((MonitoredListableBeanFactory)getBeanFactory()).setParameterNameDiscoverer(new LocalVariableTableParameterNameDiscoverer());
		((MonitoredListableBeanFactory)getBeanFactory()).setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
	}


	public int countTasks(){
		return countTasks;
	}

	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory){
		String task = "Invoke bean factory post processors";
		currentMonitor.subTask(task);
		super.invokeBeanFactoryPostProcessors(beanFactory);
		
		currentMonitor.worked(countInvokeBeanFactoryPostProcessors);
		checkMonitorCancelled(currentMonitor);
	}
	
	private void checkMonitorCancelled(IProgressMonitor monitor) {
		if (monitor.isCanceled()){
			throw new CancellationException();
		}
		
	}


	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory){
		String task = "Finish bean factory initialization";
		currentMonitor.subTask(task);
		IProgressMonitor subMonitor	= new SubProgressMonitor(currentMonitor, countFinishBeanFactoryInitialization);
		getMyBeanFactory().setCurrentMonitor(subMonitor);
		super.finishBeanFactoryInitialization(beanFactory);
//		currentMonitor.worked();
	}

	/**
	 * @param progressMonitor the progressMonitor to set
	 */
	public void setCurrentMonitor(IProgressMonitor monitor) {
		this.currentMonitor = monitor;
	}


	/* (non-Javadoc)
	 * @see org.springframework.context.support.AbstractApplicationContext#refresh()
	 */
	public void refresh(IProgressMonitor monitor) throws BeansException, IllegalStateException {
		String message = "Refresh application context. This might take a while ...";
//		progressMonitor.subTask(message);
		currentMonitor = monitor;
//		getMyBeanFactory().setMainMonitor(monitor);
		beginTask(message, countTasks);
		super.refresh();
		taskDone();
		
	}


	/**
	 * 
	 */
	private void taskDone() {
		if (currentMonitor != null){
			currentMonitor.done();
		}
	}


	/**
	 * @param monitor
	 * @param message
	 */
	private void beginTask(String message, int countTasks) {
		if (currentMonitor != null){
			currentMonitor.beginTask(message, countTasks);
		}
	}
	
	
	private MonitoredListableBeanFactory getMyBeanFactory(){
		return (MonitoredListableBeanFactory)getBeanFactory();
	}
}
