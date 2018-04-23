/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

/**
 * @author a.mueller
 \* @since 29.09.2011
 *
 */
public class MonitoredListableBeanFactory extends DefaultListableBeanFactory {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MonitoredListableBeanFactory.class);

	private boolean isInitializingBeans = false;
	private IProgressMonitor currentMonitor;

	private static List<String> beansToMonitor = Arrays.asList("sessionFactory","defaultBeanInitializer","persistentTermInitializer");
	private final Set<String> alreadyMonitoredBeans = new HashSet<>();

	public MonitoredListableBeanFactory(){
	}

//	@Override
//	protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
//		if (registeredBeanNames.contains(beanName)){
//			return super.getMergedLocalBeanDefinition(beanName);
//
//		}
////		String message = "Handle bean '%s'";
////		message = String.format(message, beanName);
////		currentMonitor.subTask(message);
//		RootBeanDefinition result = super.getMergedLocalBeanDefinition(beanName);
////		currentMonitor.worked(1);
////		registeredBeanNames.add(beanName);
//		return result;
//	}

	@Override
    public void preInstantiateSingletons() throws BeansException {
		isInitializingBeans = true;
		checkMonitorCancelled(currentMonitor);
		int countBeans = 0;
		for (String beanName : getBeanDefinitionNames()) {
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit() && beansToMonitor.contains(beanName) ){
				countBeans++;
			}
		}
		String message = "preinstantiate singletons";
		currentMonitor.beginTask(message, countBeans);
		super.preInstantiateSingletons();
		isInitializingBeans = false;
		currentMonitor.done();
	}

//	protected <T> T doGetBean(final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly){
//		boolean doMonitor = isInitializingBeans && !monitoredBeanNames.contains(name);
//		if (doMonitor){
//			String message = "Handle bean '%s'";
//			message = String.format(message, name);
//			currentMonitor.subTask(message);
//			monitoredBeanNames.add(name);
//		}
//		T result = super.doGetBean(name, requiredType, args, typeCheckOnly);
//		if (doMonitor){
//			currentMonitor.worked(1);
//		}
//		return result;
//	}

	@Override
	protected Object createBean(final String name, final RootBeanDefinition mbd, final Object[] args){
		boolean doMonitor = isInitializingBeans && beansToMonitor.contains(name) && !alreadyMonitoredBeans.contains(name);
		checkMonitorCancelled(currentMonitor);
		if (doMonitor){
			String message;
			if (name.equals("sessionFactory")){
				message = "Initializing persistence context ...";
			}else if(name.equals("persistentTermInitializer")){
				message = "Loading terms ...";
			}else{
				message = "Handling '%s'";
				message = String.format(message, name);
			}
			currentMonitor.subTask(message);
			alreadyMonitoredBeans.add(name);
		}
		Object result = super.createBean(name, mbd, args);
		if (doMonitor){
			checkMonitorCancelled(currentMonitor);
			currentMonitor.worked(1);
		}
		return result;
	}


	/**
	 * @param mainMonitor the mainMonitor to set
	 */
	public void setCurrentMonitor(IProgressMonitor monitor) {
		this.currentMonitor = monitor;
	}

	private void checkMonitorCancelled(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled()){
			throw new CancellationException();
		}
	}

}
