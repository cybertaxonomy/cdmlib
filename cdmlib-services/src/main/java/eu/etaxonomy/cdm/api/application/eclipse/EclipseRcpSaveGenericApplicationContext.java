// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.application.eclipse;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author a.mueller
 *
 */
public class EclipseRcpSaveGenericApplicationContext extends
		GenericApplicationContext {

	@Override
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new EclipseRcpSavePathMatchingResourcePatternResolver(this);
	}
	
	/**
	 * 
	 */
	public EclipseRcpSaveGenericApplicationContext() {
	}

	/**
	 * @param beanFactory
	 */
	public EclipseRcpSaveGenericApplicationContext(
			DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
	}

	/**
	 * @param parent
	 */
	public EclipseRcpSaveGenericApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * @param beanFactory
	 * @param parent
	 */
	public EclipseRcpSaveGenericApplicationContext(
			DefaultListableBeanFactory beanFactory, ApplicationContext parent) {
		super(beanFactory, parent);
	}

}
